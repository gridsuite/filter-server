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
import org.gridsuite.filter.server.entities.HvdcLineFilterEntity;
import org.gridsuite.filter.server.repositories.HvdcLineFilterRepository;
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
                setToSorterSet(entity.getCountries1()),
                setToSorterSet(entity.getCountries2()),
                convert(entity.getNominalVoltage())
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
        HvdcLineFilter hvdcLineFilter = (HvdcLineFilter) formFilter.getEquipmentFilterForm();
        var hvdcLineFilterEntityBuilder = HvdcLineFilterEntity.builder()
            .countries1(hvdcLineFilter.getCountries1())
            .countries2(hvdcLineFilter.getCountries2())
            .nominalVoltage(convert(hvdcLineFilter.getNominalVoltage()))
            .substationName1(hvdcLineFilter.getSubstationName1())
            .substationName2(hvdcLineFilter.getSubstationName2());
        buildGenericFilter(hvdcLineFilterEntityBuilder, formFilter);
        return hvdcLineFilterEntityBuilder.build();
    }
}
