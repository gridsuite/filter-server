/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.filter.server.repositories.proxies.expertfiler;

import com.powsybl.commons.PowsyblException;
import org.gridsuite.filter.AbstractFilter;
import org.gridsuite.filter.expertfilter.ExpertFilter;
import org.gridsuite.filter.expertfilter.expertrule.*;
import org.gridsuite.filter.server.entities.expertfilter.ExpertFilterEntity;
import org.gridsuite.filter.server.entities.expertfilter.ExpertRuleEntity;
import org.gridsuite.filter.server.entities.expertfilter.ExpertRulePropertiesEntity;
import org.gridsuite.filter.server.entities.expertfilter.ExpertRuleValueEntity;
import org.gridsuite.filter.server.repositories.expertfilter.ExpertFilterRepository;
import org.gridsuite.filter.server.repositories.proxies.AbstractFilterRepositoryProxy;
import org.gridsuite.filter.utils.EquipmentType;
import org.gridsuite.filter.utils.FilterType;
import org.gridsuite.filter.utils.expertfilter.DataType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.gridsuite.filter.utils.expertfilter.OperatorType.isMultipleCriteriaOperator;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
public class ExpertFilterRepositoryProxy extends AbstractFilterRepositoryProxy<ExpertFilterEntity, ExpertFilterRepository> {
    private final ExpertFilterRepository expertFilterRepository;

    public ExpertFilterRepositoryProxy(ExpertFilterRepository expertFilterRepository) {
        this.expertFilterRepository = expertFilterRepository;
    }

    @Override
    public ExpertFilterRepository getRepository() {
        return expertFilterRepository;
    }

    @Override
    public AbstractFilter toDto(ExpertFilterEntity filterEntity) {
        return ExpertFilter.builder()
                .id(filterEntity.getId())
                .modificationDate(filterEntity.getModificationDate())
                .equipmentType(filterEntity.getEquipmentType())
                .rules(entityToDto(filterEntity.getRules()))
                .build();
    }

