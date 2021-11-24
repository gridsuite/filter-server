/*
 *  Copyright (c) 2021, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.filter.server;

import com.powsybl.commons.PowsyblException;
import org.gridsuite.filter.server.dto.AbstractFilter;
import org.gridsuite.filter.server.dto.FormFilter;
import org.gridsuite.filter.server.dto.HvdcLineFilter;
import org.gridsuite.filter.server.dto.NumericalFilter;
import org.gridsuite.filter.server.entities.HvdcLineFilterEntity;
import org.gridsuite.filter.server.entities.NumericFilterEntity;
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
        return FilterType.FORM;
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
        return new FormFilter(
                entity.getId(),
                entity.getCreationDate(),
                entity.getModificationDate(),
                new HvdcLineFilter(
                        entity.getEquipmentId(),
                        entity.getEquipmentName(),
                        entity.getSubstationName1(),
                        entity.getSubstationName2(),
                        entity.getCountries1(),
                        entity.getCountries2(),
                        NumericalFilter.builder().type(entity.getNominalVoltage().getFilterType()).value1(entity.getNominalVoltage().getValue1()).value2(entity.getNominalVoltage().getValue2())
                        .build()
                )
        );
    }

    @Override
    public HvdcLineFilterEntity fromDto(AbstractFilter dto) {
        if (!(dto instanceof FormFilter)) {
            throw new PowsyblException(WRONG_FILTER_TYPE);
        }
        FormFilter formFilter = (FormFilter) dto;

        if (!(formFilter.getEquipmentFilterForm() instanceof HvdcLineFilter)) {
            throw new PowsyblException(WRONG_FILTER_TYPE);
        }
        var hvdcLineFilterEntityBuilder = HvdcLineFilterEntity.builder();
        HvdcLineFilter hvdcLineFilter = (HvdcLineFilter) formFilter.getEquipmentFilterForm();
        return HvdcLineFilterEntity.builder()
                .id(formFilter.getId())
                .creationDate(formFilter.getCreationDate())
                .modificationDate((formFilter.getModificationDate()))
                .equipmentId(formFilter.getEquipmentFilterForm().getEquipmentID())
                .equipmentName(formFilter.getEquipmentFilterForm().getEquipmentName())
                .countries1(hvdcLineFilter.getCountries1())
                .countries2(hvdcLineFilter.getCountries2())
                .nominalVoltage(new NumericFilterEntity(hvdcLineFilter.getNominalVoltage()))
                .substationName1(hvdcLineFilter.getSubstationName1())
                .substationName2(hvdcLineFilter.getSubstationName2())
                .build();
    }
}
