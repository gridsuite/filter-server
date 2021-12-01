/*
 *  Copyright (c) 2021, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.filter.server;

import com.powsybl.commons.PowsyblException;
import org.gridsuite.filter.server.dto.*;
import org.gridsuite.filter.server.entities.TwoWindingsTransformerFilterEntity;
import org.gridsuite.filter.server.repositories.TwoWindingsTransformerFilterRepository;
import org.gridsuite.filter.server.utils.FilterType;

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
        return FilterType.FORM;
    }

    @Override
    public TwoWindingsTransformerFilterRepository getRepository() {
        return twoWindingsTransformerFilterRepository;
    }

    @Override
    public AbstractFilter toDto(TwoWindingsTransformerFilterEntity entity) {
        return new FormFilter(
            entity.getId(),
            entity.getCreationDate(),
            entity.getModificationDate(),
            new TwoWindingsTransformerFilter(
                entity.getEquipmentId(),
                entity.getEquipmentName(),
                entity.getSubstationName(),
                setToSorterSet(entity.getCountries()),
                convert(entity.getNominalVoltage1()),
                convert(entity.getNominalVoltage2())
                )
        );
    }

    @Override
    public TwoWindingsTransformerFilterEntity fromDto(AbstractFilter dto) {
        if (!(dto instanceof FormFilter)) {
            throw new PowsyblException(WRONG_FILTER_TYPE);
        }
        FormFilter formFilter = (FormFilter) dto;

        if (!(formFilter.getEquipmentFilterForm() instanceof TwoWindingsTransformerFilter)) {
            throw new PowsyblException(WRONG_FILTER_TYPE);
        }

        TwoWindingsTransformerFilter twoWindingsTransformerFilter = (TwoWindingsTransformerFilter) formFilter.getEquipmentFilterForm();
        return TwoWindingsTransformerFilterEntity.builder()
                .id(formFilter.getId())
                .creationDate(getDateOrCreate(formFilter.getCreationDate()))
                .equipmentId(formFilter.getEquipmentFilterForm().getEquipmentID())
                .equipmentName(formFilter.getEquipmentFilterForm().getEquipmentName())
                .countries(twoWindingsTransformerFilter.getCountries())
                .substationName(twoWindingsTransformerFilter.getSubstationName())
                .nominalVoltage1(convert(twoWindingsTransformerFilter.getNominalVoltage1()))
                .nominalVoltage2(convert(twoWindingsTransformerFilter.getNominalVoltage2()))
                .build();
    }
}
