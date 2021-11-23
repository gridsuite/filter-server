/*
 *  Copyright (c) 2021, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.filter.server;

import org.gridsuite.filter.server.dto.*;
import org.gridsuite.filter.server.entities.*;
import org.gridsuite.filter.server.repositories.FilterMetadata;
import org.gridsuite.filter.server.repositories.FilterRepository;
import org.gridsuite.filter.server.utils.EquipmentType;
import org.gridsuite.filter.server.utils.FilterType;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * @author Jacques Borsenberger <jacques.borsenberger at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */

public abstract class AbstractFilterRepositoryProxy<FilterEntity extends AbstractFilterEntity, EntityRepository extends FilterRepository<FilterEntity>> {
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

    abstract FilterType getFilterType();

    abstract EquipmentType getEquipmentType();

    Optional<AbstractFilter> getFilter(UUID id) {
        Optional<FilterEntity> element = getRepository().findById(id);
        if (element.isPresent()) {
            return element.map(this::toDto);
        }
        return Optional.empty();
    }

    Optional<FilterEntity> getFilterEntity(UUID id) {
        return getRepository().findById(id);
    }

    Stream<FilterAttributes> getFiltersAttributes() {
        return getRepository().getFiltersMetadata().stream().map(this::metadataToAttribute);
    }

    Stream<FilterAttributes> getFiltersAttributes(List<UUID> ids) {
        return getRepository().findFiltersMetaDataById(ids).stream().map(this::metadataToAttribute);
    }

    FilterAttributes metadataToAttribute(FilterMetadata f) {
        return new FilterAttributes(f, getFilterType(), getEquipmentType());
    }

    AbstractFilter insert(AbstractFilter f) {
        return toDto(getRepository().save(fromDto(f)));
    }

    void modify(UUID id, AbstractFilter f) {
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
        return builder.id(entity.getId())
                .creationDate(entity.getCreationDate()).modificationDate(entity.getModificationDate());
    }

    AbstractFilter.AbstractFilterBuilder<?, ?> buildFormFilter(FormFilter.FormFilterBuilder<?, ?> builder, FormFilterEntity entity) {
        return buildAbstractFilter(builder.equipmentFilterForm(null), entity);
    }

    AbstractFilter.AbstractFilterBuilder<?, ?> buildInjectionFilter(AbstractInjectionFilter.AbstractInjectionFilterBuilder<?, ?> builder, AbstractInjectionFilterEntity entity) {
        return buildFormFilter(builder.substationName(entity.getSubstationName())
                .countries(AbstractFilterRepositoryProxy.cloneIfNotEmptyOrNull(entity.getCountries()))
                .nominalVoltage(AbstractFilterRepositoryProxy.convert(entity.getNominalVoltage())), entity);
    }

    void buildFormFilter(FormFilterEntity.FormFilterEntityBuilder<?, ?> builder, FormFilter dto) {
        buildAbstractFilter(builder, dto);
        builder.equipmentId(dto.getEquipmentID())
                .equipmentName(dto.getEquipmentName());
    }

    void buildInjectionFilter(AbstractInjectionFilterEntity.AbstractInjectionFilterEntityBuilder<?, ?> builder, AbstractInjectionFilter dto) {
        buildGenericFilter(builder, dto);
        builder.substationName(dto.getSubstationName())
                .countries(AbstractFilterRepositoryProxy.cloneIfNotEmptyOrNull(dto.getCountries()))
                .nominalVoltage(AbstractFilterRepositoryProxy.convert(dto.getNominalVoltage()));
    }

    void buildAbstractFilter(AbstractFilterEntity.AbstractFilterEntityBuilder<?, ?> builder, AbstractFilter dto) {
        /* modification date is managed by jpa, so we don't process it */
        builder.id(dto.getId())
                .creationDate(getDateOrCreate(dto.getCreationDate()));
    }

    Date getDateOrCreate(Date dt) {
        return dt == null ? new Date() : dt;
    }
}
