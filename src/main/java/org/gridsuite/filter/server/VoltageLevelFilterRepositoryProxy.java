/*
 *  Copyright (c) 2022, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.filter.server;

import org.gridsuite.filter.server.dto.AbstractEquipmentFilterForm;
import org.gridsuite.filter.server.dto.AbstractFilter;
import org.gridsuite.filter.server.dto.CriteriaFilter;
import org.gridsuite.filter.server.dto.VoltageLevelFilter;
import org.gridsuite.filter.server.entities.AbstractFilterEntity;
import org.gridsuite.filter.server.entities.VoltageLevelFilterEntity;
import org.gridsuite.filter.server.repositories.VoltageLevelFilterRepository;
import org.gridsuite.filter.server.utils.EquipmentType;
import org.gridsuite.filter.server.utils.FilterType;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */

public class VoltageLevelFilterRepositoryProxy extends AbstractFilterRepositoryProxy<VoltageLevelFilterEntity, VoltageLevelFilterRepository> {

    private final VoltageLevelFilterRepository voltageLevelFilterRepository;

    public VoltageLevelFilterRepositoryProxy(VoltageLevelFilterRepository voltageLevelFilterRepository) {
        this.voltageLevelFilterRepository = voltageLevelFilterRepository;
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.CRITERIA;
    }

    @Override
    public EquipmentType getEquipmentType() {
        return EquipmentType.VOLTAGE_LEVEL;
    }

    @Override
    public VoltageLevelFilterRepository getRepository() {
        return voltageLevelFilterRepository;
    }

    @Override
    public AbstractFilter toDto(VoltageLevelFilterEntity entity) {
        return super.toFormFilterDto(entity);
    }

    @Override
    public AbstractEquipmentFilterForm buildEquipmentFormFilter(AbstractFilterEntity entity) {
        VoltageLevelFilterEntity voltageLevelFilterEntity = (VoltageLevelFilterEntity) entity;
        return new VoltageLevelFilter(
            voltageLevelFilterEntity.getEquipmentId(),
            voltageLevelFilterEntity.getEquipmentName(),
            setToSorterSet(voltageLevelFilterEntity.getCountries()),
            convert(voltageLevelFilterEntity.getSubstationFreeProperties()),
            convert(voltageLevelFilterEntity.getNominalVoltage())
        );
    }

    @Override
    public VoltageLevelFilterEntity fromDto(AbstractFilter dto) {
        CriteriaFilter criteriaFilter = toFormFilter(dto, VoltageLevelFilter.class);
        VoltageLevelFilter voltageLevelFilter = (VoltageLevelFilter) criteriaFilter.getEquipmentFilterForm();
        var voltageLevelFilterEntityBuilder =  VoltageLevelFilterEntity.builder()
            .countries(voltageLevelFilter.getCountries())
            .substationFreeProperties(convert(voltageLevelFilter.getFreeProperties()))
            .nominalVoltage(convert(voltageLevelFilter.getNominalVoltage()));
        buildGenericFilter(voltageLevelFilterEntityBuilder, criteriaFilter);
        return voltageLevelFilterEntityBuilder.build();
    }
}
