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

        Battery battery = Mockito.mock(Battery.class);
        Mockito.when(battery.getType()).thenReturn(IdentifiableType.BATTERY);

        return Stream.of(
                // --- Test an unsupported field for each equipment --- //
                Arguments.of(IS, FieldType.RATED_S, network, PowsyblException.class),
                Arguments.of(IS, FieldType.RATED_S, voltageLevel, PowsyblException.class),
                Arguments.of(IS, FieldType.P0, generator, PowsyblException.class),
                Arguments.of(IS, FieldType.RATED_S, load, PowsyblException.class),
                Arguments.of(IS, FieldType.RATED_S, bus, PowsyblException.class),
                Arguments.of(IS, FieldType.RATED_S, busbarSection, PowsyblException.class),
                Arguments.of(IS, FieldType.RATED_S, battery, PowsyblException.class),

                // --- Test an unsupported operator for this rule type --- //
                Arguments.of(EQUALS, FieldType.ID, generator, PowsyblException.class)
        );
    }

    @ParameterizedTest
    @MethodSource({
        "provideArgumentsForGeneratorTest",
        "provideArgumentsForLoadTest",
        "provideArgumentsForBusTest",
        "provideArgumentsForBusBarSectionTest",
        "provideArgumentsForBatteryTest"
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

        // for testing none EXISTS
        Generator gen1 = Mockito.mock(Generator.class);
        Mockito.when(gen1.getType()).thenReturn(IdentifiableType.GENERATOR);
        // VoltageLevel fields
        VoltageLevel voltageLevel1 = Mockito.mock(VoltageLevel.class);
        Terminal terminal1 = Mockito.mock(Terminal.class);
        Mockito.when(terminal1.getVoltageLevel()).thenReturn(voltageLevel1);
        Mockito.when(gen1.getTerminal()).thenReturn(terminal1);

        return Stream.of(
                // --- IS --- //
                // Common fields
                Arguments.of(IS, FieldType.ID, "id", null, gen, true),
                Arguments.of(IS, FieldType.ID, "id_1", null, gen, false),
                Arguments.of(IS, FieldType.NAME, "name", null, gen, true),
                Arguments.of(IS, FieldType.NAME, "name_1", null, gen, false),
                // VoltageLevel fields
                Arguments.of(IS, FieldType.VOLTAGE_LEVEL_ID, "vl", null, gen, true),
                Arguments.of(IS, FieldType.VOLTAGE_LEVEL_ID, "vl_1", null, gen, false),

                // --- CONTAINS --- //
                // Common fields
                Arguments.of(CONTAINS, FieldType.ID, "i", null, gen, true),
                Arguments.of(CONTAINS, FieldType.ID, "ii", null, gen, false),
                Arguments.of(CONTAINS, FieldType.NAME, "nam", null, gen, true),
                Arguments.of(CONTAINS, FieldType.NAME, "namm", null, gen, false),
                // VoltageLevel fields
                Arguments.of(CONTAINS, FieldType.VOLTAGE_LEVEL_ID, "v", null, gen, true),
                Arguments.of(CONTAINS, FieldType.VOLTAGE_LEVEL_ID, "vv", null, gen, false),

                // --- BEGINS_WITH --- //
                // Common fields
                Arguments.of(BEGINS_WITH, FieldType.ID, "i", null, gen, true),
                Arguments.of(BEGINS_WITH, FieldType.ID, "j", null, gen, false),
                Arguments.of(BEGINS_WITH, FieldType.NAME, "n", null, gen, true),
                Arguments.of(BEGINS_WITH, FieldType.NAME, "m", null, gen, false),
                // VoltageLevel fields
                Arguments.of(BEGINS_WITH, FieldType.VOLTAGE_LEVEL_ID, "v", null, gen, true),
                Arguments.of(BEGINS_WITH, FieldType.VOLTAGE_LEVEL_ID, "s", null, gen, false),

                // --- ENDS_WITH --- //
                // Common fields
                Arguments.of(ENDS_WITH, FieldType.ID, "d", null, gen, true),
                Arguments.of(ENDS_WITH, FieldType.ID, "e", null, gen, false),
                Arguments.of(ENDS_WITH, FieldType.NAME, "e", null, gen, true),
                Arguments.of(ENDS_WITH, FieldType.NAME, "f", null, gen, false),
                // VoltageLevel fields
                Arguments.of(ENDS_WITH, FieldType.VOLTAGE_LEVEL_ID, "l", null, gen, true),
                Arguments.of(ENDS_WITH, FieldType.VOLTAGE_LEVEL_ID, "m", null, gen, false),

                // --- EXISTS --- //
                // Common fields
                Arguments.of(EXISTS, FieldType.ID, null, null, gen, true),
                Arguments.of(EXISTS, FieldType.ID, null, null, gen1, false),
                Arguments.of(EXISTS, FieldType.NAME, null, null, gen, true),
                Arguments.of(EXISTS, FieldType.NAME, null, null, gen1, false),
                // VoltageLevel fields
                Arguments.of(EXISTS, FieldType.VOLTAGE_LEVEL_ID, null, null, gen, true),
                Arguments.of(EXISTS, FieldType.VOLTAGE_LEVEL_ID, null, null, gen1, false),

                // --- IN --- //
                // Common fields
                Arguments.of(IN, FieldType.ID, null, Set.of("ID", "ID_2"), gen, true),
                Arguments.of(IN, FieldType.ID, null, Set.of("ID_2", "ID_3"), gen, false),
                Arguments.of(IN, FieldType.NAME, null, Set.of("NAME", "NAME_2"), gen, true),
                Arguments.of(IN, FieldType.NAME, null, Set.of("NAME_2", "NAME_3"), gen, false),
                // VoltageLevel fields
                Arguments.of(IN, FieldType.VOLTAGE_LEVEL_ID, null, Set.of("VL", "VL_2"), gen, true),
                Arguments.of(IN, FieldType.VOLTAGE_LEVEL_ID, null, Set.of("VL_2", "VL_3"), gen, false),

                // --- NOT_IN --- //
                // Common fields
                Arguments.of(NOT_IN, FieldType.ID, null, Set.of("ID_2", "ID_3"), gen, true),
                Arguments.of(NOT_IN, FieldType.ID, null, Set.of("ID", "ID_2"), gen, false),
                Arguments.of(NOT_IN, FieldType.NAME, null, Set.of("NAME_2", "NAME_3"), gen, true),
                Arguments.of(NOT_IN, FieldType.NAME, null, Set.of("NAME", "NAME_2"), gen, false),
                // VoltageLevel fields
                Arguments.of(NOT_IN, FieldType.VOLTAGE_LEVEL_ID, null, Set.of("VL_2", "VL_3"), gen, true),
                Arguments.of(NOT_IN, FieldType.VOLTAGE_LEVEL_ID, null, Set.of("VL", "VL_2"), gen, false)

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

        // for testing none EXISTS
        Load load1 = Mockito.mock(Load.class);
        Mockito.when(load1.getType()).thenReturn(IdentifiableType.LOAD);
        // VoltageLevel fields
        VoltageLevel voltageLevel1 = Mockito.mock(VoltageLevel.class);
        Terminal terminal1 = Mockito.mock(Terminal.class);
        Mockito.when(terminal1.getVoltageLevel()).thenReturn(voltageLevel1);
        Mockito.when(load1.getTerminal()).thenReturn(terminal1);

        return Stream.of(
                // --- IS --- //
                // Common fields
                Arguments.of(IS, FieldType.ID, "id", null, load, true),
                Arguments.of(IS, FieldType.ID, "id_1", null, load, false),
                Arguments.of(IS, FieldType.NAME, "name", null, load, true),
                Arguments.of(IS, FieldType.NAME, "name_1", null, load, false),
                // VoltageLevel fields
                Arguments.of(IS, FieldType.VOLTAGE_LEVEL_ID, "vl", null, load, true),
                Arguments.of(IS, FieldType.VOLTAGE_LEVEL_ID, "vl_1", null, load, false),

                // --- CONTAINS --- //
                // Common fields
                Arguments.of(CONTAINS, FieldType.ID, "i", null, load, true),
                Arguments.of(CONTAINS, FieldType.ID, "ii", null, load, false),
                Arguments.of(CONTAINS, FieldType.NAME, "nam", null, load, true),
                Arguments.of(CONTAINS, FieldType.NAME, "namm", null, load, false),
                // VoltageLevel fields
                Arguments.of(CONTAINS, FieldType.VOLTAGE_LEVEL_ID, "v", null, load, true),
                Arguments.of(CONTAINS, FieldType.VOLTAGE_LEVEL_ID, "vv", null, load, false),

                // --- BEGINS_WITH --- //
                // Common fields
                Arguments.of(BEGINS_WITH, FieldType.ID, "i", null, load, true),
                Arguments.of(BEGINS_WITH, FieldType.ID, "j", null, load, false),
                Arguments.of(BEGINS_WITH, FieldType.NAME, "n", null, load, true),
                Arguments.of(BEGINS_WITH, FieldType.NAME, "m", null, load, false),
                // VoltageLevel fields
                Arguments.of(BEGINS_WITH, FieldType.VOLTAGE_LEVEL_ID, "v", null, load, true),
                Arguments.of(BEGINS_WITH, FieldType.VOLTAGE_LEVEL_ID, "s", null, load, false),

                // --- ENDS_WITH --- //
                // Common fields
                Arguments.of(ENDS_WITH, FieldType.ID, "d", null, load, true),
                Arguments.of(ENDS_WITH, FieldType.ID, "e", null, load, false),
                Arguments.of(ENDS_WITH, FieldType.NAME, "e", null, load, true),
                Arguments.of(ENDS_WITH, FieldType.NAME, "f", null, load, false),
                // VoltageLevel fields
                Arguments.of(ENDS_WITH, FieldType.VOLTAGE_LEVEL_ID, "l", null, load, true),
                Arguments.of(ENDS_WITH, FieldType.VOLTAGE_LEVEL_ID, "m", null, load, false),

                // --- EXISTS --- //
                // Common fields
                Arguments.of(EXISTS, FieldType.ID, null, null, load, true),
                Arguments.of(EXISTS, FieldType.ID, null, null, load1, false),
                Arguments.of(EXISTS, FieldType.NAME, null, null, load, true),
                Arguments.of(EXISTS, FieldType.NAME, null, null, load1, false),
                // VoltageLevel fields
                Arguments.of(EXISTS, FieldType.VOLTAGE_LEVEL_ID, null, null, load, true),
                Arguments.of(EXISTS, FieldType.VOLTAGE_LEVEL_ID, null, null, load1, false),

                // --- IN --- //
                // Common fields
                Arguments.of(IN, FieldType.ID, null, Set.of("ID", "ID_2"), load, true),
                Arguments.of(IN, FieldType.ID, null, Set.of("ID_2", "ID_3"), load, false),
                Arguments.of(IN, FieldType.NAME, null, Set.of("NAME", "NAME_2"), load, true),
                Arguments.of(IN, FieldType.NAME, null, Set.of("NAME_2", "NAME_3"), load, false),
                // VoltageLevel fields
                Arguments.of(IN, FieldType.VOLTAGE_LEVEL_ID, null, Set.of("VL", "VL_2"), load, true),
                Arguments.of(IN, FieldType.VOLTAGE_LEVEL_ID, null, Set.of("VL_2", "VL_3"), load, false),

                // --- NOT_IN --- //
                // Common fields
                Arguments.of(NOT_IN, FieldType.ID, null, Set.of("ID_2", "ID_3"), load, true),
                Arguments.of(NOT_IN, FieldType.ID, null, Set.of("ID", "ID_2"), load, false),
                Arguments.of(NOT_IN, FieldType.NAME, null, Set.of("NAME_2", "NAME_3"), load, true),
                Arguments.of(NOT_IN, FieldType.NAME, null, Set.of("NAME", "NAME_2"), load, false),
                // VoltageLevel fields
                Arguments.of(NOT_IN, FieldType.VOLTAGE_LEVEL_ID, null, Set.of("VL_2", "VL_3"), load, true),
                Arguments.of(NOT_IN, FieldType.VOLTAGE_LEVEL_ID, null, Set.of("VL", "VL_2"), load, false)

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

        // for testing none EXISTS
        Bus bus1 = Mockito.mock(Bus.class);
        Mockito.when(bus1.getType()).thenReturn(IdentifiableType.BUS);
        // VoltageLevel fields
        VoltageLevel voltageLevel1 = Mockito.mock(VoltageLevel.class);
        Mockito.when(bus1.getVoltageLevel()).thenReturn(voltageLevel1);

        return Stream.of(
                // --- IS --- //
                // Common fields
                Arguments.of(IS, FieldType.ID, "id", null, bus, true),
                Arguments.of(IS, FieldType.ID, "id_1", null, bus, false),
                Arguments.of(IS, FieldType.NAME, "name", null, bus, true),
                Arguments.of(IS, FieldType.NAME, "name_1", null, bus, false),
                // VoltageLevel fields
                Arguments.of(IS, FieldType.VOLTAGE_LEVEL_ID, "vl", null, bus, true),
                Arguments.of(IS, FieldType.VOLTAGE_LEVEL_ID, "vl_1", null, bus, false),

                // --- CONTAINS --- //
                // Common fields
                Arguments.of(CONTAINS, FieldType.ID, "i", null, bus, true),
                Arguments.of(CONTAINS, FieldType.ID, "ii", null, bus, false),
                Arguments.of(CONTAINS, FieldType.NAME, "nam", null, bus, true),
                Arguments.of(CONTAINS, FieldType.NAME, "namm", null, bus, false),
                // VoltageLevel fields
                Arguments.of(CONTAINS, FieldType.VOLTAGE_LEVEL_ID, "v", null, bus, true),
                Arguments.of(CONTAINS, FieldType.VOLTAGE_LEVEL_ID, "vv", null, bus, false),

                // --- BEGINS_WITH --- //
                // Common fields
                Arguments.of(BEGINS_WITH, FieldType.ID, "i", null, bus, true),
                Arguments.of(BEGINS_WITH, FieldType.ID, "j", null, bus, false),
                Arguments.of(BEGINS_WITH, FieldType.NAME, "n", null, bus, true),
                Arguments.of(BEGINS_WITH, FieldType.NAME, "m", null, bus, false),
                // VoltageLevel fields
                Arguments.of(BEGINS_WITH, FieldType.VOLTAGE_LEVEL_ID, "v", null, bus, true),
                Arguments.of(BEGINS_WITH, FieldType.VOLTAGE_LEVEL_ID, "s", null, bus, false),

                // --- ENDS_WITH --- //
                // Common fields
                Arguments.of(ENDS_WITH, FieldType.ID, "d", null, bus, true),
                Arguments.of(ENDS_WITH, FieldType.ID, "e", null, bus, false),
                Arguments.of(ENDS_WITH, FieldType.NAME, "e", null, bus, true),
                Arguments.of(ENDS_WITH, FieldType.NAME, "f", null, bus, false),
                // VoltageLevel fields
                Arguments.of(ENDS_WITH, FieldType.VOLTAGE_LEVEL_ID, "l", null, bus, true),
                Arguments.of(ENDS_WITH, FieldType.VOLTAGE_LEVEL_ID, "m", null, bus, false),

                // --- EXISTS --- //
                // Common fields
                Arguments.of(EXISTS, FieldType.ID, null, null, bus, true),
                Arguments.of(EXISTS, FieldType.ID, null, null, bus1, false),
                Arguments.of(EXISTS, FieldType.NAME, null, null, bus, true),
                Arguments.of(EXISTS, FieldType.NAME, null, null, bus1, false),
                // VoltageLevel fields
                Arguments.of(EXISTS, FieldType.VOLTAGE_LEVEL_ID, null, null, bus, true),
                Arguments.of(EXISTS, FieldType.VOLTAGE_LEVEL_ID, null, null, bus1, false),

                // --- IN --- //
                // Common fields
                Arguments.of(IN, FieldType.ID, null, Set.of("ID", "ID_2"), bus, true),
                Arguments.of(IN, FieldType.ID, null, Set.of("ID_2", "ID_3"), bus, false),
                Arguments.of(IN, FieldType.NAME, null, Set.of("NAME", "NAME_2"), bus, true),
                Arguments.of(IN, FieldType.NAME, null, Set.of("NAME_2", "NAME_3"), bus, false),
                // VoltageLevel fields
                Arguments.of(IN, FieldType.VOLTAGE_LEVEL_ID, null, Set.of("VL", "VL_2"), bus, true),
                Arguments.of(IN, FieldType.VOLTAGE_LEVEL_ID, null, Set.of("VL_2", "VL_3"), bus, false),

                // --- NOT_IN --- //
                // Common fields
                Arguments.of(NOT_IN, FieldType.ID, null, Set.of("ID_2", "ID_3"), bus, true),
                Arguments.of(NOT_IN, FieldType.ID, null, Set.of("ID", "ID_2"), bus, false),
                Arguments.of(NOT_IN, FieldType.NAME, null, Set.of("NAME_2", "NAME_3"), bus, true),
                Arguments.of(NOT_IN, FieldType.NAME, null, Set.of("NAME", "NAME_2"), bus, false),
                // VoltageLevel fields
                Arguments.of(NOT_IN, FieldType.VOLTAGE_LEVEL_ID, null, Set.of("VL_2", "VL_3"), bus, true),
                Arguments.of(NOT_IN, FieldType.VOLTAGE_LEVEL_ID, null, Set.of("VL", "VL_2"), bus, false)

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

        // for testing none EXISTS
        BusbarSection busbarSection1 = Mockito.mock(BusbarSection.class);
        Mockito.when(busbarSection1.getType()).thenReturn(IdentifiableType.BUSBAR_SECTION);
        // VoltageLevel fields
        VoltageLevel voltageLevel1 = Mockito.mock(VoltageLevel.class);
        Terminal terminal1 = Mockito.mock(Terminal.class);
        Mockito.when(terminal1.getVoltageLevel()).thenReturn(voltageLevel1);
        Mockito.when(busbarSection1.getTerminal()).thenReturn(terminal1);

        return Stream.of(
                // --- IS --- //
                // Common fields
                Arguments.of(IS, FieldType.ID, "id", null, busbarSection, true),
                Arguments.of(IS, FieldType.ID, "id_1", null, busbarSection, false),
                Arguments.of(IS, FieldType.NAME, "name", null, busbarSection, true),
                Arguments.of(IS, FieldType.NAME, "name_1", null, busbarSection, false),
                // VoltageLevel fields
                Arguments.of(IS, FieldType.VOLTAGE_LEVEL_ID, "vl", null, busbarSection, true),
                Arguments.of(IS, FieldType.VOLTAGE_LEVEL_ID, "vl_1", null, busbarSection, false),

                // --- CONTAINS --- //
                // Common fields
                Arguments.of(CONTAINS, FieldType.ID, "i", null, busbarSection, true),
                Arguments.of(CONTAINS, FieldType.ID, "ii", null, busbarSection, false),
                Arguments.of(CONTAINS, FieldType.NAME, "nam", null, busbarSection, true),
                Arguments.of(CONTAINS, FieldType.NAME, "namm", null, busbarSection, false),
                // VoltageLevel fields
                Arguments.of(CONTAINS, FieldType.VOLTAGE_LEVEL_ID, "v", null, busbarSection, true),
                Arguments.of(CONTAINS, FieldType.VOLTAGE_LEVEL_ID, "vv", null, busbarSection, false),

                // --- BEGINS_WITH --- //
                // Common fields
                Arguments.of(BEGINS_WITH, FieldType.ID, "i", null, busbarSection, true),
                Arguments.of(BEGINS_WITH, FieldType.ID, "j", null, busbarSection, false),
                Arguments.of(BEGINS_WITH, FieldType.NAME, "n", null, busbarSection, true),
                Arguments.of(BEGINS_WITH, FieldType.NAME, "m", null, busbarSection, false),
                // VoltageLevel fields
                Arguments.of(BEGINS_WITH, FieldType.VOLTAGE_LEVEL_ID, "v", null, busbarSection, true),
                Arguments.of(BEGINS_WITH, FieldType.VOLTAGE_LEVEL_ID, "s", null, busbarSection, false),

                // --- ENDS_WITH --- //
                // Common fields
                Arguments.of(ENDS_WITH, FieldType.ID, "d", null, busbarSection, true),
                Arguments.of(ENDS_WITH, FieldType.ID, "e", null, busbarSection, false),
                Arguments.of(ENDS_WITH, FieldType.NAME, "e", null, busbarSection, true),
                Arguments.of(ENDS_WITH, FieldType.NAME, "f", null, busbarSection, false),
                // VoltageLevel fields
                Arguments.of(ENDS_WITH, FieldType.VOLTAGE_LEVEL_ID, "l", null, busbarSection, true),
                Arguments.of(ENDS_WITH, FieldType.VOLTAGE_LEVEL_ID, "m", null, busbarSection, false),

                // --- EXISTS --- //
                // Common fields
                Arguments.of(EXISTS, FieldType.ID, null, null, busbarSection, true),
                Arguments.of(EXISTS, FieldType.ID, null, null, busbarSection1, false),
                Arguments.of(EXISTS, FieldType.NAME, null, null, busbarSection, true),
                Arguments.of(EXISTS, FieldType.NAME, null, null, busbarSection1, false),
                // VoltageLevel fields
                Arguments.of(EXISTS, FieldType.VOLTAGE_LEVEL_ID, null, null, busbarSection, true),
                Arguments.of(EXISTS, FieldType.VOLTAGE_LEVEL_ID, null, null, busbarSection1, false),

                // --- IN --- //
                // Common fields
                Arguments.of(IN, FieldType.ID, null, Set.of("ID", "ID_2"), busbarSection, true),
                Arguments.of(IN, FieldType.ID, null, Set.of("ID_2", "ID_3"), busbarSection, false),
                Arguments.of(IN, FieldType.NAME, null, Set.of("NAME", "NAME_2"), busbarSection, true),
                Arguments.of(IN, FieldType.NAME, null, Set.of("NAME_2", "NAME_3"), busbarSection, false),
                // VoltageLevel fields
                Arguments.of(IN, FieldType.VOLTAGE_LEVEL_ID, null, Set.of("VL", "VL_2"), busbarSection, true),
                Arguments.of(IN, FieldType.VOLTAGE_LEVEL_ID, null, Set.of("VL_2", "VL_3"), busbarSection, false),

                // --- NOT_IN --- //
                // Common fields
                Arguments.of(NOT_IN, FieldType.ID, null, Set.of("ID_2", "ID_3"), busbarSection, true),
                Arguments.of(NOT_IN, FieldType.ID, null, Set.of("ID", "ID_2"), busbarSection, false),
                Arguments.of(NOT_IN, FieldType.NAME, null, Set.of("NAME_2", "NAME_3"), busbarSection, true),
                Arguments.of(NOT_IN, FieldType.NAME, null, Set.of("NAME", "NAME_2"), busbarSection, false),
                // VoltageLevel fields
                Arguments.of(NOT_IN, FieldType.VOLTAGE_LEVEL_ID, null, Set.of("VL_2", "VL_3"), busbarSection, true),
                Arguments.of(NOT_IN, FieldType.VOLTAGE_LEVEL_ID, null, Set.of("VL", "VL_2"), busbarSection, false)

        );
    }

    private static Stream<Arguments> provideArgumentsForBatteryTest() {

        Battery battery = Mockito.mock(Battery.class);
        Mockito.when(battery.getType()).thenReturn(IdentifiableType.BATTERY);
        // Common fields
        Mockito.when(battery.getId()).thenReturn("ID");
        Mockito.when(battery.getNameOrId()).thenReturn("NAME");
        // VoltageLevel fields
        VoltageLevel voltageLevel = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel.getId()).thenReturn("VL");
        Terminal terminal = Mockito.mock(Terminal.class);
        Mockito.when(terminal.getVoltageLevel()).thenReturn(voltageLevel);
        Mockito.when(battery.getTerminal()).thenReturn(terminal);

        // for testing none EXISTS
        Battery battery1 = Mockito.mock(Battery.class);
        Mockito.when(battery1.getType()).thenReturn(IdentifiableType.BATTERY);
        // VoltageLevel fields
        VoltageLevel voltageLevel1 = Mockito.mock(VoltageLevel.class);
        Terminal terminal1 = Mockito.mock(Terminal.class);
        Mockito.when(terminal1.getVoltageLevel()).thenReturn(voltageLevel1);
        Mockito.when(battery1.getTerminal()).thenReturn(terminal1);

        return Stream.of(
                // --- IS --- //
                // Common fields
                Arguments.of(IS, FieldType.ID, "id", null, battery, true),
                Arguments.of(IS, FieldType.ID, "id_1", null, battery, false),
                Arguments.of(IS, FieldType.NAME, "name", null, battery, true),
                Arguments.of(IS, FieldType.NAME, "name_1", null, battery, false),
                // VoltageLevel fields
                Arguments.of(IS, FieldType.VOLTAGE_LEVEL_ID, "vl", null, battery, true),
                Arguments.of(IS, FieldType.VOLTAGE_LEVEL_ID, "vl_1", null, battery, false),

                // --- CONTAINS --- //
                // Common fields
                Arguments.of(CONTAINS, FieldType.ID, "i", null, battery, true),
                Arguments.of(CONTAINS, FieldType.ID, "ii", null, battery, false),
                Arguments.of(CONTAINS, FieldType.NAME, "nam", null, battery, true),
                Arguments.of(CONTAINS, FieldType.NAME, "namm", null, battery, false),
                // VoltageLevel fields
                Arguments.of(CONTAINS, FieldType.VOLTAGE_LEVEL_ID, "v", null, battery, true),
                Arguments.of(CONTAINS, FieldType.VOLTAGE_LEVEL_ID, "vv", null, battery, false),

                // --- BEGINS_WITH --- //
                // Common fields
                Arguments.of(BEGINS_WITH, FieldType.ID, "i", null, battery, true),
                Arguments.of(BEGINS_WITH, FieldType.ID, "j", null, battery, false),
                Arguments.of(BEGINS_WITH, FieldType.NAME, "n", null, battery, true),
                Arguments.of(BEGINS_WITH, FieldType.NAME, "m", null, battery, false),
                // VoltageLevel fields
                Arguments.of(BEGINS_WITH, FieldType.VOLTAGE_LEVEL_ID, "v", null, battery, true),
                Arguments.of(BEGINS_WITH, FieldType.VOLTAGE_LEVEL_ID, "s", null, battery, false),

                // --- ENDS_WITH --- //
                // Common fields
                Arguments.of(ENDS_WITH, FieldType.ID, "d", null, battery, true),
                Arguments.of(ENDS_WITH, FieldType.ID, "e", null, battery, false),
                Arguments.of(ENDS_WITH, FieldType.NAME, "e", null, battery, true),
                Arguments.of(ENDS_WITH, FieldType.NAME, "f", null, battery, false),
                // VoltageLevel fields
                Arguments.of(ENDS_WITH, FieldType.VOLTAGE_LEVEL_ID, "l", null, battery, true),
                Arguments.of(ENDS_WITH, FieldType.VOLTAGE_LEVEL_ID, "m", null, battery, false),

                // --- EXISTS --- //
                // Common fields
                Arguments.of(EXISTS, FieldType.ID, null, null, battery, true),
                Arguments.of(EXISTS, FieldType.ID, null, null, battery1, false),
                Arguments.of(EXISTS, FieldType.NAME, null, null, battery, true),
                Arguments.of(EXISTS, FieldType.NAME, null, null, battery1, false),
                // VoltageLevel fields
                Arguments.of(EXISTS, FieldType.VOLTAGE_LEVEL_ID, null, null, battery, true),
                Arguments.of(EXISTS, FieldType.VOLTAGE_LEVEL_ID, null, null, battery1, false),

                // --- IN --- //
                // Common fields
                Arguments.of(IN, FieldType.ID, null, Set.of("ID", "ID_2"), battery, true),
                Arguments.of(IN, FieldType.ID, null, Set.of("ID_2", "ID_3"), battery, false),
                Arguments.of(IN, FieldType.NAME, null, Set.of("NAME", "NAME_2"), battery, true),
                Arguments.of(IN, FieldType.NAME, null, Set.of("NAME_2", "NAME_3"), battery, false),
                // VoltageLevel fields
                Arguments.of(IN, FieldType.VOLTAGE_LEVEL_ID, null, Set.of("VL", "VL_2"), battery, true),
                Arguments.of(IN, FieldType.VOLTAGE_LEVEL_ID, null, Set.of("VL_2", "VL_3"), battery, false),

                // --- NOT_IN --- //
                // Common fields
                Arguments.of(NOT_IN, FieldType.ID, null, Set.of("ID_2", "ID_3"), battery, true),
                Arguments.of(NOT_IN, FieldType.ID, null, Set.of("ID", "ID_2"), battery, false),
                Arguments.of(NOT_IN, FieldType.NAME, null, Set.of("NAME_2", "NAME_3"), battery, true),
                Arguments.of(NOT_IN, FieldType.NAME, null, Set.of("NAME", "NAME_2"), battery, false),
                // VoltageLevel fields
                Arguments.of(NOT_IN, FieldType.VOLTAGE_LEVEL_ID, null, Set.of("VL_2", "VL_3"), battery, true),
                Arguments.of(NOT_IN, FieldType.VOLTAGE_LEVEL_ID, null, Set.of("VL", "VL_2"), battery, false)

        );
    }
}
