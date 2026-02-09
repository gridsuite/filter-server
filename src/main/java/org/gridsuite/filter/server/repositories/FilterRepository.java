/*
 *  Copyright (c) 2021, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.filter.server.repositories;

import org.gridsuite.filter.api.dto.FilterMetadata;
import org.gridsuite.filter.server.entities.AbstractFilterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * @author Jacques Borsenberger <jacques.borsenberger at rte-france.com>
 */
@NoRepositoryBean
public interface FilterRepository<T extends AbstractFilterEntity> extends JpaRepository<T, UUID> {

    @Query(value = "SELECT t.id as id, t.modificationDate as modificationDate from #{#entityName} as t")
    List<FilterMetadata> getFiltersMetadata();

    @Query(value = "SELECT t.id as id, t.modificationDate as modificationDate from #{#entityName} as t WHERE t.id in (:ids)")
    List<FilterMetadata> findFiltersMetaDataById(List<UUID> ids);

    /**
     * Remove a filter by id.
     * @param id the filter id
     * @return the number of filter(s) removed ({@code 0} or {@code 1})
     * @see #deleteById(Object) like deleteById(id) but with the indicator of either a filter has been removed or not
     */
    @Transactional
    long removeById(@NonNull UUID id);

    @Transactional
    void deleteAllByIdIn(List<UUID> ids);
}
