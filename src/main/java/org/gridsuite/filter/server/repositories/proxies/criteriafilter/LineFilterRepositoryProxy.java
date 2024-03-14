/*
 *  Copyright (c) 2021, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.filter.server.repositories.proxies.criteriafilter;

import org.gridsuite.filter.criteriafilter.AbstractEquipmentFilterForm;
import org.gridsuite.filter.AbstractFilter;
import org.gridsuite.filter.criteriafilter.CriteriaFilter;
import org.gridsuite.filter.criteriafilter.LineFilter;
import org.gridsuite.filter.server.entities.AbstractFilterEntity;
import org.gridsuite.filter.server.entities.criteriafilter.LineFilterEntity;
import org.gridsuite.filter.server.repositories.criteriafilter.LineFilterRepository;
import org.gridsuite.filter.server.repositories.proxies.AbstractFilterRepositoryProxy;
import org.gridsuite.filter.utils.EquipmentType;
import org.gridsuite.filter.utils.FilterType;

/**
 * @author Jacques Borsenberger <jacques.borsenberger at rte-france.com>
 */

public class LineFilterRepositoryProxy extends AbstractFilterRepositoryProxy<LineFilterEntity, LineFilterRepository> {

    private final LineFilterRepository lineFilterRepository;

    public LineFilterRepositoryProxy(LineFilterRepository lineFilterRepository) {
        this.lineFilterRepository = lineFilterRepository;
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.CRITERIA;
    }

    @Override
    public EquipmentType getEquipmentType() {
        return EquipmentType.LINE;
    }

    @Override
    public LineFilterRepository getRepository() {
        return lineFilterRepository;
    }

    @Override
    public AbstractFilter toDto(LineFilterEntity entity) {
        return super.toFormFilterDto(entity);
    }

    @Override
    public AbstractEquipmentFilterForm buildEquipmentFormFilter(AbstractFilterEntity entity) {
        LineFilterEntity lineFilterEntity = (LineFilterEntity) entity;
        return LineFilter.builder()
            .equipmentID(lineFilterEntity.getEquipmentId())
            .equipmentName(lineFilterEntity.getEquipmentName())
            .substationName1(lineFilterEntity.getSubstationName1())
            .substationName2(lineFilterEntity.getSubstationName2())
            .countries1(setToSorterSet(lineFilterEntity.getCountries1()))
            .countries2(setToSorterSet(lineFilterEntity.getCountries2()))
            .nominalVoltage1(convert(lineFilterEntity.getNominalVoltage1()))
            .nominalVoltage2(convert(lineFilterEntity.getNominalVoltage2()))
            .freeProperties1(convert(lineFilterEntity.getSubstationFreeProperties1()))
            .freeProperties2(convert(lineFilterEntity.getSubstationFreeProperties2()))
            .build();
    }

    @Override
    public LineFilterEntity fromDto(AbstractFilter dto) {
        CriteriaFilter criteriaFilter = toFormFilter(dto, LineFilter.class);
        LineFilter lineFilter = (LineFilter) criteriaFilter.getEquipmentFilterForm();
        var lineFilterEntityBuilder = LineFilterEntity.builder()
            .countries1(lineFilter.getCountries1())
            .countries2(lineFilter.getCountries2())
            .nominalVoltage1(convert(lineFilter.getNominalVoltage1()))
            .nominalVoltage2(convert(lineFilter.getNominalVoltage2()))
            .substationName1(lineFilter.getSubstationName1())
            .substationName2(lineFilter.getSubstationName2())
            .substationFreeProperties1(convert(lineFilter.getFreeProperties1()))
            .substationFreeProperties2(convert(lineFilter.getFreeProperties2()));
        buildGenericFilter(lineFilterEntityBuilder, criteriaFilter);
        return lineFilterEntityBuilder.build();
    }
}
