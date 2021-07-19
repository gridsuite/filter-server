/*
 *  Copyright (c) 2021, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.filter.server;

import com.powsybl.commons.PowsyblException;
import org.gridsuite.filter.server.dto.AbstractFilter;
import org.gridsuite.filter.server.dto.BatteryFilter;
import org.gridsuite.filter.server.entities.BatteryFilterEntity;
import org.gridsuite.filter.server.repositories.BatteryFilterRepository;
import org.gridsuite.filter.server.utils.FilterType;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */

class BatteryFilterRepositoryProxy extends AbstractFilterRepositoryProxy<BatteryFilterEntity, BatteryFilterRepository> {

    private final BatteryFilterRepository batteryFilterRepository;

    public BatteryFilterRepositoryProxy(BatteryFilterRepository batteryFilterRepository) {
        this.batteryFilterRepository = batteryFilterRepository;
    }

    @Override
    public FilterType getRepositoryType() {
        return FilterType.BATTERY;
    }

    @Override
    public BatteryFilterRepository getRepository() {
        return batteryFilterRepository;
    }

    @Override
    public AbstractFilter toDto(BatteryFilterEntity entity) {
        return buildInjectionFilter(
            BatteryFilter.builder(), entity).build();
    }

    @Override
    public BatteryFilterEntity fromDto(AbstractFilter dto) {
        if (dto instanceof BatteryFilter) {
            var batteryFilterEntityBuilder = BatteryFilterEntity.builder();
            buildInjectionFilter(batteryFilterEntityBuilder, (BatteryFilter) dto);
            return batteryFilterEntityBuilder.build();
        }
        throw new PowsyblException(WRONG_FILTER_TYPE);
    }
}
