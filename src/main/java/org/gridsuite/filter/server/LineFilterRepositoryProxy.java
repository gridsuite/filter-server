/*
 *  Copyright (c) 2021, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.filter.server;

import com.powsybl.commons.PowsyblException;
import org.gridsuite.filter.server.dto.AbstractFilter;
import org.gridsuite.filter.server.dto.LineFilter;
import org.gridsuite.filter.server.entities.LineFilterEntity;
import org.gridsuite.filter.server.repositories.LineFilterRepository;
import org.gridsuite.filter.server.utils.FilterType;

/**
 * @author Jacques Borsenberger <jacques.borsenberger at rte-france.com>
 */

class LineFilterRepositoryProxy extends AbstractFilterRepositoryProxy<LineFilterEntity, LineFilterRepository> {

    private final LineFilterRepository lineFilterRepository;

    public LineFilterRepositoryProxy(LineFilterRepository lineFilterRepository) {
        this.lineFilterRepository = lineFilterRepository;
    }

    @Override
    public FilterType getRepositoryType() {
        return FilterType.LINE;
    }

    @Override
    public LineFilterRepository getRepository() {
        return lineFilterRepository;
    }

    @Override
    public AbstractFilter toDto(LineFilterEntity entity) {
        return buildGenericFilter(
            LineFilter.builder()
                .countries1(AbstractFilterRepositoryProxy.cloneIfNotEmptyOrNull(entity.getCountries1()))
                .countries2(AbstractFilterRepositoryProxy.cloneIfNotEmptyOrNull(entity.getCountries2()))
                .substationName1(entity.getSubstationName1())
                .substationName2(entity.getSubstationName2())
                .nominalVoltage1(AbstractFilterRepositoryProxy.convert(entity.getNominalVoltage1()))
                .nominalVoltage2(AbstractFilterRepositoryProxy.convert(entity.getNominalVoltage2())),
            entity).build();
    }

    @Override
    public LineFilterEntity fromDto(AbstractFilter dto) {
        if (dto instanceof LineFilter) {
            var lineFilter = (LineFilter) dto;
            var lineFilterEntityBuilder = LineFilterEntity.builder()
                .equipmentName(lineFilter.getEquipmentName())
                .equipmentId(lineFilter.getEquipmentID())
                .substationName1(lineFilter.getSubstationName1())
                .substationName2(lineFilter.getSubstationName2())
                .countries1(AbstractFilterRepositoryProxy.cloneIfNotEmptyOrNull(lineFilter.getCountries1()))
                .countries2(AbstractFilterRepositoryProxy.cloneIfNotEmptyOrNull(lineFilter.getCountries2()))
                .nominalVoltage1(AbstractFilterRepositoryProxy.convert(lineFilter.getNominalVoltage1()))
                .nominalVoltage2(AbstractFilterRepositoryProxy.convert(lineFilter.getNominalVoltage2()));
            buildGenericFilter(lineFilterEntityBuilder, lineFilter);
            return lineFilterEntityBuilder.build();
        }
        throw new PowsyblException(WRONG_FILTER_TYPE);
    }
}
