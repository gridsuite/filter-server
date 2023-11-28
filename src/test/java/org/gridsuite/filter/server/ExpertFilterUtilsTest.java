/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.server;

import com.powsybl.iidm.network.EnergySource;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.IdentifiableType;
import com.powsybl.iidm.network.extensions.GeneratorStartup;
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
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;

public class ExpertFilterUtilsTest {

    private Generator gen;

    @Before
    public void setUp() {
        gen = Mockito.mock(Generator.class);
        Mockito.when(gen.getType()).thenReturn(IdentifiableType.GENERATOR);
        Mockito.when(gen.getMinP()).thenReturn(-500.0);
        Mockito.when(gen.getMaxP()).thenReturn(100.0);
        Mockito.when(gen.getTargetV()).thenReturn(20.0);
        Mockito.when(gen.getId()).thenReturn("ID_1");
        Mockito.when(gen.getNameOrId()).thenReturn("NAME");
        Mockito.when(gen.getEnergySource()).thenReturn(EnergySource.HYDRO);
        Mockito.when(gen.isVoltageRegulatorOn()).thenReturn(true);
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

    @Test
    public void testEvaluateExpertFilterExists() {
        List<AbstractExpertRule> numRules = new ArrayList<>();
        numRules.add(NumberExpertRule.builder().field(FieldType.TARGET_V).operator(OperatorType.EXISTS).build());
        CombinatorExpertRule numFilter = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(numRules).build();

        List<AbstractExpertRule> stringRules = new ArrayList<>();
        stringRules.add(StringExpertRule.builder().field(FieldType.NAME).operator(OperatorType.EXISTS).build());
        CombinatorExpertRule stringFilter = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(stringRules).build();

        // Test when value exists
        assertTrue(numFilter.evaluateRule(gen));
        assertTrue(stringFilter.evaluateRule(gen));

        // Test when value does not exist
        Mockito.when(gen.getTargetV()).thenReturn(Double.NaN);
        assertFalse(numFilter.evaluateRule(gen));

        Mockito.when(gen.getNameOrId()).thenReturn(null);
        assertFalse(stringFilter.evaluateRule(gen));

        Mockito.when(gen.getNameOrId()).thenReturn("");
        assertFalse(stringFilter.evaluateRule(gen));
    }

    @Test
    public void testEvaluateExpertFilterExtension() {
        List<AbstractExpertRule> numRules = new ArrayList<>();
        numRules.add(NumberExpertRule.builder().field(FieldType.PLANNED_ACTIVE_POWER_SET_POINT).operator(OperatorType.EXISTS).build());
        CombinatorExpertRule numFilter = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(numRules).build();

        // Test when extension does not exist
        assertFalse(numFilter.evaluateRule(gen));

        // Test with extension
        GeneratorStartup genStart = Mockito.mock(GeneratorStartup.class);
        Mockito.when(genStart.getPlannedActivePowerSetpoint()).thenReturn(50.0);
        Mockito.when(gen.getExtension(any())).thenReturn(genStart);
        assertTrue(numFilter.evaluateRule(gen));
    }

    @Test
    public void testEvaluateExpertFilterInAndNotInOperators() {

        // --- Test IN Operator --- //

        // Mock for 1st generator
        Generator gen1 = Mockito.mock(Generator.class);
        Mockito.when(gen1.getType()).thenReturn(IdentifiableType.GENERATOR);

        Substation substation1 = Mockito.mock(Substation.class);
        Mockito.when(substation1.getCountry()).thenReturn(Optional.of(Country.FR));

        VoltageLevel voltageLevel1 = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel1.getId()).thenReturn("VL1");
        Mockito.when(voltageLevel1.getNominalV()).thenReturn(13.0);

        Mockito.when(voltageLevel1.getSubstation()).thenReturn(Optional.of(substation1));

        Terminal terminal1 = Mockito.mock(Terminal.class);
        Mockito.when(terminal1.getVoltageLevel()).thenReturn(voltageLevel1);
        Mockito.when(gen1.getTerminal()).thenReturn(terminal1);

        // Build a filter AND with only an IN operator for VOLTAGE_LEVEL_ID
        StringExpertRule stringInRule = StringExpertRule.builder().values(Set.of("VL1", "VL3"))
                .field(FieldType.VOLTAGE_LEVEL_ID).operator(OperatorType.IN).build();
        CombinatorExpertRule inFilter = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(List.of(stringInRule)).build();

        // Check gen1 must be in the list
        assertTrue(inFilter.evaluateRule(gen1));

        // Build a filter AND with only an IN operator for NOMINAL_VOLTAGE
        NumberExpertRule numberInRule = NumberExpertRule.builder().values(Set.of(13.0, 17.0))
                .field(FieldType.NOMINAL_VOLTAGE).operator(OperatorType.IN).build();
        inFilter = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(List.of(numberInRule)).build();

        // Check gen1 must be in the list
        assertTrue(inFilter.evaluateRule(gen1));

        // Build a filter AND with only an IN operator for COUNTRY
        EnumExpertRule enumInRule = EnumExpertRule.builder().values(Set.of(Country.FR.name(), Country.GB.name()))
                .field(FieldType.COUNTRY).operator(OperatorType.IN).build();
        inFilter = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(List.of(enumInRule)).build();

        // Check gen1 must be in the list
        assertTrue(inFilter.evaluateRule(gen1));

        // --- Test NOT_IN Operator --- //

        // Mock for 2nd generator
        Generator gen2 = Mockito.mock(Generator.class);
        Mockito.when(gen2.getType()).thenReturn(IdentifiableType.GENERATOR);

        Substation substation2 = Mockito.mock(Substation.class);
        Mockito.when(substation2.getCountry()).thenReturn(Optional.of(Country.GE));

        VoltageLevel voltageLevel2 = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel2.getId()).thenReturn("VL2");
        Mockito.when(voltageLevel2.getNominalV()).thenReturn(15.0);
        Mockito.when(voltageLevel2.getSubstation()).thenReturn(Optional.of(substation2));

