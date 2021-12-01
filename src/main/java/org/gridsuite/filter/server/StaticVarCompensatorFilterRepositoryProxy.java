/*
 *  Copyright (c) 2021, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.filter.server;

import com.powsybl.commons.PowsyblException;
import org.gridsuite.filter.server.dto.*;
import org.gridsuite.filter.server.entities.StaticVarCompensatorFilterEntity;
import org.gridsuite.filter.server.repositories.StaticVarCompensatorFilterRepository;
import org.gridsuite.filter.server.utils.EquipmentType;
import org.gridsuite.filter.server.utils.FilterType;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */

public class StaticVarCompensatorFilterRepositoryProxy extends AbstractFilterRepositoryProxy<StaticVarCompensatorFilterEntity, StaticVarCompensatorFilterRepository> {

    private final StaticVarCompensatorFilterRepository staticVarCompensatorFilterRepository;

    public StaticVarCompensatorFilterRepositoryProxy(StaticVarCompensatorFilterRepository staticVarCompensatorFilterRepository) {
        this.staticVarCompensatorFilterRepository = staticVarCompensatorFilterRepository;
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.FORM;
    }

    public EquipmentType getEquipmentType() {
        return EquipmentType.STATIC_VAR_COMPENSATOR;
    }

    @Override
    public StaticVarCompensatorFilterRepository getRepository() {
        return staticVarCompensatorFilterRepository;
    }

    @Override
    public AbstractFilter toDto(StaticVarCompensatorFilterEntity entity) {
        return new FormFilter(
            entity.getId(),
            entity.getCreationDate(),
            entity.getModificationDate(),
            new StaticVarCompensatorFilter(
                entity.getEquipmentId(),
                entity.getEquipmentName(),
                entity.getSubstationName(),
                setToSorterSet(entity.getCountries()),
                convert(entity.getNominalVoltage())
            )
        );
    }

    @Override
    public StaticVarCompensatorFilterEntity fromDto(AbstractFilter dto) {
        if (!(dto instanceof FormFilter)) {
            throw new PowsyblException(WRONG_FILTER_TYPE);
        }
        FormFilter formFilter = (FormFilter) dto;

        if (!(formFilter.getEquipmentFilterForm() instanceof StaticVarCompensatorFilter)) {
            throw new PowsyblException(WRONG_FILTER_TYPE);
        }
        var staticVarCompensatorFilterEntityBuilder = StaticVarCompensatorFilterEntity.builder();
        buildInjectionFilter(staticVarCompensatorFilterEntityBuilder, formFilter);
        return staticVarCompensatorFilterEntityBuilder.build();
    }
}
