/*
 *  Copyright (c) 2021, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.filter.server;

import com.powsybl.commons.PowsyblException;
import org.gridsuite.filter.server.dto.AbstractFilter;
import org.gridsuite.filter.server.dto.ThreeWindingsTransformerFilter;
import org.gridsuite.filter.server.entities.ThreeWindingsTransformerFilterEntity;
import org.gridsuite.filter.server.repositories.ThreeWindingsTransformerFilterRepository;
import org.gridsuite.filter.server.utils.FilterType;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */

class ThreeWindingsTransformerFilterRepositoryProxy extends AbstractFilterRepositoryProxy<ThreeWindingsTransformerFilterEntity, ThreeWindingsTransformerFilterRepository> {

    private final ThreeWindingsTransformerFilterRepository threeWindingsTransformerFilterRepository;

    public ThreeWindingsTransformerFilterRepositoryProxy(ThreeWindingsTransformerFilterRepository threeWindingsTransformerFilterRepository) {
        this.threeWindingsTransformerFilterRepository = threeWindingsTransformerFilterRepository;
    }

    @Override
    public FilterType getRepositoryType() {
        return FilterType.THREE_WINDINGS_TRANSFORMER;
    }

    @Override
    public ThreeWindingsTransformerFilterRepository getRepository() {
        return threeWindingsTransformerFilterRepository;
    }

    @Override
    public AbstractFilter toDto(ThreeWindingsTransformerFilterEntity entity) {
        return buildGenericFilter(
            ThreeWindingsTransformerFilter.builder()
                .countries(AbstractFilterRepositoryProxy.cloneIfNotEmptyOrNull(entity.getCountries()))
                .substationName(entity.getSubstationName())
                .nominalVoltage1(AbstractFilterRepositoryProxy.convert(entity.getNominalVoltage1()))
                .nominalVoltage2(AbstractFilterRepositoryProxy.convert(entity.getNominalVoltage2()))
                .nominalVoltage3(AbstractFilterRepositoryProxy.convert(entity.getNominalVoltage3())),
            entity).build();
    }

    @Override
    public ThreeWindingsTransformerFilterEntity fromDto(AbstractFilter dto) {
        if (dto instanceof ThreeWindingsTransformerFilter) {
            var threeWindingsTransformerFilter = (ThreeWindingsTransformerFilter) dto;
            var threeWindingsTransformerFilterEntityBuilder = ThreeWindingsTransformerFilterEntity.builder()
                .substationName(threeWindingsTransformerFilter.getSubstationName())
                .countries(AbstractFilterRepositoryProxy.cloneIfNotEmptyOrNull(threeWindingsTransformerFilter.getCountries()))
                .nominalVoltage1(AbstractFilterRepositoryProxy.convert(threeWindingsTransformerFilter.getNominalVoltage1()))
                .nominalVoltage2(AbstractFilterRepositoryProxy.convert(threeWindingsTransformerFilter.getNominalVoltage2()))
                .nominalVoltage3(AbstractFilterRepositoryProxy.convert(threeWindingsTransformerFilter.getNominalVoltage3()));
            buildGenericFilter(threeWindingsTransformerFilterEntityBuilder, threeWindingsTransformerFilter);
            return threeWindingsTransformerFilterEntityBuilder.build();
        }
        throw new PowsyblException(WRONG_FILTER_TYPE);
    }
}