    public static AbstractExpertRule entityToDto(ExpertRuleEntity expertRuleEntity) {
        switch (expertRuleEntity.getDataType()) {
            case COMBINATOR -> {
                return CombinatorExpertRule.builder()
                        .combinator(expertRuleEntity.getCombinator())
                        .field(expertRuleEntity.getField())
                        .operator(expertRuleEntity.getOperator())
                        .rules(entitiesToDto(expertRuleEntity.getRules()))
                        .build();
            }
            case BOOLEAN -> {
                ExpertRuleValueEntity booleanExpertRuleEntity = (ExpertRuleValueEntity) expertRuleEntity;
                BooleanExpertRule.BooleanExpertRuleBuilder<?, ?> ruleBuilder = BooleanExpertRule.builder()
                        .field(booleanExpertRuleEntity.getField())
                        .operator(booleanExpertRuleEntity.getOperator());
                if (booleanExpertRuleEntity.getValue() != null) {
                    ruleBuilder.value(Boolean.parseBoolean(booleanExpertRuleEntity.getValue()));
                }
                return ruleBuilder.build();
            }
            case NUMBER -> {
                ExpertRuleValueEntity numberExpertEntity = (ExpertRuleValueEntity) expertRuleEntity;
                NumberExpertRule.NumberExpertRuleBuilder<?, ?> ruleBuilder = NumberExpertRule.builder()
                        .field(numberExpertEntity.getField())
                        .operator(numberExpertEntity.getOperator());
                if (numberExpertEntity.getValue() != null) {
                    if (isMultipleCriteriaOperator(numberExpertEntity.getOperator())) { // for multiple values
                        ruleBuilder.values(Stream.of(numberExpertEntity.getValue().split(",")).map(Double::valueOf).collect(Collectors.toSet()));
                    } else { // for single value
                        ruleBuilder.value(Double.valueOf(numberExpertEntity.getValue()));
                    }
                }
                return ruleBuilder.build();

            }
            case STRING -> {
                ExpertRuleValueEntity stringExpertRuleEntity = (ExpertRuleValueEntity) expertRuleEntity;
                StringExpertRule.StringExpertRuleBuilder<?, ?> ruleBuilder = StringExpertRule.builder()
                        .field(stringExpertRuleEntity.getField())
                        .operator(stringExpertRuleEntity.getOperator());
                if (stringExpertRuleEntity.getValue() != null) {
                    if (isMultipleCriteriaOperator(stringExpertRuleEntity.getOperator())) { // for multiple values
                        ruleBuilder.values(Stream.of(stringExpertRuleEntity.getValue().split(",")).collect(Collectors.toSet()));
                    } else { // for single value
                        ruleBuilder.value(stringExpertRuleEntity.getValue());
                    }
                }
                return ruleBuilder.build();

            }
            case ENUM -> {
                ExpertRuleValueEntity enumExpertRuleEntity = (ExpertRuleValueEntity) expertRuleEntity;
                EnumExpertRule.EnumExpertRuleBuilder<?, ?> ruleBuilder = EnumExpertRule.builder()
                        .field(enumExpertRuleEntity.getField())
                        .operator(enumExpertRuleEntity.getOperator());
                if (enumExpertRuleEntity.getValue() != null) {
                    if (isMultipleCriteriaOperator(enumExpertRuleEntity.getOperator())) { // for multiple values
                        ruleBuilder.values(Stream.of(enumExpertRuleEntity.getValue().split(",")).collect(Collectors.toSet()));
                    } else { // for single value
                        ruleBuilder.value(enumExpertRuleEntity.getValue());
                    }
                }
                return ruleBuilder.build();
            }
            case FILTER_UUID -> {
                ExpertRuleValueEntity filterUuidExpertRuleEntity = (ExpertRuleValueEntity) expertRuleEntity;

                FilterUuidExpertRule.FilterUuidExpertRuleBuilder<?, ?> ruleBuilder = FilterUuidExpertRule.builder()
                        .field(filterUuidExpertRuleEntity.getField())
                        .operator(filterUuidExpertRuleEntity.getOperator());
                if (filterUuidExpertRuleEntity.getValue() != null) {
                    if (isMultipleCriteriaOperator(filterUuidExpertRuleEntity.getOperator())) { // for multiple values
                        ruleBuilder.values(Stream.of(filterUuidExpertRuleEntity.getValue().split(",")).collect(Collectors.toSet()));
                    } else { // for single value
                        ruleBuilder.value(filterUuidExpertRuleEntity.getValue());
                    }
                }
                return ruleBuilder.build();

            }
            case PROPERTIES -> {
                ExpertRulePropertiesEntity propertiesExpertRuleEntity = (ExpertRulePropertiesEntity) expertRuleEntity;
                return PropertiesExpertRule.builder()
                        .field(propertiesExpertRuleEntity.getField())
                        .operator(propertiesExpertRuleEntity.getOperator())
                        .propertyValues(propertiesExpertRuleEntity.getPropertyValues())
                        .propertyName(propertiesExpertRuleEntity.getPropertyName())
                        .build();
            }
            default ->
                    throw new PowsyblException("Unknown rule data type: " + expertRuleEntity.getDataType() + ", supported data types are: " + Arrays.stream(DataType.values()).map(Enum::name).collect(Collectors.joining(", ")));
        }
    }

    private static List<AbstractExpertRule> entitiesToDto(List<ExpertRuleEntity> entities) {
        if (entities == null) {
            return Collections.emptyList();
        }

        return entities.stream()
                .map(ExpertFilterRepositoryProxy::entityToDto)
                .collect(Collectors.toList());
    }

