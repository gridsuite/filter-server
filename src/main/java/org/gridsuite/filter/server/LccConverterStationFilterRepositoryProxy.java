/*
 *  Copyright (c) 2021, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.filter.server;

import com.powsybl.commons.PowsyblException;
import org.gridsuite.filter.server.dto.AbstractFilter;
import org.gridsuite.filter.server.dto.LccConverterStationFilter;
import org.gridsuite.filter.server.entities.LccConverterStationFilterEntity;
import org.gridsuite.filter.server.repositories.LccConverterStationFilterRepository;
import org.gridsuite.filter.server.utils.EquipmentType;
import org.gridsuite.filter.server.utils.FilterType;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */

public class LccConverterStationFilterRepositoryProxy extends AbstractFilterRepositoryProxy<LccConverterStationFilterEntity, LccConverterStationFilterRepository> {

    private final LccConverterStationFilterRepository lccConverterStationFilterRepository;

    public LccConverterStationFilterRepositoryProxy(LccConverterStationFilterRepository lccConverterStationFilterRepository) {
        this.lccConverterStationFilterRepository = lccConverterStationFilterRepository;
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.FORM;
    }

    @Override
    public EquipmentType getEquipmentType() {
        return EquipmentType.LCC_CONVERTER_STATION;
    }

    @Override
    public LccConverterStationFilterRepository getRepository() {
        return lccConverterStationFilterRepository;
    }

    @Override
    public AbstractFilter toDto(LccConverterStationFilterEntity entity) {
        return buildInjectionFilter(
            LccConverterStationFilter.builder(), entity).build();
    }

    @Override
    public LccConverterStationFilterEntity fromDto(AbstractFilter dto) {
        if (dto instanceof LccConverterStationFilter) {
            var lccConverterStationFilterEntityBuilder = LccConverterStationFilterEntity.builder();
            buildInjectionFilter(lccConverterStationFilterEntityBuilder, (LccConverterStationFilter) dto);
            return lccConverterStationFilterEntityBuilder.build();
        }
        throw new PowsyblException(WRONG_FILTER_TYPE);
    }
}
