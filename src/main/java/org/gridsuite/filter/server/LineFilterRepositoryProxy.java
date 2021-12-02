/*
 *  Copyright (c) 2021, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.filter.server;

import org.gridsuite.filter.server.dto.*;
import org.gridsuite.filter.server.entities.AbstractFilterEntity;
import org.gridsuite.filter.server.entities.LineFilterEntity;
import org.gridsuite.filter.server.repositories.LineFilterRepository;
import org.gridsuite.filter.server.utils.FilterType;

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
        return FilterType.FORM;
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
        return new LineFilter(
            lineFilterEntity.getEquipmentId(),
            lineFilterEntity.getEquipmentName(),
            lineFilterEntity.getSubstationName1(),
            lineFilterEntity.getSubstationName2(),
            setToSorterSet(lineFilterEntity.getCountries1()),
            setToSorterSet(lineFilterEntity.getCountries2()),
            convert(lineFilterEntity.getNominalVoltage1()),
            convert(lineFilterEntity.getNominalVoltage2())
        );
    }

    @Override
    public LineFilterEntity fromDto(AbstractFilter dto) {
        FormFilter formFilter = toFormFilter(dto, LineFilter.class);
        LineFilter lineFilter = (LineFilter) formFilter.getEquipmentFilterForm();
        var lineFilterEntityBuilder =    LineFilterEntity.builder()
            .countries1(lineFilter.getCountries1())
            .countries2(lineFilter.getCountries2())
            .nominalVoltage1(convert(lineFilter.getNominalVoltage1()))
            .nominalVoltage2(convert(lineFilter.getNominalVoltage2()))
            .substationName1(lineFilter.getSubstationName1())
            .substationName2(lineFilter.getSubstationName2());
        buildGenericFilter(lineFilterEntityBuilder, formFilter);
        return lineFilterEntityBuilder.build();
    }
}
