/*
 *  Copyright (c) 2021, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.filter.server;

import com.powsybl.commons.PowsyblException;
import org.gridsuite.filter.server.dto.*;
import org.gridsuite.filter.server.entities.BatteryFilterEntity;
import org.gridsuite.filter.server.repositories.BatteryFilterRepository;
import org.gridsuite.filter.server.utils.EquipmentType;
import org.gridsuite.filter.server.utils.FilterType;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */

public class BatteryFilterRepositoryProxy extends AbstractFilterRepositoryProxy<BatteryFilterEntity, BatteryFilterRepository> {

    private final BatteryFilterRepository batteryFilterRepository;

    public BatteryFilterRepositoryProxy(BatteryFilterRepository batteryFilterRepository) {
        this.batteryFilterRepository = batteryFilterRepository;
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.FORM;
    }

    public EquipmentType getEquipmentType() {
        return EquipmentType.BATTERY;
    }

    @Override
    public BatteryFilterRepository getRepository() {
        return batteryFilterRepository;
    }

    @Override
    public AbstractFilter toDto(BatteryFilterEntity entity) {
        return new FormFilter(
            entity.getId(),
            entity.getCreationDate(),
            entity.getModificationDate(),
            new BatteryFilter(
                entity.getEquipmentId(),
                entity.getEquipmentName(),
                entity.getSubstationName(),
                entity.getCountries(),
                convert(entity.getNominalVoltage())
            )
        );
    }

    @Override
    public BatteryFilterEntity fromDto(AbstractFilter dto) {
        if (!(dto instanceof FormFilter)) {
            throw new PowsyblException(WRONG_FILTER_TYPE);
        }
        FormFilter formFilter = (FormFilter) dto;

        if (!(formFilter.getEquipmentFilterForm() instanceof BatteryFilter)) {
            throw new PowsyblException(WRONG_FILTER_TYPE);
        }
        var batteryFilterEntityBuilder = BatteryFilterEntity.builder();
        buildInjectionFilter(batteryFilterEntityBuilder, formFilter);
        return batteryFilterEntityBuilder.build();
    }
}
