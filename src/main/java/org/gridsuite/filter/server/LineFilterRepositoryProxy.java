/*
 *  Copyright (c) 2021, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.filter.server;

import com.powsybl.commons.PowsyblException;
import org.gridsuite.filter.server.dto.*;
import org.gridsuite.filter.server.entities.LineFilterEntity;
import org.gridsuite.filter.server.entities.NumericFilterEntity;
import org.gridsuite.filter.server.repositories.LineFilterRepository;
import org.gridsuite.filter.server.utils.EquipmentType;
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

    public EquipmentType getEquipmentType() {
        return EquipmentType.LINE;
    }

    @Override
    public LineFilterRepository getRepository() {
        return lineFilterRepository;
    }

    @Override
    public AbstractFilter toDto(LineFilterEntity entity) {
        return new FormFilter(
                entity.getId(),
                entity.getCreationDate(),
                entity.getModificationDate(),
                new LineFilter(
                        entity.getEquipmentId(),
                        entity.getEquipmentName(),
                        entity.getSubstationName1(),
                        entity.getSubstationName2(),
                        entity.getCountries1(),
                        entity.getCountries2(),
                        NumericalFilter.builder().type(entity.getNominalVoltage1().getFilterType()).value1(entity.getNominalVoltage1().getValue1()).value2(entity.getNominalVoltage1().getValue2()).build(),
                        NumericalFilter.builder().type(entity.getNominalVoltage2().getFilterType()).value1(entity.getNominalVoltage2().getValue1()).value2(entity.getNominalVoltage2().getValue2()).build()
                )
        );
    }

    @Override
    public LineFilterEntity fromDto(AbstractFilter dto) {
        if (!(dto instanceof FormFilter)) {
            throw new PowsyblException(WRONG_FILTER_TYPE);
        }
        FormFilter formFilter = (FormFilter) dto;

        if (!(formFilter.getEquipmentFilterForm() instanceof LineFilter)) {
            throw new PowsyblException(WRONG_FILTER_TYPE);
        }
        LineFilter lineFilter = (LineFilter) formFilter.getEquipmentFilterForm();
        return LineFilterEntity.builder()
            .id(formFilter.getId())
            .creationDate(getDateOrCreate(formFilter.getCreationDate()))
            .equipmentId(formFilter.getEquipmentFilterForm().getEquipmentID())
            .equipmentName(formFilter.getEquipmentFilterForm().getEquipmentName())
            .countries1(lineFilter.getCountries1())
            .countries2(lineFilter.getCountries2())
            .nominalVoltage1(new NumericFilterEntity(lineFilter.getNominalVoltage1()))
            .nominalVoltage2(new NumericFilterEntity(lineFilter.getNominalVoltage2()))
            .substationName1(lineFilter.getSubstationName1())
            .substationName2(lineFilter.getSubstationName2())
            .build();
    }
}
