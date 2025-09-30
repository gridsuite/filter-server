/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.server;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Network;
import com.powsybl.network.store.client.NetworkStoreService;
import com.powsybl.network.store.client.PreloadingStrategy;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.gridsuite.filter.AbstractFilter;
import org.gridsuite.filter.FilterLoader;
import org.gridsuite.filter.IFilterAttributes;
import org.gridsuite.filter.identifierlistfilter.FilterEquipments;
import org.gridsuite.filter.identifierlistfilter.FilteredIdentifiables;
import org.gridsuite.filter.identifierlistfilter.IdentifiableAttributes;
import org.gridsuite.filter.server.dto.FilterAttributes;
import org.gridsuite.filter.server.dto.IdsByGroup;
import org.gridsuite.filter.server.repositories.proxies.AbstractFilterRepositoryProxy;
import org.gridsuite.filter.server.service.DirectoryService;
import org.gridsuite.filter.utils.FilterServiceUtils;
import org.gridsuite.filter.utils.FilterType;
import org.gridsuite.filter.utils.expertfilter.FilterCycleDetector;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Jacques Borsenberger <jacques.borsenberger at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@Service
@ComponentScan(basePackageClasses = {NetworkStoreService.class})
@AllArgsConstructor
public class FilterService {

    private static final String FILTER_LIST = "Filter list ";
    private static final String NOT_FOUND = " not found";
    public static final String FILTER_UUIDS_NOT_FOUND = "Some filter uuids have not bean found";

    private final RepositoryService repositoriesService;
    private final NetworkStoreService networkStoreService;
    private final NotificationService notificationService;
    private final DirectoryService directoryService;

    public List<IFilterAttributes> getFilters() {
        return this.repositoriesService.getFiltersAttributes()
                .map(IFilterAttributes.class::cast) // cast because generics are invariants
                .toList();
    }

    public List<FilterAttributes> getFiltersAttributes(List<UUID> filterUuids, String userId) {
        List<FilterAttributes> filterAttributes = this.repositoriesService.getFiltersAttributes(filterUuids).collect(Collectors.toList());
        // call directory server to add name information
        Map<UUID, String> elementsName = directoryService.getElementsName(filterAttributes.stream().map(FilterAttributes::getId).toList(), userId);
        filterAttributes.forEach(attribute -> attribute.setName(elementsName.get(attribute.getId())));

        if (filterAttributes.size() != filterUuids.size()) {
            List<UUID> foundUuids = filterAttributes.stream().map(FilterAttributes::getId).toList();
            List<UUID> notFoundUuids = filterUuids.stream().filter(filterUuid -> !foundUuids.contains(filterUuid)).toList();
            notFoundUuids.forEach(uuid -> {
                FilterAttributes filterAttr = new FilterAttributes();
                filterAttr.setId(uuid);
                filterAttributes.add(filterAttr);
            });
        }
        return filterAttributes;
    }

    @Transactional(readOnly = true)
    public Optional<AbstractFilter> getFilter(UUID id) {
        return this.repositoriesService.getFilter(id);
    }

    @Transactional(readOnly = true)
    public List<AbstractFilter> getFilters(List<UUID> ids) {
        return this.repositoriesService.getFilters(ids);
    }

    @Transactional
    public AbstractFilter createFilter(AbstractFilter filter) {
        return doCreateFilter(filter);
    }

    private AbstractFilter doCreateFilter(AbstractFilter filter) {
        return this.repositoriesService.getRepositoryFromType(filter).insert(filter);
    }

    @Transactional
    public List<AbstractFilter> createFilters(List<AbstractFilter> filters) {
        if (CollectionUtils.isEmpty(filters)) {
            return Collections.emptyList();
        }

        Map<AbstractFilterRepositoryProxy<?, ?>, List<AbstractFilter>> repositoryFiltersMap = filters.stream()
                .collect(Collectors.groupingBy(this.repositoriesService::getRepositoryFromType));

        List<AbstractFilter> createdFilters = new ArrayList<>();
        repositoryFiltersMap.forEach((repository, subFilters) -> createdFilters.addAll(repository.insertAll(subFilters)));
        return createdFilters;
    }

