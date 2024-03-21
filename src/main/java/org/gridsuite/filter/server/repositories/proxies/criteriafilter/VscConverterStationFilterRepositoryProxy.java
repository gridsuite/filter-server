/*
 *  Copyright (c) 2021, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.filter.server.repositories.proxies.criteriafilter;

import org.gridsuite.filter.criteriafilter.AbstractEquipmentFilterForm;
import org.gridsuite.filter.AbstractFilter;
import org.gridsuite.filter.criteriafilter.VscConverterStationFilter;
import org.gridsuite.filter.server.entities.AbstractFilterEntity;
import org.gridsuite.filter.server.entities.criteriafilter.AbstractInjectionFilterEntity;
import org.gridsuite.filter.server.entities.criteriafilter.VscConverterStationFilterEntity;
import org.gridsuite.filter.server.repositories.criteriafilter.VscConverterStationFilterRepository;
import org.gridsuite.filter.server.repositories.proxies.AbstractFilterRepositoryProxy;
import org.gridsuite.filter.utils.EquipmentType;
import org.gridsuite.filter.utils.FilterType;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */

public class VscConverterStationFilterRepositoryProxy extends AbstractFilterRepositoryProxy<VscConverterStationFilterEntity, VscConverterStationFilterRepository> {

    private final VscConverterStationFilterRepository vscConverterStationFilterRepository;

    public VscConverterStationFilterRepositoryProxy(VscConverterStationFilterRepository vscConverterStationFilterRepository) {
        this.vscConverterStationFilterRepository = vscConverterStationFilterRepository;
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.CRITERIA;
    }

    @Override
    public EquipmentType getEquipmentType() {
        return EquipmentType.VSC_CONVERTER_STATION;
    }

    @Override
    public VscConverterStationFilterRepository getRepository() {
        return vscConverterStationFilterRepository;
    }

    @Override
    public AbstractFilter toDto(VscConverterStationFilterEntity entity) {
        return super.toFormFilterDto(entity);
    }

    @Override
    public AbstractEquipmentFilterForm buildEquipmentFormFilter(AbstractFilterEntity entity) {
        return new VscConverterStationFilter(buildInjectionAttributesFromEntity((AbstractInjectionFilterEntity) entity));
    }

    @Override
    public VscConverterStationFilterEntity fromDto(AbstractFilter dto) {
        var vscConverterStationFilterEntityBuilder = VscConverterStationFilterEntity.builder();
        buildInjectionFilter(vscConverterStationFilterEntityBuilder, toFormFilter(dto, VscConverterStationFilter.class));
        return vscConverterStationFilterEntityBuilder.build();
    }
}
