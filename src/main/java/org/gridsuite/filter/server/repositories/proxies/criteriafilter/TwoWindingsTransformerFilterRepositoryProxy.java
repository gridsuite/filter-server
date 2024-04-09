/*
 *  Copyright (c) 2021, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.filter.server.repositories.proxies.criteriafilter;

import org.gridsuite.filter.criteriafilter.AbstractEquipmentFilterForm;
import org.gridsuite.filter.AbstractFilter;
import org.gridsuite.filter.criteriafilter.CriteriaFilter;
import org.gridsuite.filter.criteriafilter.TwoWindingsTransformerFilter;
import org.gridsuite.filter.server.entities.AbstractFilterEntity;
import org.gridsuite.filter.server.entities.criteriafilter.TwoWindingsTransformerFilterEntity;
import org.gridsuite.filter.server.repositories.criteriafilter.TwoWindingsTransformerFilterRepository;
import org.gridsuite.filter.server.repositories.proxies.AbstractFilterRepositoryProxy;
import org.gridsuite.filter.utils.EquipmentType;
import org.gridsuite.filter.utils.FilterType;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */

public class TwoWindingsTransformerFilterRepositoryProxy extends AbstractFilterRepositoryProxy<TwoWindingsTransformerFilterEntity, TwoWindingsTransformerFilterRepository> {

    private final TwoWindingsTransformerFilterRepository twoWindingsTransformerFilterRepository;

    public TwoWindingsTransformerFilterRepositoryProxy(TwoWindingsTransformerFilterRepository twoWindingsTransformerFilterRepository) {
        this.twoWindingsTransformerFilterRepository = twoWindingsTransformerFilterRepository;
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.CRITERIA;
    }

    @Override
    public EquipmentType getEquipmentType() {
        return EquipmentType.TWO_WINDINGS_TRANSFORMER;
    }

    @Override
    public TwoWindingsTransformerFilterRepository getRepository() {
        return twoWindingsTransformerFilterRepository;
    }

    @Override
    public AbstractFilter toDto(TwoWindingsTransformerFilterEntity entity) {
        return super.toFormFilterDto(entity);
    }

    @Override
    public AbstractEquipmentFilterForm buildEquipmentFormFilter(AbstractFilterEntity entity) {
        TwoWindingsTransformerFilterEntity twoWindingsTransformerFilterEntity = (TwoWindingsTransformerFilterEntity) entity;
        return TwoWindingsTransformerFilter.builder().equipmentID(twoWindingsTransformerFilterEntity.getEquipmentId())
            .equipmentName(twoWindingsTransformerFilterEntity.getEquipmentName())
            .substationName(twoWindingsTransformerFilterEntity.getSubstationName())
            .countries(setToSorterSet(twoWindingsTransformerFilterEntity.getCountries()))
            .substationFreeProperties(convert(twoWindingsTransformerFilterEntity.getSubstationFreeProperties()))
            .freeProperties(convert(twoWindingsTransformerFilterEntity.getFreeProperties()))
            .nominalVoltage1(convert(twoWindingsTransformerFilterEntity.getNominalVoltage1()))
            .nominalVoltage2(convert(twoWindingsTransformerFilterEntity.getNominalVoltage2()))
            .build();
    }

    @Override
    public TwoWindingsTransformerFilterEntity fromDto(AbstractFilter dto) {
        CriteriaFilter criteriaFilter = toFormFilter(dto, TwoWindingsTransformerFilter.class);
        TwoWindingsTransformerFilter twoWindingsTransformerFilter = (TwoWindingsTransformerFilter) criteriaFilter.getEquipmentFilterForm();
        var twoWindingsTransformerFilterEntityBuilder = TwoWindingsTransformerFilterEntity.builder()
            .countries(twoWindingsTransformerFilter.getCountries())
            .substationName(twoWindingsTransformerFilter.getSubstationName())
            .nominalVoltage1(convert(twoWindingsTransformerFilter.getNominalVoltage1()))
            .nominalVoltage2(convert(twoWindingsTransformerFilter.getNominalVoltage2()))
            .substationFreeProperties(convert(twoWindingsTransformerFilter.getSubstationFreeProperties()))
            .freeProperties(convert(twoWindingsTransformerFilter.getFreeProperties()));
        buildGenericFilter(twoWindingsTransformerFilterEntityBuilder, criteriaFilter);
        return twoWindingsTransformerFilterEntityBuilder.build();
    }
}