    @Transactional
    public Optional<UUID> duplicateFilter(UUID sourceFilterId) {
        Optional<AbstractFilter> sourceFilterOptional = this.repositoriesService.getFilter(sourceFilterId);
        if (sourceFilterOptional.isPresent()) {
            UUID newFilterId = UUID.randomUUID();
            AbstractFilter sourceFilter = sourceFilterOptional.get();
            sourceFilter.setId(newFilterId);
            doCreateFilter(sourceFilter);
            return Optional.of(newFilterId);
        }
        return Optional.empty();
    }

    /**
     * @return Map of uuids of copied filters and uuids of new filters
     */
    @Transactional
    public Map<UUID, UUID> duplicateFilters(List<UUID> filterUuids) {
        Map<UUID, UUID> uuidsMap = new HashMap<>();

        List<AbstractFilter> sourceFilters = this.repositoriesService.getFilters(filterUuids);

        // check whether found all
        if (sourceFilters.isEmpty() || sourceFilters.size() != filterUuids.size()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, FILTER_UUIDS_NOT_FOUND);
        }

        sourceFilters.forEach(sourceFilter -> {
            UUID newFilterId = UUID.randomUUID();
            uuidsMap.put(sourceFilter.getId(), newFilterId);
            sourceFilter.setId(newFilterId);
        });

        Map<AbstractFilterRepositoryProxy<?, ?>, List<AbstractFilter>> repositoryFiltersMap = sourceFilters.stream()
                .collect(Collectors.groupingBy(this.repositoriesService::getRepositoryFromType));

        repositoryFiltersMap.forEach(AbstractFilterRepositoryProxy::insertAll);

