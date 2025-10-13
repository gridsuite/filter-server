/*
 * Copyright (c) 2025, RTE
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
import org.junit.Test;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Unit tests for FilterWithEquipmentTypesUtils
 */
public class FilterWithEquipmentTypesUtilsTest {

    @Test
    public void testCreateRuleWithOneField() {
        Set<String> ids = new LinkedHashSet<>(Set.of("S1", "S2"));
        AbstractExpertRule rule = FilterWithEquipmentTypesUtils.createRuleWithOneField(ids, FieldType.SUBSTATION_ID);
        assertTrue(rule instanceof StringExpertRule);
        StringExpertRule stringRule = (StringExpertRule) rule;
        assertEquals(OperatorType.IN, stringRule.getOperator());
        assertEquals(FieldType.SUBSTATION_ID, stringRule.getField());
        assertEquals(ids, stringRule.getValues());
    }

    @Test
    public void testCreateRuleWithTwoFields() {
        Set<String> ids = new LinkedHashSet<>(Set.of("S1"));
        AbstractExpertRule rule = FilterWithEquipmentTypesUtils.createRuleWithTwoFields(ids, FieldType.SUBSTATION_ID_1, FieldType.SUBSTATION_ID_2);
        assertTrue(rule instanceof CombinatorExpertRule);
        CombinatorExpertRule comb = (CombinatorExpertRule) rule;
        assertEquals(CombinatorType.OR, comb.getCombinator());
        List<AbstractExpertRule> rules = comb.getRules();
        assertEquals(2, rules.size());
        assertTrue(rules.get(0) instanceof StringExpertRule);
        assertTrue(rules.get(1) instanceof StringExpertRule);
        StringExpertRule r1 = (StringExpertRule) rules.get(0);
        StringExpertRule r2 = (StringExpertRule) rules.get(1);
        assertEquals(OperatorType.IN, r1.getOperator());
        assertEquals(OperatorType.IN, r2.getOperator());
        assertEquals(FieldType.SUBSTATION_ID_1, r1.getField());
        assertEquals(FieldType.SUBSTATION_ID_2, r2.getField());
        assertEquals(ids, r1.getValues());
        assertEquals(ids, r2.getValues());
    }

    @Test
    public void testCreateRuleWithThreeFields() {
        Set<String> ids = new LinkedHashSet<>(Set.of("VL1"));
        AbstractExpertRule rule = FilterWithEquipmentTypesUtils.createRuleWithThreeFields(ids, FieldType.VOLTAGE_LEVEL_ID_1, FieldType.VOLTAGE_LEVEL_ID_2, FieldType.VOLTAGE_LEVEL_ID_3);
        assertTrue(rule instanceof CombinatorExpertRule);
        CombinatorExpertRule comb = (CombinatorExpertRule) rule;
        assertEquals(CombinatorType.OR, comb.getCombinator());
        List<AbstractExpertRule> rules = comb.getRules();
        assertEquals(3, rules.size());
        StringExpertRule r1 = (StringExpertRule) rules.get(0);
        StringExpertRule r2 = (StringExpertRule) rules.get(1);
        StringExpertRule r3 = (StringExpertRule) rules.get(2);
        assertEquals(FieldType.VOLTAGE_LEVEL_ID_1, r1.getField());
        assertEquals(FieldType.VOLTAGE_LEVEL_ID_2, r2.getField());
        assertEquals(FieldType.VOLTAGE_LEVEL_ID_3, r3.getField());
        assertEquals(ids, r1.getValues());
        assertEquals(ids, r2.getValues());
        assertEquals(ids, r3.getValues());
    }

