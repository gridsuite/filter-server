/*
 *  Copyright (c) 2021, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.filter.server;

import org.gridsuite.filter.server.dto.*;
import org.gridsuite.filter.server.entities.AbstractFilterEntity;
import org.gridsuite.filter.server.entities.AbstractInjectionFilterEntity;
import org.gridsuite.filter.server.entities.LccConverterStationFilterEntity;
import org.gridsuite.filter.server.repositories.LccConverterStationFilterRepository;
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
    public LccConverterStationFilterRepository getRepository() {
        return lccConverterStationFilterRepository;
    }

    @Override
    public AbstractFilter toDto(LccConverterStationFilterEntity entity) {
        return super.toFormFilterDto(entity);
    }

    @Override
    public AbstractEquipmentFilterForm buildEquipmentFormFilter(AbstractFilterEntity entity) {
        return new LccConverterStationFilter(buildInjectionAttributesFromEntity((AbstractInjectionFilterEntity) entity));
    }

    @Override
    public LccConverterStationFilterEntity fromDto(AbstractFilter dto) {
        var lccConverterStationFilterEntityBuilder = LccConverterStationFilterEntity.builder();
        buildInjectionFilter(lccConverterStationFilterEntityBuilder, toFormFilter(dto, LccConverterStationFilter.class));
        return lccConverterStationFilterEntityBuilder.build();
    }
}
