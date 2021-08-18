/*
 *  Copyright (c) 2021, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.filter.server;

import com.powsybl.commons.PowsyblException;
import org.gridsuite.filter.server.dto.AbstractFilter;
import org.gridsuite.filter.server.dto.HvdcLineFilter;
import org.gridsuite.filter.server.entities.HvdcLineFilterEntity;
import org.gridsuite.filter.server.repositories.HvdcLineFilterRepository;
import org.gridsuite.filter.server.utils.FilterType;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */

class HvdcLineFilterRepositoryProxy extends AbstractFilterRepositoryProxy<HvdcLineFilterEntity, HvdcLineFilterRepository> {

    private final HvdcLineFilterRepository hvdcLineFilterRepository;

    public HvdcLineFilterRepositoryProxy(HvdcLineFilterRepository hvdcLineFilterRepository) {
        this.hvdcLineFilterRepository = hvdcLineFilterRepository;
    }

    @Override
    public FilterType getRepositoryType() {
        return FilterType.HVDC_LINE;
    }

    @Override
    public HvdcLineFilterRepository getRepository() {
        return hvdcLineFilterRepository;
    }

    @Override
    public AbstractFilter toDto(HvdcLineFilterEntity entity) {
        return buildGenericFilter(
            HvdcLineFilter.builder()
                .countries1(AbstractFilterRepositoryProxy.cloneIfNotEmptyOrNull(entity.getCountries1()))
                .countries2(AbstractFilterRepositoryProxy.cloneIfNotEmptyOrNull(entity.getCountries2()))
                .substationName1(entity.getSubstationName1())
                .substationName2(entity.getSubstationName2())
                .nominalVoltage1(AbstractFilterRepositoryProxy.convert(entity.getNominalVoltage1()))
                .nominalVoltage2(AbstractFilterRepositoryProxy.convert(entity.getNominalVoltage2())),
            entity).build();
    }

    @Override
    public HvdcLineFilterEntity fromDto(AbstractFilter dto) {
        if (dto instanceof HvdcLineFilter) {
            var hvdcLineFilter = (HvdcLineFilter) dto;
            var hvdcLineFilterEntityBuilder = HvdcLineFilterEntity.builder()
                .substationName1(hvdcLineFilter.getSubstationName1())
                .substationName2(hvdcLineFilter.getSubstationName2())
                .countries1(AbstractFilterRepositoryProxy.cloneIfNotEmptyOrNull(hvdcLineFilter.getCountries1()))
                .countries2(AbstractFilterRepositoryProxy.cloneIfNotEmptyOrNull(hvdcLineFilter.getCountries2()))
                .nominalVoltage1(AbstractFilterRepositoryProxy.convert(hvdcLineFilter.getNominalVoltage1()))
                .nominalVoltage2(AbstractFilterRepositoryProxy.convert(hvdcLineFilter.getNominalVoltage2()));
            buildGenericFilter(hvdcLineFilterEntityBuilder, hvdcLineFilter);
            return hvdcLineFilterEntityBuilder.build();
        }
        throw new PowsyblException(WRONG_FILTER_TYPE);
    }
}
