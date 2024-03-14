/*
 *  Copyright (c) 2021, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.filter.server.repositories.proxies.criteriafilter;

import org.gridsuite.filter.criteriafilter.AbstractEquipmentFilterForm;
import org.gridsuite.filter.AbstractFilter;
import org.gridsuite.filter.criteriafilter.BatteryFilter;
import org.gridsuite.filter.server.entities.AbstractFilterEntity;
import org.gridsuite.filter.server.entities.criteriafilter.AbstractInjectionFilterEntity;
import org.gridsuite.filter.server.entities.criteriafilter.BatteryFilterEntity;
import org.gridsuite.filter.server.repositories.criteriafilter.BatteryFilterRepository;
import org.gridsuite.filter.server.repositories.proxies.AbstractFilterRepositoryProxy;
import org.gridsuite.filter.utils.EquipmentType;
import org.gridsuite.filter.utils.FilterType;

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
        return FilterType.CRITERIA;
    }

    @Override
    public EquipmentType getEquipmentType() {
        return EquipmentType.BATTERY;
    }

    @Override
    public BatteryFilterRepository getRepository() {
        return batteryFilterRepository;
    }

    @Override
    public AbstractFilter toDto(BatteryFilterEntity entity) {
        return super.toFormFilterDto(entity);
    }

    @Override
    public AbstractEquipmentFilterForm buildEquipmentFormFilter(AbstractFilterEntity entity) {
        return new BatteryFilter(buildInjectionAttributesFromEntity((AbstractInjectionFilterEntity) entity));
    }

    @Override
    public BatteryFilterEntity fromDto(AbstractFilter dto) {
        var batteryFilterEntityBuilder = BatteryFilterEntity.builder();
        buildInjectionFilter(batteryFilterEntityBuilder, toFormFilter(dto, BatteryFilter.class));
        return batteryFilterEntityBuilder.build();
    }
}
