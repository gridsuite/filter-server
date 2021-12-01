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
import org.gridsuite.filter.server.dto.GeneratorFilter;
import org.gridsuite.filter.server.entities.GeneratorFilterEntity;
import org.gridsuite.filter.server.repositories.GeneratorFilterRepository;
import org.gridsuite.filter.server.utils.EquipmentType;
import org.gridsuite.filter.server.utils.FilterType;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */

public class GeneratorFilterRepositoryProxy extends AbstractFilterRepositoryProxy<GeneratorFilterEntity, GeneratorFilterRepository> {

    private final GeneratorFilterRepository generatorFilterRepository;

    public GeneratorFilterRepositoryProxy(GeneratorFilterRepository generatorFilterRepository) {
        this.generatorFilterRepository = generatorFilterRepository;
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.FORM;
    }

    public EquipmentType getEquipmentType() {
        return EquipmentType.GENERATOR;
    }

    @Override
    public GeneratorFilterRepository getRepository() {
        return generatorFilterRepository;
    }

    @Override
    public AbstractFilter toDto(GeneratorFilterEntity entity) {
        return new FormFilter(
            entity.getId(),
            entity.getCreationDate(),
            entity.getModificationDate(),
            new GeneratorFilter(
                entity.getEquipmentId(),
                entity.getEquipmentName(),
                entity.getSubstationName(),
                setToSorterSet(entity.getCountries()),
                convert(entity.getNominalVoltage())
            )
        );
    }

    @Override
    public GeneratorFilterEntity fromDto(AbstractFilter dto) {
        if (!(dto instanceof FormFilter)) {
            throw new PowsyblException(WRONG_FILTER_TYPE);
        }
        FormFilter formFilter = (FormFilter) dto;

        if (!(formFilter.getEquipmentFilterForm() instanceof GeneratorFilter)) {
            throw new PowsyblException(WRONG_FILTER_TYPE);
        }
        var generatorFilterEntityBuilder = GeneratorFilterEntity.builder();
        buildInjectionFilter(generatorFilterEntityBuilder, formFilter);
        return generatorFilterEntityBuilder.build();
    }
}
