/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.server;

import org.gridsuite.filter.AbstractFilter;
import org.gridsuite.filter.FilterLoader;
import org.gridsuite.filter.server.repositories.proxies.AbstractFilterRepositoryProxy;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class FilterLoaderImpl implements FilterLoader {
    private final Map<String, AbstractFilterRepositoryProxy<?, ?>> filterRepositories;

    public FilterLoaderImpl(Map<String, AbstractFilterRepositoryProxy<?, ?>> filterRepositories) {
        this.filterRepositories = filterRepositories;
    }

    @Override
    public Optional<AbstractFilter> getFilter(UUID id) {
        Objects.requireNonNull(id);
        for (AbstractFilterRepositoryProxy<?, ?> repository : filterRepositories.values()) {
            Optional<AbstractFilter> res = repository.getFilter(id);
            if (res.isPresent()) {
                return res;
            }
        }
        return Optional.empty();
    }

    @Override
    public List<AbstractFilter> getFilters(List<UUID> uuids) {
        return uuids.stream()
            .map(id -> getFilter(id).orElse(null)).toList();
    }
}
