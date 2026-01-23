/*
 *  Copyright (c) 2021, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.filter.server.repositories.proxies;

import org.gridsuite.filter.AbstractFilterDto;
import org.gridsuite.filter.model.Filter;
import org.gridsuite.filter.server.dto.FilterMetadataDto;
import org.gridsuite.filter.server.entities.AbstractFilterEntity;
import org.gridsuite.filter.server.repositories.FilterMetadata;
import org.gridsuite.filter.server.repositories.FilterRepository;
import org.gridsuite.filter.utils.EquipmentType;
import org.gridsuite.filter.utils.FilterType;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;
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

    public static SortedSet<String> setToSorterSet(Set<String> set) {
        return CollectionUtils.isEmpty(set) ? null : new TreeSet<>(set);
    }

    public abstract R getRepository();

    public abstract AbstractFilterDto toDto(F filterEntity);

    public abstract Filter toModel(F filterEntity);

    public abstract F fromDto(AbstractFilterDto dto);

    public abstract FilterType getFilterType();

    public abstract EquipmentType getEquipmentType(UUID id);

    public Optional<AbstractFilterDto> getFilter(UUID id) {
        Optional<F> element = getRepository().findById(id);
        if (element.isPresent()) {
            return element.map(this::toDto);
        }
        return Optional.empty();
    }

    public Optional<Filter> getFilterModel(UUID id) {
        Optional<F> element = getRepository().findById(id);
        if (element.isPresent()) {
            return element.map(this::toModel);
        }
        return Optional.empty();
    }

    public List<AbstractFilterDto> getFilters(List<UUID> ids) {
        return getRepository().findAllById(ids)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<Filter> getFiltersModels(List<UUID> ids) {
        return getRepository().findAllById(ids)
            .stream()
            .map(this::toModel)
            .collect(Collectors.toList());
    }

    public Stream<FilterMetadataDto> getFiltersAttributes() {
        return getRepository().getFiltersMetadata().stream().map(this::metadataToAttribute);
    }

    public Stream<FilterMetadataDto> getFiltersAttributes(List<UUID> ids) {
        return getRepository().findFiltersMetaDataById(ids).stream().map(this::metadataToAttribute);
    }

    private FilterMetadataDto metadataToAttribute(final FilterMetadata f) {
        return new FilterMetadataDto(f, getFilterType(), getEquipmentType(f.getId()));
    }

    public AbstractFilterDto insert(AbstractFilterDto f) {
        return toDto(getRepository().save(fromDto(f)));
    }

    public List<AbstractFilterDto> insertAll(List<AbstractFilterDto> filters) {
        List<F> savedFilterEntities = getRepository().saveAll(filters.stream().map(this::fromDto).toList());
        return savedFilterEntities.stream().map(this::toDto).toList();
    }

    public AbstractFilterDto modify(UUID id, AbstractFilterDto f) {
        f.setId(id);
        return toDto(getRepository().save(fromDto(f)));
    }

    /**
     * Delete a filter by its id.
     * @param id the filter id
     * @return true if the filter has been deleted, false otherwise
     */
    public boolean deleteById(UUID id) {
        return getRepository().removeById(id) > 0L;
    }

    public void deleteAllByIds(List<UUID> ids) {
        getRepository().deleteAllByIdIn(ids);
    }

    public void deleteAll() {
        getRepository().deleteAll();
    }

    public static void buildAbstractFilter(AbstractFilterEntity.AbstractFilterEntityBuilder<?, ?> builder, AbstractFilterDto dto) {
        /* modification date is managed by jpa, so we don't process it */
        builder.id(dto.getId());
    }

}
