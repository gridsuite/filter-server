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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class DefaultFilterLoader implements FilterLoader {
    private final Map<String, AbstractFilterRepositoryProxy<?, ?>> filterRepositories;

    public DefaultFilterLoader(Map<String, AbstractFilterRepositoryProxy<?, ?>> filterRepositories) {
        this.filterRepositories = filterRepositories;
    }

    @Override
    public List<AbstractFilter> getFilters(List<UUID> uuids) {
        List<UUID> uuidsLoading = new ArrayList<>(uuids);
        List<AbstractFilter> result = new ArrayList<>();
        for (AbstractFilterRepositoryProxy<?, ?> repository : filterRepositories.values()) {
            List<AbstractFilter> partialResult = repository.getFilters(uuidsLoading);
            result.addAll(partialResult);

            // prepare next iteration
            List<UUID> foundUuids = partialResult.stream().map(AbstractFilter::getId).toList();
            uuidsLoading.removeAll(foundUuids);
            if (uuidsLoading.isEmpty()) {
                break;
            }
        }

        return result;
    }
}
