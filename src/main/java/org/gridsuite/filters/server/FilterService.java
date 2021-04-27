/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filters.server;

import com.powsybl.network.store.client.NetworkStoreService;
import org.gridsuite.filters.server.dto.AbstractFilter;
import org.gridsuite.filters.server.dto.FilterAttributes;
import org.gridsuite.filters.server.dto.AbstractGenericFilter;
import org.gridsuite.filters.server.dto.LineFilter;
import org.gridsuite.filters.server.dto.NumericalFilter;
import org.gridsuite.filters.server.dto.ScriptFilter;
import org.gridsuite.filters.server.entities.AbstractFilterEntity;
import org.gridsuite.filters.server.entities.LineFilterEntity;
import org.gridsuite.filters.server.entities.NumericFilterEntity;
import org.gridsuite.filters.server.entities.ScriptFilterEntity;
import org.gridsuite.filters.server.repositories.FilterRepository;
import org.gridsuite.filters.server.repositories.LineFilterRepository;
import org.gridsuite.filters.server.repositories.ScriptFilterRepository;
import org.gridsuite.filters.server.utils.FilterType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Jacques Borsenberger <jacques.borsenberger at rte-france.com>
 */

interface Repository<Entity extends AbstractFilterEntity, Repository extends FilterRepository<Entity>> {
    Repository getRepository();

    AbstractFilter toDto(Entity entity);

    Entity fromDto(AbstractFilter dto);

    default Optional<AbstractFilter> getFilter(String name) {
        Optional<Entity> element = getRepository().findByName(name);
        if (element.isPresent()) {
            return element.map(this::toDto);
        }
        return Optional.empty();
    }

    default Stream<String> getFiltersNames() {
        return getRepository().findAll().stream().map(AbstractFilterEntity::getName);
    }

    default Entity insert(AbstractFilter f) {
        return getRepository().insert(fromDto(f));
    }

}

@ComponentScan(basePackageClasses = {NetworkStoreService.class})
@Service
public class FilterService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FilterService.class);

    private final EnumMap<FilterType, Repository<?, ?>> filterRepositories = new EnumMap<>(FilterType.class);

    private AbstractFilter.AbstractFilterBuilder<?, ?> passBase(AbstractFilter.AbstractFilterBuilder<?, ?> builder, AbstractFilterEntity entity) {
        return builder.name(entity.getName());
    }

    private AbstractFilter.AbstractFilterBuilder<?, ?> passGenerics(AbstractGenericFilter.AbstractGenericFilterBuilder<?, ?> builder, org.gridsuite.filters.server.entities.AbstractGenericFilter entity) {
        return passBase(builder.equipmentID(entity.getEquipmentId()).equipmentName(entity.getEquipmentName()), entity);
    }

    <T> Set<T> cloneIfNotNull(Set<T> set) {
        if (set != null) {
            return new HashSet<>(set);
        }
        return null;
    }

    NumericalFilter convert(NumericFilterEntity entity) {
        return entity != null ? new NumericalFilter(entity.getFilterType(), entity.getValue1(), entity.getValue2()) : null;
    }

    NumericFilterEntity convert(NumericalFilter numericalFilter) {
        return numericalFilter != null ?
            new NumericFilterEntity(numericalFilter.getType(), numericalFilter.getValue1(), numericalFilter.getValue2())
            : null;
    }

    public FilterService(final ScriptFilterRepository scriptFiltersRepository, final LineFilterRepository lineFilterRepository) {
        filterRepositories.put(FilterType.LINE, new Repository<LineFilterEntity, LineFilterRepository>() {
            @Override
            public LineFilterRepository getRepository() {
                return lineFilterRepository;
            }

            @Override
            public AbstractFilter toDto(LineFilterEntity entity) {
                return passGenerics(
                    org.gridsuite.filters.server.dto.LineFilter.builder()
                        .countries1(cloneIfNotNull(entity.getCountries1()))
                        .countries2(cloneIfNotNull(entity.getCountries2()))
                        .substationName1(entity.getSubstationName1())
                        .substationName2(entity.getSubstationName2())
                        .nominalVoltage1(convert(entity.getNominalVoltage1()))
                        .nominalVoltage2(convert(entity.getNominalVoltage2())),
                    entity).build();
            }

            @Override
            public LineFilterEntity fromDto(AbstractFilter dto) {
                assert dto instanceof LineFilter;
                LineFilter lineFilter = (LineFilter) dto;
                return LineFilterEntity.builder()
                    .name(lineFilter.getName())
                    .equipmentName(lineFilter.getEquipmentName())
                    .equipmentId(lineFilter.getEquipmentID())
                    .substationName1(lineFilter.getSubstationName1())
                    .substationName2(lineFilter.getSubstationName2())
                    .countries1(cloneIfNotNull(lineFilter.getCountries1()))
                    .countries2(cloneIfNotNull(lineFilter.getCountries2()))
                    .nominalVoltage1(convert(lineFilter.getNominalVoltage1()))
                    .nominalVoltage2(convert(lineFilter.getNominalVoltage2()))
                    .build();
            }
        });

        filterRepositories.put(FilterType.SCRIPT, new Repository<ScriptFilterEntity, ScriptFilterRepository>() {
            @Override
            public ScriptFilterRepository getRepository() {
                return scriptFiltersRepository;
            }

            @Override
            public AbstractFilter toDto(ScriptFilterEntity entity) {
                return passBase(
                    org.gridsuite.filters.server.dto.ScriptFilter.builder()
                        .script(entity.getScript()),
                    entity).build();
            }

            @Override
            public ScriptFilterEntity fromDto(AbstractFilter dto) {
                assert dto instanceof ScriptFilter;
                ScriptFilter filter = (ScriptFilter) dto;
                return ScriptFilterEntity.builder()
                    .name(filter.getName())
                    .script(filter.getScript())
                    .build();
            }
        });

    }

    private static String sanitizeParam(String param) {
        return param != null ? param.replaceAll("[\n|\r\t]", "_") : null;
    }

    List<FilterAttributes> getFilters() {
        return filterRepositories.entrySet().stream()
            .flatMap(entry -> entry.getValue().getFiltersNames().map(name -> new FilterAttributes(name, entry.getKey())))
            .collect(Collectors.toList());
    }

    Optional<AbstractFilter> getFilter(String name) {
        Objects.requireNonNull(name);
        for (Repository<?, ?> repository : filterRepositories.values()) {
            Optional<AbstractFilter> res = repository.getFilter(name);
            if (res.isPresent()) {
                return res;
            }
        }
        return Optional.empty();
    }

    public <F extends AbstractFilter> void createFilterList(F filter) {
        final String name = filter.getName();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Create script contingency list '{}'", sanitizeParam(name));
        }
        filterRepositories.values().forEach(r -> r.getRepository().delete(name));
        if (filterRepositories.values().stream().noneMatch(repository -> repository.getRepository().existsByName(name))) {
            filterRepositories.get(filter.getType()).insert(filter);
        }
    }

    void deleteFilter(String name) {
        Objects.requireNonNull(name);
        if (filterRepositories.values().stream().noneMatch(repository -> repository.getRepository().delete(name))) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Filter list " + name + " not found");
        }
    }

    void renameFilter(String name, String newName) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(newName);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("rename filter contingency list '{}' to '{}'", sanitizeParam(name), sanitizeParam(newName));
        }
        if (filterRepositories.values().stream().noneMatch(repository -> repository.getRepository().renameFilter(name, newName))) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Filter list " + name + " not found");
        }
    }
}
