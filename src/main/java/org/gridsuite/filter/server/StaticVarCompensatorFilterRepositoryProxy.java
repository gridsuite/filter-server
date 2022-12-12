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
import org.gridsuite.filter.server.entities.StaticVarCompensatorFilterEntity;
import org.gridsuite.filter.server.repositories.StaticVarCompensatorFilterRepository;
import org.gridsuite.filter.server.utils.EquipmentType;
import org.gridsuite.filter.server.utils.FilterType;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */

public class StaticVarCompensatorFilterRepositoryProxy extends AbstractFilterRepositoryProxy<StaticVarCompensatorFilterEntity, StaticVarCompensatorFilterRepository> {

    private final StaticVarCompensatorFilterRepository staticVarCompensatorFilterRepository;

    public StaticVarCompensatorFilterRepositoryProxy(StaticVarCompensatorFilterRepository staticVarCompensatorFilterRepository) {
        this.staticVarCompensatorFilterRepository = staticVarCompensatorFilterRepository;
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.CRITERIA;
    }

    @Override
    public EquipmentType getEquipmentType() {
        return EquipmentType.STATIC_VAR_COMPENSATOR;
    }

    @Override
    public StaticVarCompensatorFilterRepository getRepository() {
        return staticVarCompensatorFilterRepository;
    }

    @Override
    public AbstractFilter toDto(StaticVarCompensatorFilterEntity entity) {
        return super.toFormFilterDto(entity);
    }

    @Override
    public AbstractEquipmentFilterForm buildEquipmentFormFilter(AbstractFilterEntity entity) {
        return new StaticVarCompensatorFilter(buildInjectionAttributesFromEntity((AbstractInjectionFilterEntity) entity));
    }

    @Override
    public StaticVarCompensatorFilterEntity fromDto(AbstractFilter dto) {
        var staticVarCompensatorFilterEntityBuilder = StaticVarCompensatorFilterEntity.builder();
        buildInjectionFilter(staticVarCompensatorFilterEntityBuilder, toFormFilter(dto, StaticVarCompensatorFilter.class));
        return staticVarCompensatorFilterEntityBuilder.build();
    }
}
