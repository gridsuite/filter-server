/*
 *  Copyright (c) 2021, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.filter.server;

import com.powsybl.commons.PowsyblException;
import org.gridsuite.filter.server.dto.AbstractFilter;
import org.gridsuite.filter.server.dto.VscConverterStationFilter;
import org.gridsuite.filter.server.entities.VscConverterStationFilterEntity;
import org.gridsuite.filter.server.repositories.VscConverterStationFilterRepository;
import org.gridsuite.filter.server.utils.FilterType;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */

class VscConverterStationFilterRepositoryProxy extends AbstractFilterRepositoryProxy<VscConverterStationFilterEntity, VscConverterStationFilterRepository> {

    private final VscConverterStationFilterRepository vscConverterStationFilterRepository;

    public VscConverterStationFilterRepositoryProxy(VscConverterStationFilterRepository vscConverterStationFilterRepository) {
        this.vscConverterStationFilterRepository = vscConverterStationFilterRepository;
    }

    @Override
    public FilterType getRepositoryType() {
        return FilterType.VSC_CONVERTER_STATION;
    }

    @Override
    public VscConverterStationFilterRepository getRepository() {
        return vscConverterStationFilterRepository;
    }

    @Override
    public AbstractFilter toDto(VscConverterStationFilterEntity entity) {
        return buildInjectionFilter(
            VscConverterStationFilter.builder(), entity).build();
    }

    @Override
    public VscConverterStationFilterEntity fromDto(AbstractFilter dto) {
        if (dto instanceof VscConverterStationFilter) {
            var vscConverterStationFilterEntityBuilder   = VscConverterStationFilterEntity.builder();
            buildInjectionFilter(vscConverterStationFilterEntityBuilder, (VscConverterStationFilter) dto);
            return vscConverterStationFilterEntityBuilder.build();
        }
        throw new PowsyblException(WRONG_FILTER_TYPE);
    }
}
