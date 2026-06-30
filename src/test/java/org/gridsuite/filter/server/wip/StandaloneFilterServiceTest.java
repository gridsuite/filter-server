/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.server.wip;

import org.gridsuite.filter.server.entities.expertfilter.ExpertFilterEntity;
import org.gridsuite.filter.server.entities.expertfilter.ExpertRuleEntity;
import org.gridsuite.filter.server.entities.expertfilter.ExpertRulePropertiesEntity;
import org.gridsuite.filter.server.entities.expertfilter.ExpertRuleValueEntity;
import org.gridsuite.filter.server.entities.identifierlistfilter.IdentifierListFilterEntity;
import org.gridsuite.filter.server.entities.identifierlistfilter.IdentifierListFilterEquipmentEntity;
import org.gridsuite.filter.server.repositories.expertfilter.ExpertFilterRepository;
import org.gridsuite.filter.server.repositories.identifierlistfilter.IdentifierListFilterRepository;
import org.gridsuite.filter.utils.EquipmentType;
import org.gridsuite.filter.utils.expertfilter.CombinatorType;
import org.gridsuite.filter.utils.expertfilter.DataType;
import org.gridsuite.filter.utils.expertfilter.FieldType;
import org.gridsuite.filter.utils.expertfilter.OperatorType;
import org.gridsuite.filter.wip.ExpertFilter;
import org.gridsuite.filter.wip.Filter;
import org.gridsuite.filter.wip.IdentifierListFilter;
import org.gridsuite.filter.wip.rule.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Mohamed Ben Rejeb {@literal <mohamed.ben-rejeb at rte-france.com>}
 */
@ExtendWith(MockitoExtension.class)
class StandaloneFilterServiceTest {

    @Mock
    private IdentifierListFilterRepository identifierListFilterRepository;

    @Mock
    private ExpertFilterRepository expertFilterRepository;

    @InjectMocks
    private StandaloneFilterService service;

    // --- getFilter delegation ---

    @Test
    void getFilterWhenFoundInIdentifierListRepositoryReturnsIdentifierListFilter() {
        // Arrange
        UUID id = UUID.randomUUID();
        when(identifierListFilterRepository.findById(id))
                .thenReturn(Optional.of(identifierListFilterEntity(id, EquipmentType.GENERATOR, "GEN1", "GEN2")));

        // Act
        Filter filter = service.getFilter(id).orElseThrow();

        // Assert
        assertThat(filter).isInstanceOf(IdentifierListFilter.class);
        assertThat(filter.getEquipmentType()).isEqualTo(EquipmentType.GENERATOR);
        assertThat(((IdentifierListFilter) filter).getEquipmentIds()).containsExactlyInAnyOrder("GEN1", "GEN2");
        verify(identifierListFilterRepository).findById(id);
    }

    @Test
    void getFilterWhenNotInIdentifierListRepositoryFallsBackToExpertFilter() {
        // Arrange
        UUID id = UUID.randomUUID();
        when(identifierListFilterRepository.findById(id)).thenReturn(Optional.empty());
        when(expertFilterRepository.findById(id)).thenReturn(Optional.of(
                expertFilterEntity(id, EquipmentType.LINE,
                        combinator(numberValue(FieldType.NOMINAL_VOLTAGE, OperatorType.GREATER, "400.0")))));

        // Act
        Filter filter = service.getFilter(id).orElseThrow();

        // Assert
        assertThat(filter).isInstanceOf(ExpertFilter.class);
        assertThat(filter.getEquipmentType()).isEqualTo(EquipmentType.LINE);
        verify(identifierListFilterRepository).findById(id);
        verify(expertFilterRepository).findById(id);
    }