        return uuidsMap;
    }

    @Transactional
    public AbstractFilter updateFilter(UUID id, AbstractFilter newFilter, String userId) {
        return doUpdateFilter(id, newFilter, userId);
    }

    private AbstractFilter doUpdateFilter(UUID id, AbstractFilter newFilter, String userId) {
        Optional<AbstractFilter> filterOpt = this.repositoriesService.getFilter(id);
        if (filterOpt.isPresent()) {
            newFilter.setId(id);

            FilterLoader filterLoader = uuids -> uuids.stream()
                .map(uuid -> uuid.equals(id) ? newFilter : this.repositoriesService.getFilter(uuid).orElse(null))
                .toList();
            FilterCycleDetector.checkNoCycle(newFilter, filterLoader);

            AbstractFilter modifiedOrCreatedFilter;
            if (filterOpt.get().getType() == newFilter.getType()) { // filter type has not changed
                modifiedOrCreatedFilter = this.repositoriesService.getRepositoryFromType(newFilter).modify(id, newFilter);
            } else { // filter type has changed
                this.repositoriesService.getRepositoryFromType(filterOpt.get()).deleteById(id);
                newFilter.setId(id);
                modifiedOrCreatedFilter = doCreateFilter(newFilter);
            }

            if (userId != null) {
                notificationService.emitElementUpdated(id, userId);
            }

            return modifiedOrCreatedFilter;
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, FILTER_LIST + id + NOT_FOUND);
        }
    }

    @Transactional
    public List<AbstractFilter> updateFilters(Map<UUID, AbstractFilter> filtersToUpdateMap) {
        return filtersToUpdateMap.keySet().stream()
            .map(filterUuid -> doUpdateFilter(filterUuid, filtersToUpdateMap.get(filterUuid), null))
            .toList();
    }

    public void deleteFilter(UUID id) {
        Objects.requireNonNull(id);
        if (!this.repositoriesService.deleteFilter(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, FILTER_LIST + id + NOT_FOUND);
        }
    }

    public void deleteFilters(List<UUID> ids) {
        Objects.requireNonNull(ids);
        this.repositoriesService.deleteFilters(ids);
    }

    public void deleteAll() {
        this.repositoriesService.deleteAll();
    }

    private Network getNetwork(UUID networkUuid, String variantId) {
        Network network = networkStoreService.getNetwork(networkUuid, PreloadingStrategy.COLLECTION);
        if (network == null) {
            throw new PowsyblException("Network '" + networkUuid + "' not found");
        }
        if (variantId != null) {
            network.getVariantManager().setWorkingVariant(variantId);
        }
        return network;
    }

    private List<IdentifiableAttributes> getIdentifiableAttributes(AbstractFilter filter, UUID networkUuid, String variantId, FilterLoader filterLoader) {
        Network network = getNetwork(networkUuid, variantId);
        return FilterServiceUtils.getIdentifiableAttributes(filter, network, filterLoader);
    }

    @Transactional(readOnly = true)
    public List<IdentifiableAttributes> evaluateFilter(AbstractFilter filter, UUID networkUuid, String variantId) {
        Objects.requireNonNull(filter);
        return getIdentifiableAttributes(filter, networkUuid, variantId, this.repositoriesService.getFilterLoader());
    }

    @Transactional(readOnly = true)
    public FilteredIdentifiables evaluateFilters(List<UUID> filters, UUID networkUuid, String variantId) {
        Map<String, IdentifiableAttributes> result = new TreeMap<>();
        Map<String, IdentifiableAttributes> notFound = new TreeMap<>();
        Network network = getNetwork(networkUuid, variantId);

        filters.forEach((UUID filterUuid) -> {
                Optional<AbstractFilter> optFilter = this.repositoriesService.getFilter(filterUuid);
                if (optFilter.isEmpty()) {
                    return;
                }
                AbstractFilter filter = optFilter.get();
                Objects.requireNonNull(filter);
                FilterLoader filterLoader = this.repositoriesService.getFilterLoader();
                FilteredIdentifiables filterIdentiables = filter.toFilteredIdentifiables(FilterServiceUtils.getIdentifiableAttributes(filter, network, filterLoader));

                // unduplicate equipments and merge in common lists
                if (filterIdentiables.notFoundIds() != null) {
                    filterIdentiables.notFoundIds().forEach(element -> notFound.put(element.getId(), element));
                }

                if (filterIdentiables.equipmentIds() != null) {
                    filterIdentiables.equipmentIds().forEach(element -> result.put(element.getId(), element));
                }
            }
        );
        return new FilteredIdentifiables(result.values().stream().toList(), notFound.values().stream().toList());
    }

    @Transactional(readOnly = true)
    public Optional<List<IdentifiableAttributes>> exportFilter(UUID id, UUID networkUuid, String variantId) {
        Objects.requireNonNull(id);
        final FilterLoader filterLoader = this.repositoriesService.getFilterLoader();
        return this.repositoriesService.getFilter(id).map(filter -> getIdentifiableAttributes(filter, networkUuid, variantId, filterLoader));
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getIdentifiablesCountByGroup(IdsByGroup idsByGroup, UUID networkUuid, String variantId) {
        Objects.requireNonNull(idsByGroup);
        final FilterLoader filterLoader = this.repositoriesService.getFilterLoader();
        return idsByGroup.getIds().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> this.repositoriesService.getFilters(entry.getValue()).stream()
                                .mapToLong(f -> getIdentifiableAttributes(f, networkUuid, variantId, filterLoader).size())
                                .sum()
                        )
                );
    }

    @Transactional(readOnly = true)
    public List<FilterEquipments> exportFilters(List<UUID> ids, UUID networkUuid, String variantId) {
        Network network = getNetwork(networkUuid, variantId);
        return exportFilters(ids, network, Set.of(), this.repositoriesService.getFilterLoader());
    }

    public List<FilterEquipments> exportFilters(List<UUID> ids, Network network, Set<FilterType> filterTypesToExclude, FilterLoader filterLoader) {
        // we stream on the ids so that we can keep the same order of ids sent
        return ids.stream()
            .map(id -> this.repositoriesService.getFilter(id).orElse(null))
            .filter(filter -> filter != null && !filterTypesToExclude.contains(filter.getType()))
            .map(filter -> filter.toFilterEquipments(FilterServiceUtils.getIdentifiableAttributes(filter, network, filterLoader)))
            .toList();
    }
}
