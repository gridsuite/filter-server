/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.filter.server;

import com.powsybl.commons.PowsyblException;
import org.gridsuite.filter.server.dto.*;
import org.gridsuite.filter.server.dto.expertrule.*;
import org.gridsuite.filter.server.entities.*;
import org.gridsuite.filter.server.repositories.ExpertFilterRepository;
import org.gridsuite.filter.server.utils.EquipmentType;
import org.gridsuite.filter.server.utils.FilterType;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
public class ExpertFilterRepositoryProxy extends AbstractFilterRepositoryProxy<ExpertFilterEntity, ExpertFilterRepository> {
    private final ExpertFilterRepository expertFilterRepository;

    public ExpertFilterRepositoryProxy(ExpertFilterRepository expertFilterRepository) {
        this.expertFilterRepository = expertFilterRepository;
    }

    @Override
    ExpertFilterRepository getRepository() {
        return expertFilterRepository;
    }

    @Override
    AbstractFilter toDto(ExpertFilterEntity filterEntity) {
        return ExpertFilter.builder()
                .id(filterEntity.getId())
                .modificationDate(filterEntity.getModificationDate())
                .equipmentType(filterEntity.getEquipmentType())
                .rules(mapEntityToRule(filterEntity.getRules()))
                .build();
    }

    public static AbstractExpertRule mapEntityToRule(ExpertRuleEntity filterEntity) {
        switch (filterEntity.getDataType()) {
            case COMBINATOR -> {
                return CombinatorExpertRule.builder()
                        .combinator(filterEntity.getCombinator())
                        .rules(mapEntitiesToRules(filterEntity.getRules()))
                        .build();
            }
            case BOOLEAN -> {
                return BooleanExpertRule.builder()
                        .field(filterEntity.getField())
                        .operator(filterEntity.getOperator())
                        .value(Boolean.parseBoolean(filterEntity.getValue()))
                        .build();
            }
            case NUMBER -> {
                return NumberExpertRule.builder()
                        .field(filterEntity.getField())
                        .operator(filterEntity.getOperator())
                        .value(Double.valueOf(filterEntity.getValue()))
                        .build();
            }
            case STRING -> {
                return StringExpertRule.builder()
                        .field(filterEntity.getField())
                        .operator(filterEntity.getOperator())
                        .value(filterEntity.getValue())
                        .build();
            }
            case ENUM -> {
                return EnumExpertRule.builder()
                        .field(filterEntity.getField())
                        .operator(filterEntity.getOperator())
                        .value(filterEntity.getValue())
                        .build();
            }
            default -> throw new PowsyblException(WRONG_FILTER_TYPE);
        }
    }

    private static List<AbstractExpertRule> mapEntitiesToRules(List<ExpertRuleEntity> entities) {
        if (entities == null) {
            return Collections.emptyList();
        }

        return entities.stream()
                .map(ExpertFilterRepositoryProxy::mapEntityToRule)
                .collect(Collectors.toList());
    }

    @Override
    ExpertFilterEntity fromDto(AbstractFilter dto) {
        if (dto instanceof ExpertFilter filter) {
            var expertFilterEntityBuilder = ExpertFilterEntity.builder()
                    .modificationDate(filter.getModificationDate())
                    .equipmentType(filter.getEquipmentType())
                    .rules(mapRuleToEntity(filter.getRules()));
            buildAbstractFilter(expertFilterEntityBuilder, filter);
            return expertFilterEntityBuilder.build();
        }
        throw new PowsyblException(WRONG_FILTER_TYPE);
    }

    public static ExpertRuleEntity mapRuleToEntity(AbstractExpertRule filter) {
        var expertFilterEntityBuilder = ExpertRuleEntity.builder()
                .id(UUID.randomUUID())
                .combinator(filter.getCombinator())
                .operator(filter.getOperator())
                .dataType(filter.getDataType())
                .field(filter.getField())
                .value(filter.getStringValue());
        expertFilterEntityBuilder.rules(mapRulesToEntities(filter.getRules(), expertFilterEntityBuilder.build()));
        return expertFilterEntityBuilder.build();
    }

    private static List<ExpertRuleEntity> mapRulesToEntities(List<AbstractExpertRule> ruleFromDto, ExpertRuleEntity parentRuleEntity) {
        if (ruleFromDto == null) {
            return Collections.emptyList();
        }

        return ruleFromDto.stream()
                .map(rule -> {
                    var expertRuleEntityBuilder = ExpertRuleEntity.builder()
                            .id(UUID.randomUUID())
                            .combinator(rule.getCombinator())
                            .operator(rule.getOperator())
                            .dataType(rule.getDataType())
                            .field(rule.getField())
                            .value(rule.getStringValue())
                            .parentRule(parentRuleEntity);

                    List<ExpertRuleEntity> rules = mapRulesToEntities(rule.getRules(), expertRuleEntityBuilder.build());
                    expertRuleEntityBuilder.rules(rules);

                    return expertRuleEntityBuilder.build();
                })
                .collect(Collectors.toList());
    }

    @Override
    FilterType getFilterType() {
        return FilterType.EXPERT;
    }

    @Override
    public EquipmentType getEquipmentType() {
        throw new UnsupportedOperationException("A filter id must be provided to get equipment type !!");
    }

    @Override
    public EquipmentType getEquipmentType(UUID id) {
        return expertFilterRepository.findById(id)
                .map(ExpertFilterEntity::getEquipmentType)
                .orElseThrow(() -> new PowsyblException("Identifier list filter " + id + " not found"));
    }

    @Override
    public AbstractEquipmentFilterForm buildEquipmentFormFilter(AbstractFilterEntity entity) {
        return null;
    }
}
