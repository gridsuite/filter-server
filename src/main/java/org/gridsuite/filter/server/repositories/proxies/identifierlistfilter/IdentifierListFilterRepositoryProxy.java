/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.filter.server.repositories.proxies.identifierlistfilter;

import com.powsybl.commons.PowsyblException;
import org.gridsuite.filter.server.dto.criteriafilter.AbstractEquipmentFilterForm;
import org.gridsuite.filter.server.dto.AbstractFilter;
import org.gridsuite.filter.server.dto.identifierlistfilter.IdentifierListFilterEquipmentAttributes;
import org.gridsuite.filter.server.dto.identifierlistfilter.IdentifierListFilter;
import org.gridsuite.filter.server.entities.AbstractFilterEntity;
import org.gridsuite.filter.server.entities.identifierlistfilter.IdentifierListFilterEntity;
import org.gridsuite.filter.server.entities.identifierlistfilter.IdentifierListFilterEquipmentEntity;
import org.gridsuite.filter.server.repositories.identifierlistfilter.IdentifierListFilterRepository;
import org.gridsuite.filter.server.repositories.proxies.AbstractFilterRepositoryProxy;
import org.gridsuite.filter.server.utils.EquipmentType;
import org.gridsuite.filter.server.utils.FilterType;

import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Seddik Yengui <seddik.yengui at rte-france.com>
 */

public class IdentifierListFilterRepositoryProxy extends AbstractFilterRepositoryProxy<IdentifierListFilterEntity, IdentifierListFilterRepository> {
    private final IdentifierListFilterRepository identifierListFilterRepository;

    public IdentifierListFilterRepositoryProxy(IdentifierListFilterRepository identifierListFilterRepository) {
        this.identifierListFilterRepository = identifierListFilterRepository;
    }

    @Override
    public IdentifierListFilterRepository getRepository() {
        return identifierListFilterRepository;
    }

    @Override
    public AbstractFilter toDto(IdentifierListFilterEntity filterEntity) {
        return new IdentifierListFilter(filterEntity.getId(),
                filterEntity.getModificationDate(),
                filterEntity.getEquipmentType(),
                filterEntity.getFilterEquipmentEntityList()
                        .stream()
                        .map(entity -> new IdentifierListFilterEquipmentAttributes(entity.getEquipmentId(),
                                                                     entity.getDistributionKey()))
                        .collect(Collectors.toList()));
    }

    @Override
    public IdentifierListFilterEntity fromDto(AbstractFilter dto) {
        if (dto instanceof IdentifierListFilter filter) {
            var identifierListFilterEntityBuilder = IdentifierListFilterEntity.builder()
                    .equipmentType(filter.getEquipmentType())
                    .filterEquipmentEntityList(filter.getFilterEquipmentsAttributes()
                            .stream()
                            .map(attributes -> IdentifierListFilterEquipmentEntity.builder()
                                    .id(UUID.randomUUID())
                                    .equipmentId(attributes.getEquipmentID())
                                    .distributionKey(attributes.getDistributionKey())
                                    .build())
                            .collect(Collectors.toList()));

            buildAbstractFilter(identifierListFilterEntityBuilder, filter);
            return identifierListFilterEntityBuilder.build();
        }
        throw new PowsyblException(WRONG_FILTER_TYPE);
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.IDENTIFIER_LIST;
    }

    @Override
    public EquipmentType getEquipmentType() {
        throw new UnsupportedOperationException("A filter id must be provided to get equipment type !!");
    }

    @Override
    public EquipmentType getEquipmentType(UUID id) {
        return identifierListFilterRepository.findById(id)
            .map(IdentifierListFilterEntity::getEquipmentType)
            .orElseThrow(() -> new PowsyblException("Identifier list filter " + id + " not found"));
    }

    @Override
    public AbstractEquipmentFilterForm buildEquipmentFormFilter(AbstractFilterEntity entity) {
        return null;
    }
}
