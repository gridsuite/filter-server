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
import org.apache.commons.collections4.CollectionUtils;
import org.gridsuite.filter.AbstractFilter;
import org.gridsuite.filter.FilterLoader;
import org.gridsuite.filter.IFilterAttributes;
import org.gridsuite.filter.identifierlistfilter.FilterEquipments;
import org.gridsuite.filter.identifierlistfilter.IdentifiableAttributes;
import org.gridsuite.filter.identifierlistfilter.FilteredIdentifiables;
import org.gridsuite.filter.server.dto.FilterAttributes;
import org.gridsuite.filter.server.dto.IdsByGroup;
import org.gridsuite.filter.server.entities.AbstractFilterEntity;
import org.gridsuite.filter.server.repositories.FilterRepository;
import org.gridsuite.filter.server.repositories.expertfilter.ExpertFilterRepository;
import org.gridsuite.filter.server.repositories.identifierlistfilter.IdentifierListFilterRepository;
import org.gridsuite.filter.server.repositories.proxies.AbstractFilterRepositoryProxy;
import org.gridsuite.filter.server.repositories.proxies.expertfiler.ExpertFilterRepositoryProxy;
import org.gridsuite.filter.server.repositories.proxies.identifierlistfilter.IdentifierListFilterRepositoryProxy;
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
public class FilterService {

    private static final String FILTER_LIST = "Filter list ";
    private static final String NOT_FOUND = " not found";
    public static final String FILTER_UUIDS_NOT_FOUND = "Some filter uuids have not bean found";

    private final Map<String, AbstractFilterRepositoryProxy<?, ?>> filterRepositories = new HashMap<>();

    private final NetworkStoreService networkStoreService;

    private final NotificationService notificationService;

    private final DirectoryService directoryService;

    public FilterService(final IdentifierListFilterRepository identifierListFilterRepository,
                         final ExpertFilterRepository expertFilterRepository,
                         NetworkStoreService networkStoreService,
                         NotificationService notificationService,
                         DirectoryService directoryService) {
        filterRepositories.put(FilterType.IDENTIFIER_LIST.name(), new IdentifierListFilterRepositoryProxy(identifierListFilterRepository));

        filterRepositories.put(FilterType.EXPERT.name(), new ExpertFilterRepositoryProxy(expertFilterRepository));
        this.networkStoreService = networkStoreService;
        this.notificationService = notificationService;
        this.directoryService = directoryService;
    }

    public List<IFilterAttributes> getFilters() {
        return filterRepositories.entrySet().stream()
                .flatMap(entry -> entry.getValue().getFiltersAttributes())
                .collect(Collectors.toList());
    }

    public List<FilterAttributes> getFiltersAttributes(List<UUID> filterUuids, String userId) {
        List<FilterAttributes> filterAttributes = filterRepositories.entrySet().stream()
            .flatMap(entry -> entry.getValue().getFiltersAttributes(filterUuids))
            .collect(Collectors.toList());
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
        return getFilterFromRepository(id);
    }

    public Optional<AbstractFilter> getFilterFromRepository(UUID id) {
        Objects.requireNonNull(id);
        for (AbstractFilterRepositoryProxy<?, ?> repository : filterRepositories.values()) {
            Optional<AbstractFilter> res = repository.getFilter(id);
            if (res.isPresent()) {
                return res;
            }
        }
        return Optional.empty();
    }

    @Transactional(readOnly = true)
    public List<AbstractFilter> getFilters(List<UUID> ids) {
        return getFiltersFromRepositories(ids);
    }

    private List<AbstractFilter> getFiltersFromRepositories(List<UUID> ids) {
        Objects.requireNonNull(ids);
        return filterRepositories.values()
                .stream()
                .flatMap(repository -> repository.getFilters(ids)
                        .stream())
                .toList();
    }

    @Transactional
    public <F extends AbstractFilter> AbstractFilter createFilter(F filter) {
        return doCreateFilter(filter);
    }

    private <F extends AbstractFilter> AbstractFilter doCreateFilter(F filter) {
        return getRepository(filter).insert(filter);
    }

    @Transactional
    public List<AbstractFilter> createFilters(List<AbstractFilter> filters) {
        if (CollectionUtils.isEmpty(filters)) {
            return Collections.emptyList();
        }

        Map<AbstractFilterRepositoryProxy<?, ?>, List<AbstractFilter>> repositoryFiltersMap = filters.stream()
                .collect(Collectors.groupingBy(this::getRepository));

        List<AbstractFilter> createdFilters = new ArrayList<>();
        repositoryFiltersMap.forEach((repository, subFilters) -> createdFilters.addAll(repository.insertAll(subFilters)));
        return createdFilters;
    }

    @Transactional
    public Optional<UUID> duplicateFilter(UUID sourceFilterId) {
        Optional<AbstractFilter> sourceFilterOptional = getFilterFromRepository(sourceFilterId);
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

        List<AbstractFilter> sourceFilters = getFiltersFromRepositories(filterUuids);

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
                .collect(Collectors.groupingBy(this::getRepository));

        repositoryFiltersMap.forEach(AbstractFilterRepositoryProxy::insertAll);

        return uuidsMap;
    }

