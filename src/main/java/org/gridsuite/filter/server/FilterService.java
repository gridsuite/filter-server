/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.server;

import com.powsybl.commons.PowsyblException;
import org.gridsuite.filter.server.dto.IFilterAttributes;
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
import org.gridsuite.filter.server.repositories.FilterMetadata;
import org.gridsuite.filter.server.repositories.FilterRepository;
import org.gridsuite.filter.server.repositories.LineFilterRepository;
import org.gridsuite.filter.server.repositories.ScriptFilterRepository;
import org.gridsuite.filter.server.utils.FilterType;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.EntityNotFoundException;
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

    FilterType getRepositoryType();

    default Optional<AbstractFilter> getFilter(UUID id) {
        Optional<FilterEntity> element = getRepository().findById(id);
        if (element.isPresent()) {
            return element.map(this::toDto);
        }
        return Optional.empty();
    }

    default Stream<FilterAttributes> getFiltersAttributes() {
        return getRepository().getFiltersMetadata().stream().map(this::metadataToAttribute);
    }

    default Stream<FilterAttributes> getFiltersAttributes(List<UUID> ids) {
        return getRepository().findFiltersMetaDataById(ids).stream().map(this::metadataToAttribute);
    }

    default FilterAttributes metadataToAttribute(FilterMetadata f) {
        return new FilterAttributes(f, getRepositoryType());
    }

    default AbstractFilter insert(AbstractFilter f) {
        return toDto(getRepository().save(fromDto(f)));
    }

    default void modify(UUID id, AbstractFilter f) {
        if (!getRepository().existsById(id)) {
            throw new EntityNotFoundException(id.toString());
        }
        f.setId(id);
        toDto(getRepository().save(fromDto(f)));
    }

    default boolean deleteById(UUID id) {
        return getRepository().removeById(id) != 0;
    }

    default void deleteAll() {
        getRepository().deleteAll();
    }
}

@Service
public class FilterService {

    private static final String FILTER_LIST = "Filter list ";
    private static final String NOT_FOUND = " not found";
    private static final String WRONG_FILTER_TYPE = "Wrong filter type, should never happen";

    private final EnumMap<FilterType, Repository<?, ?>> filterRepositories = new EnumMap<>(FilterType.class);

    private FiltersToGroovyScript filtersToScript;

    private AbstractFilter.AbstractFilterBuilder<?, ?> buildAbstractFilter(AbstractFilter.AbstractFilterBuilder<?, ?> builder, AbstractFilterEntity entity) {
        return builder.name(entity.getName()).id(entity.getId())
            .creationDate(entity.getCreationDate()).modificationDate(entity.getModificationDate())
            .description(entity.getDescription());
    }

    private AbstractFilter.AbstractFilterBuilder<?, ?> buildGenericFilter(AbstractGenericFilter.AbstractGenericFilterBuilder<?, ?> builder, AbstractGenericFilterEntity entity) {
        return buildAbstractFilter(builder.equipmentID(entity.getEquipmentId()).equipmentName(entity.getEquipmentName()), entity);
    }

    private void buildGenericFilter(AbstractGenericFilterEntity.AbstractGenericFilterEntityBuilder<?, ?> builder, AbstractGenericFilter dto) {
        buildAbstractFilter(builder, dto);
        builder.equipmentId(dto.getEquipmentID())
            .equipmentName(dto.getEquipmentName());
    }

    private void buildAbstractFilter(AbstractFilterEntity.AbstractFilterEntityBuilder<?, ?> builder, AbstractFilter dto) {
        /* dates a managed by jpa, so we don't process them */
        builder.name(dto.getName())
            .id(getIdOrCreate(dto.getId()))
            .description(dto.getDescription());
    }

