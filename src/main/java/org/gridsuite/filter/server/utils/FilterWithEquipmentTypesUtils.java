/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.filter.server.utils;

import com.powsybl.iidm.network.IdentifiableType;
import org.gridsuite.filter.model.Filter;
import org.gridsuite.filter.model.expertfilter.ExpertFilter;
import org.gridsuite.filter.model.expertfilter.rules.CombinatorExpertRule;
import org.gridsuite.filter.model.expertfilter.rules.ExpertRule;
import org.gridsuite.filter.model.expertfilter.rules.StringListExpertRule;
import org.gridsuite.filter.utils.EquipmentType;
import org.gridsuite.filter.utils.expertfilter.CombinatorType;
import org.gridsuite.filter.utils.expertfilter.FieldType;
import org.gridsuite.filter.utils.expertfilter.OperatorType;

import java.util.List;

import static org.gridsuite.filter.utils.expertfilter.FieldType.*;

/**
 * Sometimes, when we apply a filter on a substation or voltage level,
 * we actually want the equipments related to these substations or voltage levels.
 * This class is used to build these special filters,
 * and we can specify which equipment types we are interested into.
 * PS : We could have used a mix of expert filters with the operator IS_PART_OF
 * but in this case we also need the notFoundIds of the original filter,
 * which is not possible because it is different equipment types.
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
    public static List<Filter> createFiltersForSubEquipments(EquipmentType filterEquipmentType, List<String> filteredEquipmentIDs, List<IdentifiableType> subEquipmentTypes) {
        return switch (filterEquipmentType) {
            case SUBSTATION -> subEquipmentTypes.stream().map(identifiableType -> (Filter) ExpertFilter.builder()
                .equipmentType(EquipmentType.valueOf(identifiableType.name()))
                .rule(createSubstationRuleByEquipmentType(identifiableType, filteredEquipmentIDs))
                .build()).toList();
            case VOLTAGE_LEVEL -> subEquipmentTypes.stream().map(identifiableType -> (Filter) ExpertFilter.builder()
                .equipmentType(EquipmentType.valueOf(identifiableType.name()))
                .rule(createVoltageLevelRuleByEquipmentType(identifiableType, filteredEquipmentIDs))
                .build()).toList();
            default ->
                throw new UnsupportedOperationException("Unsupported filter equipment type " + filterEquipmentType
                    + " : we can only filter sub equipments from substation and voltage level");
        };
    }

    public static ExpertRule createSubstationRuleByEquipmentType(IdentifiableType equipmentType, List<String> substationIds) {
        return switch (equipmentType) {
            case LOAD, GENERATOR, SHUNT_COMPENSATOR, STATIC_VAR_COMPENSATOR, BUSBAR_SECTION, BATTERY,
                 DANGLING_LINE, TWO_WINDINGS_TRANSFORMER, THREE_WINDINGS_TRANSFORMER -> createRuleWithOneField(substationIds, SUBSTATION_ID);
            case LINE, HVDC_LINE -> createRuleWithTwoFields(substationIds, SUBSTATION_ID_1, SUBSTATION_ID_2);
            default -> throw new UnsupportedOperationException("Unsupported equipment type " + equipmentType);
        };
    }

    public static ExpertRule createVoltageLevelRuleByEquipmentType(IdentifiableType equipmentType, List<String> voltageLevelIds) {
        return switch (equipmentType) {
            case LOAD, GENERATOR, SHUNT_COMPENSATOR, STATIC_VAR_COMPENSATOR, BUSBAR_SECTION, BATTERY,
                 DANGLING_LINE -> createRuleWithOneField(voltageLevelIds, VOLTAGE_LEVEL_ID);
            case LINE, HVDC_LINE, TWO_WINDINGS_TRANSFORMER -> createRuleWithTwoFields(voltageLevelIds, VOLTAGE_LEVEL_ID_1, VOLTAGE_LEVEL_ID_2);
            case THREE_WINDINGS_TRANSFORMER -> createRuleWithThreeFields(voltageLevelIds, VOLTAGE_LEVEL_ID_1, VOLTAGE_LEVEL_ID_2, VOLTAGE_LEVEL_ID_3);
            default -> throw new UnsupportedOperationException("Unsupported equipment type " + equipmentType);
        };
    }

    public static ExpertRule createRuleWithOneField(List<String> equipmentIds, FieldType field) {
        return StringListExpertRule.builder()
            .operator(OperatorType.IN).field(field)
            .value(equipmentIds).build();
    }

    public static ExpertRule createRuleWithTwoFields(List<String> equipmentIds, FieldType field1, FieldType field2) {
        ExpertRule rule1 = StringListExpertRule.builder()
            .operator(OperatorType.IN).field(field1)
            .value(equipmentIds).build();
        ExpertRule rule2 = StringListExpertRule.builder()
            .operator(OperatorType.IN).field(field2)
            .value(equipmentIds).build();
        return CombinatorExpertRule.builder().combinator(CombinatorType.OR).rules(List.of(rule1, rule2)).build();
    }

    public static ExpertRule createRuleWithThreeFields(List<String> equipmentIds, FieldType field1, FieldType field2, FieldType field3) {
        ExpertRule rule1 = StringListExpertRule.builder()
            .operator(OperatorType.IN).field(field1)
            .value(equipmentIds).build();
        ExpertRule rule2 = StringListExpertRule.builder()
            .operator(OperatorType.IN).field(field2)
            .value(equipmentIds).build();
        ExpertRule rule3 = StringListExpertRule.builder()
            .operator(OperatorType.IN).field(field3)
            .value(equipmentIds).build();
        return CombinatorExpertRule.builder().combinator(CombinatorType.OR).rules(List.of(rule1, rule2, rule3)).build();
    }
}