    private AbstractFilterRepositoryProxy<? extends AbstractFilterEntity,
            ? extends FilterRepository<? extends AbstractFilterEntity>> getRepository(AbstractFilter filter) {
        return filterRepositories.get(filter.getType().name());
    }

    @Transactional
    public <F extends AbstractFilter> AbstractFilter updateFilter(UUID id, F newFilter, String userId) {
        return doUpdateFilter(id, newFilter, userId);
    }

    private <F extends AbstractFilter> AbstractFilter doUpdateFilter(UUID id, F newFilter, String userId) {
        Optional<AbstractFilter> filterOpt = getFilterFromRepository(id);
        AbstractFilter modifiedOrCreatedFilter;
        if (filterOpt.isPresent()) {
            newFilter.setId(id);

            FilterLoader filterLoader = uuids -> uuids.stream()
                .map(uuid -> uuid.equals(id) ? newFilter : getFilterFromRepository(uuid).orElse(null))
                .toList();
            FilterCycleDetector.checkNoCycle(newFilter, filterLoader);

            if (getRepository(filterOpt.get()) == getRepository(newFilter)) { // filter type has not changed
                modifiedOrCreatedFilter = getRepository(newFilter).modify(id, newFilter);
            } else { // filter type has changed
                getRepository(filterOpt.get()).deleteById(id);
                modifiedOrCreatedFilter = doCreateFilter(newFilter);
            }
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, FILTER_LIST + id + NOT_FOUND);
        }

        if (userId != null) {
            notificationService.emitElementUpdated(id, userId);
        }

        return modifiedOrCreatedFilter;
    }

    @Transactional
    public List<AbstractFilter> updateFilters(Map<UUID, AbstractFilter> filtersToUpdateMap) {
        return filtersToUpdateMap.keySet().stream()
            .map(filterUuid -> doUpdateFilter(filterUuid, filtersToUpdateMap.get(filterUuid), null))
            .toList();
    }

    public void deleteFilter(UUID id) {
        Objects.requireNonNull(id);
        if (filterRepositories.values().stream().noneMatch(repository -> repository.deleteById(id))) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, FILTER_LIST + id + NOT_FOUND);
        }
    }

    public void deleteFilters(List<UUID> ids) {
        Objects.requireNonNull(ids);
        filterRepositories.values().forEach(repository -> repository.deleteAllByIds(ids));
    }

    public void deleteAll() {
        filterRepositories.values().forEach(AbstractFilterRepositoryProxy::deleteAll);
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
        FilterLoader filterLoader = new FilterLoaderImpl(filterRepositories);
        return getIdentifiableAttributes(filter, networkUuid, variantId, filterLoader);
    }

    @Transactional(readOnly = true)
    public FilteredIdentifiables evaluateFilters(List<UUID> filters, UUID networkUuid, String variantId) {
        Map<String, IdentifiableAttributes> result = new TreeMap<>();
        Map<String, IdentifiableAttributes> notFound = new TreeMap<>();
        Network network = getNetwork(networkUuid, variantId);

        filters.forEach((UUID filterUuid) -> {
                Optional<AbstractFilter> optFilter = getFilterFromRepository(filterUuid);
                if (optFilter.isEmpty()) {
                    return;
                }
                AbstractFilter filter = optFilter.get();
                Objects.requireNonNull(filter);
                FilterLoader filterLoader = new FilterLoaderImpl(filterRepositories);
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
        FilterLoader filterLoader = new FilterLoaderImpl(filterRepositories);
        return getFilterFromRepository(id).map(filter -> getIdentifiableAttributes(filter, networkUuid, variantId, filterLoader));
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getIdentifiablesCountByGroup(IdsByGroup idsByGroup, UUID networkUuid, String variantId) {
        Objects.requireNonNull(idsByGroup);
        FilterLoader filterLoader = new FilterLoaderImpl(filterRepositories);
        return idsByGroup.getIds().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> getFiltersFromRepositories(entry.getValue()).stream()
                                .mapToLong(f -> getIdentifiableAttributes(f, networkUuid, variantId, filterLoader).size())
                                .sum()
                        )
                );
    }

    @Transactional(readOnly = true)
    public List<FilterEquipments> exportFilters(List<UUID> ids, UUID networkUuid, String variantId) {
        Network network = getNetwork(networkUuid, variantId);
        FilterLoader filterLoader = new FilterLoaderImpl(filterRepositories);
        return exportFilters(ids, network, Set.of(), filterLoader);
    }

    public List<FilterEquipments> exportFilters(List<UUID> ids, Network network, Set<FilterType> filterTypesToExclude, FilterLoader filterLoader) {
        // we stream on the ids so that we can keep the same order of ids sent
        return ids.stream()
            .map(id -> getFilterFromRepository(id).orElse(null))
            .filter(filter -> filter != null && !filterTypesToExclude.contains(filter.getType()))
            .map(filter -> filter.toFilterEquipments(FilterServiceUtils.getIdentifiableAttributes(filter, network, filterLoader)))
            .toList();
    }
}
