/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.server;

import com.powsybl.commons.PowsyblException;
import org.gridsuite.filter.server.dto.FilterAttributes;
import org.gridsuite.filter.server.dto.IFilterAttributes;
import org.gridsuite.filter.server.dto.AbstractFilter;
import org.gridsuite.filter.server.dto.ScriptFilter;
import org.gridsuite.filter.server.entities.AbstractFilterEntity;
import org.gridsuite.filter.server.repositories.BatteryFilterRepository;
import org.gridsuite.filter.server.repositories.BusBarSectionFilterRepository;
import org.gridsuite.filter.server.repositories.DanglingLineFilterRepository;
import org.gridsuite.filter.server.repositories.GeneratorFilterRepository;
import org.gridsuite.filter.server.repositories.HvdcLineFilterRepository;
import org.gridsuite.filter.server.repositories.LccConverterStationFilterRepository;
import org.gridsuite.filter.server.repositories.LineFilterRepository;
import org.gridsuite.filter.server.repositories.LoadFilterRepository;
import org.gridsuite.filter.server.repositories.ScriptFilterRepository;
import org.gridsuite.filter.server.repositories.ShuntCompensatorFilterRepository;
import org.gridsuite.filter.server.repositories.StaticVarCompensatorFilterRepository;
import org.gridsuite.filter.server.repositories.ThreeWindingsTransformerFilterRepository;
import org.gridsuite.filter.server.repositories.TwoWindingsTransformerFilterRepository;
import org.gridsuite.filter.server.repositories.VscConverterStationFilterRepository;
import org.gridsuite.filter.server.utils.FilterType;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

import static org.gridsuite.filter.server.AbstractFilterRepositoryProxy.WRONG_FILTER_TYPE;

