/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filters.server.repositories;

import org.gridsuite.filters.server.entities.AbstractFilterEntity;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * @author Borsenebrger Jacques <jacques.borsenberger at rte-france.com>
 */
public interface FiltersRepository<T extends AbstractFilterEntity> extends CassandraRepository<T, String> {

    Optional<T> findByName(String name);

    void deleteByName(String name);

    boolean existsByName(String name);

    <S extends T> S insert(S entity);

    @Transactional
    default boolean renameFilter(String name, String newName) {
        Optional<T> optFilter = findByName(name);
        if (optFilter.isPresent()) {
            T filter = optFilter.get();
            deleteByName(name);
            optFilter.get().setName(newName);
            insert(filter);
            return true;
        }
        return false;
    }

    default boolean delete(String name) {
        if (existsByName(name)) {
            deleteByName(name);
            return true;
        }
        return false;
    }
}
