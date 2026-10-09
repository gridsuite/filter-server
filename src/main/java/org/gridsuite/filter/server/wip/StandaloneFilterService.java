/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.server.wip;

import com.google.common.annotations.Beta;
import org.gridsuite.filter.server.entities.expertfilter.ExpertFilterEntity;
import org.gridsuite.filter.server.entities.expertfilter.ExpertRuleEntity;
import org.gridsuite.filter.server.entities.expertfilter.ExpertRulePropertiesEntity;
import org.gridsuite.filter.server.entities.expertfilter.ExpertRuleValueEntity;
import org.gridsuite.filter.server.entities.identifierlistfilter.IdentifierListFilterEntity;
import org.gridsuite.filter.server.entities.identifierlistfilter.IdentifierListFilterEquipmentEntity;
import org.gridsuite.filter.server.repositories.expertfilter.ExpertFilterRepository;
import org.gridsuite.filter.server.repositories.identifierlistfilter.IdentifierListFilterRepository;
import org.gridsuite.filter.server.wip.dto.FilterWithDistributionKeys;
import org.gridsuite.filter.wip.ExpertFilter;
import org.gridsuite.filter.wip.Filter;
import org.gridsuite.filter.wip.IdentifierListFilter;
import org.gridsuite.filter.wip.rule.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.gridsuite.filter.utils.expertfilter.OperatorType.isMultipleCriteriaOperator;

/**
 * @author Mohamed Ben Rejeb {@literal <mohamed.ben-rejeb at rte-france.com>}
 */
@Service
@Beta
public class StandaloneFilterService {

    private final IdentifierListFilterRepository identifierListFilterRepository;
    private final ExpertFilterRepository expertFilterRepository;

    public StandaloneFilterService(IdentifierListFilterRepository identifierListFilterRepository,
                                   ExpertFilterRepository expertFilterRepository) {
        this.identifierListFilterRepository = identifierListFilterRepository;
        this.expertFilterRepository = expertFilterRepository;
    }

    @Transactional(readOnly = true)
    public Optional<Filter> getFilter(UUID id) {
        Optional<Filter> identifierListFilter = identifierListFilterRepository.findById(id).map(this::toWipFilter);
        if (identifierListFilter.isPresent()) {
            return identifierListFilter;
        }
        return expertFilterRepository.findById(id).map(this::toWipFilter);
    }

    /**
     * Loads the filters matching the given identifiers.
     * Identifiers with no matching filter are omitted from the result.
     *
     * @param ids the identifiers of the filters to load
     * @return the filters found, indexed by their identifier
     */
    @Transactional(readOnly = true)
    public Map<UUID, Filter> getFilters(List<UUID> ids) {
        Map<UUID, Filter> filtersById = new LinkedHashMap<>();
        identifierListFilterRepository.findAllById(ids).forEach(entity -> filtersById.put(entity.getId(), toWipFilter(entity)));
        expertFilterRepository.findAllById(ids).forEach(entity -> filtersById.put(entity.getId(), toWipFilter(entity)));
        return filtersById;
    }

    //TODO WHAT TO DO WHEN FILTER NOT FOUND ? THEY ARE SILENTLY DROPPED AND MAY CAUSE A PROBLEM - TICKET GRD-5559
    /**
     * Loads the filters matching the given identifiers, together with the distribution keys recorded for their equipments.
     * Identifiers with no matching filter are omitted from the result.
     *
     * @param ids the identifiers of the filters to load
     * @return the filters found, indexed by their identifier
     */
    @Transactional(readOnly = true)
    public Map<UUID, FilterWithDistributionKeys> getFiltersWithDistributionKeys(List<UUID> ids) {
        Map<UUID, FilterWithDistributionKeys> filtersWithDistributionKeysById = new LinkedHashMap<>();
        identifierListFilterRepository.findAllById(ids).forEach(entity -> filtersWithDistributionKeysById.put(entity.getId(), toFilterWithDistributionKeys(entity)));
        expertFilterRepository.findAllById(ids).forEach(entity -> filtersWithDistributionKeysById.put(entity.getId(), toFilterWithDistributionKeys(entity)));
        return filtersWithDistributionKeysById;
    }

    private FilterWithDistributionKeys toFilterWithDistributionKeys(IdentifierListFilterEntity entity) {
        Map<String, Double> distributionKeys = new LinkedHashMap<>();
        // TODO UPDATE WHEN DISTRIBUTION KEYS ARE FINALLY UNIQUE (TODAY ONE FILTER CAN HAVE THE SAME EQUIPMENT ID MULTIPLE TIMES)
        entity.getFilterEquipmentEntityList().forEach(equipmentEntity -> distributionKeys.put(equipmentEntity.getEquipmentId(), equipmentEntity.getDistributionKey()));

        return FilterWithDistributionKeys.builder()
                .filter(toWipFilter(entity))
                .distributionKeys(distributionKeys)
                .build();
    }

