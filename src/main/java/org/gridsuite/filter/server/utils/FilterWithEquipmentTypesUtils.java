/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.server.utils;

import com.powsybl.iidm.network.IdentifiableType;
import org.gridsuite.filter.expertfilter.ExpertFilter;
import org.gridsuite.filter.expertfilter.expertrule.AbstractExpertRule;
import org.gridsuite.filter.expertfilter.expertrule.CombinatorExpertRule;
import org.gridsuite.filter.expertfilter.expertrule.StringExpertRule;
import org.gridsuite.filter.utils.EquipmentType;
import org.gridsuite.filter.utils.expertfilter.CombinatorType;
import org.gridsuite.filter.utils.expertfilter.FieldType;
import org.gridsuite.filter.utils.expertfilter.OperatorType;

import java.util.List;
import java.util.Set;

import static org.gridsuite.filter.utils.expertfilter.FieldType.*;

/**
 * Sometimes, when we apply a filter on a substation or voltage level,
 * we actually want the equipments related to these substations or voltage levels.
 * This class is used to build these special filters,
 * and we can specify which equipment types we are interested into.
 * @author Florent MILLOT <florent.millot at rte-france.com>
 */
public final class FilterWithEquipmentTypesUtils {

    private FilterWithEquipmentTypesUtils() {
        // Utility class
    }

    /**
     * Create one filter for each equipment type we want to retrieve from a previous filter result.
     * @param filterEquipmentType the equipment type of the original filter (substation or voltage level)
     * @param filteredEquipmentIDs the filtered equipment IDs of the original filter
     * @param subEquipmentTypes the equipment types we want to collect from the original filter result (so the equipments related to filteredEquipmentIDs)
     * @return the list of filters
     */
    public static List<ExpertFilter> createFiltersForSubEquipments(EquipmentType filterEquipmentType, Set<String> filteredEquipmentIDs, Set<IdentifiableType> subEquipmentTypes) {
        if (filterEquipmentType == EquipmentType.SUBSTATION) {
            return subEquipmentTypes.stream().map(identifiableType -> new ExpertFilter(
                null,
                null,
                EquipmentType.valueOf(identifiableType.name()),
                createSubstationRuleByEquipmentType(identifiableType, filteredEquipmentIDs))).toList();
        } else if (filterEquipmentType == EquipmentType.VOLTAGE_LEVEL) {
            return subEquipmentTypes.stream().map(identifiableType -> new ExpertFilter(
                null,
                null,
                EquipmentType.valueOf(identifiableType.name()),
                createVoltageLevelRuleByEquipmentType(identifiableType, filteredEquipmentIDs))).toList();
        } else {
            throw new UnsupportedOperationException("Unsupported filter equipment type " + filterEquipmentType
                + " : we can only filter sub equipments from substation and voltage level");
        }
    }

    public static AbstractExpertRule createSubstationRuleByEquipmentType(IdentifiableType equipmentType, Set<String> substationIds) {
        return switch (equipmentType) {
            case LOAD, GENERATOR, SHUNT_COMPENSATOR, STATIC_VAR_COMPENSATOR, BUSBAR_SECTION, BATTERY,
                 DANGLING_LINE, TWO_WINDINGS_TRANSFORMER, THREE_WINDINGS_TRANSFORMER -> createSubstationRule(substationIds, 1);
            case LINE, HVDC_LINE -> createSubstationRule(substationIds, 2);
            default -> throw new UnsupportedOperationException("Unsupported equipment type " + equipmentType);
        };
    }

    public static AbstractExpertRule createVoltageLevelRuleByEquipmentType(IdentifiableType equipmentType, Set<String> voltageLevelIds) {
        return switch (equipmentType) {
            case LOAD, GENERATOR, SHUNT_COMPENSATOR, STATIC_VAR_COMPENSATOR, BUSBAR_SECTION, BATTERY,
                 DANGLING_LINE -> createVoltageLevelRule(voltageLevelIds, 1);
            case LINE, HVDC_LINE, TWO_WINDINGS_TRANSFORMER -> createVoltageLevelRule(voltageLevelIds, 2);
            case THREE_WINDINGS_TRANSFORMER -> createVoltageLevelRule(voltageLevelIds, 3);
            default -> throw new UnsupportedOperationException("Unsupported equipment type " + equipmentType);
        };
    }

    public static AbstractExpertRule createSubstationRule(Set<String> substationIds, int fieldCount) {
        return switch (fieldCount) {
            case 1 -> createRuleWithOneField(substationIds, SUBSTATION_ID);
            case 2 -> createRuleWithTwoFields(substationIds, SUBSTATION_ID_1, SUBSTATION_ID_2);
            default -> throw new UnsupportedOperationException("Unsupported field count " + fieldCount);
        };
    }

    public static AbstractExpertRule createVoltageLevelRule(Set<String> voltageLevelIds, int fieldCount) {
        return switch (fieldCount) {
            case 1 -> createRuleWithOneField(voltageLevelIds, VOLTAGE_LEVEL_ID);
            case 2 -> createRuleWithTwoFields(voltageLevelIds, VOLTAGE_LEVEL_ID_1, VOLTAGE_LEVEL_ID_2);
            case 3 ->
                createRuleWithThreeFields(voltageLevelIds, VOLTAGE_LEVEL_ID_1, VOLTAGE_LEVEL_ID_2, VOLTAGE_LEVEL_ID_3);
            default -> throw new UnsupportedOperationException("Unsupported field count " + fieldCount);
        };
    }

    public static AbstractExpertRule createRuleWithOneField(Set<String> equipmentIds, FieldType field) {
        return StringExpertRule.builder()
            .operator(OperatorType.IN).field(field)
            .values(equipmentIds).build();
    }

    public static AbstractExpertRule createRuleWithTwoFields(Set<String> equipmentIds, FieldType field1, FieldType field2) {
        StringExpertRule rule1 = StringExpertRule.builder()
            .operator(OperatorType.IN).field(field1)
            .values(equipmentIds).build();
        StringExpertRule rule2 = StringExpertRule.builder()
            .operator(OperatorType.IN).field(field2)
            .values(equipmentIds).build();
        return CombinatorExpertRule.builder().combinator(CombinatorType.OR).rules(List.of(rule1, rule2)).build();
    }

    public static AbstractExpertRule createRuleWithThreeFields(Set<String> equipmentIds, FieldType field1, FieldType field2, FieldType field3) {
        StringExpertRule rule1 = StringExpertRule.builder()
            .operator(OperatorType.IN).field(field1)
            .values(equipmentIds).build();
        StringExpertRule rule2 = StringExpertRule.builder()
            .operator(OperatorType.IN).field(field2)
            .values(equipmentIds).build();
        StringExpertRule rule3 = StringExpertRule.builder()
            .operator(OperatorType.IN).field(field3)
            .values(equipmentIds).build();
        return CombinatorExpertRule.builder().combinator(CombinatorType.OR).rules(List.of(rule1, rule2, rule3)).build();
    }
}