    <T> Set<T> cloneIfNotEmptyOrNull(Set<T> set) {
        if (set != null && !set.isEmpty()) {
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

    public FilterService(FiltersToGroovyScript filtersToScript,
                         final ScriptFilterRepository scriptFiltersRepository,
                         final LineFilterRepository lineFilterRepository) {
        this.filtersToScript = filtersToScript;
        filterRepositories.put(FilterType.LINE, new Repository<LineFilterEntity, LineFilterRepository>() {

            @Override
            public FilterType getRepositoryType() {
                return FilterType.LINE;
            }

            @Override
            public LineFilterRepository getRepository() {
                return lineFilterRepository;
            }

            @Override
            public AbstractFilter toDto(LineFilterEntity entity) {
                return buildGenericFilter(
                    LineFilter.builder()
                        .countries1(cloneIfNotEmptyOrNull(entity.getCountries1()))
                        .countries2(cloneIfNotEmptyOrNull(entity.getCountries2()))
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
                    var lineFilterEntityBuilder = LineFilterEntity.builder()
                        .equipmentName(lineFilter.getEquipmentName())
                        .equipmentId(lineFilter.getEquipmentID())
                        .substationName1(lineFilter.getSubstationName1())
                        .substationName2(lineFilter.getSubstationName2())
                        .countries1(cloneIfNotEmptyOrNull(lineFilter.getCountries1()))
                        .countries2(cloneIfNotEmptyOrNull(lineFilter.getCountries2()))
                        .nominalVoltage1(convert(lineFilter.getNominalVoltage1()))
                        .nominalVoltage2(convert(lineFilter.getNominalVoltage2()));
                    buildGenericFilter(lineFilterEntityBuilder, lineFilter);
                    return lineFilterEntityBuilder.build();
                }
                throw new PowsyblException(WRONG_FILTER_TYPE);
            }
        });

        filterRepositories.put(FilterType.SCRIPT, new Repository<ScriptFilterEntity, ScriptFilterRepository>() {
            @Override
            public FilterType getRepositoryType() {
                return FilterType.SCRIPT;
            }

            @Override
            public ScriptFilterRepository getRepository() {
                return scriptFiltersRepository;
            }

            @Override
            public AbstractFilter toDto(ScriptFilterEntity entity) {
                return buildAbstractFilter(
                    ScriptFilter.builder()
                        .script(entity.getScript()),
                    entity).build();
            }

            @Override
            public ScriptFilterEntity fromDto(AbstractFilter dto) {
                if (dto instanceof ScriptFilter) {
                    var filter = (ScriptFilter) dto;
                    var scriptBuilderEntity = ScriptFilterEntity.builder()
                        .script(filter.getScript());
                    buildAbstractFilter(scriptBuilderEntity, filter);
                    return scriptBuilderEntity.build();
                }
                throw new PowsyblException(WRONG_FILTER_TYPE);
            }
        });
    }

    private UUID getIdOrCreate(UUID id) {
        return id == null ? UUID.randomUUID() : id;
    }

    List<IFilterAttributes> getFilters() {
        return filterRepositories.entrySet().stream()
            .flatMap(entry -> entry.getValue().getFiltersAttributes())
            .collect(Collectors.toList());
    }

    List<IFilterAttributes> getFilters(List<UUID> ids) {
        return filterRepositories.entrySet().stream()
            .flatMap(entry -> entry.getValue().getFiltersAttributes(ids))
            .collect(Collectors.toList());
    }

    Optional<AbstractFilter> getFilter(UUID id) {
        Objects.requireNonNull(id);
        for (Repository<?, ?> repository : filterRepositories.values()) {
            Optional<AbstractFilter> res = repository.getFilter(id);
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
        filterRepositories.get(filter.getType()).modify(id, filter);
    }

    void deleteFilter(UUID id) {
        Objects.requireNonNull(id);
        if (filterRepositories.values().stream().noneMatch(repository -> repository.deleteById(id))) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, FILTER_LIST + id + NOT_FOUND);
        }
    }

    public void deleteAll() {
        filterRepositories.values().forEach(Repository::deleteAll);
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
    public AbstractFilter newScriptFromFilter(UUID id, String scriptName) {
        Objects.requireNonNull(id);

        Optional<AbstractFilter> filter = getFilter(id);
        if (filter.isPresent()) {
            if (filter.get().getType() == FilterType.SCRIPT) {
                throw new PowsyblException(WRONG_FILTER_TYPE);
            } else {
                String script = generateGroovyScriptFromFilter(filter.get());
                return filterRepositories.get(FilterType.SCRIPT).insert(ScriptFilter.builder().name(scriptName).script(script).build());
            }
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, FILTER_LIST + id + NOT_FOUND);
        }
    }
}
