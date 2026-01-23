/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.server;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.IdentifiableType;
import com.powsybl.iidm.network.Network;
import com.powsybl.network.store.client.NetworkStoreService;
import com.powsybl.network.store.client.PreloadingStrategy;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.gridsuite.filter.AbstractFilterDto;
import org.gridsuite.filter.FilterLoader;
import org.gridsuite.filter.exception.FilterCycleException;
import org.gridsuite.filter.identifierlistfilter.FilteredIdentifiables;
import org.gridsuite.filter.identifierlistfilter.IdentifiableAttributes;
import org.gridsuite.filter.model.Filter;
import org.gridsuite.filter.model.FilterEquipments;
import org.gridsuite.filter.server.dto.EquipmentTypesByFilterId;
import org.gridsuite.filter.server.dto.FilterMetadataDto;
import org.gridsuite.filter.server.dto.FiltersWithEquipmentTypes;
import org.gridsuite.filter.server.dto.IdsByGroup;
import org.gridsuite.filter.server.error.FilterBusinessErrorCode;
import org.gridsuite.filter.server.error.FilterException;
import org.gridsuite.filter.server.repositories.proxies.AbstractFilterRepositoryProxy;
import org.gridsuite.filter.server.service.DirectoryService;
import org.gridsuite.filter.server.utils.FilterWithEquipmentTypesUtils;
import org.gridsuite.filter.utils.EquipmentType;
import org.gridsuite.filter.utils.FilterType;
import org.gridsuite.filter.utils.expertfilter.FilterCycleDetector;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.Comparator;
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

    public List<FilterMetadataDto> getFilters() {
        return this.repositoriesService.getFiltersAttributes().toList();
    }

    public List<FilterMetadataDto> getFiltersAttributes(List<UUID> filterUuids, String userId) {
        List<FilterMetadataDto> filterAttributes = this.repositoriesService.getFiltersAttributes(filterUuids).collect(Collectors.toList());
        // call directory server to add name information
        Map<UUID, String> elementsName = directoryService.getElementsName(filterAttributes.stream().map(FilterMetadataDto::getId).toList(), userId);
        filterAttributes.forEach(attribute -> attribute.setName(elementsName.get(attribute.getId())));

        if (filterAttributes.size() != filterUuids.size()) {
            List<UUID> foundUuids = filterAttributes.stream().map(FilterMetadataDto::getId).toList();
            List<UUID> notFoundUuids = filterUuids.stream().filter(filterUuid -> !foundUuids.contains(filterUuid)).toList();
            notFoundUuids.forEach(uuid -> {
                FilterMetadataDto filterAttr = new FilterMetadataDto();
                filterAttr.setId(uuid);
                filterAttributes.add(filterAttr);
            });
        }
        return filterAttributes;
    }

    @Transactional(readOnly = true)
    public Optional<AbstractFilterDto> getFilter(UUID id) {
        return this.repositoriesService.getFilter(id);
    }

    @Transactional(readOnly = true)
    public List<AbstractFilterDto> getFilters(List<UUID> ids) {
        return this.repositoriesService.getFilters(ids);
    }

    @Transactional
    public AbstractFilterDto createFilter(AbstractFilterDto filter) {
        return doCreateFilter(filter);
    }

    private AbstractFilterDto doCreateFilter(AbstractFilterDto filter) {
        return this.repositoriesService.getRepositoryFromType(filter).insert(filter);
    }

    @Transactional
    public List<AbstractFilterDto> createFilters(List<AbstractFilterDto> filters) {
        if (CollectionUtils.isEmpty(filters)) {
            return Collections.emptyList();
        }

        Map<AbstractFilterRepositoryProxy<?, ?>, List<AbstractFilterDto>> repositoryFiltersMap = filters.stream()
            .collect(Collectors.groupingBy(this.repositoriesService::getRepositoryFromType));

        List<AbstractFilterDto> createdFilters = new ArrayList<>();
        repositoryFiltersMap.forEach((repository, subFilters) -> createdFilters.addAll(repository.insertAll(subFilters)));
        return createdFilters;
    }

    @Transactional
    public Optional<UUID> duplicateFilter(UUID sourceFilterId) {
        Optional<AbstractFilterDto> sourceFilterOptional = this.repositoriesService.getFilter(sourceFilterId);
        if (sourceFilterOptional.isPresent()) {
            UUID newFilterId = UUID.randomUUID();
            AbstractFilterDto sourceFilter = sourceFilterOptional.get();
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

        List<AbstractFilterDto> sourceFilters = this.repositoriesService.getFilters(filterUuids);

        // check whether found all
        if (sourceFilters.isEmpty() || sourceFilters.size() != filterUuids.size()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, FILTER_UUIDS_NOT_FOUND);
        }

        sourceFilters.forEach(sourceFilter -> {
            UUID newFilterId = UUID.randomUUID();
            uuidsMap.put(sourceFilter.getId(), newFilterId);
            sourceFilter.setId(newFilterId);
        });

        Map<AbstractFilterRepositoryProxy<?, ?>, List<AbstractFilterDto>> repositoryFiltersMap = sourceFilters.stream()
            .collect(Collectors.groupingBy(this.repositoriesService::getRepositoryFromType));

        repositoryFiltersMap.forEach(AbstractFilterRepositoryProxy::insertAll);

        return uuidsMap;
    }

    @Transactional
    public AbstractFilterDto updateFilter(UUID id, AbstractFilterDto newFilter, String userId) {
        return doUpdateFilter(id, newFilter, userId);
    }

    private AbstractFilterDto doUpdateFilter(UUID id, AbstractFilterDto newFilter, String userId) {
        Optional<AbstractFilterDto> filterOpt = this.repositoriesService.getFilter(id);
        if (filterOpt.isPresent()) {
            newFilter.setId(id);

            FilterLoader filterLoader = uuids -> uuids.stream()
                .map(uuid -> uuid.equals(id) ? newFilter : this.repositoriesService.getFilter(uuid).orElse(null))
                .toList();
            try {
                FilterCycleDetector.checkNoCycle(newFilter, filterLoader);
            } catch (FilterCycleException exception) {
                Map<String, Object> cyclicFilterNames = getCyclicFilterNames(userId, exception);
                throw new FilterException(
                    FilterBusinessErrorCode.FILTER_CYCLE_DETECTED,
                    exception.getMessage(),
                    cyclicFilterNames
                );

            }

            AbstractFilterDto modifiedOrCreatedFilter;
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
    public List<AbstractFilterDto> updateFilters(Map<UUID, AbstractFilterDto> filtersToUpdateMap) {
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

    @Transactional(readOnly = true)
    public org.gridsuite.filter.model.FilterEquipments evaluateFilter(Filter filter, UUID networkUuid, String variantId) {
        Objects.requireNonNull(filter);
        Network network = getNetwork(networkUuid, variantId);
        return filter.evaluate(network);
    }

    @Transactional(readOnly = true)
    public FilteredIdentifiables evaluateFiltersWithEquipmentTypes(FiltersWithEquipmentTypes filtersWithEquipmentTypes, UUID networkUuid, String variantId) {
        Map<String, EquipmentType> result = new TreeMap<>();
        Map<String, EquipmentType> notFound = new TreeMap<>();
        Network network = getNetwork(networkUuid, variantId);

        filtersWithEquipmentTypes.filters().forEach((FilterMetadataDto filterMetadataDto) -> {
                UUID filterUuid = filterMetadataDto.getId();
                Optional<AbstractFilterDto> optFilter = this.repositoriesService.getFilter(filterUuid);
                if (optFilter.isEmpty()) {
                    return;
                }
                AbstractFilterDto filter = optFilter.get();
                Objects.requireNonNull(filter);
                EquipmentType filterEquipmentType = filter.getEquipmentType();
                FilteredIdentifiables filteredIdentifiables = filter.toFilteredIdentifiables(FilterServiceUtils.getIdentifiableAttributes(filter, network, filterLoader));

                // unduplicate equipments and merge in common lists
                if (filteredIdentifiables.notFoundIds() != null) {
                    filteredIdentifiables.notFoundIds().forEach(element -> notFound.put(element.getId(), element));
                }

                if (filteredIdentifiables.equipmentIds() != null) {
                    if (filterEquipmentType != EquipmentType.SUBSTATION && filterEquipmentType != EquipmentType.VOLTAGE_LEVEL) {
                        filteredIdentifiables.equipmentIds().forEach(element -> result.put(element.getId(), element));
                    } else {
                        Set<IdentifiableType> selectedEquipmentTypes = filtersWithEquipmentTypes.selectedEquipmentTypesByFilter()
                            .stream()
                            .filter(equipmentTypesByFilterId -> equipmentTypesByFilterId.filterId().equals(filterUuid))
                            .findFirst()
                            .map(EquipmentTypesByFilterId::equipmentTypes)
                            .orElseThrow(
                                () -> new IllegalStateException("No selected equipment types for filter " + filterUuid
                                    + " : substation and voltage level filters should contain an equipment types list")
                            );

                        // This list is the result of the original filter and so necessarily contains a list of IDs of substations or voltage levels
                        Set<String> filteredEquipmentIds = filteredIdentifiables.equipmentIds().stream().map(IdentifiableAttributes::getId).collect(Collectors.toSet());
                        List<ExpertFilter> filters = FilterWithEquipmentTypesUtils.createFiltersForSubEquipments(filterEquipmentType,
                            filteredEquipmentIds,
                            selectedEquipmentTypes);
                        filters.stream().flatMap(expertFilter -> getIdentifiableAttributes(expertFilter, networkUuid, variantId, filterLoader).stream())
                            .forEach(element -> result.put(element.getId(), element));
                    }
                }
            }
        );
        return new FilteredIdentifiables(
            result.values().stream().sorted(Comparator.comparing(e -> e.getType().ordinal())).toList(),
            notFound.values().stream().sorted(Comparator.comparing(e -> e.getType().ordinal())).toList());
    }

    @Transactional(readOnly = true)
    public Optional<List<IdentifiableAttributes>> exportFilter(UUID id, UUID networkUuid, String variantId) {
        Objects.requireNonNull(id);
        Network network = getNetwork(networkUuid, variantId);
        return this.repositoriesService.getFilterModel(id).map(filter -> {
            FilterEquipments filterEquipments = filter.evaluate(network);
            return filterEquipments.getFoundEquipments()
                .stream()
                .map(equipmentId ->
                    new IdentifiableAttributes(equipmentId, filter.getEquipmentType(), null));
        });
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getIdentifiablesCountByGroup(IdsByGroup idsByGroup, UUID networkUuid, String variantId) {
        Objects.requireNonNull(idsByGroup);
        final FilterLoader filterLoader = this.repositoriesService.getFilterLoader();
        Network network = getNetwork(networkUuid, variantId);
        return idsByGroup.getIds().entrySet().stream()
                .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> this.repositoriesService.getFiltersModels(entry.getValue()).stream()
                                        .flatMap(f -> f.evaluate(network).getFoundEquipments().stream())
                                        .distinct()
                                        .count()
                        )
                );
    }

    @Transactional(readOnly = true)
    public List<org.gridsuite.filter.model.FilterEquipments> exportFilters(List<UUID> ids, UUID networkUuid, String variantId) {
        Network network = getNetwork(networkUuid, variantId);
        return exportFilters(ids, network, Set.of());
    }

    public List<org.gridsuite.filter.model.FilterEquipments> exportFilters(List<UUID> ids, Network network, Set<FilterType> filterTypesToExclude) {
        // we stream on the ids so that we can keep the same order of ids sent
        return ids.stream()
            .map(id -> this.repositoriesService.getFilterModel(id).orElse(null))
            .filter(filter -> filter != null && !filterTypesToExclude.contains(filter.getType()))
            .map(filter -> filter.evaluate(network))
            .toList();
    }

    private Map<String, Object> getCyclicFilterNames(String userId, FilterCycleException exception) {
        try {
            List<UUID> orderedCycle = exception.getCycleFilterIds();
            Map<UUID, String> names;
            if (!orderedCycle.isEmpty()) {
                names = directoryService.getElementsName(orderedCycle, userId);
            } else {
                names = Collections.emptyMap();
            }
            String filters = orderedCycle.stream()
                .map(id -> Optional.ofNullable(names.get(id)).filter(name -> !name.isBlank()).orElse(id.toString()))
                .collect(Collectors.joining(" -> "));
            return filters.isEmpty() ? Map.of() : Map.of("filters", filters);
        } catch (Exception any) {
            // fallback in case of exception while trying to get filter names implied in the cycle from directory-server
            throw new FilterException(FilterBusinessErrorCode.FILTER_CYCLE_DETECTED, exception.getMessage());
        }
    }
}
