/*
 *  Copyright (c) 2021, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.filter.server;

import com.powsybl.commons.PowsyblException;
import org.gridsuite.filter.server.dto.AbstractFilter;
import org.gridsuite.filter.server.dto.FormFilter;
import org.gridsuite.filter.server.dto.ShuntCompensatorFilter;
import org.gridsuite.filter.server.entities.ShuntCompensatorFilterEntity;
import org.gridsuite.filter.server.repositories.ShuntCompensatorFilterRepository;
import org.gridsuite.filter.server.utils.EquipmentType;
import org.gridsuite.filter.server.utils.FilterType;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */

public class ShuntCompensatorFilterRepositoryProxy extends AbstractFilterRepositoryProxy<ShuntCompensatorFilterEntity, ShuntCompensatorFilterRepository> {

    private final ShuntCompensatorFilterRepository shuntCompensatorFilterRepository;

    public ShuntCompensatorFilterRepositoryProxy(ShuntCompensatorFilterRepository shuntCompensatorFilterRepository) {
        this.shuntCompensatorFilterRepository = shuntCompensatorFilterRepository;
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.FORM;
    }

    @Override
    public EquipmentType getEquipmentType() {
        return EquipmentType.SHUNT_COMPENSATOR;
    }

    @Override
    public ShuntCompensatorFilterRepository getRepository() {
        return shuntCompensatorFilterRepository;
    }

    @Override
    public AbstractFilter toDto(ShuntCompensatorFilterEntity entity) {
        return new FormFilter(
                entity.getId(),
                entity.getCreationDate(),
                entity.getModificationDate(),
                new ShuntCompensatorFilter(
                        entity.getEquipmentId(),
                        entity.getEquipmentName(),
                        entity.getSubstationName(),
                        entity.getCountries(),
                        entity.getNominalVoltage()
                )
        );
    }

    @Override
    public ShuntCompensatorFilterEntity fromDto(AbstractFilter dto) {
        if (!(dto instanceof FormFilter)) {
            throw new PowsyblException(WRONG_FILTER_TYPE);
        }
        FormFilter formFilter = (FormFilter) dto;

        if (!(formFilter.getEquipmentFilterForm() instanceof ShuntCompensatorFilter)) {
            throw new PowsyblException(WRONG_FILTER_TYPE);
        }
        var shuntCompensatorFilterEntityBuilder = ShuntCompensatorFilterEntity.builder();
        buildInjectionFilter(shuntCompensatorFilterEntityBuilder, formFilter);
        return shuntCompensatorFilterEntityBuilder.build();
    }
}
