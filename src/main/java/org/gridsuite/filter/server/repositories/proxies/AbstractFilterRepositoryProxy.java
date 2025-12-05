/*
 *  Copyright (c) 2021, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.filter.server.repositories.proxies;

import org.gridsuite.filter.AbstractFilter;
import org.gridsuite.filter.server.dto.FilterAttributes;
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

    public abstract AbstractFilter toDto(F filterEntity);

    public abstract F fromDto(AbstractFilter dto);

    public abstract FilterType getFilterType();

    public abstract EquipmentType getEquipmentType(UUID id);

    public Optional<AbstractFilter> getFilter(UUID id) {
        Optional<F> element = getRepository().findById(id);
        if (element.isPresent()) {
            return element.map(this::toDto);
        }
        return Optional.empty();
    }

    public List<AbstractFilter> getFilters(List<UUID> ids) {
        return getRepository().findAllById(ids)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public Stream<FilterAttributes> getFiltersAttributes() {
        return getRepository().getFiltersMetadata().stream().map(this::metadataToAttribute);
    }

    public Stream<FilterAttributes> getFiltersAttributes(List<UUID> ids) {
        return getRepository().findFiltersMetaDataById(ids).stream().map(this::metadataToAttribute);
    }

    private FilterAttributes metadataToAttribute(final FilterMetadata f) {
        return new FilterAttributes(f, getFilterType(), getEquipmentType(f.getId()));
    }

    public AbstractFilter insert(AbstractFilter f) {
        return toDto(getRepository().save(fromDto(f)));
    }

    public List<AbstractFilter> insertAll(List<AbstractFilter> filters) {
        List<F> savedFilterEntities = getRepository().saveAll(filters.stream().map(this::fromDto).toList());
        return savedFilterEntities.stream().map(this::toDto).toList();
    }

    public AbstractFilter modify(UUID id, AbstractFilter f) {
        f.setId(id);
        return toDto(getRepository().save(fromDto(f)));
    }

    /**
     * Delete a filter by its id.
     * @param id the filter id
     * @return true if the filter has been deleted, false otherwise
     */
    public boolean deleteById(UUID id) {
        Long result = getRepository().removeById(id);
        return result != null && result > 0L;
    }

    public void deleteAllByIds(List<UUID> ids) {
        getRepository().deleteAllByIdIn(ids);
    }

    public void deleteAll() {
        getRepository().deleteAll();
    }

    public static void buildAbstractFilter(AbstractFilterEntity.AbstractFilterEntityBuilder<?, ?> builder, AbstractFilter dto) {
        /* modification date is managed by jpa, so we don't process it */
        builder.id(dto.getId());
    }

}