    @Test
    void getFilterWhenNotFoundInAnyRepositoryReturnsEmpty() {
        // Arrange
        UUID id = UUID.randomUUID();
        when(identifierListFilterRepository.findById(id)).thenReturn(Optional.empty());
        when(expertFilterRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThat(service.getFilter(id)).isEmpty();
        verify(identifierListFilterRepository).findById(id);
        verify(expertFilterRepository).findById(id);
    }

    @Test
    void getFiltersWhenMixedTypeIdsReturnsBothFilterTypes() {
        // Arrange
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        when(identifierListFilterRepository.findAllById(List.of(id1, id2)))
                .thenReturn(List.of(identifierListFilterEntity(id1, EquipmentType.LINE, "L1")));
        when(expertFilterRepository.findAllById(List.of(id1, id2)))
                .thenReturn(List.of(expertFilterEntity(id2, EquipmentType.GENERATOR,
                        combinator(numberValue(FieldType.NOMINAL_VOLTAGE, OperatorType.GREATER, "100.0")))));

        // Act
        List<Filter> filters = service.getFilters(List.of(id1, id2));

        // Assert
        assertThat(filters).hasSize(2)
                .anySatisfy(f -> assertThat(f).isInstanceOf(IdentifierListFilter.class))
                .anySatisfy(f -> assertThat(f).isInstanceOf(ExpertFilter.class));
        verify(identifierListFilterRepository).findAllById(List.of(id1, id2));
        verify(expertFilterRepository).findAllById(List.of(id1, id2));
    }

    // --- entity→domain mapping ---

    @Test
    void getFilterWhenAllLeafRuleTypesMapsToCorrectRuleInstances() {
        // Arrange
        UUID id = UUID.randomUUID();
        ExpertRuleEntity root = combinator(
                numberValue(FieldType.NOMINAL_VOLTAGE_1, OperatorType.BETWEEN, "360.0,400.0"),
                stringValue(),
                booleanValue(),
                enumValue()
        );
        when(identifierListFilterRepository.findById(id)).thenReturn(Optional.empty());
        when(expertFilterRepository.findById(id)).thenReturn(Optional.of(expertFilterEntity(id, EquipmentType.LINE, root)));

        // Act
        CombinatorExpertRule combinator = (CombinatorExpertRule) ((ExpertFilter) service.getFilter(id).orElseThrow()).getRule();

        // Assert
        assertThat(combinator.getCombinator()).isEqualTo(CombinatorType.AND);
        assertThat(combinator.getRules()).hasSize(4)
                .anySatisfy(r -> assertThat(r).isInstanceOf(NumberExpertRule.class))
                .anySatisfy(r -> assertThat(r).isInstanceOf(StringExpertRule.class))
                .anySatisfy(r -> assertThat(r).isInstanceOf(BooleanExpertRule.class))
                .anySatisfy(r -> assertThat(r).isInstanceOf(EnumExpertRule.class));
    }

    @Test
    void getFilterWhenPropertiesEntityMapsToPropertiesExpertRule() {
        // Arrange
        UUID id = UUID.randomUUID();
        ExpertRuleEntity root = combinator(propertiesValue(List.of("north", "south")));
        when(identifierListFilterRepository.findById(id)).thenReturn(Optional.empty());
        when(expertFilterRepository.findById(id)).thenReturn(Optional.of(expertFilterEntity(id, EquipmentType.SUBSTATION, root)));

        // Act
        CombinatorExpertRule combinator = (CombinatorExpertRule) ((ExpertFilter) service.getFilter(id).orElseThrow()).getRule();

        // Assert
        assertThat(combinator.getRules()).singleElement()
                .isInstanceOf(PropertiesExpertRule.class)
                .satisfies(r -> {
                    PropertiesExpertRule pr = (PropertiesExpertRule) r;
                    assertThat(pr.getPropertyName()).isEqualTo("region");
                    assertThat(pr.getPropertyValues()).containsExactlyInAnyOrder("north", "south");
                });
    }

    @Test
    void getFilterWhenFilterUuidEntityResolvesReferencedFilter() {
        // Arrange
        UUID referencedId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        when(identifierListFilterRepository.findById(referencedId))
                .thenReturn(Optional.of(identifierListFilterEntity(referencedId, EquipmentType.GENERATOR, "GEN1")));
        ExpertRuleEntity root = combinator(filterUuidValue(referencedId.toString()));
        when(identifierListFilterRepository.findById(id)).thenReturn(Optional.empty());
        when(expertFilterRepository.findById(id)).thenReturn(Optional.of(expertFilterEntity(id, EquipmentType.GENERATOR, root)));

        // Act
        CombinatorExpertRule combinator = (CombinatorExpertRule) ((ExpertFilter) service.getFilter(id).orElseThrow()).getRule();

        // Assert
        assertThat(combinator.getRules()).singleElement()
                .isInstanceOf(FilterExpertRule.class)
                .satisfies(r -> assertThat(((FilterExpertRule) r).getFilters())
                        .singleElement().isInstanceOf(IdentifierListFilter.class));
    }

    // --- entity builders (no parentRule needed: service traverses the in-memory rules list, not JPA relationships) ---

    private IdentifierListFilterEntity identifierListFilterEntity(UUID id, EquipmentType type, String... equipmentIds) {
        List<IdentifierListFilterEquipmentEntity> equipment = Arrays.stream(equipmentIds)
                .map(eqId -> IdentifierListFilterEquipmentEntity.builder().id(UUID.randomUUID()).equipmentId(eqId).build())
                .map(IdentifierListFilterEquipmentEntity.class::cast)
                .toList();
        return IdentifierListFilterEntity.builder().id(id).equipmentType(type).filterEquipmentEntityList(equipment)
                .build();
    }

    private ExpertFilterEntity expertFilterEntity(UUID id, EquipmentType type, ExpertRuleEntity root) {
        return ExpertFilterEntity.builder().id(id).equipmentType(type).rules(root).build();
    }

    private ExpertRuleEntity combinator(ExpertRuleEntity... children) {
        return ExpertRuleEntity.builder()
                .id(UUID.randomUUID()).dataType(DataType.COMBINATOR).combinator(CombinatorType.AND)
                .rules(List.of(children))
                .build();
    }

    private ExpertRuleValueEntity numberValue(FieldType field, OperatorType op, String value) {
        return ExpertRuleValueEntity.builder().id(UUID.randomUUID()).dataType(DataType.NUMBER).field(field).operator(op).value(value)
                .build();
    }

    private ExpertRuleValueEntity stringValue() {
        return ExpertRuleValueEntity.builder().id(UUID.randomUUID()).dataType(DataType.STRING).field(FieldType.ID).operator(OperatorType.BEGINS_WITH).value("NHV").build();
    }

    private ExpertRuleValueEntity booleanValue() {
        return ExpertRuleValueEntity.builder().id(UUID.randomUUID()).dataType(DataType.BOOLEAN).field(FieldType.CONNECTED_1).operator(OperatorType.EQUALS).value("true")
                .build();
    }

    private ExpertRuleValueEntity enumValue() {
        return ExpertRuleValueEntity.builder().id(UUID.randomUUID()).dataType(DataType.ENUM).field(FieldType.COUNTRY_1).operator(OperatorType.IN).value("BE,FR")
                .build();
    }

    private ExpertRulePropertiesEntity propertiesValue(List<String> propertyValues) {
        return ExpertRulePropertiesEntity.builder().id(UUID.randomUUID()).dataType(DataType.PROPERTIES).field(FieldType.FREE_PROPERTIES).operator(OperatorType.IN).propertyName("region")
                .propertyValues(propertyValues)
                .build();
    }

    private ExpertRuleValueEntity filterUuidValue(String value) {
        return ExpertRuleValueEntity.builder().id(UUID.randomUUID()).dataType(DataType.FILTER_UUID).field(FieldType.ID).operator(OperatorType.IS_PART_OF).value(value)
                .build();
    }
}
