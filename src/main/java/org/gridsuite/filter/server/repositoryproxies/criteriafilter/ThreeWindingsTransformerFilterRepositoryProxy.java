/*
 *  Copyright (c) 2021, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.filter.server.repositoryproxies.criteriafilter;

import org.gridsuite.filter.server.dto.criteriafilter.AbstractEquipmentFilterForm;
import org.gridsuite.filter.server.dto.AbstractFilter;
import org.gridsuite.filter.server.dto.criteriafilter.CriteriaFilter;
import org.gridsuite.filter.server.dto.criteriafilter.ThreeWindingsTransformerFilter;
import org.gridsuite.filter.server.entities.AbstractFilterEntity;
import org.gridsuite.filter.server.entities.criteriafilter.ThreeWindingsTransformerFilterEntity;
import org.gridsuite.filter.server.repositories.criteriafilter.ThreeWindingsTransformerFilterRepository;
import org.gridsuite.filter.server.repositoryproxies.AbstractFilterRepositoryProxy;
import org.gridsuite.filter.server.utils.EquipmentType;
import org.gridsuite.filter.server.utils.FilterType;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */

public class ThreeWindingsTransformerFilterRepositoryProxy extends AbstractFilterRepositoryProxy<ThreeWindingsTransformerFilterEntity, ThreeWindingsTransformerFilterRepository> {

    private final ThreeWindingsTransformerFilterRepository threeWindingsTransformerFilterRepository;

    public ThreeWindingsTransformerFilterRepositoryProxy(ThreeWindingsTransformerFilterRepository threeWindingsTransformerFilterRepository) {
        this.threeWindingsTransformerFilterRepository = threeWindingsTransformerFilterRepository;
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.CRITERIA;
    }

    @Override
    public EquipmentType getEquipmentType() {
        return EquipmentType.THREE_WINDINGS_TRANSFORMER;
    }

    @Override
    public ThreeWindingsTransformerFilterRepository getRepository() {
        return threeWindingsTransformerFilterRepository;
    }

    @Override
    public AbstractFilter toDto(ThreeWindingsTransformerFilterEntity entity) {
        return super.toFormFilterDto(entity);
    }

    @Override
    public AbstractEquipmentFilterForm buildEquipmentFormFilter(AbstractFilterEntity entity) {
        ThreeWindingsTransformerFilterEntity threeWindingsTransformerFilterEntity = (ThreeWindingsTransformerFilterEntity) entity;
        return ThreeWindingsTransformerFilter.builder()
            .equipmentID(threeWindingsTransformerFilterEntity.getEquipmentId())
            .equipmentName(threeWindingsTransformerFilterEntity.getEquipmentName())
            .substationName(threeWindingsTransformerFilterEntity.getSubstationName())
            .countries(setToSorterSet(threeWindingsTransformerFilterEntity.getCountries()))
            .freeProperties(convert(threeWindingsTransformerFilterEntity.getSubstationFreeProperties()))
            .nominalVoltage1(convert(threeWindingsTransformerFilterEntity.getNominalVoltage1()))
            .nominalVoltage2(convert(threeWindingsTransformerFilterEntity.getNominalVoltage2()))
            .nominalVoltage3(convert(threeWindingsTransformerFilterEntity.getNominalVoltage3())).build();
    }

    @Override
    public ThreeWindingsTransformerFilterEntity fromDto(AbstractFilter dto) {
        CriteriaFilter criteriaFilter = toFormFilter(dto, ThreeWindingsTransformerFilter.class);
        ThreeWindingsTransformerFilter threeWindingsTransformerFilter = (ThreeWindingsTransformerFilter) criteriaFilter.getEquipmentFilterForm();
        var threeWindingsTransformerFilterEntityBuilder = ThreeWindingsTransformerFilterEntity.builder()
            .countries(threeWindingsTransformerFilter.getCountries())
            .substationName(threeWindingsTransformerFilter.getSubstationName())
            .nominalVoltage1(convert(threeWindingsTransformerFilter.getNominalVoltage1()))
            .nominalVoltage2(convert(threeWindingsTransformerFilter.getNominalVoltage2()))
            .nominalVoltage3(convert(threeWindingsTransformerFilter.getNominalVoltage3()))
            .substationFreeProperties(convert(threeWindingsTransformerFilter.getFreeProperties()));
        buildGenericFilter(threeWindingsTransformerFilterEntityBuilder, criteriaFilter);
        return threeWindingsTransformerFilterEntityBuilder.build();
    }
}
