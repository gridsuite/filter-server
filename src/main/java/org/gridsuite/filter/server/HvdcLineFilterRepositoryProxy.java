/*
 *  Copyright (c) 2021, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.filter.server;

import org.gridsuite.filter.server.dto.*;
import org.gridsuite.filter.server.entities.AbstractFilterEntity;
import org.gridsuite.filter.server.entities.HvdcLineFilterEntity;
import org.gridsuite.filter.server.repositories.HvdcLineFilterRepository;
import org.gridsuite.filter.server.utils.EquipmentType;
import org.gridsuite.filter.server.utils.FilterType;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */

public class HvdcLineFilterRepositoryProxy extends AbstractFilterRepositoryProxy<HvdcLineFilterEntity, HvdcLineFilterRepository> {

    private final HvdcLineFilterRepository hvdcLineFilterRepository;

    public HvdcLineFilterRepositoryProxy(HvdcLineFilterRepository hvdcLineFilterRepository) {
        this.hvdcLineFilterRepository = hvdcLineFilterRepository;
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.CRITERIA;
    }

    @Override
    public EquipmentType getEquipmentType() {
        return EquipmentType.HVDC_LINE;
    }

    @Override
    public HvdcLineFilterRepository getRepository() {
        return hvdcLineFilterRepository;
    }

    @Override
    public AbstractFilter toDto(HvdcLineFilterEntity entity) {
        return super.toFormFilterDto(entity);
    }

    @Override
    public AbstractEquipmentFilterForm buildEquipmentFormFilter(AbstractFilterEntity entity) {
        HvdcLineFilterEntity hvdcLineFilterEntity = (HvdcLineFilterEntity) entity;
        return HvdcLineFilter.builder()
            .equipmentID(hvdcLineFilterEntity.getEquipmentId())
            .equipmentName(hvdcLineFilterEntity.getEquipmentName())
            .substationName1(hvdcLineFilterEntity.getSubstationName1())
            .substationName2(hvdcLineFilterEntity.getSubstationName2())
            .countries1(setToSorterSet(hvdcLineFilterEntity.getCountries1()))
            .countries2(setToSorterSet(hvdcLineFilterEntity.getCountries2()))
            .nominalVoltage(convert(hvdcLineFilterEntity.getNominalVoltage()))
            .freeProperties1(convert(hvdcLineFilterEntity.getSubstationFreeProperties1()))
            .freeProperties2(convert(hvdcLineFilterEntity.getSubstationFreeProperties2()))
            .build();
    }

    @Override
    public HvdcLineFilterEntity fromDto(AbstractFilter dto) {
        CriteriaFilter criteriaFilter = toFormFilter(dto, HvdcLineFilter.class);
        HvdcLineFilter hvdcLineFilter = (HvdcLineFilter) criteriaFilter.getEquipmentFilterForm();
        var hvdcLineFilterEntityBuilder = HvdcLineFilterEntity.builder()
            .countries1(hvdcLineFilter.getCountries1())
            .countries2(hvdcLineFilter.getCountries2())
            .nominalVoltage(convert(hvdcLineFilter.getNominalVoltage()))
            .substationName1(hvdcLineFilter.getSubstationName1())
            .substationName2(hvdcLineFilter.getSubstationName2())
            .substationFreeProperties1(convert(hvdcLineFilter.getFreeProperties1()))
            .substationFreeProperties2(convert(hvdcLineFilter.getFreeProperties2()));
        buildGenericFilter(hvdcLineFilterEntityBuilder, criteriaFilter);
        return hvdcLineFilterEntityBuilder.build();
    }
}
