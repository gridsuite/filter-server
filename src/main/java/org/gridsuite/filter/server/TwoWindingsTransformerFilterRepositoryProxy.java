/*
 *  Copyright (c) 2021, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.filter.server;

import com.powsybl.commons.PowsyblException;
import org.gridsuite.filter.server.dto.*;
import org.gridsuite.filter.server.entities.AbstractFilterEntity;
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
        return super.toFormFilterDto(entity);
    }

    @Override
    protected AbstractEquipmentFilterForm buildEquipmentFormFilter(AbstractFilterEntity entity) {
        TwoWindingsTransformerFilterEntity twoWindingsTransformerFilterEntity = (TwoWindingsTransformerFilterEntity) entity;
        return new TwoWindingsTransformerFilter(
            twoWindingsTransformerFilterEntity.getEquipmentId(),
            twoWindingsTransformerFilterEntity.getEquipmentName(),
            twoWindingsTransformerFilterEntity.getSubstationName(),
            setToSorterSet(twoWindingsTransformerFilterEntity.getCountries()),
            convert(twoWindingsTransformerFilterEntity.getNominalVoltage1()),
            convert(twoWindingsTransformerFilterEntity.getNominalVoltage2())
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
        var twoWindingsTransformerFilterEntityBuilder =  TwoWindingsTransformerFilterEntity.builder()
            .countries(twoWindingsTransformerFilter.getCountries())
            .substationName(twoWindingsTransformerFilter.getSubstationName())
            .nominalVoltage1(convert(twoWindingsTransformerFilter.getNominalVoltage1()))
            .nominalVoltage2(convert(twoWindingsTransformerFilter.getNominalVoltage2()));
        buildGenericFilter(twoWindingsTransformerFilterEntityBuilder, formFilter);
        return twoWindingsTransformerFilterEntityBuilder.build();
    }
}
