/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.server;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.GeneratorStartup;
import org.gridsuite.filter.server.dto.expertfilter.expertrule.*;
import org.gridsuite.filter.server.utils.expertfilter.CombinatorType;
import org.gridsuite.filter.server.utils.expertfilter.FieldType;
import org.gridsuite.filter.server.utils.expertfilter.OperatorType;
import org.junit.Ignore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@Ignore
class ExpertFilterUtilsTest {

    private Generator gen;

    @BeforeEach
    void setUp() {
        gen = Mockito.mock(Generator.class);
        Mockito.when(gen.getType()).thenReturn(IdentifiableType.GENERATOR);
        Mockito.when(gen.getMinP()).thenReturn(-500.0);
        Mockito.when(gen.getMaxP()).thenReturn(100.0);
        Mockito.when(gen.getTargetV()).thenReturn(20.0);
        Mockito.when(gen.getId()).thenReturn("ID_1");
        Mockito.when(gen.getNameOrId()).thenReturn("NAME");
        Mockito.when(gen.getEnergySource()).thenReturn(EnergySource.HYDRO);
        Mockito.when(gen.isVoltageRegulatorOn()).thenReturn(true);
        Mockito.when(gen.getRatedS()).thenReturn(60.0);
    }

    @Test
    void testEvaluateExpertFilterWithANDCombination() {
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
        NumberExpertRule numRule31 = NumberExpertRule.builder().value(40.0)
            .field(FieldType.RATED_S).operator(OperatorType.GREATER).build();
        andRules1.add(numRule31);
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
    void testEvaluateExpertFilterWithFalseANDCombination() {
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
    void testEvaluateExpertFilterWithORCombination() {
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
    void testEvaluateExpertFilterWithFalseORCombination() {
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
    void testEvaluateExpertFilterIgnoreCase() {
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
    void testEvaluateExpertFilterExists() {
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
    void testEvaluateExpertFilterExtension() {
        List<AbstractExpertRule> numRules = new ArrayList<>();
        numRules.add(NumberExpertRule.builder().field(FieldType.PLANNED_ACTIVE_POWER_SET_POINT).operator(OperatorType.EXISTS).build());
        numRules.add(NumberExpertRule.builder().field(FieldType.MARGINAL_COST).value(50.0).operator(OperatorType.EQUALS).build());
        numRules.add(NumberExpertRule.builder().field(FieldType.PLANNED_OUTAGE_RATE).value(50.0).operator(OperatorType.EQUALS).build());
        numRules.add(NumberExpertRule.builder().field(FieldType.FORCED_OUTAGE_RATE).value(50.0).operator(OperatorType.EQUALS).build());
        CombinatorExpertRule numFilter = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(numRules).build();

        // Test when extension does not exist
        assertFalse(numFilter.evaluateRule(gen));

        // Test with extension
        GeneratorStartup genStart = Mockito.mock(GeneratorStartup.class);
        Mockito.when(genStart.getPlannedActivePowerSetpoint()).thenReturn(50.0);
        Mockito.when(genStart.getMarginalCost()).thenReturn(50.0);
        Mockito.when(genStart.getPlannedOutageRate()).thenReturn(50.0);
        Mockito.when(genStart.getForcedOutageRate()).thenReturn(50.0);
        Mockito.when(gen.getExtension(any())).thenReturn(genStart);
        assertTrue(numFilter.evaluateRule(gen));
    }

    @Test
    void testEvaluateExpertFilterGeneratorWithInAndNotIn() {

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

    @Test
    void testEvaluateExpertFilterBetweenOperator() {
        List<AbstractExpertRule> numRules = List.of(NumberExpertRule.builder().values(Set.of(50.0, 150.0)).field(FieldType.MAX_P).operator(OperatorType.BETWEEN).build());
        CombinatorExpertRule numFilter = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(numRules).build();

        // Test OK
        assertTrue(numFilter.evaluateRule(gen));

        // Test not OK
        Mockito.when(gen.getMaxP()).thenReturn(200.0);
        assertFalse(numFilter.evaluateRule(gen));

        Mockito.when(gen.getMaxP()).thenReturn(20.0);
        assertFalse(numFilter.evaluateRule(gen));
    }

    @Test
    void testConnectedField() {
        List<AbstractExpertRule> boolRule = List.of(BooleanExpertRule.builder().value(true).field(FieldType.CONNECTED).operator(OperatorType.EQUALS).build());
        CombinatorExpertRule boolFilter = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(boolRule).build();

        Terminal terminal = Mockito.mock(Terminal.class);
        Mockito.when(gen.getTerminal()).thenReturn(terminal);

        // Test OK
        Mockito.when(terminal.isConnected()).thenReturn(true);
        assertTrue(boolFilter.evaluateRule(gen));

        // Test not OK
        Mockito.when(terminal.isConnected()).thenReturn(false);
        assertFalse(boolFilter.evaluateRule(gen));
    }

    @Test
    void testEvaluateExpertFilterBusAndBusBarSectionWithInAndNotIn() {

        // --- Test IN Operator --- //

        // Mock for 1st bus
        Bus bus1 = Mockito.mock(Bus.class);
        Mockito.when(bus1.getType()).thenReturn(IdentifiableType.BUS);
        Mockito.when(bus1.getId()).thenReturn("BUS_ID_1");
        Mockito.when(bus1.getNameOrId()).thenReturn("BUS_NAME_1");

        Substation substation1 = Mockito.mock(Substation.class);
        Mockito.when(substation1.getCountry()).thenReturn(Optional.of(Country.FR));

        VoltageLevel voltageLevel1 = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel1.getId()).thenReturn("VL1");
        Mockito.when(voltageLevel1.getNominalV()).thenReturn(13.0);

        Mockito.when(voltageLevel1.getSubstation()).thenReturn(Optional.of(substation1));

        Mockito.when(bus1.getVoltageLevel()).thenReturn(voltageLevel1);

        // Mock for 1st busBarSection
        BusbarSection busBarSection1 = Mockito.mock(BusbarSection.class);
        Mockito.when(busBarSection1.getType()).thenReturn(IdentifiableType.BUSBAR_SECTION);
        Mockito.when(busBarSection1.getId()).thenReturn("BUS_BAR_SECTION_ID_1");
        Mockito.when(busBarSection1.getNameOrId()).thenReturn("BUS_BAR_SECTION_NAME_1");

        Terminal terminal1 = Mockito.mock(Terminal.class);
        Mockito.when(terminal1.getVoltageLevel()).thenReturn(voltageLevel1);
        Mockito.when(busBarSection1.getTerminal()).thenReturn(terminal1);

        // Build a filter AND with only an IN operator for VOLTAGE_LEVEL_ID
        StringExpertRule stringInRule = StringExpertRule.builder().values(Set.of("VL1", "VL3"))
                .field(FieldType.VOLTAGE_LEVEL_ID).operator(OperatorType.IN).build();
        CombinatorExpertRule inFilter = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(List.of(stringInRule)).build();

        // Check bus1 must be in the list
        assertTrue(inFilter.evaluateRule(bus1));
        // Check busBarSection1 must be in the list
        assertTrue(inFilter.evaluateRule(busBarSection1));

        // Build a filter AND with only an IN operator for NOMINAL_VOLTAGE
        NumberExpertRule numberInRule = NumberExpertRule.builder().values(Set.of(13.0, 17.0))
                .field(FieldType.NOMINAL_VOLTAGE).operator(OperatorType.IN).build();
        inFilter = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(List.of(numberInRule)).build();

        // Check bus1 must be in the list
        assertTrue(inFilter.evaluateRule(bus1));
        // Check busBarSection1 must be in the list
        assertTrue(inFilter.evaluateRule(busBarSection1));

        // Build a filter AND with only an IN operator for COUNTRY
        EnumExpertRule enumInRule = EnumExpertRule.builder().values(Set.of(Country.FR.name(), Country.GB.name()))
                .field(FieldType.COUNTRY).operator(OperatorType.IN).build();
        inFilter = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(List.of(enumInRule)).build();

        // Check bus1 must be in the list
        assertTrue(inFilter.evaluateRule(bus1));
        // Check busBarSection1 must be in the list
        assertTrue(inFilter.evaluateRule(busBarSection1));

        // --- Test NOT_IN Operator --- //

        // Mock for 2nd bus
        Bus bus2 = Mockito.mock(Bus.class);
        Mockito.when(bus2.getType()).thenReturn(IdentifiableType.BUS);
        Mockito.when(bus2.getId()).thenReturn("BUS_ID_2");
        Mockito.when(bus2.getNameOrId()).thenReturn("BUS_NAME_2");

        Substation substation2 = Mockito.mock(Substation.class);
        Mockito.when(substation2.getCountry()).thenReturn(Optional.of(Country.GE));

        VoltageLevel voltageLevel2 = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel2.getId()).thenReturn("VL2");
        Mockito.when(voltageLevel2.getNominalV()).thenReturn(15.0);

        Mockito.when(voltageLevel2.getSubstation()).thenReturn(Optional.of(substation2));

        Mockito.when(bus2.getVoltageLevel()).thenReturn(voltageLevel2);

        // Mock for 2nd busBarSection
        BusbarSection busBarSection2 = Mockito.mock(BusbarSection.class);
        Mockito.when(busBarSection2.getType()).thenReturn(IdentifiableType.BUSBAR_SECTION);
        Mockito.when(busBarSection2.getId()).thenReturn("BUS_BAR_SECTION_ID_2");
        Mockito.when(busBarSection2.getNameOrId()).thenReturn("BUS_BAR_SECTION_NAME_2");

        Terminal terminal2 = Mockito.mock(Terminal.class);
        Mockito.when(terminal2.getVoltageLevel()).thenReturn(voltageLevel2);
        Mockito.when(busBarSection2.getTerminal()).thenReturn(terminal2);

        // Build a filter AND with only a NOT_IN operator for VOLTAGE_LEVEL_ID
        StringExpertRule stringNotInRule = StringExpertRule.builder().values(Set.of("VL1", "VL3"))
                .field(FieldType.VOLTAGE_LEVEL_ID).operator(OperatorType.NOT_IN).build();
        CombinatorExpertRule notInFilter = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(List.of(stringNotInRule)).build();

        // Check bus2 must be not in the list
        assertTrue(notInFilter.evaluateRule(bus2));
        // Check busBarSection2 must be not in the list
        assertTrue(notInFilter.evaluateRule(busBarSection2));

        // Build a filter AND with only a NOT_IN operator for NOMINAL_VOLTAGE
        NumberExpertRule numberNotInRule = NumberExpertRule.builder().values(Set.of(13.0, 17.0))
                .field(FieldType.NOMINAL_VOLTAGE).operator(OperatorType.NOT_IN).build();
        notInFilter = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(List.of(numberNotInRule)).build();

        // Check bus2 must be not in the list
        assertTrue(notInFilter.evaluateRule(bus2));
        // Check busBarSection2 must be not in the list
        assertTrue(notInFilter.evaluateRule(busBarSection2));

        // Build a filter AND with only a NOT_IN operator for COUNTRY
        EnumExpertRule enumNotInRule = EnumExpertRule.builder().values(Set.of(Country.FR.name(), Country.GB.name()))
                .field(FieldType.COUNTRY).operator(OperatorType.NOT_IN).build();
        notInFilter = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(List.of(enumNotInRule)).build();

        // Check bus2 must be not in the list
        assertTrue(notInFilter.evaluateRule(bus2));
        // Check busBarSection2 must be not in the list
        assertTrue(notInFilter.evaluateRule(busBarSection2));

    }

    @Test
    void testEvaluateExpertFilterWithException() {

        Network network = Mockito.mock(Network.class);
        Mockito.when(network.getType()).thenReturn(IdentifiableType.NETWORK);

        VoltageLevel voltageLevel = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel.getType()).thenReturn(IdentifiableType.VOLTAGE_LEVEL);

        Load load = Mockito.mock(Load.class);
        Mockito.when(load.getType()).thenReturn(IdentifiableType.LOAD);

        Generator generator = Mockito.mock(Generator.class);
        Mockito.when(generator.getType()).thenReturn(IdentifiableType.GENERATOR);

        Bus bus = Mockito.mock(Bus.class);
        Mockito.when(bus.getType()).thenReturn(IdentifiableType.BUS);

        BusbarSection busbarSection = Mockito.mock(BusbarSection.class);
        Mockito.when(busbarSection.getType()).thenReturn(IdentifiableType.BUSBAR_SECTION);

        // --- Test with whatever operator with exception --- //

        // Build a filter AND with only a NOT_IN operator for UNKNOWN
        EnumExpertRule enumNotInRule = EnumExpertRule.builder().values(Set.of(Country.FR.name(), Country.GB.name()))
                .field(FieldType.UNKNOWN).operator(OperatorType.NOT_IN).build();
        CombinatorExpertRule notInFilter = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(List.of(enumNotInRule)).build();

        // type not supported
        assertThrows(PowsyblException.class, () -> notInFilter.evaluateRule(network));

        // field not supported

        assertThrows(PowsyblException.class, () -> notInFilter.evaluateRule(voltageLevel));

        assertThrows(PowsyblException.class, () -> notInFilter.evaluateRule(load));

        assertThrows(PowsyblException.class, () -> notInFilter.evaluateRule(generator));

        assertThrows(PowsyblException.class, () -> notInFilter.evaluateRule(bus));

        assertThrows(PowsyblException.class, () -> notInFilter.evaluateRule(busbarSection));
    }

}
