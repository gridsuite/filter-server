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
import org.gridsuite.filter.server.dto.AbstractFilter;
import org.gridsuite.filter.server.dto.IFilterAttributes;
import org.gridsuite.filter.server.dto.criteriafilter.*;
import org.gridsuite.filter.server.dto.expertfilter.ExpertFilter;
import org.gridsuite.filter.server.dto.identifierlistfilter.FilterEquipments;
import org.gridsuite.filter.server.dto.identifierlistfilter.IdentifiableAttributes;
import org.gridsuite.filter.server.dto.identifierlistfilter.IdentifierListFilter;
import org.gridsuite.filter.server.dto.identifierlistfilter.IdentifierListFilterEquipmentAttributes;
import org.gridsuite.filter.server.dto.scriptfilter.ScriptFilter;
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
import org.gridsuite.filter.server.utils.EquipmentType;
import org.gridsuite.filter.server.utils.FilterType;
import org.gridsuite.filter.server.utils.FiltersUtils;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    private final FiltersToGroovyScript filtersToScript;

    private final NetworkStoreService networkStoreService;

    private final NotificationService notificationService;

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
                         final VoltageLevelFilterRepository voltageLevelFilterRepository,
                         final SubstationFilterRepository substationFilterRepository,
                         final IdentifierListFilterRepository identifierListFilterRepository,
                         final ExpertFilterRepository expertFilterRepository,
                         NetworkStoreService networkStoreService,
                         NotificationService notificationService) {
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

    private String generateGroovyScriptFromFilter(AbstractFilter filter) {
        return filtersToScript.generateGroovyScriptFromFilters(filter);
    }

    @Transactional
    public AbstractFilter replaceFilterWithScript(UUID id, String userId) {
        Objects.requireNonNull(id);
        AbstractFilter result;
        Optional<AbstractFilter> filter = getFilter(id);
        if (filter.isPresent()) {
            if (filter.get().getType() == FilterType.SCRIPT) {
                throw new PowsyblException(WRONG_FILTER_TYPE);
            } else {
                String script = generateGroovyScriptFromFilter(filter.get());
                getRepository(filter.get()).deleteById(filter.get().getId());
                result = getRepository(new ScriptFilter()).insert(ScriptFilter.builder().id(filter.get().getId()).script(script).build());
            }
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, FILTER_LIST + id + NOT_FOUND);
        }
        notificationService.emitElementUpdated(id, userId);
        return result;
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

    private boolean freePropertiesFilter(Terminal terminal, Map<String, List<String>> propertiesWithValues) {
        Optional<Substation> optSubstation = terminal.getVoltageLevel().getSubstation();
        return optSubstation.filter(substation -> freePropertiesFilter(substation, propertiesWithValues)).isPresent();
    }

    private boolean countryFilter(Terminal terminal, Set<String> countries) {
        Optional<Country> country = terminal.getVoltageLevel().getSubstation().flatMap(Substation::getCountry);
        return CollectionUtils.isEmpty(countries) || country.map(c -> countries.contains(c.name())).orElse(false);
    }

    private boolean countryFilter(VoltageLevel voltageLevel, Set<String> countries) {
        Optional<Country> country = voltageLevel.getSubstation().flatMap(Substation::getCountry);
        return CollectionUtils.isEmpty(countries) || country.map(c -> countries.contains(c.name())).orElse(false);
    }

    private boolean countryFilter(Substation substation, Set<String> countries) {
        Optional<Country> country = substation.getCountry();
        return CollectionUtils.isEmpty(countries) || country.map(c -> countries.contains(c.name())).orElse(false);
    }

    private boolean freePropertiesFilter(Substation substation, Map<String, List<String>> propertiesWithValues) {
        return FiltersUtils.matchesFreeProps(propertiesWithValues, substation);
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
            default:
                throw new PowsyblException("Unknown numerical filter type");
        }
    }

    private boolean filterByEnergySource(Generator generator, EnergySource energySource) {
        return energySource == null || generator.getEnergySource() == energySource;
    }

    private <I extends Injection<I>> Stream<Injection<I>> getInjectionList(Stream<Injection<I>> stream, AbstractFilter filter) {
        if (filter instanceof CriteriaFilter) {
            CriteriaFilter criteriaFilter = (CriteriaFilter) filter;
            AbstractInjectionFilter injectionFilter = (AbstractInjectionFilter) criteriaFilter.getEquipmentFilterForm();
            return stream
                    .filter(injection -> equipmentIdFilter(injection, injectionFilter.getEquipmentID()))
                    .filter(injection -> equipmentNameFilter(injection, injectionFilter.getEquipmentName()))
                    .filter(injection -> filterByVoltage(injection.getTerminal().getVoltageLevel().getNominalV(), injectionFilter.getNominalVoltage()))
                    .filter(injection -> countryFilter(injection.getTerminal(), injectionFilter.getCountries()))
                    .filter(injection -> substationNameFilter(injection.getTerminal(), injectionFilter.getSubstationName()))
                    .filter(injection -> freePropertiesFilter(injection.getTerminal(), injectionFilter.getFreeProperties()));
        } else if (filter instanceof IdentifierListFilter) {
            List<String> equipmentIds = getIdentifierListFilterEquipmentIds((IdentifierListFilter) filter);
            return stream.filter(injection -> equipmentIds.contains(injection.getId()));
        } else if (filter instanceof ExpertFilter expertFilter) {
            var rule = expertFilter.getRules();
            return stream.filter(rule::evaluateRule);
        } else {
            return Stream.empty();
        }
    }

    private List<Identifiable<?>> getGeneratorList(Network network, AbstractFilter filter) {
        if (filter instanceof CriteriaFilter) {
            CriteriaFilter criteriaFilter = (CriteriaFilter) filter;
            GeneratorFilter generatorFilter = (GeneratorFilter) criteriaFilter.getEquipmentFilterForm();
            return getInjectionList(network.getGeneratorStream().map(injection -> injection), filter)
                    .filter(injection -> filterByEnergySource((Generator) injection, generatorFilter.getEnergySource()))
                    .collect(Collectors.toList());
        } else if (filter instanceof IdentifierListFilter || filter instanceof ExpertFilter) {
            return getInjectionList(network.getGeneratorStream().map(generator -> generator), filter).collect(Collectors.toList());
        } else {
            return List.of();
        }
    }

    private List<Identifiable<?>> getLoadList(Network network, AbstractFilter filter) {
        return getInjectionList(network.getLoadStream().map(load -> load), filter).collect(Collectors.toList());
    }

    private List<Identifiable<?>> getBatteryList(Network network, AbstractFilter filter) {
        return getInjectionList(network.getBatteryStream().map(battery -> battery), filter).collect(Collectors.toList());
    }

    private List<Identifiable<?>> getStaticVarCompensatorList(Network network, AbstractFilter filter) {
        return getInjectionList(network.getStaticVarCompensatorStream().map(svc -> svc), filter).collect(Collectors.toList());
    }

    private List<Identifiable<?>> getShuntCompensatorList(Network network, AbstractFilter filter) {
        return getInjectionList(network.getShuntCompensatorStream().map(sc -> sc), filter).collect(Collectors.toList());
    }

    private boolean filterByCountries(Terminal terminal1, Terminal terminal2, Set<String> filter1, Set<String> filter2) {
        return
            // terminal 1 matches filter 1 and terminal 2 matches filter 2
            countryFilter(terminal1, filter1) &&
            countryFilter(terminal2, filter2)
            || // or the opposite
            countryFilter(terminal1, filter2) &&
            countryFilter(terminal2, filter1);
    }

    private boolean filterByProperties(Terminal terminal1, Terminal terminal2,
        Map<String, List<String>> freeProperties1, Map<String, List<String>> freeProperties2) {
        return freePropertiesFilter(terminal1, freeProperties1) &&
            freePropertiesFilter(terminal2, freeProperties2)
            || freePropertiesFilter(terminal1, freeProperties2) &&
            freePropertiesFilter(terminal2, freeProperties1);
    }

    private boolean filterByProperties(Line line, LineFilter lineFilter) {
        return filterByProperties(line.getTerminal1(), line.getTerminal2(), lineFilter.getFreeProperties1(), lineFilter.getFreeProperties2());
    }

    private boolean filterByCountries(Line line, LineFilter filter) {
        return filterByCountries(line.getTerminal1(), line.getTerminal2(), filter.getCountries1(), filter.getCountries2());
    }

    private boolean filterByProperties(HvdcLine line, HvdcLineFilter filter) {
        return filterByProperties(line.getConverterStation1().getTerminal(), line.getConverterStation2().getTerminal(),
            filter.getFreeProperties1(), filter.getFreeProperties2());
    }

    private boolean filterByCountries(HvdcLine line, HvdcLineFilter filter) {
        return filterByCountries(line.getConverterStation1().getTerminal(), line.getConverterStation2().getTerminal(), filter.getCountries1(), filter.getCountries2());
    }

    private boolean filterByVoltage(Terminal terminal, NumericalFilter numericalFilter) {
        return filterByVoltage(terminal.getVoltageLevel(), numericalFilter);
    }

    private boolean filterByVoltage(VoltageLevel voltageLevel, NumericalFilter numericalFilter) {
        return filterByVoltage(voltageLevel.getNominalV(), numericalFilter);
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

    private List<Identifiable<?>> getLineList(Network network, AbstractFilter filter) {
        if (filter instanceof CriteriaFilter) {
            CriteriaFilter criteriaFilter = (CriteriaFilter) filter;
            LineFilter lineFilter = (LineFilter) criteriaFilter.getEquipmentFilterForm();
            return network.getLineStream()
                .filter(line -> equipmentIdFilter(line, lineFilter.getEquipmentID()))
                .filter(line -> equipmentNameFilter(line, lineFilter.getEquipmentName()))
                .filter(line -> filterByVoltages(line, lineFilter.getNominalVoltage1(), lineFilter.getNominalVoltage2()))
                .filter(line -> filterByCountries(line, lineFilter))
                .filter(line -> filterByProperties(line, lineFilter))
                .filter(line -> substationNameFilter(line.getTerminal1(), lineFilter.getSubstationName1()) &&
                                substationNameFilter(line.getTerminal2(), lineFilter.getSubstationName2()))
                .collect(Collectors.toList());
        } else if (filter instanceof IdentifierListFilter) {
            List<String> equipmentIds = getIdentifierListFilterEquipmentIds((IdentifierListFilter) filter);
            return network.getLineStream()
                .filter(line -> equipmentIds.contains(line.getId()))
                .collect(Collectors.toList());
        } else {
            return List.of();
        }
    }

    private List<Identifiable<?>> get2WTransformerList(Network network, AbstractFilter filter) {
        if (filter instanceof CriteriaFilter) {
            CriteriaFilter criteriaFilter = (CriteriaFilter) filter;
            TwoWindingsTransformerFilter twoWindingsTransformerFilter = (TwoWindingsTransformerFilter) criteriaFilter.getEquipmentFilterForm();
            return network.getTwoWindingsTransformerStream()
                .filter(twoWindingsTransformer -> equipmentIdFilter(twoWindingsTransformer, twoWindingsTransformerFilter.getEquipmentID()))
                .filter(twoWindingsTransformer -> equipmentNameFilter(twoWindingsTransformer, twoWindingsTransformerFilter.getEquipmentName()))
                .filter(twoWindingsTransformer -> filterByVoltages(twoWindingsTransformer, twoWindingsTransformerFilter.getNominalVoltage1(), twoWindingsTransformerFilter.getNominalVoltage2()))
                .filter(twoWindingsTransformer -> countryFilter(twoWindingsTransformer.getTerminal1(), twoWindingsTransformerFilter.getCountries()) ||
                                                  countryFilter(twoWindingsTransformer.getTerminal2(), twoWindingsTransformerFilter.getCountries()))
                .filter(twoWindingsTransformer -> freePropertiesFilter(twoWindingsTransformer.getTerminal1(), twoWindingsTransformerFilter.getFreeProperties()) ||
                    freePropertiesFilter(twoWindingsTransformer.getTerminal2(), twoWindingsTransformerFilter.getFreeProperties()))
                .filter(twoWindingsTransformer -> substationNameFilter(twoWindingsTransformer.getTerminal1(), twoWindingsTransformerFilter.getSubstationName()) ||
                                                  substationNameFilter(twoWindingsTransformer.getTerminal2(), twoWindingsTransformerFilter.getSubstationName()))
                .collect(Collectors.toList());
        } else if (filter instanceof IdentifierListFilter) {
            List<String> equipmentIds = getIdentifierListFilterEquipmentIds((IdentifierListFilter) filter);

            return network.getTwoWindingsTransformerStream()
                    .filter(twoWindingsTransformer -> equipmentIds.contains(twoWindingsTransformer.getId()))
                    .collect(Collectors.toList());
        } else {
            return List.of();
        }
    }

    private List<Identifiable<?>> get3WTransformerList(Network network, AbstractFilter filter) {
        if (filter instanceof CriteriaFilter) {
            CriteriaFilter criteriaFilter = (CriteriaFilter) filter;
            ThreeWindingsTransformerFilter threeWindingsTransformerFilter = (ThreeWindingsTransformerFilter) criteriaFilter.getEquipmentFilterForm();
            return network.getThreeWindingsTransformerStream()
                .filter(threeWindingsTransformer -> equipmentIdFilter(threeWindingsTransformer, threeWindingsTransformerFilter.getEquipmentID()))
                .filter(threeWindingsTransformer -> equipmentNameFilter(threeWindingsTransformer, threeWindingsTransformerFilter.getEquipmentName()))
                .filter(threeWindingsTransformer -> filterByVoltages(threeWindingsTransformer, threeWindingsTransformerFilter))
                .filter(threeWindingsTransformer -> countryFilter(threeWindingsTransformer.getLeg1().getTerminal(), threeWindingsTransformerFilter.getCountries()) ||
                                                    countryFilter(threeWindingsTransformer.getLeg2().getTerminal(), threeWindingsTransformerFilter.getCountries()) ||
                                                    countryFilter(threeWindingsTransformer.getLeg3().getTerminal(), threeWindingsTransformerFilter.getCountries()))
                .filter(threeWindingsTransformer -> freePropertiesFilter(threeWindingsTransformer.getLeg1().getTerminal(), threeWindingsTransformerFilter.getFreeProperties()) ||
                                                    freePropertiesFilter(threeWindingsTransformer.getLeg2().getTerminal(), threeWindingsTransformerFilter.getFreeProperties()) ||
                                                    freePropertiesFilter(threeWindingsTransformer.getLeg3().getTerminal(), threeWindingsTransformerFilter.getFreeProperties()))
                .filter(threeWindingsTransformer -> substationNameFilter(threeWindingsTransformer.getLeg1().getTerminal(), threeWindingsTransformerFilter.getSubstationName()) ||
                                                    substationNameFilter(threeWindingsTransformer.getLeg2().getTerminal(), threeWindingsTransformerFilter.getSubstationName()) ||
                                                    substationNameFilter(threeWindingsTransformer.getLeg3().getTerminal(), threeWindingsTransformerFilter.getSubstationName()))
                .collect(Collectors.toList());
        } else if (filter instanceof IdentifierListFilter) {
            List<String> equipmentIds = getIdentifierListFilterEquipmentIds((IdentifierListFilter) filter);

            return network.getThreeWindingsTransformerStream()
                .filter(threeWindingsTransformer -> equipmentIds.contains(threeWindingsTransformer.getId()))
                .collect(Collectors.toList());
        } else {
            return List.of();
        }
    }

    private List<Identifiable<?>> getHvdcList(Network network, AbstractFilter filter) {
        if (filter instanceof CriteriaFilter) {
            CriteriaFilter criteriaFilter = (CriteriaFilter) filter;
            HvdcLineFilter hvdcLineFilter = (HvdcLineFilter) criteriaFilter.getEquipmentFilterForm();
            return network.getHvdcLineStream()
                .filter(hvdcLine -> equipmentIdFilter(hvdcLine, hvdcLineFilter.getEquipmentID()))
                .filter(hvdcLine -> equipmentNameFilter(hvdcLine, hvdcLineFilter.getEquipmentName()))
                .filter(hvdcLine -> filterByVoltage(hvdcLine.getNominalV(), hvdcLineFilter.getNominalVoltage()))
                .filter(hvdcLine -> filterByCountries(hvdcLine, hvdcLineFilter))
                .filter(hvdcLine -> filterByProperties(hvdcLine, hvdcLineFilter))
                .filter(hvdcLine -> substationNameFilter(hvdcLine.getConverterStation1().getTerminal(), hvdcLineFilter.getSubstationName1()) &&
                                    substationNameFilter(hvdcLine.getConverterStation2().getTerminal(), hvdcLineFilter.getSubstationName2()))
                .collect(Collectors.toList());
        } else if (filter instanceof IdentifierListFilter) {
            List<String> equipmentsIds = getIdentifierListFilterEquipmentIds((IdentifierListFilter) filter);
            return network.getHvdcLineStream()
                .filter(hvdcLine -> equipmentsIds.contains(hvdcLine.getId()))
                .collect(Collectors.toList());
        } else {
            return List.of();
        }
    }

    private List<Identifiable<?>> getVoltageLevelList(Network network, AbstractFilter filter) {
        if (filter instanceof CriteriaFilter) {
            CriteriaFilter criteriaFilter = (CriteriaFilter) filter;
            VoltageLevelFilter voltageLevelFilter = (VoltageLevelFilter) criteriaFilter.getEquipmentFilterForm();
            return network.getVoltageLevelStream()
                .filter(voltageLevel -> equipmentIdFilter(voltageLevel, voltageLevelFilter.getEquipmentID()))
                .filter(voltageLevel -> equipmentNameFilter(voltageLevel, voltageLevelFilter.getEquipmentName()))
                .filter(voltageLevel -> filterByVoltage(voltageLevel, voltageLevelFilter.getNominalVoltage()))
                .filter(voltageLevel -> countryFilter(voltageLevel, voltageLevelFilter.getCountries()))
                .filter(voltageLevel -> freePropertiesFilter(voltageLevel.getNullableSubstation(), voltageLevelFilter.getFreeProperties()))
                .collect(Collectors.toList());
        } else if (filter instanceof IdentifierListFilter) {
            List<String> equipmentIds = getIdentifierListFilterEquipmentIds((IdentifierListFilter) filter);
            return network.getVoltageLevelStream()
                .filter(voltageLevel -> equipmentIds.contains(voltageLevel.getId()))
                .collect(Collectors.toList());
        } else {
            return List.of();
        }
    }

    private List<Identifiable<?>> getSubstationList(Network network, AbstractFilter filter) {
        if (filter instanceof CriteriaFilter) {
            CriteriaFilter criteriaFilter = (CriteriaFilter) filter;
            SubstationFilter substationFilter = (SubstationFilter) criteriaFilter.getEquipmentFilterForm();
            return network.getSubstationStream()
                .filter(substation -> equipmentIdFilter(substation, substationFilter.getEquipmentID()))
                .filter(substation -> equipmentNameFilter(substation, substationFilter.getEquipmentName()))
                .filter(substation -> countryFilter(substation, substationFilter.getCountries()))
                .filter(substation -> freePropertiesFilter(substation, substationFilter.getFreeProperties()))
                .collect(Collectors.toList());
        } else if (filter instanceof IdentifierListFilter) {
            List<String> equipmentIds = getIdentifierListFilterEquipmentIds((IdentifierListFilter) filter);
            return network.getSubstationStream()
                .filter(substation -> equipmentIds.contains(substation.getId()))
                .collect(Collectors.toList());
        } else {
            return List.of();
        }
    }

    private List<String> getIdentifierListFilterEquipmentIds(IdentifierListFilter identifierListFilter) {
        return identifierListFilter.getFilterEquipmentsAttributes()
            .stream()
            .map(IdentifierListFilterEquipmentAttributes::getEquipmentID)
            .collect(Collectors.toList());
    }

    private List<Identifiable<?>> getDanglingLineList(Network network, AbstractFilter filter) {
        return getInjectionList(network.getDanglingLineStream().map(dl -> dl), filter).collect(Collectors.toList());
    }

    private List<Identifiable<?>> getLccConverterStationList(Network network, AbstractFilter filter) {
        return getInjectionList(network.getLccConverterStationStream().map(lcc -> lcc), filter).collect(Collectors.toList());
    }

    private List<Identifiable<?>> getVscConverterStationList(Network network, AbstractFilter filter) {
        return getInjectionList(network.getVscConverterStationStream().map(vsc -> vsc), filter).collect(Collectors.toList());
    }

    private List<Identifiable<?>> getBusbarSectionList(Network network, AbstractFilter filter) {
        return getInjectionList(network.getBusbarSectionStream().map(bbs -> bbs), filter).collect(Collectors.toList());
    }

    private List<Identifiable<?>> getIdentifiables(AbstractFilter filter, Network network) {
        List<Identifiable<?>> identifiables;
        switch (filter.getEquipmentType()) {
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
            case VOLTAGE_LEVEL:
                identifiables = getVoltageLevelList(network, filter);
                break;
            case SUBSTATION:
                identifiables = getSubstationList(network, filter);
                break;
            default:
                throw new PowsyblException("Unknown equipment type");
        }
        return identifiables;
    }

    private List<Identifiable<?>> toIdentifiableFilter(AbstractFilter filter, UUID networkUuid, String variantId) {
        if (filter.getType() == FilterType.CRITERIA || filter.getType() == FilterType.IDENTIFIER_LIST || filter.getType() == FilterType.EXPERT) {
            Network network = networkStoreService.getNetwork(networkUuid);

            if (network == null) {
                throw new PowsyblException("Network '" + networkUuid + "' not found");
            }

            if (variantId != null) {
                network.getVariantManager().setWorkingVariant(variantId);
            }

            return getIdentifiables(filter, network);
        } else {
            throw new PowsyblException("Filter implementation not yet supported: " + filter.getClass().getSimpleName());
        }
    }

    private List<IdentifiableAttributes> getIdentifiableAttributes(AbstractFilter filter, UUID networkUuid, String variantId) {
        if (filter instanceof IdentifierListFilter &&
            (filter.getEquipmentType() == EquipmentType.GENERATOR ||
             filter.getEquipmentType() == EquipmentType.LOAD)) {
            IdentifierListFilter identifierListFilter = (IdentifierListFilter) filter;
            return toIdentifiableFilter(filter, networkUuid, variantId)
                    .stream()
                    .map(identifiable -> new IdentifiableAttributes(identifiable.getId(),
                            identifiable.getType(),
                            identifierListFilter.getDistributionKey(identifiable.getId())))
                    .collect(Collectors.toList());
        } else {
            return toIdentifiableFilter(filter, networkUuid, variantId).stream()
                    .map(identifiable -> new IdentifiableAttributes(identifiable.getId(), identifiable.getType(), null))
                    .collect(Collectors.toList());
        }
    }

    public List<IdentifiableAttributes> evaluateFilter(AbstractFilter filter, UUID networkUuid, String variantId) {
        Objects.requireNonNull(filter);
        return getIdentifiableAttributes(filter, networkUuid, variantId);
    }

    public Optional<List<IdentifiableAttributes>> exportFilter(UUID id, UUID networkUuid, String variantId) {
        Objects.requireNonNull(id);
        return getFilter(id).map(filter -> getIdentifiableAttributes(filter, networkUuid, variantId));
    }

    public Map<String, List<Integer>> containersListCount(Map<String, List<UUID>> filtersIdsMap, UUID networkUuid, String variantId) {
        Objects.requireNonNull(filtersIdsMap);
        Map<String, List<Integer>> resultMap = new HashMap<>();
        for (String key : filtersIdsMap.keySet()) {
            List<UUID> uuids = filtersIdsMap.get(key);
            List<FilterEquipments> filtersEquipments = exportFilters(uuids, networkUuid, variantId);
            resultMap.put(key, filtersEquipments.stream().map(filterEquipments -> filterEquipments.getIdentifiableAttributes().size()).toList());
        }
        return resultMap;
    }

    public List<FilterEquipments> exportFilters(List<UUID> ids, UUID networkUuid, String variantId) {

        // we stream on the ids so that we can keep the same order of ids sent
        return ids.stream()
                .map(id -> getFilter(id).orElse(null))
                .filter(Objects::nonNull)
                .map(filter -> filter.getFilterEquipments(getIdentifiableAttributes(filter, networkUuid, variantId)))
                .collect(Collectors.toList());
    }
}