/**
 * @author Jacques Borsenberger <jacques.borsenberger at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@Service
public class FilterService {

    private static final String FILTER_LIST = "Filter list ";
    private static final String NOT_FOUND = " not found";

    private final EnumMap<FilterType, AbstractFilterRepositoryProxy<?, ?>> filterRepositories = new EnumMap<>(FilterType.class);

    private FiltersToGroovyScript filtersToScript;

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
                         final HvdcLineFilterRepository hvdcLineFilterRepository) {
        this.filtersToScript = filtersToScript;

        filterRepositories.put(FilterType.LINE, new LineFilterRepositoryProxy(lineFilterRepository));
        filterRepositories.put(FilterType.GENERATOR, new GeneratorFilterRepositoryProxy(generatorFilterRepository));
        filterRepositories.put(FilterType.LOAD, new LoadFilterRepositoryProxy(loadFilterRepository));
        filterRepositories.put(FilterType.SHUNT_COMPENSATOR, new ShuntCompensatorFilterRepositoryProxy(shuntCompensatorFilterRepository));
        filterRepositories.put(FilterType.STATIC_VAR_COMPENSATOR, new StaticVarCompensatorFilterRepositoryProxy(staticVarCompensatorFilterRepository));
        filterRepositories.put(FilterType.BATTERY, new BatteryFilterRepositoryProxy(batteryFilterRepository));
        filterRepositories.put(FilterType.BUSBAR_SECTION, new BusBarSectionFilterRepositoryProxy(busBarSectionFilterRepository));
        filterRepositories.put(FilterType.DANGLING_LINE, new DanglingLineFilterRepositoryProxy(danglingLineFilterRepository));
        filterRepositories.put(FilterType.LCC_CONVERTER_STATION, new LccConverterStationFilterRepositoryProxy(lccConverterStationFilterRepository));
        filterRepositories.put(FilterType.VSC_CONVERTER_STATION, new VscConverterStationFilterRepositoryProxy(vscConverterStationFilterRepository));
        filterRepositories.put(FilterType.TWO_WINDINGS_TRANSFORMER, new TwoWindingsTransformerFilterRepositoryProxy(twoWindingsTransformerFilterRepository));
        filterRepositories.put(FilterType.THREE_WINDINGS_TRANSFORMER, new ThreeWindingsTransformerFilterRepositoryProxy(threeWindingsTransformerFilterRepository));
        filterRepositories.put(FilterType.HVDC_LINE, new HvdcLineFilterRepositoryProxy(hvdcLineFilterRepository));

        filterRepositories.put(FilterType.SCRIPT, new ScriptFilterRepositoryProxy(scriptFiltersRepository));

    }

    List<IFilterAttributes> getFilters() {
        return filterRepositories.entrySet().stream()
            .flatMap(entry -> entry.getValue().getFiltersAttributes())
            .collect(Collectors.toList());
    }

    List<FilterAttributes> getFiltersMetadata(List<UUID> ids) {
        return filterRepositories.entrySet().stream()
                .flatMap(entry -> entry.getValue().getFiltersAttributes(ids)).map(filter -> {
//                     In the filter-server repository, filters are stored with types that are SCRIPT or LINE, BATTERY etc
//                     In the other services and especially in the gridexplore, we don't need to know this implementation information.
//                     We just need to know if the filter is of type SCRIPT or FILTRE. That's why we simplify the type here.
                    filter.setType(filter.getType().equals(FilterType.SCRIPT) ? FilterType.SCRIPT : FilterType.FILTER);
                    return filter;
                })
                .collect(Collectors.toList());
    }

    Optional<AbstractFilter> getFilter(UUID id) {
        Objects.requireNonNull(id);
        for (AbstractFilterRepositoryProxy<?, ?> repository : filterRepositories.values()) {
            Optional<AbstractFilter> res = repository.getFilter(id);
            if (res.isPresent()) {
                return res;
            }
        }
        return Optional.empty();
    }

    Optional<AbstractFilterEntity> getFilterEntity(UUID id) {
        Objects.requireNonNull(id);
        for (AbstractFilterRepositoryProxy<?, ?> repository : filterRepositories.values()) {
            Optional<AbstractFilterEntity> res = (Optional<AbstractFilterEntity>) repository.getFilterEntity(id);
            if (res.isPresent()) {
                return res;
            }
        }
        return Optional.empty();
    }

    @Transactional
    public <F extends AbstractFilter> AbstractFilter createFilter(F filter) {
        return filterRepositories.get(filter.getType()).insert(filter);
    }

    @Transactional
    public <F extends AbstractFilter> void changeFilter(UUID id, F filter) {
        Optional<AbstractFilter> f = getFilter(id);
        if (f.isPresent()) {
            if (f.get().getType() == filter.getType()) {  // filter type has not changed
                filter.setCreationDate(f.get().getCreationDate());
                filterRepositories.get(filter.getType()).modify(id, filter);
            } else {  // filter type has changed
                if ((f.get().getType() == FilterType.SCRIPT && filter.getType() != FilterType.SCRIPT) ||
                    (f.get().getType() != FilterType.SCRIPT && filter.getType() == FilterType.SCRIPT)) {
                    throw new PowsyblException(WRONG_FILTER_TYPE);
                } else {
                    filterRepositories.get(f.get().getType()).deleteById(id);
                    filter.setId(id);
                    filter.setCreationDate(f.get().getCreationDate());
                    createFilter(filter);
                }
            }
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, FILTER_LIST + id + NOT_FOUND);
        }
    }

    @Transactional
    public void renameFilter(UUID id, String newName) {
        Optional<AbstractFilterEntity> f = getFilterEntity(id);
        if (f.isPresent()) {
            f.get().setName(newName);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, FILTER_LIST + id + NOT_FOUND);
        }
    }

    void deleteFilter(UUID id) {
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
                filterRepositories.get(filter.get().getType()).deleteById(filter.get().getId());
                return filterRepositories.get(FilterType.SCRIPT).insert(ScriptFilter.builder().id(filter.get().getId()).name(filter.get().getName()).script(script).build());
            }
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, FILTER_LIST + id + NOT_FOUND);
        }
    }

    @Transactional
    public AbstractFilter newScriptFromFilter(UUID filterId, UUID scriptId, String scriptName) {
        Objects.requireNonNull(filterId);
        Objects.requireNonNull(scriptId);

        Optional<AbstractFilter> filter = getFilter(filterId);
        if (filter.isPresent()) {
            if (filter.get().getType() == FilterType.SCRIPT) {
                throw new PowsyblException(WRONG_FILTER_TYPE);
            } else {
                String script = generateGroovyScriptFromFilter(filter.get());
                return filterRepositories.get(FilterType.SCRIPT).insert(ScriptFilter.builder().id(scriptId).name(scriptName).script(script).build());
            }
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, FILTER_LIST + filterId + NOT_FOUND);
        }
    }
}
