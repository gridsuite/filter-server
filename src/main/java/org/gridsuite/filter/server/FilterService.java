/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.server;

import org.gridsuite.filter.server.dto.LineFilter;
import org.gridsuite.filter.server.dto.AbstractFilter;
import org.gridsuite.filter.server.dto.FilterAttributes;
import org.gridsuite.filter.server.dto.AbstractGenericFilter;
import org.gridsuite.filter.server.dto.NumericalFilter;
import org.gridsuite.filter.server.dto.ScriptFilter;
import org.gridsuite.filter.server.entities.AbstractFilterEntity;
import org.gridsuite.filter.server.entities.AbstractGenericFilterEntity;
import org.gridsuite.filter.server.entities.LineFilterEntity;
import org.gridsuite.filter.server.entities.NumericFilterEntity;
import org.gridsuite.filter.server.entities.ScriptFilterEntity;
import org.gridsuite.filter.server.repositories.FilterRepository;
import org.gridsuite.filter.server.repositories.LineFilterRepository;
import org.gridsuite.filter.server.repositories.ScriptFilterRepository;
import org.gridsuite.filter.server.utils.FilterType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Jacques Borsenberger <jacques.borsenberger at rte-france.com>
 */

interface Repository<FilterEntity extends AbstractFilterEntity, EntityRepository extends FilterRepository<FilterEntity>> {
    EntityRepository getRepository();

    AbstractFilter toDto(FilterEntity filterEntity);

    FilterEntity fromDto(AbstractFilter dto);

    default Optional<AbstractFilter> getFilter(String name) {
        Optional<FilterEntity> element = getRepository().findByName(name);
        if (element.isPresent()) {
            return element.map(this::toDto);
        }
        return Optional.empty();
    }

    default Stream<String> getFiltersNames() {
        return getRepository().findAll().stream().map(AbstractFilterEntity::getName);
    }

    default FilterEntity insert(AbstractFilter f) {
        return getRepository().insert(fromDto(f));
    }

    default void deleteAll() {
        getRepository().deleteAll();
    }
}

@Service
public class FilterService {

    @Autowired
    FilterService self;

    private static final Logger LOGGER = LoggerFactory.getLogger(FilterService.class);

    private final EnumMap<FilterType, Repository<?, ?>> filterRepositories = new EnumMap<>(FilterType.class);

    private AbstractFilter.AbstractFilterBuilder<?, ?> passBase(AbstractFilter.AbstractFilterBuilder<?, ?> builder, AbstractFilterEntity entity) {
        return builder.name(entity.getName());
    }

    private AbstractFilter.AbstractFilterBuilder<?, ?> passGenerics(AbstractGenericFilter.AbstractGenericFilterBuilder<?, ?> builder, AbstractGenericFilterEntity entity) {
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
            new NumericFilterEntity(null, numericalFilter.getType(), numericalFilter.getValue1(), numericalFilter.getValue2())
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
                    LineFilter.builder()
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
                if (dto instanceof LineFilter) {
                    var lineFilter = (LineFilter) dto;
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
                throw new RuntimeException("Wrong filter type, should never happen");
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
                    ScriptFilter.builder()
                        .script(entity.getScript()),
                    entity).build();
            }

            @Override
            public ScriptFilterEntity fromDto(AbstractFilter dto) {
                if (dto instanceof ScriptFilter) {
                    var filter = (ScriptFilter) dto;
                    return ScriptFilterEntity.builder()
                        .name(filter.getName())
                        .script(filter.getScript())
                        .build();
                }
                throw new RuntimeException("Wrong filter type, should never happen");
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

    @Transactional
    public <F extends AbstractFilter> void createFilterList(F filter) {
        final String name = filter.getName();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Create script filter '{}'", sanitizeParam(name));
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
            LOGGER.debug("rename filter '{}' to '{}'", sanitizeParam(name), sanitizeParam(newName));
        }
        if (filterRepositories.values().stream().noneMatch(repository -> repository.getRepository().renameFilter(name, newName))) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Filter list " + name + " not found");
        }
    }

    public void deleteAll() {
        filterRepositories.values().forEach(Repository::deleteAll);
    }
}
