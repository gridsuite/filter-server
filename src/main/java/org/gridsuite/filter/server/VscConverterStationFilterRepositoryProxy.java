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
import org.gridsuite.filter.server.entities.VscConverterStationFilterEntity;
import org.gridsuite.filter.server.repositories.VscConverterStationFilterRepository;
import org.gridsuite.filter.server.utils.FilterType;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */

public class VscConverterStationFilterRepositoryProxy extends AbstractFilterRepositoryProxy<VscConverterStationFilterEntity, VscConverterStationFilterRepository> {

    private final VscConverterStationFilterRepository vscConverterStationFilterRepository;

    public VscConverterStationFilterRepositoryProxy(VscConverterStationFilterRepository vscConverterStationFilterRepository) {
        this.vscConverterStationFilterRepository = vscConverterStationFilterRepository;
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.AUTOMATIC;
    }

    @Override
    public VscConverterStationFilterRepository getRepository() {
        return vscConverterStationFilterRepository;
    }

    @Override
    public AbstractFilter toDto(VscConverterStationFilterEntity entity) {
        return super.toFormFilterDto(entity);
    }

    @Override
    public AbstractEquipmentFilterForm buildEquipmentFormFilter(AbstractFilterEntity entity) {
        return new VscConverterStationFilter(buildInjectionAttributesFromEntity((AbstractInjectionFilterEntity) entity));
    }

    @Override
    public VscConverterStationFilterEntity fromDto(AbstractFilter dto) {
        var vscConverterStationFilterEntityBuilder = VscConverterStationFilterEntity.builder();
        buildInjectionFilter(vscConverterStationFilterEntityBuilder, toFormFilter(dto, VscConverterStationFilter.class));
        return vscConverterStationFilterEntityBuilder.build();
    }
}
