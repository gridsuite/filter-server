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
import org.gridsuite.filter.AbstractFilter;
import org.gridsuite.filter.FilterLoader;
import org.gridsuite.filter.IFilterAttributes;
import org.gridsuite.filter.criteriafilter.CriteriaFilter;
import org.gridsuite.filter.identifierlistfilter.FilterEquipments;
import org.gridsuite.filter.identifierlistfilter.IdentifiableAttributes;
import org.gridsuite.filter.server.dto.IdsByGroup;
import org.gridsuite.filter.server.entities.AbstractFilterEntity;
import org.gridsuite.filter.server.repositories.FilterRepository;
import org.gridsuite.filter.server.repositories.criteriafilter.BatteryFilterRepository;
import org.gridsuite.filter.server.repositories.criteriafilter.BusBarSectionFilterRepository;
import org.gridsuite.filter.server.repositories.criteriafilter.DanglingLineFilterRepository;
import org.gridsuite.filter.server.repositories.criteriafilter.GeneratorFilterRepository;
import org.gridsuite.filter.server.repositories.criteriafilter.HvdcLineFilterRepository;
import org.gridsuite.filter.server.repositories.criteriafilter.LccConverterStationFilterRepository;
import org.gridsuite.filter.server.repositories.criteriafilter.LineFilterRepository;
import org.gridsuite.filter.server.repositories.criteriafilter.LoadFilterRepository;
import org.gridsuite.filter.server.repositories.criteriafilter.ShuntCompensatorFilterRepository;
import org.gridsuite.filter.server.repositories.criteriafilter.StaticVarCompensatorFilterRepository;
import org.gridsuite.filter.server.repositories.criteriafilter.SubstationFilterRepository;
import org.gridsuite.filter.server.repositories.criteriafilter.ThreeWindingsTransformerFilterRepository;
import org.gridsuite.filter.server.repositories.criteriafilter.TwoWindingsTransformerFilterRepository;
import org.gridsuite.filter.server.repositories.criteriafilter.VoltageLevelFilterRepository;
import org.gridsuite.filter.server.repositories.criteriafilter.VscConverterStationFilterRepository;
import org.gridsuite.filter.server.repositories.expertfilter.ExpertFilterRepository;
import org.gridsuite.filter.server.repositories.identifierlistfilter.IdentifierListFilterRepository;
import org.gridsuite.filter.server.repositories.proxies.AbstractFilterRepositoryProxy;
import org.gridsuite.filter.server.repositories.proxies.criteriafilter.BatteryFilterRepositoryProxy;
import org.gridsuite.filter.server.repositories.proxies.criteriafilter.BusBarSectionFilterRepositoryProxy;
import org.gridsuite.filter.server.repositories.proxies.criteriafilter.DanglingLineFilterRepositoryProxy;
import org.gridsuite.filter.server.repositories.proxies.criteriafilter.GeneratorFilterRepositoryProxy;
import org.gridsuite.filter.server.repositories.proxies.criteriafilter.HvdcLineFilterRepositoryProxy;
import org.gridsuite.filter.server.repositories.proxies.criteriafilter.LccConverterStationFilterRepositoryProxy;
import org.gridsuite.filter.server.repositories.proxies.criteriafilter.LineFilterRepositoryProxy;
import org.gridsuite.filter.server.repositories.proxies.criteriafilter.LoadFilterRepositoryProxy;
import org.gridsuite.filter.server.repositories.proxies.criteriafilter.ShuntCompensatorFilterRepositoryProxy;
import org.gridsuite.filter.server.repositories.proxies.criteriafilter.StaticVarCompensatorFilterRepositoryProxy;
import org.gridsuite.filter.server.repositories.proxies.criteriafilter.SubstationFilterRepositoryProxy;
import org.gridsuite.filter.server.repositories.proxies.criteriafilter.ThreeWindingsTransformerFilterRepositoryProxy;
import org.gridsuite.filter.server.repositories.proxies.criteriafilter.TwoWindingsTransformerFilterRepositoryProxy;
import org.gridsuite.filter.server.repositories.proxies.criteriafilter.VoltageLevelFilterRepositoryProxy;
import org.gridsuite.filter.server.repositories.proxies.criteriafilter.VscConverterStationFilterRepositoryProxy;
import org.gridsuite.filter.server.repositories.proxies.expertfiler.ExpertFilterRepositoryProxy;
import org.gridsuite.filter.server.repositories.proxies.identifierlistfilter.IdentifierListFilterRepositoryProxy;
import org.gridsuite.filter.server.repositories.proxies.scriptfilter.ScriptFilterRepositoryProxy;
import org.gridsuite.filter.server.repositories.scriptfilter.ScriptFilterRepository;
import org.gridsuite.filter.utils.EquipmentType;
import org.gridsuite.filter.utils.FilterServiceUtils;
import org.gridsuite.filter.utils.FilterType;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
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

    private final Map<String, AbstractFilterRepositoryProxy<?, ?>> filterRepositories = new HashMap<>();

    private final NetworkStoreService networkStoreService;

    private final NotificationService notificationService;

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
                         NotificationService notificationService) {
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
                .collect(Collectors.toList());
    }

    @Transactional
    public <F extends AbstractFilter> AbstractFilter createFilter(F filter) {
        return getRepository(filter).insert(filter);
    }

    @Transactional
    public Optional<AbstractFilter> createFilter(UUID sourceFilterId, UUID filterId) {
        Optional<AbstractFilter> sourceFilterOptional = getFilter(sourceFilterId);
        if (sourceFilterOptional.isPresent()) {
            AbstractFilter sourceFilter = sourceFilterOptional.get();
            sourceFilter.setId(filterId);
            return Optional.of(createFilter(sourceFilter));
        }
        return Optional.empty();
    }

    private AbstractFilterRepositoryProxy<? extends AbstractFilterEntity, ? extends FilterRepository<? extends AbstractFilterEntity>> getRepository(AbstractFilter filter) {
        if (!filter.getType().equals(FilterType.CRITERIA)) {
            return filterRepositories.get(filter.getType().name());
        }
        return filterRepositories.get(((CriteriaFilter) filter).getEquipmentFilterForm().getEquipmentType().name());
    }

    @Transactional
    public <F extends AbstractFilter> void changeFilter(UUID id, F newFilter, String userId) {
        Optional<AbstractFilter> f = getFilter(id);
        if (f.isPresent()) {
            if (getRepository(f.get()) == getRepository(newFilter)) { // filter type has not changed
                getRepository(newFilter).modify(id, newFilter);
            } else { // filter type has changed
                if (f.get().getType() == FilterType.SCRIPT || newFilter.getType() == FilterType.SCRIPT) {
                    throw new PowsyblException(WRONG_FILTER_TYPE);
                } else {
                    getRepository(f.get()).deleteById(id);
                    newFilter.setId(id);
                    createFilter(newFilter);
                }
            }
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, FILTER_LIST + id + NOT_FOUND);
        }
        notificationService.emitElementUpdated(id, userId);
    }

    public void deleteFilter(UUID id) {
        Objects.requireNonNull(id);
        if (filterRepositories.values().stream().noneMatch(repository -> repository.deleteById(id))) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, FILTER_LIST + id + NOT_FOUND);
        }
    }

    public void deleteAll() {
        filterRepositories.values().forEach(AbstractFilterRepositoryProxy::deleteAll);
    }

    private Network getNetwork(UUID networkUuid, String variantId) {
        Network network = networkStoreService.getNetwork(networkUuid);
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