        Terminal terminal2 = Mockito.mock(Terminal.class);
        Mockito.when(terminal2.getVoltageLevel()).thenReturn(voltageLevel2);
        Mockito.when(gen2.getTerminal()).thenReturn(terminal2);

        // Build a filter AND with only a NOT_IN operator for VOLTAGE_LEVEL_ID
        StringExpertRule stringNotInRule = StringExpertRule.builder().values(Set.of("VL1", "VL3"))
                .field(FieldType.VOLTAGE_LEVEL_ID).operator(OperatorType.NOT_IN).build();
        CombinatorExpertRule notInFilter = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(List.of(stringNotInRule)).build();

        // Check gen2 must be not in the list
        assertTrue(notInFilter.evaluateRule(gen2));

        // Build a filter AND with only a NOT_IN operator for NOMINAL_VOLTAGE
        NumberExpertRule numberNotInRule = NumberExpertRule.builder().values(Set.of(13.0, 17.0))
                .field(FieldType.NOMINAL_VOLTAGE).operator(OperatorType.NOT_IN).build();
        notInFilter = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(List.of(numberNotInRule)).build();

        // Check gen2 must be not in the list
        assertTrue(notInFilter.evaluateRule(gen2));

        // Build a filter AND with only a NOT_IN operator for COUNTRY
        EnumExpertRule enumNotInRule = EnumExpertRule.builder().values(Set.of(Country.FR.name(), Country.GB.name()))
                .field(FieldType.COUNTRY).operator(OperatorType.NOT_IN).build();
        notInFilter = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(List.of(enumNotInRule)).build();

        // Check gen2 must be not in the list
        assertTrue(notInFilter.evaluateRule(gen2));
    }
}
