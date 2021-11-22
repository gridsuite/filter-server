/*
 *  Copyright (c) 2021, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.filter.server;

import com.powsybl.commons.PowsyblException;
import org.gridsuite.filter.server.dto.AbstractFilter;
import org.gridsuite.filter.server.dto.BusBarSectionFilter;
import org.gridsuite.filter.server.entities.BusBarSectionFilterEntity;
import org.gridsuite.filter.server.repositories.BusBarSectionFilterRepository;
import org.gridsuite.filter.server.utils.EquipmentType;
import org.gridsuite.filter.server.utils.FilterType;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */

class BusBarSectionFilterRepositoryProxy extends AbstractFilterRepositoryProxy<BusBarSectionFilterEntity, BusBarSectionFilterRepository> {

    private final BusBarSectionFilterRepository busBarSectionFilterRepository;

    public BusBarSectionFilterRepositoryProxy(BusBarSectionFilterRepository busBarSectionFilterRepository) {
        this.busBarSectionFilterRepository = busBarSectionFilterRepository;
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.FILTER;
    }

    @Override
    public EquipmentType getFilterSubtype() {
        return EquipmentType.BUSBAR_SECTION;
    }

    @Override
    public BusBarSectionFilterRepository getRepository() {
        return busBarSectionFilterRepository;
    }

    @Override
    public AbstractFilter toDto(BusBarSectionFilterEntity entity) {
        return buildInjectionFilter(
            BusBarSectionFilter.builder(), entity).build();
    }

    @Override
    public BusBarSectionFilterEntity fromDto(AbstractFilter dto) {
        if (dto instanceof BusBarSectionFilter) {
            var busBarSectionFilterEntityBuilder = BusBarSectionFilterEntity.builder();
            buildInjectionFilter(busBarSectionFilterEntityBuilder, (BusBarSectionFilter) dto);
            return busBarSectionFilterEntityBuilder.build();
        }
        throw new PowsyblException(WRONG_FILTER_TYPE);
    }
}