    @Override
    public ExpertFilterEntity fromDto(AbstractFilter dto) {
        if (dto instanceof ExpertFilter filter) {
            var expertFilterEntityBuilder = ExpertFilterEntity.builder()
                    .modificationDate(filter.getModificationDate())
                    .equipmentType(filter.getEquipmentType())
                    .rules(dtoToEntity(filter.getRules()));
            buildAbstractFilter(expertFilterEntityBuilder, filter);
            return expertFilterEntityBuilder.build();
        }
        throw new PowsyblException(WRONG_FILTER_TYPE);
    }

    public static ExpertRuleEntity.ExpertRuleEntityBuilder<?, ?> getRuleBuilder(AbstractExpertRule filter) {
        ExpertRuleEntity.ExpertRuleEntityBuilder<?, ?> expertRuleEntityBuilder = null;
        if (filter.getDataType() == DataType.COMBINATOR) {
            expertRuleEntityBuilder = ExpertRuleEntity.builder()
                    .id(UUID.randomUUID())
                    .combinator(filter.getCombinator())
                    .operator(filter.getOperator())
                    .dataType(filter.getDataType())
                    .field(filter.getField());
        }
        if (filter.getDataType() == DataType.PROPERTIES) {
            PropertiesExpertRule propertiesRule = (PropertiesExpertRule) filter;
            expertRuleEntityBuilder = ExpertRulePropertiesEntity.builder()
                    .id(UUID.randomUUID())
                    .combinator(filter.getCombinator())
                    .operator(filter.getOperator())
                    .dataType(filter.getDataType())
                    .field(filter.getField())
                    .propertyValues(propertiesRule.getPropertyValues())
                    .propertyName(propertiesRule.getPropertyName());

        } else if (filter.getDataType() == DataType.BOOLEAN ||
                filter.getDataType() == DataType.NUMBER ||
                filter.getDataType() == DataType.STRING ||
                filter.getDataType() == DataType.ENUM ||
                filter.getDataType() == DataType.FILTER_UUID) {
            expertRuleEntityBuilder = ExpertRuleValueEntity.builder()
                    .id(UUID.randomUUID())
                    .combinator(filter.getCombinator())
                    .operator(filter.getOperator())
                    .dataType(filter.getDataType())
                    .field(filter.getField())
                    .value(filter.getStringValue());
        }

        return expertRuleEntityBuilder;
    }

    public static ExpertRuleEntity dtoToEntity(AbstractExpertRule filter) {
        ExpertRuleEntity.ExpertRuleEntityBuilder<?, ?> expertRuleEntityBuilder = getRuleBuilder(filter);

        if (expertRuleEntityBuilder == null) {
            throw new PowsyblException("Unsupported data type: " + filter.getDataType());
        }

        List<ExpertRuleEntity> rules = dtoToEntities(filter.getRules(), expertRuleEntityBuilder.build());
        expertRuleEntityBuilder.rules(rules);

        return expertRuleEntityBuilder.build();
    }

    private static List<ExpertRuleEntity> dtoToEntities(List<AbstractExpertRule> ruleFromDto, ExpertRuleEntity parentRuleEntity) {
        if (ruleFromDto == null) {
            return Collections.emptyList();
        }

        return ruleFromDto.stream()
                .map(rule -> {
                    ExpertRuleEntity.ExpertRuleEntityBuilder<?, ?> expertRuleEntityBuilder = getRuleBuilder(rule).parentRule(parentRuleEntity);
                    List<ExpertRuleEntity> rules = dtoToEntities(rule.getRules(), expertRuleEntityBuilder.build());
                    expertRuleEntityBuilder.rules(rules);

                    return expertRuleEntityBuilder.build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.EXPERT;
    }

    @Override
    public EquipmentType getEquipmentType(UUID id) {
        return expertFilterRepository.findById(id)
                .map(ExpertFilterEntity::getEquipmentType)
                .orElseThrow(() -> new PowsyblException("Identifier list filter " + id + " not found"));
    }

}
