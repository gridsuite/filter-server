/*
 *  Copyright (c) 2021, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.filter.server;

import org.gridsuite.filter.server.dto.*;
import org.gridsuite.filter.server.entities.AbstractFilterEntity;
import org.gridsuite.filter.server.entities.ThreeWindingsTransformerFilterEntity;
import org.gridsuite.filter.server.repositories.ThreeWindingsTransformerFilterRepository;
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
        return FilterType.FORM;
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
        return new ThreeWindingsTransformerFilter(
            threeWindingsTransformerFilterEntity.getEquipmentId(),
            threeWindingsTransformerFilterEntity.getEquipmentName(),
            threeWindingsTransformerFilterEntity.getSubstationName(),
            setToSorterSet(threeWindingsTransformerFilterEntity.getCountries()),
            convert(threeWindingsTransformerFilterEntity.getNominalVoltage1()),
            convert(threeWindingsTransformerFilterEntity.getNominalVoltage2()),
            convert(threeWindingsTransformerFilterEntity.getNominalVoltage3())
        );
    }

    @Override
    public ThreeWindingsTransformerFilterEntity fromDto(AbstractFilter dto) {
        FormFilter formFilter = toFormFilter(dto, ThreeWindingsTransformerFilter.class);
        ThreeWindingsTransformerFilter threeWindingsTransformerFilter = (ThreeWindingsTransformerFilter) formFilter.getEquipmentFilterForm();
        var threeWindingsTransformerFilterEntityBuilder =   ThreeWindingsTransformerFilterEntity.builder()
            .countries(threeWindingsTransformerFilter.getCountries())
            .substationName(threeWindingsTransformerFilter.getSubstationName())
            .nominalVoltage1(convert(threeWindingsTransformerFilter.getNominalVoltage1()))
            .nominalVoltage2(convert(threeWindingsTransformerFilter.getNominalVoltage2()))
            .nominalVoltage3(convert(threeWindingsTransformerFilter.getNominalVoltage3()));
        buildGenericFilter(threeWindingsTransformerFilterEntityBuilder, formFilter);
        return threeWindingsTransformerFilterEntityBuilder.build();
    }
}
