/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.server;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.network.store.client.NetworkStoreService;
import com.powsybl.network.store.client.PreloadingStrategy;
import org.apache.commons.collections4.CollectionUtils;
import org.gridsuite.filter.server.dto.*;
import org.gridsuite.filter.server.entities.AbstractFilterEntity;
import org.gridsuite.filter.server.repositories.*;
import org.gridsuite.filter.server.utils.EquipmentType;
import org.gridsuite.filter.server.utils.FilterType;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.gridsuite.filter.server.AbstractFilterRepositoryProxy.WRONG_FILTER_TYPE;

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

    private final FiltersToGroovyScript filtersToScript;

    private final NetworkStoreService networkStoreService;

    public FilterService(FiltersToGroovyScript filtersToScript,
                         final ScriptFilterRepository scriptFiltersRepository,
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
                         NetworkStoreService networkStoreService) {
        this.filtersToScript = filtersToScript;

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

        filterRepositories.put(FilterType.SCRIPT.name(), new ScriptFilterRepositoryProxy(scriptFiltersRepository));

        this.networkStoreService = networkStoreService;
    }

    public List<IFilterAttributes> getFilters() {
        return filterRepositories.entrySet().stream()
            .flatMap(entry -> entry.getValue().getFiltersAttributes())
            .collect(Collectors.toList());
    }

    public List<FilterAttributes> getFiltersMetadata(List<UUID> ids) {
        return filterRepositories.entrySet().stream()
                .flatMap(entry -> entry.getValue().getFiltersAttributes(ids))
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
        if (filter.getType().equals(FilterType.SCRIPT)) {
            return filterRepositories.get(FilterType.SCRIPT.name());
        }
        return filterRepositories.get(((FormFilter) filter).getEquipmentFilterForm().getEquipmentType().name());
    }

    @Transactional
    public <F extends AbstractFilter> void changeFilter(UUID id, F newFilter) {
        Optional<AbstractFilter> f = getFilter(id);
        if (f.isPresent()) {
            if (getRepository(f.get()) == getRepository(newFilter)) { // filter type has not changed
                newFilter.setCreationDate(f.get().getCreationDate());
                getRepository(newFilter).modify(id, newFilter);
            } else { // filter type has changed
                if (f.get().getType() == FilterType.SCRIPT || newFilter.getType() == FilterType.SCRIPT) {
                    throw new PowsyblException(WRONG_FILTER_TYPE);
                } else {
                    getRepository(f.get()).deleteById(id);
                    newFilter.setId(id);
                    newFilter.setCreationDate(f.get().getCreationDate());
                    createFilter(newFilter);
                }
            }
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, FILTER_LIST + id + NOT_FOUND);
        }
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

    private String generateGroovyScriptFromFilter(AbstractFilter filter) {
        return filtersToScript.generateGroovyScriptFromFilters(filter);
    }

    @Transactional
    public AbstractFilter replaceFilterWithScript(UUID id) {
        Objects.requireNonNull(id);

        Optional<AbstractFilter> filter = getFilter(id);
        if (filter.isPresent()) {
            if (filter.get().getType() == FilterType.SCRIPT) {
                throw new PowsyblException(WRONG_FILTER_TYPE);
            } else {
                String script = generateGroovyScriptFromFilter(filter.get());
                getRepository(filter.get()).deleteById(filter.get().getId());
                return getRepository(new ScriptFilter()).insert(ScriptFilter.builder().id(filter.get().getId()).script(script).build());
            }
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, FILTER_LIST + id + NOT_FOUND);
        }
    }

    @Transactional
    public AbstractFilter newScriptFromFilter(UUID filterId, UUID newId) {
        Objects.requireNonNull(filterId);

        Optional<AbstractFilter> filter = getFilter(filterId);
        if (filter.isPresent()) {
            if (filter.get().getType() == FilterType.SCRIPT) {
                throw new PowsyblException(WRONG_FILTER_TYPE);
            } else {
                String script = generateGroovyScriptFromFilter(filter.get());
                return getRepository(new ScriptFilter()).insert(ScriptFilter.builder().id(newId == null ? UUID.randomUUID() : newId).script(script).build());
            }
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, FILTER_LIST + filterId + NOT_FOUND);
        }
    }

    private boolean countryFilter(Terminal terminal, Set<String> countries) {
        Optional<Country> country = terminal.getVoltageLevel().getSubstation().flatMap(Substation::getCountry);
        return CollectionUtils.isEmpty(countries) || country.map(c -> countries.contains(c.name())).orElse(false);
    }

    private boolean equipmentIdFilter(Identifiable<?> identifiable, String equipmentId) {
        return equipmentId == null || identifiable.getId().equals(equipmentId);
    }

    private boolean equipmentNameFilter(Identifiable<?> identifiable, String equipmentName) {
        return equipmentName == null || identifiable.getNameOrId().equals(equipmentName);
    }

    private boolean substationNameFilter(Terminal terminal, String substationName) {
        return substationName == null || terminal.getVoltageLevel().getSubstation().map(s -> s.getNameOrId().equals(substationName)).orElse(Boolean.TRUE);
    }

    private boolean filterByVoltage(double equipmentNominalVoltage, NumericalFilter numericalFilter) {
        if (numericalFilter == null) {
            return true;
        }
        switch (numericalFilter.getType()) {
            case EQUALITY:
                return equipmentNominalVoltage == numericalFilter.getValue1();
            case GREATER_THAN:
                return equipmentNominalVoltage > numericalFilter.getValue1();
            case GREATER_OR_EQUAL:
                return equipmentNominalVoltage >= numericalFilter.getValue1();
            case LESS_THAN:
                return equipmentNominalVoltage < numericalFilter.getValue1();
            case LESS_OR_EQUAL:
                return equipmentNominalVoltage <= numericalFilter.getValue1();
            case RANGE:
                return equipmentNominalVoltage >= numericalFilter.getValue1() && equipmentNominalVoltage <= numericalFilter.getValue2();
            case APPROX:
                return equipmentNominalVoltage >= (numericalFilter.getValue1() - (numericalFilter.getValue1() * numericalFilter.getValue2() / 100.))
                    && equipmentNominalVoltage <= (numericalFilter.getValue1() + (numericalFilter.getValue1() * numericalFilter.getValue2() / 100.));
            default:
                throw new PowsyblException("Unknown numerical filter type");
        }
    }

    private <I extends Injection<I>> Stream<Injection<I>> getInjectionList(Stream<Injection<I>> stream, FormFilter filter) {
        AbstractInjectionFilter injectionFilter = (AbstractInjectionFilter) filter.getEquipmentFilterForm();
        return stream
            .filter(injection -> equipmentIdFilter(injection, injectionFilter.getEquipmentID()))
            .filter(injection -> equipmentNameFilter(injection, injectionFilter.getEquipmentName()))
            .filter(injection -> filterByVoltage(injection.getTerminal().getVoltageLevel().getNominalV(), injectionFilter.getNominalVoltage()))
            .filter(injection -> countryFilter(injection.getTerminal(), injectionFilter.getCountries()))
            .filter(injection -> substationNameFilter(injection.getTerminal(), injectionFilter.getSubstationName()));
    }

    private List<Identifiable<?>> getGeneratorList(Network network, FormFilter filter) {
        return getInjectionList(network.getGeneratorStream().map(gen -> gen), filter).collect(Collectors.toList());
    }

    private List<Identifiable<?>> getLoadList(Network network, FormFilter filter) {
        return getInjectionList(network.getLoadStream().map(load -> load), filter).collect(Collectors.toList());
    }

    private List<Identifiable<?>> getBatteryList(Network network, FormFilter filter) {
        return getInjectionList(network.getBatteryStream().map(battery -> battery), filter).collect(Collectors.toList());
    }

    private List<Identifiable<?>> getStaticVarCompensatorList(Network network, FormFilter filter) {
        return getInjectionList(network.getStaticVarCompensatorStream().map(svc -> svc), filter).collect(Collectors.toList());
    }

    private List<Identifiable<?>> getShuntCompensatorList(Network network, FormFilter filter) {
        return getInjectionList(network.getShuntCompensatorStream().map(sc -> sc), filter).collect(Collectors.toList());
    }

    private boolean filterByCountries(Terminal terminal1, Terminal terminal2, SortedSet<String> filter1, SortedSet<String> filter2) {
        return
            // terminal 1 matches filter 1 and terminal 2 matches filter 2
            countryFilter(terminal1, filter1) &&
            countryFilter(terminal2, filter2)
            || // or the opposite
            countryFilter(terminal1, filter2) &&
            countryFilter(terminal2, filter1);
    }

    private boolean filterByCountries(Line line, LineFilter filter) {
        return filterByCountries(line.getTerminal1(), line.getTerminal2(), filter.getCountries1(), filter.getCountries2());
    }

    private boolean filterByCountries(HvdcLine line, HvdcLineFilter filter) {
        return filterByCountries(line.getConverterStation1().getTerminal(), line.getConverterStation2().getTerminal(), filter.getCountries1(), filter.getCountries2());
    }

    private boolean filterByVoltage(Terminal terminal, NumericalFilter numericalFilter) {
        return filterByVoltage(terminal.getVoltageLevel().getNominalV(), numericalFilter);
    }

    private boolean filterByVoltages(Branch<?> branch, NumericalFilter numFilter1, NumericalFilter numFilter2) {
        return
            // terminal 1 matches filter 1 and terminal 2 matches filter 2
            filterByVoltage(branch.getTerminal1(), numFilter1) &&
            filterByVoltage(branch.getTerminal2(), numFilter2)
            || // or the opposite
            filterByVoltage(branch.getTerminal1(), numFilter2) &&
            filterByVoltage(branch.getTerminal2(), numFilter1);
    }

    private boolean filterByVoltages(ThreeWindingsTransformer transformer, ThreeWindingsTransformerFilter filter) {
        return
            // leg 1 matches filter 1, leg 2 matches filter 2, and leg 3 filter 3
            filterByVoltage(transformer.getLeg1().getTerminal(), filter.getNominalVoltage1()) &&
            filterByVoltage(transformer.getLeg2().getTerminal(), filter.getNominalVoltage2()) &&
            filterByVoltage(transformer.getLeg3().getTerminal(), filter.getNominalVoltage3())
            // or any other combination :
            || // keep leg1 on filter 1, switch legs 2/3
            filterByVoltage(transformer.getLeg1().getTerminal(), filter.getNominalVoltage1()) &&
            filterByVoltage(transformer.getLeg3().getTerminal(), filter.getNominalVoltage2()) &&
            filterByVoltage(transformer.getLeg2().getTerminal(), filter.getNominalVoltage3())
            || // now leg2 matches filter 1
            filterByVoltage(transformer.getLeg2().getTerminal(), filter.getNominalVoltage1()) &&
            filterByVoltage(transformer.getLeg1().getTerminal(), filter.getNominalVoltage2()) &&
            filterByVoltage(transformer.getLeg3().getTerminal(), filter.getNominalVoltage3())
            || // keep leg2 on filter 1, switch legs 1/3
            filterByVoltage(transformer.getLeg2().getTerminal(), filter.getNominalVoltage1()) &&
            filterByVoltage(transformer.getLeg3().getTerminal(), filter.getNominalVoltage2()) &&
            filterByVoltage(transformer.getLeg1().getTerminal(), filter.getNominalVoltage3())
            || // now leg3 matches filter 1
            filterByVoltage(transformer.getLeg3().getTerminal(), filter.getNominalVoltage1()) &&
            filterByVoltage(transformer.getLeg1().getTerminal(), filter.getNominalVoltage2()) &&
            filterByVoltage(transformer.getLeg2().getTerminal(), filter.getNominalVoltage3())
            || // keep leg3 on filter 1, switch legs 1/2
            filterByVoltage(transformer.getLeg3().getTerminal(), filter.getNominalVoltage1()) &&
            filterByVoltage(transformer.getLeg2().getTerminal(), filter.getNominalVoltage2()) &&
            filterByVoltage(transformer.getLeg1().getTerminal(), filter.getNominalVoltage3());
    }

    private List<Identifiable<?>> getLineList(Network network, FormFilter filter) {
        LineFilter lineFilter = (LineFilter) filter.getEquipmentFilterForm();
        return network.getLineStream()
            .filter(line -> equipmentIdFilter(line, lineFilter.getEquipmentID()))
            .filter(line -> equipmentNameFilter(line, lineFilter.getEquipmentName()))
            .filter(line -> filterByVoltages(line, lineFilter.getNominalVoltage1(), lineFilter.getNominalVoltage2()))
            .filter(line -> filterByCountries(line, lineFilter))
            .filter(line -> substationNameFilter(line.getTerminal1(), lineFilter.getSubstationName1()) &&
                            substationNameFilter(line.getTerminal2(), lineFilter.getSubstationName2()))
            .collect(Collectors.toList());
    }

    private List<Identifiable<?>> get2WTransformerList(Network network, FormFilter filter) {
        TwoWindingsTransformerFilter twoWindingsTransformerFilter = (TwoWindingsTransformerFilter) filter.getEquipmentFilterForm();
        return network.getTwoWindingsTransformerStream()
            .filter(twoWindingsTransformer -> equipmentIdFilter(twoWindingsTransformer, twoWindingsTransformerFilter.getEquipmentID()))
            .filter(twoWindingsTransformer -> equipmentNameFilter(twoWindingsTransformer, twoWindingsTransformerFilter.getEquipmentName()))
            .filter(twoWindingsTransformer -> filterByVoltages(twoWindingsTransformer, twoWindingsTransformerFilter.getNominalVoltage1(), twoWindingsTransformerFilter.getNominalVoltage2()))
            .filter(twoWindingsTransformer -> countryFilter(twoWindingsTransformer.getTerminal1(), twoWindingsTransformerFilter.getCountries()) ||
                                              countryFilter(twoWindingsTransformer.getTerminal2(), twoWindingsTransformerFilter.getCountries()))
            .filter(twoWindingsTransformer -> substationNameFilter(twoWindingsTransformer.getTerminal1(), twoWindingsTransformerFilter.getSubstationName()) ||
                                              substationNameFilter(twoWindingsTransformer.getTerminal2(), twoWindingsTransformerFilter.getSubstationName()))
            .collect(Collectors.toList());
    }

    private List<Identifiable<?>> get3WTransformerList(Network network, FormFilter filter) {
        ThreeWindingsTransformerFilter threeWindingsTransformerFilter = (ThreeWindingsTransformerFilter) filter.getEquipmentFilterForm();
        return network.getThreeWindingsTransformerStream()
            .filter(threeWindingsTransformer -> equipmentIdFilter(threeWindingsTransformer, threeWindingsTransformerFilter.getEquipmentID()))
            .filter(threeWindingsTransformer -> equipmentNameFilter(threeWindingsTransformer, threeWindingsTransformerFilter.getEquipmentName()))
            .filter(threeWindingsTransformer -> filterByVoltages(threeWindingsTransformer, threeWindingsTransformerFilter))
            .filter(threeWindingsTransformer -> countryFilter(threeWindingsTransformer.getLeg1().getTerminal(), threeWindingsTransformerFilter.getCountries()) ||
                                                countryFilter(threeWindingsTransformer.getLeg2().getTerminal(), threeWindingsTransformerFilter.getCountries()) ||
                                                countryFilter(threeWindingsTransformer.getLeg3().getTerminal(), threeWindingsTransformerFilter.getCountries()))
            .filter(threeWindingsTransformer -> substationNameFilter(threeWindingsTransformer.getLeg1().getTerminal(), threeWindingsTransformerFilter.getSubstationName()) ||
                                                substationNameFilter(threeWindingsTransformer.getLeg2().getTerminal(), threeWindingsTransformerFilter.getSubstationName()) ||
                                                substationNameFilter(threeWindingsTransformer.getLeg3().getTerminal(), threeWindingsTransformerFilter.getSubstationName()))
            .collect(Collectors.toList());
    }

    private List<Identifiable<?>> getHvdcList(Network network, FormFilter filter) {
        HvdcLineFilter hvdcLineFilter = (HvdcLineFilter) filter.getEquipmentFilterForm();
        return network.getHvdcLineStream()
            .filter(hvdcLine -> equipmentIdFilter(hvdcLine, hvdcLineFilter.getEquipmentID()))
            .filter(hvdcLine -> equipmentNameFilter(hvdcLine, hvdcLineFilter.getEquipmentName()))
            .filter(hvdcLine -> filterByVoltage(hvdcLine.getNominalV(), hvdcLineFilter.getNominalVoltage()))
            .filter(hvdcLine -> filterByCountries(hvdcLine, hvdcLineFilter))
            .filter(hvdcLine -> substationNameFilter(hvdcLine.getConverterStation1().getTerminal(), hvdcLineFilter.getSubstationName1()) &&
                                substationNameFilter(hvdcLine.getConverterStation2().getTerminal(), hvdcLineFilter.getSubstationName2()))
            .collect(Collectors.toList());
    }

    private List<Identifiable<?>> getDanglingLineList(Network network, FormFilter filter) {
        return getInjectionList(network.getDanglingLineStream().map(dl -> dl), filter).collect(Collectors.toList());
    }

    private List<Identifiable<?>> getLccConverterStationList(Network network, FormFilter filter) {
        return getInjectionList(network.getLccConverterStationStream().map(lcc -> lcc), filter).collect(Collectors.toList());
    }

    private List<Identifiable<?>> getVscConverterStationList(Network network, FormFilter filter) {
        return getInjectionList(network.getVscConverterStationStream().map(vsc -> vsc), filter).collect(Collectors.toList());
    }

    private List<Identifiable<?>> getBusbarSectionList(Network network, FormFilter filter) {
        return getInjectionList(network.getBusbarSectionStream().map(bbs -> bbs), filter).collect(Collectors.toList());
    }

    private List<Identifiable<?>> getIdentifiables(FormFilter filter, Network network) {
        List<Identifiable<?>> identifiables;
        switch (filter.getEquipmentFilterForm().getEquipmentType()) {
            case GENERATOR:
                identifiables = getGeneratorList(network, filter);
                break;
            case LOAD:
                identifiables = getLoadList(network, filter);
                break;
            case BATTERY:
                identifiables = getBatteryList(network, filter);
                break;
            case STATIC_VAR_COMPENSATOR:
                identifiables = getStaticVarCompensatorList(network, filter);
                break;
            case SHUNT_COMPENSATOR:
                identifiables = getShuntCompensatorList(network, filter);
                break;
            case LCC_CONVERTER_STATION:
                identifiables = getLccConverterStationList(network, filter);
                break;
            case VSC_CONVERTER_STATION:
                identifiables = getVscConverterStationList(network, filter);
                break;
            case HVDC_LINE:
                identifiables = getHvdcList(network, filter);
                break;
            case DANGLING_LINE:
                identifiables = getDanglingLineList(network, filter);
                break;
            case LINE:
                identifiables = getLineList(network, filter);
                break;
            case TWO_WINDINGS_TRANSFORMER:
                identifiables = get2WTransformerList(network, filter);
                break;
            case THREE_WINDINGS_TRANSFORMER:
                identifiables = get3WTransformerList(network, filter);
                break;
            case BUSBAR_SECTION:
                identifiables = getBusbarSectionList(network, filter);
                break;
            default:
                throw new PowsyblException("Unknown equipment type");
        }
        return identifiables;
    }

    private List<Identifiable<?>> toIdentifiableFilter(AbstractFilter filter, UUID networkUuid, String variantId) {
        if (filter instanceof FormFilter) {
            FormFilter formFilter = (FormFilter) filter;

            Network network;
            network = networkStoreService.getNetwork(networkUuid, PreloadingStrategy.COLLECTION);
            if (network == null) {
                throw new PowsyblException("Network '" + networkUuid + "' not found");
            }
            if (variantId != null) {
                network.getVariantManager().setWorkingVariant(variantId);
            }

            return getIdentifiables(formFilter, network);
        } else {
            throw new PowsyblException("Filter implementation not yet supported: " + filter.getClass().getSimpleName());
        }
    }

    public Optional<List<IdentifiableAttributes>> exportFilter(UUID id, UUID networkUuid, String variantId) {
        Objects.requireNonNull(id);

        return getFilter(id).map(filter ->
            toIdentifiableFilter(filter, networkUuid, variantId).stream()
                    .map(identifiable -> new IdentifiableAttributes(identifiable.getId(), identifiable.getType()))
                    .collect(Collectors.toList())
        );
    }
}
