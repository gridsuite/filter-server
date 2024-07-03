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
import org.gridsuite.filter.criteriafilter.CriteriaFilter;
import org.gridsuite.filter.identifierlistfilter.FilterEquipments;
import org.gridsuite.filter.identifierlistfilter.IdentifiableAttributes;
import org.gridsuite.filter.server.dto.IdsByGroup;
import org.gridsuite.filter.server.entities.AbstractFilterEntity;
import org.gridsuite.filter.server.repositories.FilterRepository;
import org.gridsuite.filter.server.repositories.criteriafilter.*;
import org.gridsuite.filter.server.repositories.expertfilter.ExpertFilterRepository;
import org.gridsuite.filter.server.repositories.identifierlistfilter.IdentifierListFilterRepository;
import org.gridsuite.filter.server.repositories.proxies.AbstractFilterRepositoryProxy;
import org.gridsuite.filter.server.repositories.proxies.criteriafilter.*;
import org.gridsuite.filter.server.repositories.proxies.expertfiler.ExpertFilterRepositoryProxy;
import org.gridsuite.filter.server.repositories.proxies.identifierlistfilter.IdentifierListFilterRepositoryProxy;
import org.gridsuite.filter.server.repositories.proxies.scriptfilter.ScriptFilterRepositoryProxy;
import org.gridsuite.filter.server.repositories.scriptfilter.ScriptFilterRepository;
import org.gridsuite.filter.utils.EquipmentType;
import org.gridsuite.filter.utils.FilterServiceUtils;
import org.gridsuite.filter.utils.FilterType;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

