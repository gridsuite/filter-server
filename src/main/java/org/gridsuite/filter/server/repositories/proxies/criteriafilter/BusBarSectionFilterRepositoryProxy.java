/*
 *  Copyright (c) 2021, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.filter.server.repositories.proxies.criteriafilter;

import org.gridsuite.filter.criteriafilter.AbstractEquipmentFilterForm;
import org.gridsuite.filter.AbstractFilter;
import org.gridsuite.filter.criteriafilter.BusBarSectionFilter;
import org.gridsuite.filter.server.entities.AbstractFilterEntity;
import org.gridsuite.filter.server.entities.criteriafilter.AbstractInjectionFilterEntity;
import org.gridsuite.filter.server.entities.criteriafilter.BusBarSectionFilterEntity;
import org.gridsuite.filter.server.repositories.criteriafilter.BusBarSectionFilterRepository;
import org.gridsuite.filter.server.repositories.proxies.AbstractFilterRepositoryProxy;
import org.gridsuite.filter.utils.EquipmentType;
import org.gridsuite.filter.utils.FilterType;

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
        return FilterType.CRITERIA;
    }

    @Override
    public EquipmentType getEquipmentType() {
        return EquipmentType.BUSBAR_SECTION;
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
