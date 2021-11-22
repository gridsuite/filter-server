/*
 *  Copyright (c) 2021, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.filter.server;

import com.powsybl.commons.PowsyblException;
import org.gridsuite.filter.server.dto.AbstractFilter;
import org.gridsuite.filter.server.dto.DanglingLineFilter;
import org.gridsuite.filter.server.entities.DanglingLineFilterEntity;
import org.gridsuite.filter.server.repositories.DanglingLineFilterRepository;
import org.gridsuite.filter.server.utils.EquipmentType;
import org.gridsuite.filter.server.utils.FilterType;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */

class DanglingLineFilterRepositoryProxy extends AbstractFilterRepositoryProxy<DanglingLineFilterEntity, DanglingLineFilterRepository> {

    private final DanglingLineFilterRepository danglingLineFilterRepository;

    public DanglingLineFilterRepositoryProxy(DanglingLineFilterRepository danglingLineFilterRepository) {
        this.danglingLineFilterRepository = danglingLineFilterRepository;
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.FILTER;
    }

    @Override
    public EquipmentType getFilterSubtype() {
        return EquipmentType.DANGLING_LINE;
    }

    @Override
    public DanglingLineFilterRepository getRepository() {
        return danglingLineFilterRepository;
    }

    @Override
    public AbstractFilter toDto(DanglingLineFilterEntity entity) {
        return buildInjectionFilter(
            DanglingLineFilter.builder(), entity).build();
    }

    @Override
    public DanglingLineFilterEntity fromDto(AbstractFilter dto) {
        if (dto instanceof DanglingLineFilter) {
            var danglingLineFilterEntityBuilder = DanglingLineFilterEntity.builder();
            buildInjectionFilter(danglingLineFilterEntityBuilder, (DanglingLineFilter) dto);
            return danglingLineFilterEntityBuilder.build();
        }
        throw new PowsyblException(WRONG_FILTER_TYPE);
    }
}
