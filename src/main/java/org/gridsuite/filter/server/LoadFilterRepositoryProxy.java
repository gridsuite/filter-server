/*
 *  Copyright (c) 2021, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.filter.server;

import com.powsybl.commons.PowsyblException;
import org.gridsuite.filter.server.dto.AbstractEquipmentFilterForm;
import org.gridsuite.filter.server.dto.AbstractFilter;
import org.gridsuite.filter.server.dto.FormFilter;
import org.gridsuite.filter.server.dto.LoadFilter;
import org.gridsuite.filter.server.entities.AbstractFilterEntity;
import org.gridsuite.filter.server.entities.AbstractInjectionFilterEntity;
import org.gridsuite.filter.server.entities.LoadFilterEntity;
import org.gridsuite.filter.server.repositories.LoadFilterRepository;
import org.gridsuite.filter.server.utils.FilterType;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */

public class LoadFilterRepositoryProxy extends AbstractFilterRepositoryProxy<LoadFilterEntity, LoadFilterRepository> {

    private final LoadFilterRepository loadFilterRepository;

    public LoadFilterRepositoryProxy(LoadFilterRepository loadFilterRepository) {
        this.loadFilterRepository = loadFilterRepository;
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.FORM;
    }

    @Override
    public LoadFilterRepository getRepository() {
        return loadFilterRepository;
    }

    @Override
    public AbstractFilter toDto(LoadFilterEntity entity) {
        return super.toFormFilterDto(entity);
    }

    @Override
    public AbstractEquipmentFilterForm buildEquipmentFormFilter(AbstractFilterEntity entity) {
        return new LoadFilter(buildInjectionAttributesFromEntity((AbstractInjectionFilterEntity) entity));
    }

    @Override
    public LoadFilterEntity fromDto(AbstractFilter dto) {
        if (!(dto instanceof FormFilter)) {
            throw new PowsyblException(WRONG_FILTER_TYPE);
        }
        FormFilter formFilter = (FormFilter) dto;

        if (!(formFilter.getEquipmentFilterForm() instanceof LoadFilter)) {
            throw new PowsyblException(WRONG_FILTER_TYPE);
        }
        var loadFilterEntityBuilder = LoadFilterEntity.builder();
        buildInjectionFilter(loadFilterEntityBuilder, formFilter);
        return loadFilterEntityBuilder.build();
    }
}
