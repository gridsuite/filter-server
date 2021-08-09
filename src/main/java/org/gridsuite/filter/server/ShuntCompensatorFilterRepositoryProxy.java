/*
 *  Copyright (c) 2021, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.filter.server;

import com.powsybl.commons.PowsyblException;
import org.gridsuite.filter.server.dto.AbstractFilter;
import org.gridsuite.filter.server.dto.ShuntCompensatorFilter;
import org.gridsuite.filter.server.entities.ShuntCompensatorFilterEntity;
import org.gridsuite.filter.server.repositories.ShuntCompensatorFilterRepository;
import org.gridsuite.filter.server.utils.FilterType;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */

class ShuntCompensatorFilterRepositoryProxy extends AbstractFilterRepositoryProxy<ShuntCompensatorFilterEntity, ShuntCompensatorFilterRepository> {

    private final ShuntCompensatorFilterRepository shuntCompensatorFilterRepository;

    public ShuntCompensatorFilterRepositoryProxy(ShuntCompensatorFilterRepository shuntCompensatorFilterRepository) {
        this.shuntCompensatorFilterRepository = shuntCompensatorFilterRepository;
    }

    @Override
    public FilterType getRepositoryType() {
        return FilterType.SHUNT_COMPENSATOR;
    }

    @Override
    public ShuntCompensatorFilterRepository getRepository() {
        return shuntCompensatorFilterRepository;
    }

    @Override
    public AbstractFilter toDto(ShuntCompensatorFilterEntity entity) {
        return buildInjectionFilter(
            ShuntCompensatorFilter.builder(), entity).build();
    }

    @Override
    public ShuntCompensatorFilterEntity fromDto(AbstractFilter dto) {
        if (dto instanceof ShuntCompensatorFilter) {
            var shuntCompensatorFilterEntityBuilder = ShuntCompensatorFilterEntity.builder();
            buildInjectionFilter(shuntCompensatorFilterEntityBuilder, (ShuntCompensatorFilter) dto);
            return shuntCompensatorFilterEntityBuilder.build();
        }
        throw new PowsyblException(WRONG_FILTER_TYPE);
    }
}