    @Test
    public void testCreateSubstationRuleByEquipmentType() {
        Set<String> subs = new LinkedHashSet<>(Set.of("S1"));
        // LOAD -> one field (SUBSTATION_ID)
        AbstractExpertRule loadRule = FilterWithEquipmentTypesUtils.createSubstationRuleByEquipmentType(IdentifiableType.LOAD, subs);
        assertTrue(loadRule instanceof StringExpertRule);
        assertEquals(FieldType.SUBSTATION_ID, loadRule.getField());
        assertEquals(subs, ((StringExpertRule) loadRule).getValues());
        // LINE -> two fields (SUBSTATION_ID_1, _2)
        AbstractExpertRule lineRule = FilterWithEquipmentTypesUtils.createSubstationRuleByEquipmentType(IdentifiableType.LINE, subs);
        assertTrue(lineRule instanceof CombinatorExpertRule);
        CombinatorExpertRule comb = (CombinatorExpertRule) lineRule;
        assertEquals(2, comb.getRules().size());
        StringExpertRule r1 = (StringExpertRule) comb.getRules().get(0);
        StringExpertRule r2 = (StringExpertRule) comb.getRules().get(1);
        assertEquals(FieldType.SUBSTATION_ID_1, r1.getField());
        assertEquals(FieldType.SUBSTATION_ID_2, r2.getField());
        assertEquals(subs, r1.getValues());
        assertEquals(subs, r2.getValues());
    }

    @Test
    public void testCreateVoltageLevelRuleByEquipmentType() {
        Set<String> vls = new LinkedHashSet<>(Set.of("VL1", "VL2"));
        // LOAD -> one field (VOLTAGE_LEVEL_ID)
        AbstractExpertRule loadRule = FilterWithEquipmentTypesUtils.createVoltageLevelRuleByEquipmentType(IdentifiableType.LOAD, vls);
        assertTrue(loadRule instanceof StringExpertRule);
        assertEquals(FieldType.VOLTAGE_LEVEL_ID, loadRule.getField());
        assertEquals(vls, ((StringExpertRule) loadRule).getValues());
        // LINE -> two fields
        AbstractExpertRule lineRule = FilterWithEquipmentTypesUtils.createVoltageLevelRuleByEquipmentType(IdentifiableType.LINE, vls);
        assertTrue(lineRule instanceof CombinatorExpertRule);
        CombinatorExpertRule comb2 = (CombinatorExpertRule) lineRule;
        assertEquals(2, comb2.getRules().size());
        assertEquals(FieldType.VOLTAGE_LEVEL_ID_1, comb2.getRules().get(0).getField());
        assertEquals(FieldType.VOLTAGE_LEVEL_ID_2, comb2.getRules().get(1).getField());
        assertEquals(vls, ((StringExpertRule) comb2.getRules().get(0)).getValues());
        assertEquals(vls, ((StringExpertRule) comb2.getRules().get(1)).getValues());
        // THREE_WINDINGS_TRANSFORMER -> three fields
        AbstractExpertRule twtRule = FilterWithEquipmentTypesUtils.createVoltageLevelRuleByEquipmentType(IdentifiableType.THREE_WINDINGS_TRANSFORMER, vls);
        assertTrue(twtRule instanceof CombinatorExpertRule);
        CombinatorExpertRule comb3 = (CombinatorExpertRule) twtRule;
        assertEquals(3, comb3.getRules().size());
        assertEquals(FieldType.VOLTAGE_LEVEL_ID_1, comb3.getRules().get(0).getField());
        assertEquals(FieldType.VOLTAGE_LEVEL_ID_2, comb3.getRules().get(1).getField());
        assertEquals(FieldType.VOLTAGE_LEVEL_ID_3, comb3.getRules().get(2).getField());
        assertEquals(vls, ((StringExpertRule) comb3.getRules().get(0)).getValues());
        assertEquals(vls, ((StringExpertRule) comb3.getRules().get(1)).getValues());
        assertEquals(vls, ((StringExpertRule) comb3.getRules().get(2)).getValues());
    }

