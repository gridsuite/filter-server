/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.server;

import com.powsybl.iidm.network.*;
import org.gridsuite.filter.server.dto.expertfilter.expertrule.*;
import org.gridsuite.filter.server.utils.expertfilter.CombinatorType;
import org.gridsuite.filter.server.utils.expertfilter.FieldType;
import org.gridsuite.filter.server.utils.expertfilter.OperatorType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ExpertFilterUtilsTest {

    private Generator gen;

    @Before
    public void setUp() {
        VoltageLevel voltageLevel = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel.getId()).thenReturn("GEN_01");

        Terminal terminal = Mockito.mock(Terminal.class);
        Mockito.when(terminal.getVoltageLevel()).thenReturn(voltageLevel);

        gen = Mockito.mock(Generator.class);
        Mockito.when(gen.getType()).thenReturn(IdentifiableType.GENERATOR);
        Mockito.when(gen.getMinP()).thenReturn(-500.0);
        Mockito.when(gen.getMaxP()).thenReturn(100.0);
        Mockito.when(gen.getTargetV()).thenReturn(20.0);
        Mockito.when(gen.getId()).thenReturn("ID_1");
        Mockito.when(gen.getNameOrId()).thenReturn("NAME");
        Mockito.when(gen.getEnergySource()).thenReturn(EnergySource.HYDRO);
        Mockito.when(gen.isVoltageRegulatorOn()).thenReturn(true);
        Mockito.when(gen.getTerminal()).thenReturn(terminal);
    }

    @Test
    public void testEvaluateExpertFilterWithANDCombination() {
        List<AbstractExpertRule> andRules1 = new ArrayList<>();
        NumberExpertRule numRule1 = NumberExpertRule.builder().value(0.0)
                .field(FieldType.MIN_P).operator(OperatorType.LOWER).build();
        andRules1.add(numRule1);
        NumberExpertRule numRule2 = NumberExpertRule.builder().value(-500.0)
                .field(FieldType.MIN_P).operator(OperatorType.LOWER_OR_EQUALS).build();
        andRules1.add(numRule2);
        NumberExpertRule numRule3 = NumberExpertRule.builder().value(100.0)
                .field(FieldType.MAX_P).operator(OperatorType.GREATER_OR_EQUALS).build();
        andRules1.add(numRule3);
        CombinatorExpertRule andCombination = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(andRules1).build();

        List<AbstractExpertRule> andRules2 = new ArrayList<>();
        andRules2.add(andCombination);
        NumberExpertRule numRule4 = NumberExpertRule.builder().value(20.0)
                .field(FieldType.TARGET_V).operator(OperatorType.EQUALS).build();
        andRules2.add(numRule4);
        EnumExpertRule enumRule4 = EnumExpertRule.builder().value("HYDRO")
                .field(FieldType.ENERGY_SOURCE).operator(OperatorType.EQUALS).build();
        andRules2.add(enumRule4);
        EnumExpertRule enumRule5 = EnumExpertRule.builder().value("OTHER")
                .field(FieldType.ENERGY_SOURCE).operator(OperatorType.NOT_EQUALS).build();
        andRules2.add(enumRule5);
        StringExpertRule stringRule5 = StringExpertRule.builder().value("ID")
                .field(FieldType.ID).operator(OperatorType.BEGINS_WITH).build();
        andRules2.add(stringRule5);
        BooleanExpertRule booleanRule6 = BooleanExpertRule.builder().value(true)
                .field(FieldType.VOLTAGE_REGULATOR_ON).operator(OperatorType.EQUALS).build();
        andRules2.add(booleanRule6);
        BooleanExpertRule booleanRule7 = BooleanExpertRule.builder().value(false)
                .field(FieldType.VOLTAGE_REGULATOR_ON).operator(OperatorType.NOT_EQUALS).build();
        andRules2.add(booleanRule7);
        StringExpertRule stringRule8 = StringExpertRule.builder().value("GEN_01")
                .field(FieldType.VOLTAGE_LEVEL_ID).operator(OperatorType.IS).build();
        andRules2.add(stringRule8);

        CombinatorExpertRule andFilter = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(andRules2).build();

        boolean result = andFilter.evaluateRule(gen);

        assertTrue(result);
    }

    @Test
    public void testEvaluateExpertFilterWithFalseANDCombination() {
        List<AbstractExpertRule> andRules1 = new ArrayList<>();
        NumberExpertRule numRule1 = NumberExpertRule.builder().value(0.0)
                .field(FieldType.MIN_P).operator(OperatorType.LOWER).build();
        andRules1.add(numRule1);
        NumberExpertRule numRule2 = NumberExpertRule.builder().value(105.0)
                .field(FieldType.MAX_P).operator(OperatorType.GREATER_OR_EQUALS).build(); // false
        andRules1.add(numRule2);
        CombinatorExpertRule andCombination = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(andRules1).build();

        List<AbstractExpertRule> andRules2 = new ArrayList<>();
        andRules2.add(andCombination);
        NumberExpertRule numRule3 = NumberExpertRule.builder().value(20.0)
                .field(FieldType.TARGET_V).operator(OperatorType.EQUALS).build();
        andRules2.add(numRule3);
        EnumExpertRule enumRule4 = EnumExpertRule.builder().value("HYDRO")
                .field(FieldType.ENERGY_SOURCE).operator(OperatorType.EQUALS).build();
        andRules2.add(enumRule4);

        CombinatorExpertRule andFilter = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(andRules2).build();

        boolean result = andFilter.evaluateRule(gen);

        assertFalse(result);
    }

    @Test
    public void testEvaluateExpertFilterWithORCombination() {
        List<AbstractExpertRule> orRules1 = new ArrayList<>();
        NumberExpertRule numRule1 = NumberExpertRule.builder().value(0.0)
                .field(FieldType.MIN_P).operator(OperatorType.LOWER).build();
        orRules1.add(numRule1);
        NumberExpertRule numRule2 = NumberExpertRule.builder().value(100.0)
                .field(FieldType.MAX_P).operator(OperatorType.GREATER_OR_EQUALS).build(); // false
        orRules1.add(numRule2);
        CombinatorExpertRule orCombination = CombinatorExpertRule.builder().combinator(CombinatorType.OR).rules(orRules1).build();

        List<AbstractExpertRule> orRules2 = new ArrayList<>();
        orRules2.add(orCombination);
        NumberExpertRule numRule3 = NumberExpertRule.builder().value(20.0)
                .field(FieldType.TARGET_V).operator(OperatorType.EQUALS).build();
        orRules2.add(numRule3);
        EnumExpertRule enumRule4 = EnumExpertRule.builder().value("OTHER")
                .field(FieldType.ENERGY_SOURCE).operator(OperatorType.EQUALS).build(); //false
        orRules2.add(enumRule4);

        CombinatorExpertRule orFilter = CombinatorExpertRule.builder().combinator(CombinatorType.OR).rules(orRules2).build();

        boolean result = orFilter.evaluateRule(gen);

        assertTrue(result);
    }

    @Test
    public void testEvaluateExpertFilterWithFalseORCombination() {
        List<AbstractExpertRule> orRules1 = new ArrayList<>();
        NumberExpertRule numRule1 = NumberExpertRule.builder().value(0.0)
                .field(FieldType.MIN_P).operator(OperatorType.GREATER).build(); // false
        orRules1.add(numRule1);
        NumberExpertRule numRule2 = NumberExpertRule.builder().value(105.0)
                .field(FieldType.MAX_P).operator(OperatorType.GREATER_OR_EQUALS).build(); // false
        orRules1.add(numRule2);
        NumberExpertRule numRule3 = NumberExpertRule.builder().value(-500.0)
                .field(FieldType.MIN_P).operator(OperatorType.LOWER).build(); // false
        orRules1.add(numRule3);
        NumberExpertRule numRule4 = NumberExpertRule.builder().value(-500.5)
                .field(FieldType.MAX_P).operator(OperatorType.LOWER_OR_EQUALS).build(); // false
        orRules1.add(numRule4);
        CombinatorExpertRule orCombination = CombinatorExpertRule.builder().combinator(CombinatorType.OR).rules(orRules1).build();

        List<AbstractExpertRule> orRules2 = new ArrayList<>();
        orRules2.add(orCombination);
        NumberExpertRule numRule5 = NumberExpertRule.builder().value(25.0) // False
                .field(FieldType.TARGET_V).operator(OperatorType.EQUALS).build();
        orRules2.add(numRule5);
        EnumExpertRule enumRule4 = EnumExpertRule.builder().value("OTHER")
                .field(FieldType.ENERGY_SOURCE).operator(OperatorType.EQUALS).build(); //false
        orRules2.add(enumRule4);
        EnumExpertRule enumRule5 = EnumExpertRule.builder().value("HYDRO")
                .field(FieldType.ENERGY_SOURCE).operator(OperatorType.NOT_EQUALS).build(); //false
        orRules2.add(enumRule5);
        StringExpertRule stringRule5 = StringExpertRule.builder().value("TEST")
                .field(FieldType.ID).operator(OperatorType.BEGINS_WITH).build(); //false
        orRules2.add(stringRule5);
        BooleanExpertRule booleanRule6 = BooleanExpertRule.builder().value(false)
                .field(FieldType.VOLTAGE_REGULATOR_ON).operator(OperatorType.EQUALS).build(); //false
        orRules2.add(booleanRule6);
        BooleanExpertRule booleanRule7 = BooleanExpertRule.builder().value(true)
                .field(FieldType.VOLTAGE_REGULATOR_ON).operator(OperatorType.NOT_EQUALS).build(); //false
        orRules2.add(booleanRule7);

        CombinatorExpertRule orFilter = CombinatorExpertRule.builder().combinator(CombinatorType.OR).rules(orRules2).build();

        boolean result = orFilter.evaluateRule(gen);

        assertFalse(result);
    }

    @Test
    public void testEvaluateExpertFilterIgnoreCase() {
        List<AbstractExpertRule> andRules1 = new ArrayList<>();
        StringExpertRule rule1 = StringExpertRule.builder().value("id")
                .field(FieldType.ID).operator(OperatorType.CONTAINS).build();
        andRules1.add(rule1);
        StringExpertRule rule2 = StringExpertRule.builder().value("ID")
                .field(FieldType.ID).operator(OperatorType.CONTAINS).build();
        andRules1.add(rule2);
        StringExpertRule rule3 = StringExpertRule.builder().value("id_1")
                .field(FieldType.ID).operator(OperatorType.IS).build();
        andRules1.add(rule3);
        StringExpertRule rule4 = StringExpertRule.builder().value("ID_1")
                .field(FieldType.ID).operator(OperatorType.IS).build();
        andRules1.add(rule4);
        StringExpertRule rule5 = StringExpertRule.builder().value("id")
                .field(FieldType.ID).operator(OperatorType.BEGINS_WITH).build();
        andRules1.add(rule5);
        StringExpertRule rule6 = StringExpertRule.builder().value("ID")
                .field(FieldType.ID).operator(OperatorType.BEGINS_WITH).build();
        andRules1.add(rule6);
        StringExpertRule rule7 = StringExpertRule.builder().value("me")
                .field(FieldType.NAME).operator(OperatorType.ENDS_WITH).build();
        andRules1.add(rule7);
        StringExpertRule rule8 = StringExpertRule.builder().value("ME")
                .field(FieldType.NAME).operator(OperatorType.ENDS_WITH).build();
        andRules1.add(rule8);
        CombinatorExpertRule andCombination = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(andRules1).build();

        boolean result = andCombination.evaluateRule(gen);

        assertTrue(result);
    }
}
