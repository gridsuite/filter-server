/*
 *  Copyright (c) 2021, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.filter.server.repositories.proxies.criteriafilter;

import org.gridsuite.filter.server.dto.criteriafilter.AbstractEquipmentFilterForm;
import org.gridsuite.filter.server.dto.AbstractFilter;
import org.gridsuite.filter.server.dto.criteriafilter.DanglingLineFilter;
import org.gridsuite.filter.server.entities.AbstractFilterEntity;
import org.gridsuite.filter.server.entities.criteriafilter.AbstractInjectionFilterEntity;
import org.gridsuite.filter.server.entities.criteriafilter.DanglingLineFilterEntity;
import org.gridsuite.filter.server.repositories.criteriafilter.DanglingLineFilterRepository;
import org.gridsuite.filter.server.repositories.proxies.AbstractFilterRepositoryProxy;
import org.gridsuite.filter.server.utils.EquipmentType;
import org.gridsuite.filter.server.utils.FilterType;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */

public class DanglingLineFilterRepositoryProxy extends AbstractFilterRepositoryProxy<DanglingLineFilterEntity, DanglingLineFilterRepository> {

    private final DanglingLineFilterRepository danglingLineFilterRepository;

    public DanglingLineFilterRepositoryProxy(DanglingLineFilterRepository danglingLineFilterRepository) {
        this.danglingLineFilterRepository = danglingLineFilterRepository;
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.CRITERIA;
    }

    @Override
    public EquipmentType getEquipmentType() {
        return EquipmentType.DANGLING_LINE;
    }

    @Override
    public DanglingLineFilterRepository getRepository() {
        return danglingLineFilterRepository;
    }

    @Override
    public AbstractFilter toDto(DanglingLineFilterEntity entity) {
        return super.toFormFilterDto(entity);
    }

    @Override
    public AbstractEquipmentFilterForm buildEquipmentFormFilter(AbstractFilterEntity entity) {
        return new DanglingLineFilter(buildInjectionAttributesFromEntity((AbstractInjectionFilterEntity) entity));
    }

    @Override
    public DanglingLineFilterEntity fromDto(AbstractFilter dto) {
        var danglingLineFilterEntityBuilder = DanglingLineFilterEntity.builder();
        buildInjectionFilter(danglingLineFilterEntityBuilder, toFormFilter(dto, DanglingLineFilter.class));
        return danglingLineFilterEntityBuilder.build();
    }
}