    private FilterWithDistributionKeys toFilterWithDistributionKeys(ExpertFilterEntity entity) {
        return FilterWithDistributionKeys.builder()
                .filter(toWipFilter(entity))
                .distributionKeys(Collections.emptyMap()) // Expert filters do not have distribution keys
                .build();
    }

    private Filter toWipFilter(IdentifierListFilterEntity entity) {
        Set<String> equipmentIds = entity.getFilterEquipmentEntityList().stream()
                .map(IdentifierListFilterEquipmentEntity::getEquipmentId)
                .collect(Collectors.toSet());
        return IdentifierListFilter.builder()
                .equipmentType(entity.getEquipmentType())
                .equipmentIds(equipmentIds)
                .build();
    }

    private Filter toWipFilter(ExpertFilterEntity entity) {
        return ExpertFilter.builder()
                .equipmentType(entity.getEquipmentType())
                .rule(toWipRule(entity.getRules()))
                .build();
    }

    private ExpertRule toWipRule(ExpertRuleEntity entity) {
        return switch (entity.getDataType()) {
            case COMBINATOR -> CombinatorExpertRule.builder()
                    .combinator(entity.getCombinator())
                    .rules(entity.getRules().stream()
                            .map(this::toWipRule)
                            .toList())
                    .build();
            case BOOLEAN -> {
                ExpertRuleValueEntity e = (ExpertRuleValueEntity) entity;
                yield BooleanExpertRule.builder()
                        .field(e.getField())
                        .operator(e.getOperator())
                        .value(e.getValue() != null ? Boolean.parseBoolean(e.getValue()) : null)
                        .build();
            }
            case NUMBER -> {
                ExpertRuleValueEntity e = (ExpertRuleValueEntity) entity;
                NumberExpertRule.NumberExpertRuleBuilder builder = NumberExpertRule.builder()
                        .field(e.getField())
                        .operator(e.getOperator());
                if (e.getValue() != null) {
                    if (isMultipleCriteriaOperator(e.getOperator())) {
                        builder.values(Stream.of(e.getValue().split(",")).map(Double::valueOf).toList());
                    } else {
                        builder.value(Double.valueOf(e.getValue()));
                    }
                }
                yield builder.build();
            }
            case STRING -> {
                ExpertRuleValueEntity e = (ExpertRuleValueEntity) entity;
                StringExpertRule.StringExpertRuleBuilder builder = StringExpertRule.builder()
                        .field(e.getField())
                        .operator(e.getOperator());
                if (e.getValue() != null) {
                    if (isMultipleCriteriaOperator(e.getOperator())) {
                        builder.values(Stream.of(e.getValue().split(",")).toList());
                    } else {
                        builder.value(e.getValue());
                    }
                }
                yield builder.build();
            }
            case ENUM -> {
                ExpertRuleValueEntity e = (ExpertRuleValueEntity) entity;
                EnumExpertRule.EnumExpertRuleBuilder builder = EnumExpertRule.builder()
                        .field(e.getField())
                        .operator(e.getOperator());
                if (e.getValue() != null) {
                    if (isMultipleCriteriaOperator(e.getOperator())) {
                        builder.values(Stream.of(e.getValue().split(",")).toList());
                    } else {
                        builder.value(e.getValue());
                    }
                }
                yield builder.build();
            }
            case FILTER_UUID -> {
                ExpertRuleValueEntity e = (ExpertRuleValueEntity) entity;
                List<Filter> referenceFilters = new ArrayList<>();
                if (e.getValue() != null) {
                    Stream.of(e.getValue().split(","))
                            .map(String::trim)
                            .map(UUID::fromString)
                            .forEach(filterId -> getFilter(filterId).ifPresent(referenceFilters::add));
                }
                yield FilterExpertRule.builder()
                        .field(e.getField())
                        .operator(e.getOperator())
                        .filters(referenceFilters)
                        .build();
            }
            case PROPERTIES -> {
                ExpertRulePropertiesEntity e = (ExpertRulePropertiesEntity) entity;
                yield PropertiesExpertRule.builder()
                        .field(e.getField())
                        .operator(e.getOperator())
                        .propertyName(e.getPropertyName())
                        .propertyValues(e.getPropertyValues())
                        .build();
            }
        };
    }
}