import static org.gridsuite.filter.server.repositories.proxies.AbstractFilterRepositoryProxy.WRONG_FILTER_TYPE;

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

    private final FilterService self;

    public FilterService(final ScriptFilterRepository scriptFiltersRepository,
                         final LineFilterRepository lineFilterRepository,
                         final GeneratorFilterRepository generatorFilterRepository,
                         final LoadFilterRepository loadFilterRepository,
                         final ShuntCompensatorFilterRepository shuntCompensatorFilterRepository,
                         final StaticVarCompensatorFilterRepository staticVarCompensatorFilterRepository,
                         final BatteryFilterRepository batteryFilterRepository,
                         final BusBarSectionFilterRepository busBarSectionFilterRepository,
                         final DanglingLineFilterRepository danglingLineFilterRepository,
                         final LccConverterStationFilterRepository lccConverterStationFilterRepository,
                         final VscConverterStationFilterRepository vscConverterStationFilterRepository,
                         final TwoWindingsTransformerFilterRepository twoWindingsTransformerFilterRepository,
                         final ThreeWindingsTransformerFilterRepository threeWindingsTransformerFilterRepository,
                         final HvdcLineFilterRepository hvdcLineFilterRepository,
                         final VoltageLevelFilterRepository voltageLevelFilterRepository,
                         final SubstationFilterRepository substationFilterRepository,
                         final IdentifierListFilterRepository identifierListFilterRepository,
                         final ExpertFilterRepository expertFilterRepository,
                         NetworkStoreService networkStoreService,
                         NotificationService notificationService,
                         @Lazy FilterService self) {
        filterRepositories.put(EquipmentType.LINE.name(), new LineFilterRepositoryProxy(lineFilterRepository));
        filterRepositories.put(EquipmentType.GENERATOR.name(), new GeneratorFilterRepositoryProxy(generatorFilterRepository));
        filterRepositories.put(EquipmentType.LOAD.name(), new LoadFilterRepositoryProxy(loadFilterRepository));
        filterRepositories.put(EquipmentType.SHUNT_COMPENSATOR.name(), new ShuntCompensatorFilterRepositoryProxy(shuntCompensatorFilterRepository));
        filterRepositories.put(EquipmentType.STATIC_VAR_COMPENSATOR.name(), new StaticVarCompensatorFilterRepositoryProxy(staticVarCompensatorFilterRepository));
        filterRepositories.put(EquipmentType.BATTERY.name(), new BatteryFilterRepositoryProxy(batteryFilterRepository));
        filterRepositories.put(EquipmentType.BUSBAR_SECTION.name(), new BusBarSectionFilterRepositoryProxy(busBarSectionFilterRepository));
        filterRepositories.put(EquipmentType.DANGLING_LINE.name(), new DanglingLineFilterRepositoryProxy(danglingLineFilterRepository));
        filterRepositories.put(EquipmentType.LCC_CONVERTER_STATION.name(), new LccConverterStationFilterRepositoryProxy(lccConverterStationFilterRepository));
        filterRepositories.put(EquipmentType.VSC_CONVERTER_STATION.name(), new VscConverterStationFilterRepositoryProxy(vscConverterStationFilterRepository));
        filterRepositories.put(EquipmentType.TWO_WINDINGS_TRANSFORMER.name(), new TwoWindingsTransformerFilterRepositoryProxy(twoWindingsTransformerFilterRepository));
        filterRepositories.put(EquipmentType.THREE_WINDINGS_TRANSFORMER.name(), new ThreeWindingsTransformerFilterRepositoryProxy(threeWindingsTransformerFilterRepository));
        filterRepositories.put(EquipmentType.HVDC_LINE.name(), new HvdcLineFilterRepositoryProxy(hvdcLineFilterRepository));
        filterRepositories.put(EquipmentType.VOLTAGE_LEVEL.name(), new VoltageLevelFilterRepositoryProxy(voltageLevelFilterRepository));
        filterRepositories.put(EquipmentType.SUBSTATION.name(), new SubstationFilterRepositoryProxy(substationFilterRepository));

        filterRepositories.put(FilterType.SCRIPT.name(), new ScriptFilterRepositoryProxy(scriptFiltersRepository));

        filterRepositories.put(FilterType.IDENTIFIER_LIST.name(), new IdentifierListFilterRepositoryProxy(identifierListFilterRepository));

        filterRepositories.put(FilterType.EXPERT.name(), new ExpertFilterRepositoryProxy(expertFilterRepository));
        this.networkStoreService = networkStoreService;
        this.notificationService = notificationService;
        this.self = self;
    }

    public List<IFilterAttributes> getFilters() {
        return filterRepositories.entrySet().stream()
                .flatMap(entry -> entry.getValue().getFiltersAttributes())
                .collect(Collectors.toList());
    }

    public Optional<AbstractFilter> getFilter(UUID id) {
        Objects.requireNonNull(id);
        for (AbstractFilterRepositoryProxy<?, ?> repository : filterRepositories.values()) {
            Optional<AbstractFilter> res = repository.getFilter(id);
            if (res.isPresent()) {
                return res;
            }
        }
        return Optional.empty();
    }

    public List<AbstractFilter> getFilters(List<UUID> ids) {
        Objects.requireNonNull(ids);
        return filterRepositories.values()
                .stream()
                .flatMap(repository -> repository.getFilters(ids)
                        .stream())
                .toList();
    }

    @Transactional
    public <F extends AbstractFilter> AbstractFilter createFilter(F filter) {
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
        Optional<AbstractFilter> sourceFilterOptional = getFilter(sourceFilterId);
        if (sourceFilterOptional.isPresent()) {
            UUID newFilterId = UUID.randomUUID();
            AbstractFilter sourceFilter = sourceFilterOptional.get();
            sourceFilter.setId(newFilterId);
            self.createFilter(sourceFilter);
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

        List<AbstractFilter> sourceFilters = getFilters(filterUuids);

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
        if (!filter.getType().equals(FilterType.CRITERIA)) {
            return filterRepositories.get(filter.getType().name());
        }
        return filterRepositories.get(((CriteriaFilter) filter).getEquipmentFilterForm().getEquipmentType().name());
    }

    @Transactional
    public <F extends AbstractFilter> AbstractFilter updateFilter(UUID id, F newFilter, String userId) {
        Optional<AbstractFilter> filterOpt = getFilter(id);
        AbstractFilter modifiedOrCreatedFilter;
        if (filterOpt.isPresent()) {
            if (getRepository(filterOpt.get()) == getRepository(newFilter)) { // filter type has not changed
                modifiedOrCreatedFilter = getRepository(newFilter).modify(id, newFilter);
            } else { // filter type has changed
                if (filterOpt.get().getType() == FilterType.SCRIPT || newFilter.getType() == FilterType.SCRIPT) {
                    throw new PowsyblException(WRONG_FILTER_TYPE);
                } else {
                    getRepository(filterOpt.get()).deleteById(id);
                    newFilter.setId(id);
                    modifiedOrCreatedFilter = self.createFilter(newFilter);
                }
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
            .map(filterUuid -> self.updateFilter(filterUuid, filtersToUpdateMap.get(filterUuid), null))
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
        if (filter.getType() == FilterType.SCRIPT) {
            throw new PowsyblException("Filter implementation not yet supported: " + filter.getClass().getSimpleName());
        }
        Network network = getNetwork(networkUuid, variantId);
        return FilterServiceUtils.getIdentifiableAttributes(filter, network, filterLoader);
    }

    public List<IdentifiableAttributes> evaluateFilter(AbstractFilter filter, UUID networkUuid, String variantId) {
        Objects.requireNonNull(filter);
        FilterLoader filterLoader = new FilterLoaderImpl(filterRepositories);
        return getIdentifiableAttributes(filter, networkUuid, variantId, filterLoader);
    }

    public Optional<List<IdentifiableAttributes>> exportFilter(UUID id, UUID networkUuid, String variantId) {
        Objects.requireNonNull(id);
        FilterLoader filterLoader = new FilterLoaderImpl(filterRepositories);
        return getFilter(id).map(filter -> getIdentifiableAttributes(filter, networkUuid, variantId, filterLoader));
    }

    public Map<String, Long> getIdentifiablesCountByGroup(IdsByGroup idsByGroup, UUID networkUuid, String variantId) {
        Objects.requireNonNull(idsByGroup);
        FilterLoader filterLoader = new FilterLoaderImpl(filterRepositories);
        return idsByGroup.getIds().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> getFilters(entry.getValue()).stream()
                                .mapToLong(f -> getIdentifiableAttributes(f, networkUuid, variantId, filterLoader).size())
                                .sum()
                        )
                );
    }

    public List<FilterEquipments> exportFilters(List<UUID> ids, UUID networkUuid, String variantId) {
        Network network = getNetwork(networkUuid, variantId);
        FilterLoader filterLoader = new FilterLoaderImpl(filterRepositories);
        return exportFilters(ids, network, Set.of(), filterLoader);
    }

    public List<FilterEquipments> exportFilters(List<UUID> ids, Network network, Set<FilterType> filterTypesToExclude, FilterLoader filterLoader) {
        // we stream on the ids so that we can keep the same order of ids sent
        return ids.stream()
            .map(id -> getFilter(id).orElse(null))
            .filter(filter -> filter != null && !filterTypesToExclude.contains(filter.getType()))
            .map(filter -> filter.toFilterEquipments(FilterServiceUtils.getIdentifiableAttributes(filter, network, filterLoader)))
            .toList();
    }
}
