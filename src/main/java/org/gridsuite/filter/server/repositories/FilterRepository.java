/*
 *  Copyright (c) 2021, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.filter.server.repositories;

import org.gridsuite.filter.server.entities.AbstractFilterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * @author Jacques Borsenberger <jacques.borsenberger at rte-france.com>
 */
@NoRepositoryBean
public interface FilterRepository<T extends AbstractFilterEntity> extends JpaRepository<T, UUID> {

    @Query(value = "SELECT t.id as id, t.creationDate as creationDate, t.modificationDate as modificationDate from #{#entityName} as t")
    List<FilterMetadata> getFiltersMetadata();

    @Query(value = "SELECT t.id as id, t.creationDate as creationDate, t.modificationDate as modificationDate from #{#entityName} as t WHERE t.id in (:ids)")
    List<FilterMetadata> findFiltersMetaDataById(List<UUID> ids);

    @Transactional
    Integer removeById(UUID id);
}