    @Test
    public void testCreateFiltersForSubEquipmentsFromSubstation() {
        Set<String> parentIds = new LinkedHashSet<>(Set.of("S1", "S2"));
        Set<IdentifiableType> subTypes = Set.of(IdentifiableType.LOAD, IdentifiableType.LINE);
        List<ExpertFilter> filters = FilterWithEquipmentTypesUtils.createFiltersForSubEquipments(EquipmentType.SUBSTATION, parentIds, subTypes);
        assertEquals(2, filters.size());
        for (ExpertFilter f : filters) {
            if (f.getEquipmentType() == EquipmentType.LOAD) {
                assertTrue(f.getRules() instanceof StringExpertRule);
                assertEquals(FieldType.SUBSTATION_ID, ((StringExpertRule) f.getRules()).getField());
                assertEquals(parentIds, ((StringExpertRule) f.getRules()).getValues());
            } else if (f.getEquipmentType() == EquipmentType.LINE) {
                assertTrue(f.getRules() instanceof CombinatorExpertRule);
                CombinatorExpertRule comb = (CombinatorExpertRule) f.getRules();
                assertEquals(CombinatorType.OR, comb.getCombinator());
                assertEquals(2, comb.getRules().size());
                assertEquals(FieldType.SUBSTATION_ID_1, ((StringExpertRule) comb.getRules().get(0)).getField());
                assertEquals(FieldType.SUBSTATION_ID_2, ((StringExpertRule) comb.getRules().get(1)).getField());
                assertEquals(parentIds, ((StringExpertRule) comb.getRules().get(0)).getValues());
                assertEquals(parentIds, ((StringExpertRule) comb.getRules().get(1)).getValues());
            } else {
                fail("Unexpected equipment type: " + f.getEquipmentType());
            }
        }
    }

    @Test
    public void testCreateFiltersForSubEquipmentsFromVoltageLevel() {
        Set<String> parentIds = new LinkedHashSet<>(Set.of("VL1"));
        Set<IdentifiableType> subTypes = Set.of(IdentifiableType.GENERATOR, IdentifiableType.THREE_WINDINGS_TRANSFORMER);
        List<ExpertFilter> filters = FilterWithEquipmentTypesUtils.createFiltersForSubEquipments(EquipmentType.VOLTAGE_LEVEL, parentIds, subTypes);
        assertEquals(2, filters.size());
        for (ExpertFilter f : filters) {
            if (f.getEquipmentType() == EquipmentType.GENERATOR) {
                assertTrue(f.getRules() instanceof StringExpertRule);
                assertEquals(FieldType.VOLTAGE_LEVEL_ID, ((StringExpertRule) f.getRules()).getField());
                assertEquals(parentIds, ((StringExpertRule) f.getRules()).getValues());
            } else if (f.getEquipmentType() == EquipmentType.THREE_WINDINGS_TRANSFORMER) {
                assertTrue(f.getRules() instanceof CombinatorExpertRule);
                CombinatorExpertRule comb = (CombinatorExpertRule) f.getRules();
                assertEquals(3, comb.getRules().size());
                assertEquals(FieldType.VOLTAGE_LEVEL_ID_1, ((StringExpertRule) comb.getRules().get(0)).getField());
                assertEquals(FieldType.VOLTAGE_LEVEL_ID_2, ((StringExpertRule) comb.getRules().get(1)).getField());
                assertEquals(FieldType.VOLTAGE_LEVEL_ID_3, ((StringExpertRule) comb.getRules().get(2)).getField());
                assertEquals(parentIds, ((StringExpertRule) comb.getRules().get(0)).getValues());
                assertEquals(parentIds, ((StringExpertRule) comb.getRules().get(1)).getValues());
                assertEquals(parentIds, ((StringExpertRule) comb.getRules().get(2)).getValues());
            } else {
                fail("Unexpected equipment type: " + f.getEquipmentType());
            }
        }
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testCreateFiltersForSubEquipmentsUnsupported() {
        FilterWithEquipmentTypesUtils.createFiltersForSubEquipments(EquipmentType.LINE, Set.of("X"), Set.of(IdentifiableType.LOAD));
    }
}
