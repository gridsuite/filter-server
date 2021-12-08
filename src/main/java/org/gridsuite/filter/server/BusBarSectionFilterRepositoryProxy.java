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
import org.gridsuite.filter.server.entities.BusBarSectionFilterEntity;
import org.gridsuite.filter.server.repositories.BusBarSectionFilterRepository;
import org.gridsuite.filter.server.utils.FilterType;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */

public class BusBarSectionFilterRepositoryProxy extends AbstractFilterRepositoryProxy<BusBarSectionFilterEntity, BusBarSectionFilterRepository> {

    private final BusBarSectionFilterRepository busBarSectionFilterRepository;

    public BusBarSectionFilterRepositoryProxy(BusBarSectionFilterRepository busBarSectionFilterRepository) {
        this.busBarSectionFilterRepository = busBarSectionFilterRepository;
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.FORM;
    }

    @Override
    public BusBarSectionFilterRepository getRepository() {
        return busBarSectionFilterRepository;
    }

    @Override
    public AbstractFilter toDto(BusBarSectionFilterEntity entity) {
        return super.toFormFilterDto(entity);
    }

    @Override
    public AbstractEquipmentFilterForm buildEquipmentFormFilter(AbstractFilterEntity entity) {
        return new BusBarSectionFilter(buildInjectionAttributesFromEntity((AbstractInjectionFilterEntity) entity));
    }

    @Override
    public BusBarSectionFilterEntity fromDto(AbstractFilter dto) {
        var busBarSectionFilterEntityBuilder = BusBarSectionFilterEntity.builder();
        buildInjectionFilter(busBarSectionFilterEntityBuilder, toFormFilter(dto, BusBarSectionFilter.class));
        return busBarSectionFilterEntityBuilder.build();
    }
}
