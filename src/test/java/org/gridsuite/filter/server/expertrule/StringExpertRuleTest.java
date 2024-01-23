package org.gridsuite.filter.server.expertrule;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import org.gridsuite.filter.server.dto.expertfilter.expertrule.StringExpertRule;
import org.gridsuite.filter.server.utils.expertfilter.FieldType;
import org.gridsuite.filter.server.utils.expertfilter.OperatorType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.util.Set;
import java.util.stream.Stream;

import static org.gridsuite.filter.server.utils.expertfilter.OperatorType.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StringExpertRuleTest {

    @ParameterizedTest
    @MethodSource({
        "provideArgumentsForTestWithException"
    })
    void testEvaluateRuleWithException(OperatorType operator, FieldType field, Identifiable<?> equipment, Class expectedException) {
        StringExpertRule rule = StringExpertRule.builder().operator(operator).field(field).build();
        assertThrows(expectedException, () -> rule.evaluateRule(equipment));
    }

    static Stream<Arguments> provideArgumentsForTestWithException() {

        Network network = Mockito.mock(Network.class);
        Mockito.when(network.getType()).thenReturn(IdentifiableType.NETWORK);

        VoltageLevel voltageLevel = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel.getType()).thenReturn(IdentifiableType.VOLTAGE_LEVEL);

        Generator generator = Mockito.mock(Generator.class);
        Mockito.when(generator.getType()).thenReturn(IdentifiableType.GENERATOR);
        Mockito.when(generator.getId()).thenReturn("GEN");

        Load load = Mockito.mock(Load.class);
        Mockito.when(load.getType()).thenReturn(IdentifiableType.LOAD);

        Bus bus = Mockito.mock(Bus.class);
        Mockito.when(bus.getType()).thenReturn(IdentifiableType.BUS);

        BusbarSection busbarSection = Mockito.mock(BusbarSection.class);
        Mockito.when(busbarSection.getType()).thenReturn(IdentifiableType.BUSBAR_SECTION);

        return Stream.of(
                // --- Test with whatever operator with UNKNOWN field for an expected exception --- //
                Arguments.of(IS, FieldType.UNKNOWN, network, PowsyblException.class),
                Arguments.of(IS, FieldType.UNKNOWN, voltageLevel, PowsyblException.class),
                Arguments.of(IS, FieldType.UNKNOWN, generator, PowsyblException.class),
                Arguments.of(IS, FieldType.UNKNOWN, load, PowsyblException.class),
                Arguments.of(IS, FieldType.UNKNOWN, bus, PowsyblException.class),
                Arguments.of(IS, FieldType.UNKNOWN, busbarSection, PowsyblException.class),

                // --- Test with UNKNOWN operator with a supported field for an expected exception --- //
                Arguments.of(UNKNOWN, FieldType.ID, generator, PowsyblException.class)
        );
    }

    @ParameterizedTest
    @MethodSource({
        "provideArgumentsForGeneratorTest",
        "provideArgumentsForLoadTest",
        "provideArgumentsForBusTest",
        "provideArgumentsForBusBarSectionTest"
    })
    void testEvaluateRule(OperatorType operator, FieldType field, String value, Set<String> values, Identifiable<?> equipment, boolean expected) {
        StringExpertRule rule = StringExpertRule.builder().operator(operator).field(field).value(value).values(values).build();
        assertEquals(expected, rule.evaluateRule(equipment));
    }

    private static Stream<Arguments> provideArgumentsForGeneratorTest() {

        Generator gen = Mockito.mock(Generator.class);
        Mockito.when(gen.getType()).thenReturn(IdentifiableType.GENERATOR);
        // Common fields
        Mockito.when(gen.getId()).thenReturn("ID");
        Mockito.when(gen.getNameOrId()).thenReturn("NAME");
        // VoltageLevel fields
        VoltageLevel voltageLevel = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel.getId()).thenReturn("VL");
        Terminal terminal = Mockito.mock(Terminal.class);
        Mockito.when(terminal.getVoltageLevel()).thenReturn(voltageLevel);
        Mockito.when(gen.getTerminal()).thenReturn(terminal);

        return Stream.of(
                // --- IS --- //
                // Common fields
                Arguments.of(IS, FieldType.ID, "ID", null, gen, true),
                Arguments.of(IS, FieldType.NAME, "NAME", null, gen, true),
                // VoltageLevel fields
                Arguments.of(IS, FieldType.VOLTAGE_LEVEL_ID, "VL", null, gen, true),

                // --- CONTAINS --- //
                // Common fields
                Arguments.of(CONTAINS, FieldType.ID, "I", null, gen, true),
                Arguments.of(CONTAINS, FieldType.NAME, "NAM", null, gen, true),
                // VoltageLevel fields
                Arguments.of(CONTAINS, FieldType.VOLTAGE_LEVEL_ID, "V", null, gen, true),

                // --- BEGINS_WITH --- //
                // Common fields
                Arguments.of(BEGINS_WITH, FieldType.ID, "I", null, gen, true),
                Arguments.of(BEGINS_WITH, FieldType.NAME, "N", null, gen, true),
                // VoltageLevel fields
                Arguments.of(BEGINS_WITH, FieldType.VOLTAGE_LEVEL_ID, "V", null, gen, true),

                // --- ENDS_WITH --- //
                // Common fields
                Arguments.of(ENDS_WITH, FieldType.ID, "D", null, gen, true),
                Arguments.of(ENDS_WITH, FieldType.NAME, "E", null, gen, true),
                // VoltageLevel fields
                Arguments.of(ENDS_WITH, FieldType.VOLTAGE_LEVEL_ID, "L", null, gen, true),

                // --- EXISTS --- //
                // Common fields
                Arguments.of(EXISTS, FieldType.ID, null, null, gen, true),
                Arguments.of(EXISTS, FieldType.NAME, null, null, gen, true),
                // VoltageLevel fields
                Arguments.of(EXISTS, FieldType.VOLTAGE_LEVEL_ID, null, null, gen, true),

                // --- IN --- //
                // Common fields
                Arguments.of(IN, FieldType.ID, null, Set.of("ID", "ID_2"), gen, true),
                Arguments.of(IN, FieldType.NAME, null, Set.of("NAME", "NAME_2"), gen, true),
                // VoltageLevel fields
                Arguments.of(IN, FieldType.VOLTAGE_LEVEL_ID, null, Set.of("VL", "VL_2"), gen, true),

                // --- NOT_IN --- //
                // Common fields
                Arguments.of(NOT_IN, FieldType.ID, null, Set.of("ID_2", "ID_3"), gen, true),
                Arguments.of(NOT_IN, FieldType.NAME, null, Set.of("NAME_2", "NAME_3"), gen, true),
                // VoltageLevel fields
                Arguments.of(NOT_IN, FieldType.VOLTAGE_LEVEL_ID, null, Set.of("VL_2", "VL_3"), gen, true)

        );
    }

    private static Stream<Arguments> provideArgumentsForLoadTest() {

        Load load = Mockito.mock(Load.class);
        Mockito.when(load.getType()).thenReturn(IdentifiableType.LOAD);
        // Common fields
        Mockito.when(load.getId()).thenReturn("ID");
        Mockito.when(load.getNameOrId()).thenReturn("NAME");
        // VoltageLevel fields
        VoltageLevel voltageLevel = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel.getId()).thenReturn("VL");
        Terminal terminal = Mockito.mock(Terminal.class);
        Mockito.when(terminal.getVoltageLevel()).thenReturn(voltageLevel);
        Mockito.when(load.getTerminal()).thenReturn(terminal);

        return Stream.of(
                // --- IS --- //
                // Common fields
                Arguments.of(IS, FieldType.ID, "ID", null, load, true),
                Arguments.of(IS, FieldType.NAME, "NAME", null, load, true),
                // VoltageLevel fields
                Arguments.of(IS, FieldType.VOLTAGE_LEVEL_ID, "VL", null, load, true),

                // --- CONTAINS --- //
                // Common fields
                Arguments.of(CONTAINS, FieldType.ID, "I", null, load, true),
                Arguments.of(CONTAINS, FieldType.NAME, "NAM", null, load, true),
                // VoltageLevel fields
                Arguments.of(CONTAINS, FieldType.VOLTAGE_LEVEL_ID, "V", null, load, true),

                // --- BEGINS_WITH --- //
                // Common fields
                Arguments.of(BEGINS_WITH, FieldType.ID, "I", null, load, true),
                Arguments.of(BEGINS_WITH, FieldType.NAME, "N", null, load, true),
                // VoltageLevel fields
                Arguments.of(BEGINS_WITH, FieldType.VOLTAGE_LEVEL_ID, "V", null, load, true),

                // --- ENDS_WITH --- //
                // Common fields
                Arguments.of(ENDS_WITH, FieldType.ID, "D", null, load, true),
                Arguments.of(ENDS_WITH, FieldType.NAME, "E", null, load, true),
                // VoltageLevel fields
                Arguments.of(ENDS_WITH, FieldType.VOLTAGE_LEVEL_ID, "L", null, load, true),

                // --- EXISTS --- //
                // Common fields
                Arguments.of(EXISTS, FieldType.ID, null, null, load, true),
                Arguments.of(EXISTS, FieldType.NAME, null, null, load, true),
                // VoltageLevel fields
                Arguments.of(EXISTS, FieldType.VOLTAGE_LEVEL_ID, null, null, load, true),

                // --- IN --- //
                // Common fields
                Arguments.of(IN, FieldType.ID, null, Set.of("ID", "ID_2"), load, true),
                Arguments.of(IN, FieldType.NAME, null, Set.of("NAME", "NAME_2"), load, true),
                // VoltageLevel fields
                Arguments.of(IN, FieldType.VOLTAGE_LEVEL_ID, null, Set.of("VL", "VL_2"), load, true),

                // --- NOT_IN --- //
                // Common fields
                Arguments.of(NOT_IN, FieldType.ID, null, Set.of("ID_2", "ID_3"), load, true),
                Arguments.of(NOT_IN, FieldType.NAME, null, Set.of("NAME_2", "NAME_3"), load, true),
                // VoltageLevel fields
                Arguments.of(NOT_IN, FieldType.VOLTAGE_LEVEL_ID, null, Set.of("VL_2", "VL_3"), load, true)

        );
    }

    private static Stream<Arguments> provideArgumentsForBusTest() {

        Bus bus = Mockito.mock(Bus.class);
        Mockito.when(bus.getType()).thenReturn(IdentifiableType.BUS);
        // Common fields
        Mockito.when(bus.getId()).thenReturn("ID");
        Mockito.when(bus.getNameOrId()).thenReturn("NAME");
        // VoltageLevel fields
        VoltageLevel voltageLevel = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel.getId()).thenReturn("VL");
        Mockito.when(bus.getVoltageLevel()).thenReturn(voltageLevel);

        return Stream.of(
                // --- IS --- //
                // Common fields
                Arguments.of(IS, FieldType.ID, "ID", null, bus, true),
                Arguments.of(IS, FieldType.NAME, "NAME", null, bus, true),
                // VoltageLevel fields
                Arguments.of(IS, FieldType.VOLTAGE_LEVEL_ID, "VL", null, bus, true),

                // --- CONTAINS --- //
                // Common fields
                Arguments.of(CONTAINS, FieldType.ID, "I", null, bus, true),
                Arguments.of(CONTAINS, FieldType.NAME, "NAM", null, bus, true),
                // VoltageLevel fields
                Arguments.of(CONTAINS, FieldType.VOLTAGE_LEVEL_ID, "V", null, bus, true),

                // --- BEGINS_WITH --- //
                // Common fields
                Arguments.of(BEGINS_WITH, FieldType.ID, "I", null, bus, true),
                Arguments.of(BEGINS_WITH, FieldType.NAME, "N", null, bus, true),
                // VoltageLevel fields
                Arguments.of(BEGINS_WITH, FieldType.VOLTAGE_LEVEL_ID, "V", null, bus, true),

                // --- ENDS_WITH --- //
                // Common fields
                Arguments.of(ENDS_WITH, FieldType.ID, "D", null, bus, true),
                Arguments.of(ENDS_WITH, FieldType.NAME, "E", null, bus, true),
                // VoltageLevel fields
                Arguments.of(ENDS_WITH, FieldType.VOLTAGE_LEVEL_ID, "L", null, bus, true),

                // --- EXISTS --- //
                // Common fields
                Arguments.of(EXISTS, FieldType.ID, null, null, bus, true),
                Arguments.of(EXISTS, FieldType.NAME, null, null, bus, true),
                // VoltageLevel fields
                Arguments.of(EXISTS, FieldType.VOLTAGE_LEVEL_ID, null, null, bus, true),

                // --- IN --- //
                // Common fields
                Arguments.of(IN, FieldType.ID, null, Set.of("ID", "ID_2"), bus, true),
                Arguments.of(IN, FieldType.NAME, null, Set.of("NAME", "NAME_2"), bus, true),
                // VoltageLevel fields
                Arguments.of(IN, FieldType.VOLTAGE_LEVEL_ID, null, Set.of("VL", "VL_2"), bus, true),

                // --- NOT_IN --- //
                // Common fields
                Arguments.of(NOT_IN, FieldType.ID, null, Set.of("ID_2", "ID_3"), bus, true),
                Arguments.of(NOT_IN, FieldType.NAME, null, Set.of("NAME_2", "NAME_3"), bus, true),
                // VoltageLevel fields
                Arguments.of(NOT_IN, FieldType.VOLTAGE_LEVEL_ID, null, Set.of("VL_2", "VL_3"), bus, true)

        );
    }

    private static Stream<Arguments> provideArgumentsForBusBarSectionTest() {

        BusbarSection busbarSection = Mockito.mock(BusbarSection.class);
        Mockito.when(busbarSection.getType()).thenReturn(IdentifiableType.BUSBAR_SECTION);
        // Common fields
        Mockito.when(busbarSection.getId()).thenReturn("ID");
        Mockito.when(busbarSection.getNameOrId()).thenReturn("NAME");
        // VoltageLevel fields
        VoltageLevel voltageLevel = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel.getId()).thenReturn("VL");
        Terminal terminal = Mockito.mock(Terminal.class);
        Mockito.when(terminal.getVoltageLevel()).thenReturn(voltageLevel);
        Mockito.when(busbarSection.getTerminal()).thenReturn(terminal);

        return Stream.of(
                // --- IS --- //
                // Common fields
                Arguments.of(IS, FieldType.ID, "ID", null, busbarSection, true),
                Arguments.of(IS, FieldType.NAME, "NAME", null, busbarSection, true),
                // VoltageLevel fields
                Arguments.of(IS, FieldType.VOLTAGE_LEVEL_ID, "VL", null, busbarSection, true),

                // --- CONTAINS --- //
                // Common fields
                Arguments.of(CONTAINS, FieldType.ID, "I", null, busbarSection, true),
                Arguments.of(CONTAINS, FieldType.NAME, "NAM", null, busbarSection, true),
                // VoltageLevel fields
                Arguments.of(CONTAINS, FieldType.VOLTAGE_LEVEL_ID, "V", null, busbarSection, true),

                // --- BEGINS_WITH --- //
                // Common fields
                Arguments.of(BEGINS_WITH, FieldType.ID, "I", null, busbarSection, true),
                Arguments.of(BEGINS_WITH, FieldType.NAME, "N", null, busbarSection, true),
                // VoltageLevel fields
                Arguments.of(BEGINS_WITH, FieldType.VOLTAGE_LEVEL_ID, "V", null, busbarSection, true),

                // --- ENDS_WITH --- //
                // Common fields
                Arguments.of(ENDS_WITH, FieldType.ID, "D", null, busbarSection, true),
                Arguments.of(ENDS_WITH, FieldType.NAME, "E", null, busbarSection, true),
                // VoltageLevel fields
                Arguments.of(ENDS_WITH, FieldType.VOLTAGE_LEVEL_ID, "L", null, busbarSection, true),

                // --- EXISTS --- //
                // Common fields
                Arguments.of(EXISTS, FieldType.ID, null, null, busbarSection, true),
                Arguments.of(EXISTS, FieldType.NAME, null, null, busbarSection, true),
                // VoltageLevel fields
                Arguments.of(EXISTS, FieldType.VOLTAGE_LEVEL_ID, null, null, busbarSection, true),

                // --- IN --- //
                // Common fields
                Arguments.of(IN, FieldType.ID, null, Set.of("ID", "ID_2"), busbarSection, true),
                Arguments.of(IN, FieldType.NAME, null, Set.of("NAME", "NAME_2"), busbarSection, true),
                // VoltageLevel fields
                Arguments.of(IN, FieldType.VOLTAGE_LEVEL_ID, null, Set.of("VL", "VL_2"), busbarSection, true),

                // --- NOT_IN --- //
                // Common fields
                Arguments.of(NOT_IN, FieldType.ID, null, Set.of("ID_2", "ID_3"), busbarSection, true),
                Arguments.of(NOT_IN, FieldType.NAME, null, Set.of("NAME_2", "NAME_3"), busbarSection, true),
                // VoltageLevel fields
                Arguments.of(NOT_IN, FieldType.VOLTAGE_LEVEL_ID, null, Set.of("VL_2", "VL_3"), busbarSection, true)

        );
    }
}
