/*
 *  Copyright (c) 2021, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.filter.server;

import com.powsybl.commons.PowsyblException;
import org.gridsuite.filter.server.dto.*;
import org.gridsuite.filter.server.entities.*;
import org.gridsuite.filter.server.repositories.FilterMetadata;
import org.gridsuite.filter.server.repositories.FilterRepository;
import org.gridsuite.filter.server.utils.FilterType;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Stream;

/**
 * @author Jacques Borsenberger <jacques.borsenberger at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */

public abstract class AbstractFilterRepositoryProxy<F extends AbstractFilterEntity, R extends FilterRepository<F>> {
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

    static SortedSet<String> setToSorterSet(Set<String> set) {
        return CollectionUtils.isEmpty(set) ? null : new TreeSet<>(set);
    }

    static NumericFilterEntity convert(NumericalFilter numericalFilter) {
        return numericalFilter != null ?
                new NumericFilterEntity(null, numericalFilter.getType(), numericalFilter.getValue1(), numericalFilter.getValue2())
                : null;
    }

    abstract R getRepository();

    abstract AbstractFilter toDto(F filterEntity);

    abstract F fromDto(AbstractFilter dto);

    abstract FilterType getFilterType();

    Optional<AbstractFilter> getFilter(UUID id) {
        Optional<F> element = getRepository().findById(id);
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
        return new FilterAttributes(f, getFilterType());
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

    void buildGenericFilter(AbstractGenericFilterEntity.AbstractGenericFilterEntityBuilder<?, ?> builder, FormFilter dto) {
        buildAbstractFilter(builder, dto);
        builder.equipmentId(dto.getEquipmentFilterForm().getEquipmentID())
                .equipmentName(dto.getEquipmentFilterForm().getEquipmentName());
    }

    void buildInjectionFilter(AbstractInjectionFilterEntity.AbstractInjectionFilterEntityBuilder<?, ?> builder, FormFilter dto) {
        buildGenericFilter(builder, dto);
        if (!(dto.getEquipmentFilterForm() instanceof AbstractInjectionFilter)) {
            throw new PowsyblException(WRONG_FILTER_TYPE);
        }
        AbstractInjectionFilter injectionFilter = (AbstractInjectionFilter) dto.getEquipmentFilterForm();
        builder.substationName(injectionFilter.getSubstationName())
            .countries(AbstractFilterRepositoryProxy.cloneIfNotEmptyOrNull(injectionFilter.getCountries()))
            .nominalVoltage(AbstractFilterRepositoryProxy.convert(injectionFilter.getNominalVoltage()));
    }

    void buildAbstractFilter(AbstractFilterEntity.AbstractFilterEntityBuilder<?, ?> builder, AbstractFilter dto) {
        /* modification date is managed by jpa, so we don't process it */
        builder.id(dto.getId())
                .creationDate(getDateOrCreate(dto.getCreationDate()));
    }

    public static Date getDateOrCreate(Date dt) {
        return dt == null ? new Date() : dt;
    }

    public AbstractFilter toFormFilterDto(AbstractGenericFilterEntity entity) {
        return new FormFilter(
                entity.getId(),
                entity.getCreationDate(),
                entity.getModificationDate(),
                buildEquipmentFormFilter(entity)
        );
    }

    public abstract AbstractEquipmentFilterForm buildEquipmentFormFilter(AbstractFilterEntity entity);

    public InjectionFilterAttributes buildInjectionAttributesFromEntity(AbstractInjectionFilterEntity entity) {
        return new InjectionFilterAttributes(entity.getEquipmentId(),
            entity.getEquipmentName(),
            entity.getSubstationName(),
            setToSorterSet(entity.getCountries()),
            convert(entity.getNominalVoltage())
        );
    }

    public static FormFilter toFormFilter(AbstractFilter dto, Class<? extends AbstractEquipmentFilterForm> clazz) {
        if (!(dto instanceof FormFilter)) {
            throw new PowsyblException(WRONG_FILTER_TYPE);
        }
        FormFilter formFilter = (FormFilter) dto;

        if (!(clazz.isInstance(formFilter.getEquipmentFilterForm()))) {
            throw new PowsyblException(WRONG_FILTER_TYPE);
        }
        return formFilter;
    }
}
