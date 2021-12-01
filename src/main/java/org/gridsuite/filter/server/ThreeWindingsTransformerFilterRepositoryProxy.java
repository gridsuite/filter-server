/*
 *  Copyright (c) 2021, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.filter.server;

import com.powsybl.commons.PowsyblException;
import org.gridsuite.filter.server.dto.*;
import org.gridsuite.filter.server.entities.ThreeWindingsTransformerFilterEntity;
import org.gridsuite.filter.server.repositories.ThreeWindingsTransformerFilterRepository;
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
        return FilterType.FORM;
    }

    public EquipmentType getEquipmentType() {
        return EquipmentType.THREE_WINDINGS_TRANSFORMER;
    }

    @Override
    public ThreeWindingsTransformerFilterRepository getRepository() {
        return threeWindingsTransformerFilterRepository;
    }

    @Override
    public AbstractFilter toDto(ThreeWindingsTransformerFilterEntity entity) {
        return new FormFilter(
            entity.getId(),
            entity.getCreationDate(),
            entity.getModificationDate(),
            new ThreeWindingsTransformerFilter(
                entity.getEquipmentId(),
                entity.getEquipmentName(),
                entity.getSubstationName(),
                setToSorterSet(entity.getCountries()),
                convert(entity.getNominalVoltage1()),
                convert(entity.getNominalVoltage2()),
                convert(entity.getNominalVoltage3())
            )
        );
    }

    @Override
    public ThreeWindingsTransformerFilterEntity fromDto(AbstractFilter dto) {
        if (!(dto instanceof FormFilter)) {
            throw new PowsyblException(WRONG_FILTER_TYPE);
        }
        FormFilter formFilter = (FormFilter) dto;

        if (!(formFilter.getEquipmentFilterForm() instanceof ThreeWindingsTransformerFilter)) {
            throw new PowsyblException(WRONG_FILTER_TYPE);
        }
        ThreeWindingsTransformerFilter threeWindingsTransformerFilter = (ThreeWindingsTransformerFilter) formFilter.getEquipmentFilterForm();
        return ThreeWindingsTransformerFilterEntity.builder()
                .id(formFilter.getId())
                .creationDate(getDateOrCreate(formFilter.getCreationDate()))
                .equipmentId(formFilter.getEquipmentFilterForm().getEquipmentID())
                .equipmentName(formFilter.getEquipmentFilterForm().getEquipmentName())
                .countries(threeWindingsTransformerFilter.getCountries())
                .substationName(threeWindingsTransformerFilter.getSubstationName())
                .nominalVoltage1(convert(threeWindingsTransformerFilter.getNominalVoltage1()))
                .nominalVoltage2(convert(threeWindingsTransformerFilter.getNominalVoltage2()))
                .nominalVoltage3(convert(threeWindingsTransformerFilter.getNominalVoltage3()))
                .build();
    }
}
