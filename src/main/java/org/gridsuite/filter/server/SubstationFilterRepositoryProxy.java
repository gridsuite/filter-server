/*
 *  Copyright (c) 2022, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.filter.server;

import org.gridsuite.filter.server.dto.AbstractEquipmentFilterForm;
import org.gridsuite.filter.server.dto.AbstractFilter;
import org.gridsuite.filter.server.dto.CriteriaFilter;
import org.gridsuite.filter.server.dto.SubstationFilter;
import org.gridsuite.filter.server.entities.AbstractFilterEntity;
import org.gridsuite.filter.server.entities.SubstationFilterEntity;
import org.gridsuite.filter.server.repositories.SubstationFilterRepository;
import org.gridsuite.filter.server.utils.EquipmentType;
import org.gridsuite.filter.server.utils.FilterType;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */

public class SubstationFilterRepositoryProxy extends AbstractFilterRepositoryProxy<SubstationFilterEntity, SubstationFilterRepository> {

    private final SubstationFilterRepository substationFilterRepository;

    public SubstationFilterRepositoryProxy(SubstationFilterRepository substationFilterRepository) {
        this.substationFilterRepository = substationFilterRepository;
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.CRITERIA;
    }

    @Override
    public EquipmentType getEquipmentType() {
        return EquipmentType.SUBSTATION;
    }

    @Override
    public SubstationFilterRepository getRepository() {
        return substationFilterRepository;
    }

    @Override
    public AbstractFilter toDto(SubstationFilterEntity entity) {
        return super.toFormFilterDto(entity);
    }

    @Override
    public AbstractEquipmentFilterForm buildEquipmentFormFilter(AbstractFilterEntity entity) {
        SubstationFilterEntity substationFilterEntity = (SubstationFilterEntity) entity;
        return new SubstationFilter(
            substationFilterEntity.getEquipmentId(),
            substationFilterEntity.getEquipmentName(),
            setToSorterSet(substationFilterEntity.getCountries()),
            convert(substationFilterEntity.getSubstationFreeProperties())
        );
    }

    @Override
    public SubstationFilterEntity fromDto(AbstractFilter dto) {
        CriteriaFilter criteriaFilter = toFormFilter(dto, SubstationFilter.class);
        SubstationFilter substationFilter = (SubstationFilter) criteriaFilter.getEquipmentFilterForm();
        var substationFilterEntityBuilder =  SubstationFilterEntity.builder()
            .countries(substationFilter.getCountries());
        buildGenericFilter(substationFilterEntityBuilder, criteriaFilter);
        return substationFilterEntityBuilder.build();
    }
}
