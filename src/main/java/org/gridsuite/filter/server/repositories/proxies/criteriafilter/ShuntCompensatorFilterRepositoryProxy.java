/*
 *  Copyright (c) 2021, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.filter.server.repositories.proxies.criteriafilter;

import org.gridsuite.filter.server.dto.criteriafilter.AbstractEquipmentFilterForm;
import org.gridsuite.filter.server.dto.AbstractFilter;
import org.gridsuite.filter.server.dto.criteriafilter.ShuntCompensatorFilter;
import org.gridsuite.filter.server.entities.AbstractFilterEntity;
import org.gridsuite.filter.server.entities.criteriafilter.AbstractInjectionFilterEntity;
import org.gridsuite.filter.server.entities.criteriafilter.ShuntCompensatorFilterEntity;
import org.gridsuite.filter.server.repositories.criteriafilter.ShuntCompensatorFilterRepository;
import org.gridsuite.filter.server.repositories.proxies.AbstractFilterRepositoryProxy;
import org.gridsuite.filter.server.utils.EquipmentType;
import org.gridsuite.filter.server.utils.FilterType;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */

public class ShuntCompensatorFilterRepositoryProxy extends AbstractFilterRepositoryProxy<ShuntCompensatorFilterEntity, ShuntCompensatorFilterRepository> {

    private final ShuntCompensatorFilterRepository shuntCompensatorFilterRepository;

    public ShuntCompensatorFilterRepositoryProxy(ShuntCompensatorFilterRepository shuntCompensatorFilterRepository) {
        this.shuntCompensatorFilterRepository = shuntCompensatorFilterRepository;
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.CRITERIA;
    }

    @Override
    public EquipmentType getEquipmentType() {
        return EquipmentType.SHUNT_COMPENSATOR;
    }

    @Override
    public ShuntCompensatorFilterRepository getRepository() {
        return shuntCompensatorFilterRepository;
    }

    @Override
    public AbstractFilter toDto(ShuntCompensatorFilterEntity entity) {
        return super.toFormFilterDto(entity);
    }

    @Override
    public AbstractEquipmentFilterForm buildEquipmentFormFilter(AbstractFilterEntity entity) {
        return new ShuntCompensatorFilter(buildInjectionAttributesFromEntity((AbstractInjectionFilterEntity) entity));
    }

    @Override
    public ShuntCompensatorFilterEntity fromDto(AbstractFilter dto) {
        var shuntCompensatorFilterEntityBuilder = ShuntCompensatorFilterEntity.builder();
        buildInjectionFilter(shuntCompensatorFilterEntityBuilder, toFormFilter(dto, ShuntCompensatorFilter.class));
        return shuntCompensatorFilterEntityBuilder.build();
    }
}
