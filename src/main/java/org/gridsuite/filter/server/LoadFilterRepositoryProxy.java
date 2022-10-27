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
import org.gridsuite.filter.server.entities.LoadFilterEntity;
import org.gridsuite.filter.server.repositories.LoadFilterRepository;
import org.gridsuite.filter.server.utils.EquipmentType;
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
        return FilterType.AUTOMATIC;
    }

    @Override
    public EquipmentType getEquipmentType() {
        return EquipmentType.LOAD;
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
        var loadFilterEntityBuilder = LoadFilterEntity.builder();
        buildInjectionFilter(loadFilterEntityBuilder, toFormFilter(dto, LoadFilter.class));
        return loadFilterEntityBuilder.build();
    }
}
