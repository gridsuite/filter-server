/*
 *  Copyright (c) 2021, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.filter.server;

import org.gridsuite.filter.server.dto.AbstractFilter;
import org.gridsuite.filter.server.dto.AbstractGenericFilter;
import org.gridsuite.filter.server.dto.FilterAttributes;
import org.gridsuite.filter.server.dto.NumericalFilter;
import org.gridsuite.filter.server.entities.AbstractFilterEntity;
import org.gridsuite.filter.server.entities.AbstractGenericFilterEntity;
import org.gridsuite.filter.server.entities.NumericFilterEntity;
import org.gridsuite.filter.server.repositories.FilterMetadata;
import org.gridsuite.filter.server.repositories.FilterRepository;
import org.gridsuite.filter.server.utils.FilterType;

import javax.persistence.EntityNotFoundException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * @author Jacques Borsenberger <jacques.borsenberger at rte-france.com>
 */

abstract class AbstractFilterRepositoryProxy<FilterEntity extends AbstractFilterEntity, EntityRepository extends FilterRepository<FilterEntity>> {
    public static final String WRONG_FILTER_TYPE = "Wrong filter type, should never happen";

    static <T> Set<T> cloneIfNotEmptyOrNull(Set<T> set) {
        if (set != null && !set.isEmpty()) {
            return new HashSet<>(set);
        }
        return null;
    }

    static NumericalFilter convert(NumericFilterEntity entity) {
        return entity != null ? new NumericalFilter(entity.getFilterType(), entity.getValue1(), entity.getValue2()) : null;
    }

    static NumericFilterEntity convert(NumericalFilter numericalFilter) {
        return numericalFilter != null ?
            new NumericFilterEntity(null, numericalFilter.getType(), numericalFilter.getValue1(), numericalFilter.getValue2())
            : null;
    }

    abstract EntityRepository getRepository();

    abstract AbstractFilter toDto(FilterEntity filterEntity);

    abstract FilterEntity fromDto(AbstractFilter dto);

    abstract FilterType getRepositoryType();

    Optional<AbstractFilter> getFilter(UUID id) {
        Optional<FilterEntity> element = getRepository().findById(id);
        if (element.isPresent()) {
            return element.map(this::toDto);
        }
        return Optional.empty();
    }

    Stream<FilterAttributes> getFiltersAttributes() {
        return getRepository().getFiltersMetadata().stream().map(this::metadataToAttribute);
    }

    Stream<FilterAttributes> getFiltersAttributes(List<UUID> ids) {
        return getRepository().findFiltersMetaDataById(ids).stream().map(this::metadataToAttribute);
    }

    FilterAttributes metadataToAttribute(FilterMetadata f) {
        return new FilterAttributes(f, getRepositoryType());
    }

    AbstractFilter insert(AbstractFilter f) {
        return toDto(getRepository().save(fromDto(f)));
    }

    void modify(UUID id, AbstractFilter f) {
        if (!getRepository().existsById(id)) {
            throw new EntityNotFoundException(id.toString());
        }
        f.setId(id);
        toDto(getRepository().save(fromDto(f)));
    }

    boolean deleteById(UUID id) {
        return getRepository().removeById(id) != 0;
    }

    void deleteAll() {
        getRepository().deleteAll();
    }

    AbstractFilter.AbstractFilterBuilder<?, ?> buildAbstractFilter(AbstractFilter.AbstractFilterBuilder<?, ?> builder, AbstractFilterEntity entity) {
        return builder.name(entity.getName()).id(entity.getId())
            .creationDate(entity.getCreationDate()).modificationDate(entity.getModificationDate())
            .description(entity.getDescription());
    }

    AbstractFilter.AbstractFilterBuilder<?, ?> buildGenericFilter(AbstractGenericFilter.AbstractGenericFilterBuilder<?, ?> builder, AbstractGenericFilterEntity entity) {
        return buildAbstractFilter(builder.equipmentID(entity.getEquipmentId()).equipmentName(entity.getEquipmentName()), entity);
    }

    void buildGenericFilter(AbstractGenericFilterEntity.AbstractGenericFilterEntityBuilder<?, ?> builder, AbstractGenericFilter dto) {
        buildAbstractFilter(builder, dto);
        builder.equipmentId(dto.getEquipmentID())
            .equipmentName(dto.getEquipmentName());
    }

    void buildAbstractFilter(AbstractFilterEntity.AbstractFilterEntityBuilder<?, ?> builder, AbstractFilter dto) {
        /* dates a managed by jpa, so we don't process them */
        builder.name(dto.getName())
            .id(getIdOrCreate(dto.getId()))
            .description(dto.getDescription());
    }

    UUID getIdOrCreate(UUID id) {
        return id == null ? UUID.randomUUID() : id;
    }
}
