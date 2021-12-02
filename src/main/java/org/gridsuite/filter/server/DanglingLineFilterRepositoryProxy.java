/*
 *  Copyright (c) 2021, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.filter.server;

import com.powsybl.commons.PowsyblException;
import org.gridsuite.filter.server.dto.*;
import org.gridsuite.filter.server.entities.AbstractFilterEntity;
import org.gridsuite.filter.server.entities.AbstractInjectionFilterEntity;
import org.gridsuite.filter.server.entities.DanglingLineFilterEntity;
import org.gridsuite.filter.server.repositories.DanglingLineFilterRepository;
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
        return FilterType.FORM;
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
        if (!(dto instanceof FormFilter)) {
            throw new PowsyblException(WRONG_FILTER_TYPE);
        }
        FormFilter formFilter = (FormFilter) dto;

        if (!(formFilter.getEquipmentFilterForm() instanceof DanglingLineFilter)) {
            throw new PowsyblException(WRONG_FILTER_TYPE);
        }
        var danglingLineFilterEntityBuilder = DanglingLineFilterEntity.builder();
        buildInjectionFilter(danglingLineFilterEntityBuilder, formFilter);
        return danglingLineFilterEntityBuilder.build();
    }
}
