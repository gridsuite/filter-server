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
import org.gridsuite.filter.server.entities.GeneratorFilterEntity;
import org.gridsuite.filter.server.repositories.GeneratorFilterRepository;
import org.gridsuite.filter.server.utils.FilterType;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */

public class GeneratorFilterRepositoryProxy extends AbstractFilterRepositoryProxy<GeneratorFilterEntity, GeneratorFilterRepository> {

    private final GeneratorFilterRepository generatorFilterRepository;

    public GeneratorFilterRepositoryProxy(GeneratorFilterRepository generatorFilterRepository) {
        this.generatorFilterRepository = generatorFilterRepository;
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.FORM;
    }

    @Override
    public GeneratorFilterRepository getRepository() {
        return generatorFilterRepository;
    }

    @Override
    public AbstractFilter toDto(GeneratorFilterEntity entity) {
        return super.toFormFilterDto(entity);
    }

    @Override
    public AbstractEquipmentFilterForm buildEquipmentFormFilter(AbstractFilterEntity entity) {
        return new GeneratorFilter(buildInjectionAttributesFromEntity((AbstractInjectionFilterEntity) entity));
    }

    @Override
    public GeneratorFilterEntity fromDto(AbstractFilter dto) {
        var generatorFilterEntityBuilder = GeneratorFilterEntity.builder();
        buildInjectionFilter(generatorFilterEntityBuilder, toFormFilter(dto, GeneratorFilter.class));
        return generatorFilterEntityBuilder.build();
    }
}
