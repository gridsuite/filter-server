/*
 *  Copyright (c) 2021, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.filter.server.repositories.proxies.criteriafilter;

import org.gridsuite.filter.server.dto.criteriafilter.AbstractEquipmentFilterForm;
import org.gridsuite.filter.server.dto.AbstractFilter;
import org.gridsuite.filter.server.dto.criteriafilter.CriteriaFilter;
import org.gridsuite.filter.server.dto.criteriafilter.GeneratorFilter;
import org.gridsuite.filter.server.entities.AbstractFilterEntity;
import org.gridsuite.filter.server.entities.criteriafilter.GeneratorFilterEntity;
import org.gridsuite.filter.server.repositories.criteriafilter.GeneratorFilterRepository;
import org.gridsuite.filter.server.repositories.proxies.AbstractFilterRepositoryProxy;
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
        return FilterType.CRITERIA;
    }

    @Override
    public EquipmentType getEquipmentType() {
        return EquipmentType.GENERATOR;
    }

    @Override
    public GeneratorFilterRepository getRepository() {
        return generatorFilterRepository;
    }

    @Override
    public AbstractFilter toDto(GeneratorFilterEntity entity) {
        return super.toFormFilterDto(entity);
    }

    @Override
    public AbstractEquipmentFilterForm buildEquipmentFormFilter(AbstractFilterEntity entity) {
        GeneratorFilterEntity generatorFilterEntity = (GeneratorFilterEntity) entity;
        return new GeneratorFilter(generatorFilterEntity.getEquipmentId(),
                generatorFilterEntity.getEquipmentName(),
                generatorFilterEntity.getSubstationName(),
                AbstractFilterRepositoryProxy.setToSorterSet(generatorFilterEntity.getCountries()),
                AbstractFilterRepositoryProxy.convert(generatorFilterEntity.getSubstationFreeProperties()),
                //TODO ADAPT ONCE ENTITIES HAVE BEEN UPDATED
                AbstractFilterRepositoryProxy.convert(generatorFilterEntity.getSubstationFreeProperties()),
                AbstractFilterRepositoryProxy.convert(generatorFilterEntity.getNominalVoltage()),
                generatorFilterEntity.getEnergySource());
    }

    @Override
    public GeneratorFilterEntity fromDto(AbstractFilter dto) {
        var generatorFilterEntityBuilder = GeneratorFilterEntity.builder();
        CriteriaFilter criteriaFilter = toFormFilter(dto, GeneratorFilter.class);
        buildInjectionFilter(generatorFilterEntityBuilder, criteriaFilter);
        GeneratorFilter generatorFilter = (GeneratorFilter) criteriaFilter.getEquipmentFilterForm();
        generatorFilterEntityBuilder.energySource(generatorFilter.getEnergySource());
        return generatorFilterEntityBuilder.build();
    }
}
