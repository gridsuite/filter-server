package org.gridsuite.filter.server.expertrule;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.GeneratorStartup;
import org.gridsuite.filter.server.dto.expertfilter.expertrule.NumberExpertRule;
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
import static org.mockito.ArgumentMatchers.any;

class NumberExpertRuleTest {

    @ParameterizedTest
    @MethodSource({
        "provideArgumentsForTestWithException"
    })
    void testEvaluateRuleWithException(OperatorType operator, FieldType field, Identifiable<?> equipment, Class expectedException) {
        NumberExpertRule rule = NumberExpertRule.builder().operator(operator).field(field).build();
        assertThrows(expectedException, () -> rule.evaluateRule(equipment));
    }

    static Stream<Arguments> provideArgumentsForTestWithException() {

        Network network = Mockito.mock(Network.class);
        Mockito.when(network.getType()).thenReturn(IdentifiableType.NETWORK);

        VoltageLevel voltageLevel = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel.getType()).thenReturn(IdentifiableType.VOLTAGE_LEVEL);

        Generator generator = Mockito.mock(Generator.class);
        Mockito.when(generator.getType()).thenReturn(IdentifiableType.GENERATOR);
        Mockito.when(generator.getMinP()).thenReturn(-500.0);

        Load load = Mockito.mock(Load.class);
        Mockito.when(load.getType()).thenReturn(IdentifiableType.LOAD);

        Bus bus = Mockito.mock(Bus.class);
        Mockito.when(bus.getType()).thenReturn(IdentifiableType.BUS);

        BusbarSection busbarSection = Mockito.mock(BusbarSection.class);
        Mockito.when(busbarSection.getType()).thenReturn(IdentifiableType.BUSBAR_SECTION);

        return Stream.of(
                // --- Test with whatever operator with UNKNOWN field for an expected exception --- //
                Arguments.of(EQUALS, FieldType.UNKNOWN, network, PowsyblException.class),
                Arguments.of(EQUALS, FieldType.UNKNOWN, voltageLevel, PowsyblException.class),
                Arguments.of(EQUALS, FieldType.UNKNOWN, generator, PowsyblException.class),
                Arguments.of(EQUALS, FieldType.UNKNOWN, load, PowsyblException.class),
                Arguments.of(EQUALS, FieldType.UNKNOWN, bus, PowsyblException.class),
                Arguments.of(EQUALS, FieldType.UNKNOWN, busbarSection, PowsyblException.class),

                // --- Test with UNKNOWN operator with a supported field for an expected exception --- //
                Arguments.of(UNKNOWN, FieldType.MIN_P, generator, PowsyblException.class)
        );
    }

    @ParameterizedTest
    @MethodSource({
        "provideArgumentsForGeneratorTest",
        "provideArgumentsForLoadTest",
        "provideArgumentsForBusTest",
        "provideArgumentsForBusBarSectionTest"
    })
    void testEvaluateRule(OperatorType operator, FieldType field, Double value, Set<Double> values, Identifiable<?> equipment, boolean expected) {
        NumberExpertRule rule = NumberExpertRule.builder().operator(operator).field(field).value(value).values(values).build();
        assertEquals(expected, rule.evaluateRule(equipment));
    }

    private static Stream<Arguments> provideArgumentsForGeneratorTest() {

        Generator gen = Mockito.mock(Generator.class);
        Mockito.when(gen.getType()).thenReturn(IdentifiableType.GENERATOR);
        // Generator fields
        Mockito.when(gen.getMinP()).thenReturn(-500.0);
        Mockito.when(gen.getMaxP()).thenReturn(100.0);
        Mockito.when(gen.getTargetV()).thenReturn(20.0);
        Mockito.when(gen.getTargetP()).thenReturn(30.0);
        Mockito.when(gen.getTargetQ()).thenReturn(40.0);
        Mockito.when(gen.getRatedS()).thenReturn(60.0);
        // GeneratorStartup extension fields
        GeneratorStartup genStartup = Mockito.mock(GeneratorStartup.class);
        Mockito.when(genStartup.getPlannedActivePowerSetpoint()).thenReturn(50.0);
        Mockito.when(genStartup.getMarginalCost()).thenReturn(50.0);
        Mockito.when(genStartup.getPlannedOutageRate()).thenReturn(50.0);
        Mockito.when(genStartup.getForcedOutageRate()).thenReturn(50.0);
        Mockito.when(gen.getExtension(any())).thenReturn(genStartup);
        // VoltageLevel fields
        VoltageLevel voltageLevel = Mockito.mock(VoltageLevel.class);
        Terminal terminal = Mockito.mock(Terminal.class);
        Mockito.when(terminal.getVoltageLevel()).thenReturn(voltageLevel);
        Mockito.when(gen.getTerminal()).thenReturn(terminal);
        Mockito.when(voltageLevel.getNominalV()).thenReturn(13.0);

        return Stream.of(
                // --- EQUALS --- //
                // Generator fields
                Arguments.of(EQUALS, FieldType.MIN_P, -500.0, null, gen, true),
                Arguments.of(EQUALS, FieldType.MAX_P, 100.0, null, gen, true),
                Arguments.of(EQUALS, FieldType.TARGET_V, 20.0, null, gen, true),
                Arguments.of(EQUALS, FieldType.TARGET_P, 30.0, null, gen, true),
                Arguments.of(EQUALS, FieldType.TARGET_Q, 40.0, null, gen, true),
                Arguments.of(EQUALS, FieldType.RATED_S, 60.0, null, gen, true),
                // GeneratorStartup extension fields
                Arguments.of(EQUALS, FieldType.PLANNED_ACTIVE_POWER_SET_POINT, 50.0, null, gen, true),
                Arguments.of(EQUALS, FieldType.MARGINAL_COST, 50.0, null, gen, true),
                Arguments.of(EQUALS, FieldType.PLANNED_OUTAGE_RATE, 50.0, null, gen, true),
                Arguments.of(EQUALS, FieldType.FORCED_OUTAGE_RATE, 50.0, null, gen, true),
                // VoltageLevel fields
                Arguments.of(EQUALS, FieldType.NOMINAL_VOLTAGE, 13.0, null, gen, true),

                // --- GREATER_OR_EQUALS --- //
                // Generator fields
                Arguments.of(GREATER_OR_EQUALS, FieldType.MIN_P, -600.0, null, gen, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.MAX_P, 90.0, null, gen, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.TARGET_V, 10.0, null, gen, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.TARGET_P, 20.0, null, gen, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.TARGET_Q, 30.0, null, gen, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.RATED_S, 50.0, null, gen, true),
                // GeneratorStartup extension fields
                Arguments.of(GREATER_OR_EQUALS, FieldType.PLANNED_ACTIVE_POWER_SET_POINT, 40.0, null, gen, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.MARGINAL_COST, 40.0, null, gen, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.PLANNED_OUTAGE_RATE, 40.0, null, gen, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.FORCED_OUTAGE_RATE, 40.0, null, gen, true),
                // VoltageLevel fields
                Arguments.of(GREATER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 12.0, null, gen, true),

                // --- GREATER --- //
                // Generator fields
                Arguments.of(GREATER, FieldType.MIN_P, -600.0, null, gen, true),
                Arguments.of(GREATER, FieldType.MAX_P, 90.0, null, gen, true),
                Arguments.of(GREATER, FieldType.TARGET_V, 10.0, null, gen, true),
                Arguments.of(GREATER, FieldType.TARGET_P, 20.0, null, gen, true),
                Arguments.of(GREATER, FieldType.TARGET_Q, 30.0, null, gen, true),
                Arguments.of(GREATER, FieldType.RATED_S, 50.0, null, gen, true),
                // GeneratorStartup extension fields
                Arguments.of(GREATER, FieldType.PLANNED_ACTIVE_POWER_SET_POINT, 40.0, null, gen, true),
                Arguments.of(GREATER, FieldType.MARGINAL_COST, 40.0, null, gen, true),
                Arguments.of(GREATER, FieldType.PLANNED_OUTAGE_RATE, 40.0, null, gen, true),
                Arguments.of(GREATER, FieldType.FORCED_OUTAGE_RATE, 40.0, null, gen, true),
                // VoltageLevel fields
                Arguments.of(GREATER, FieldType.NOMINAL_VOLTAGE, 12.0, null, gen, true),

                // --- LOWER_OR_EQUALS --- //
                // Generator fields
                Arguments.of(LOWER_OR_EQUALS, FieldType.MIN_P, -400.0, null, gen, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.MAX_P, 110.0, null, gen, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.TARGET_V, 30.0, null, gen, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.TARGET_P, 40.0, null, gen, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.TARGET_Q, 50.0, null, gen, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.RATED_S, 70.0, null, gen, true),
                // GeneratorStartup extension fields
                Arguments.of(LOWER_OR_EQUALS, FieldType.PLANNED_ACTIVE_POWER_SET_POINT, 60.0, null, gen, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.MARGINAL_COST, 60.0, null, gen, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.PLANNED_OUTAGE_RATE, 60.0, null, gen, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.FORCED_OUTAGE_RATE, 60.0, null, gen, true),
                // VoltageLevel fields
                Arguments.of(LOWER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 14.0, null, gen, true),

                // --- LOWER --- //
                // Generator fields
                Arguments.of(LOWER, FieldType.MIN_P, -400.0, null, gen, true),
                Arguments.of(LOWER, FieldType.MAX_P, 110.0, null, gen, true),
                Arguments.of(LOWER, FieldType.TARGET_V, 30.0, null, gen, true),
                Arguments.of(LOWER, FieldType.TARGET_P, 40.0, null, gen, true),
                Arguments.of(LOWER, FieldType.TARGET_Q, 50.0, null, gen, true),
                Arguments.of(LOWER, FieldType.RATED_S, 70.0, null, gen, true),
                // GeneratorStartup extension fields
                Arguments.of(LOWER, FieldType.PLANNED_ACTIVE_POWER_SET_POINT, 60.0, null, gen, true),
                Arguments.of(LOWER, FieldType.MARGINAL_COST, 60.0, null, gen, true),
                Arguments.of(LOWER, FieldType.PLANNED_OUTAGE_RATE, 60.0, null, gen, true),
                Arguments.of(LOWER, FieldType.FORCED_OUTAGE_RATE, 60.0, null, gen, true),
                // VoltageLevel fields
                Arguments.of(LOWER, FieldType.NOMINAL_VOLTAGE, 14.0, null, gen, true),

                // --- BETWEEN --- //
                // Generator fields
                Arguments.of(BETWEEN, FieldType.MIN_P, null, Set.of(-600.0, -400.0), gen, true),
                Arguments.of(BETWEEN, FieldType.MAX_P, null, Set.of(90.0, 110.0), gen, true),
                Arguments.of(BETWEEN, FieldType.TARGET_V, null, Set.of(10.0, 30.0), gen, true),
                Arguments.of(BETWEEN, FieldType.TARGET_P, null, Set.of(20.0, 40.0), gen, true),
                Arguments.of(BETWEEN, FieldType.TARGET_Q, null, Set.of(30.0, 50.0), gen, true),
                Arguments.of(BETWEEN, FieldType.RATED_S, null, Set.of(50.0, 70.0), gen, true),
                // GeneratorStartup extension fields
                Arguments.of(BETWEEN, FieldType.PLANNED_ACTIVE_POWER_SET_POINT, null, Set.of(40.0, 60.0), gen, true),
                Arguments.of(BETWEEN, FieldType.MARGINAL_COST, null, Set.of(40.0, 60.0), gen, true),
                Arguments.of(BETWEEN, FieldType.PLANNED_OUTAGE_RATE, null, Set.of(40.0, 60.0), gen, true),
                Arguments.of(BETWEEN, FieldType.FORCED_OUTAGE_RATE, null, Set.of(40.0, 60.0), gen, true),
                // VoltageLevel fields
                Arguments.of(BETWEEN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 14.0), gen, true),

                // --- EXISTS --- //
                // Generator fields
                Arguments.of(EXISTS, FieldType.MIN_P, null, null, gen, true),
                Arguments.of(EXISTS, FieldType.MAX_P, null, null, gen, true),
                Arguments.of(EXISTS, FieldType.TARGET_V, null, null, gen, true),
                Arguments.of(EXISTS, FieldType.TARGET_P, null, null, gen, true),
                Arguments.of(EXISTS, FieldType.TARGET_Q, null, null, gen, true),
                Arguments.of(EXISTS, FieldType.RATED_S, null, null, gen, true),
                // GeneratorStartup extension fields
                Arguments.of(EXISTS, FieldType.PLANNED_ACTIVE_POWER_SET_POINT, null, null, gen, true),
                Arguments.of(EXISTS, FieldType.MARGINAL_COST, null, null, gen, true),
                Arguments.of(EXISTS, FieldType.PLANNED_OUTAGE_RATE, null, null, gen, true),
                Arguments.of(EXISTS, FieldType.FORCED_OUTAGE_RATE, null, null, gen, true),
                // VoltageLevel fields
                Arguments.of(EXISTS, FieldType.NOMINAL_VOLTAGE, null, null, gen, true),

                // --- IN --- //
                // Generator fields
                Arguments.of(IN, FieldType.MIN_P, null, Set.of(-600.0, -500.0, -400.0), gen, true),
                Arguments.of(IN, FieldType.MAX_P, null, Set.of(90.0, 100.0, 110.0), gen, true),
                Arguments.of(IN, FieldType.TARGET_V, null, Set.of(10.0, 20.0, 30.0), gen, true),
                Arguments.of(IN, FieldType.TARGET_P, null, Set.of(20.0, 30.0, 40.0), gen, true),
                Arguments.of(IN, FieldType.TARGET_Q, null, Set.of(30.0, 40.0, 50.0), gen, true),
                Arguments.of(IN, FieldType.RATED_S, null, Set.of(50.0, 60.0, 70.0), gen, true),
                // GeneratorStartup extension fields
                Arguments.of(IN, FieldType.PLANNED_ACTIVE_POWER_SET_POINT, null, Set.of(40.0, 50.0, 60.0), gen, true),
                Arguments.of(IN, FieldType.MARGINAL_COST, null, Set.of(40.0, 50.0, 60.0), gen, true),
                Arguments.of(IN, FieldType.PLANNED_OUTAGE_RATE, null, Set.of(40.0, 50.0, 60.0), gen, true),
                Arguments.of(IN, FieldType.FORCED_OUTAGE_RATE, null, Set.of(40.0, 50.0, 60.0), gen, true),
                // VoltageLevel fields
                Arguments.of(IN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 13.0, 14.0), gen, true),

                // --- NOT_IN --- //
                // Generator fields
                Arguments.of(NOT_IN, FieldType.MIN_P, null, Set.of(-600.0, -400.0), gen, true),
                Arguments.of(NOT_IN, FieldType.MAX_P, null, Set.of(90.0, 110.0), gen, true),
                Arguments.of(NOT_IN, FieldType.TARGET_V, null, Set.of(10.0, 30.0), gen, true),
                Arguments.of(NOT_IN, FieldType.TARGET_P, null, Set.of(20.0, 40.0), gen, true),
                Arguments.of(NOT_IN, FieldType.TARGET_Q, null, Set.of(30.0, 50.0), gen, true),
                Arguments.of(NOT_IN, FieldType.RATED_S, null, Set.of(50.0, 70.0), gen, true),
                // GeneratorStartup extension fields
                Arguments.of(NOT_IN, FieldType.PLANNED_ACTIVE_POWER_SET_POINT, null, Set.of(40.0, 60.0), gen, true),
                Arguments.of(NOT_IN, FieldType.MARGINAL_COST, null, Set.of(40.0, 60.0), gen, true),
                Arguments.of(NOT_IN, FieldType.PLANNED_OUTAGE_RATE, null, Set.of(40.0, 60.0), gen, true),
                Arguments.of(NOT_IN, FieldType.FORCED_OUTAGE_RATE, null, Set.of(40.0, 60.0), gen, true),
                // VoltageLevel fields
                Arguments.of(NOT_IN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 14.0), gen, true)
        );
    }

    private static Stream<Arguments> provideArgumentsForLoadTest() {

        Load load = Mockito.mock(Load.class);
        Mockito.when(load.getType()).thenReturn(IdentifiableType.LOAD);
        // VoltageLevel fields
        VoltageLevel voltageLevel = Mockito.mock(VoltageLevel.class);
        Terminal terminal = Mockito.mock(Terminal.class);
        Mockito.when(terminal.getVoltageLevel()).thenReturn(voltageLevel);
        Mockito.when(load.getTerminal()).thenReturn(terminal);
        Mockito.when(voltageLevel.getNominalV()).thenReturn(13.0);

        return Stream.of(
                // --- EQUALS --- //
                // VoltageLevel fields
                Arguments.of(EQUALS, FieldType.NOMINAL_VOLTAGE, 13.0, null, load, true),

                // --- GREATER_OR_EQUALS --- //
                // VoltageLevel fields
                Arguments.of(GREATER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 12.0, null, load, true),

                // --- GREATER --- //
                // VoltageLevel fields
                Arguments.of(GREATER, FieldType.NOMINAL_VOLTAGE, 12.0, null, load, true),

                // --- LOWER_OR_EQUALS --- //
                // VoltageLevel fields
                Arguments.of(LOWER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 14.0, null, load, true),

                // --- LOWER --- //
                // VoltageLevel fields
                Arguments.of(LOWER, FieldType.NOMINAL_VOLTAGE, 14.0, null, load, true),

                // --- BETWEEN --- //
                // VoltageLevel fields
                Arguments.of(BETWEEN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 14.0), load, true),

                // --- EXISTS --- //
                // VoltageLevel fields
                Arguments.of(EXISTS, FieldType.NOMINAL_VOLTAGE, null, null, load, true),

                // --- IN --- //
                // VoltageLevel fields
                Arguments.of(IN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 13.0, 14.0), load, true),

                // --- NOT_IN --- //
                // VoltageLevel fields
                Arguments.of(NOT_IN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 14.0), load, true)
        );
    }

    private static Stream<Arguments> provideArgumentsForBusTest() {

        Bus bus = Mockito.mock(Bus.class);
        Mockito.when(bus.getType()).thenReturn(IdentifiableType.BUS);
        // VoltageLevel fields
        VoltageLevel voltageLevel = Mockito.mock(VoltageLevel.class);
        Mockito.when(bus.getVoltageLevel()).thenReturn(voltageLevel);
        Mockito.when(voltageLevel.getNominalV()).thenReturn(13.0);

        return Stream.of(
                // --- EQUALS --- //
                // VoltageLevel fields
                Arguments.of(EQUALS, FieldType.NOMINAL_VOLTAGE, 13.0, null, bus, true),

                // --- GREATER_OR_EQUALS --- //
                // VoltageLevel fields
                Arguments.of(GREATER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 12.0, null, bus, true),

                // --- GREATER --- //
                // VoltageLevel fields
                Arguments.of(GREATER, FieldType.NOMINAL_VOLTAGE, 12.0, null, bus, true),

                // --- LOWER_OR_EQUALS --- //
                // VoltageLevel fields
                Arguments.of(LOWER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 14.0, null, bus, true),

                // --- LOWER --- //
                // VoltageLevel fields
                Arguments.of(LOWER, FieldType.NOMINAL_VOLTAGE, 14.0, null, bus, true),

                // --- BETWEEN --- //
                // VoltageLevel fields
                Arguments.of(BETWEEN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 14.0), bus, true),

                // --- EXISTS --- //
                // VoltageLevel fields
                Arguments.of(EXISTS, FieldType.NOMINAL_VOLTAGE, null, null, bus, true),

                // --- IN --- //
                // VoltageLevel fields
                Arguments.of(IN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 13.0, 14.0), bus, true),

                // --- NOT_IN --- //
                // VoltageLevel fields
                Arguments.of(NOT_IN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 14.0), bus, true)
        );
    }

    private static Stream<Arguments> provideArgumentsForBusBarSectionTest() {

        BusbarSection busbarSection = Mockito.mock(BusbarSection.class);
        Mockito.when(busbarSection.getType()).thenReturn(IdentifiableType.BUSBAR_SECTION);
        // VoltageLevel fields
        VoltageLevel voltageLevel = Mockito.mock(VoltageLevel.class);
        Terminal terminal = Mockito.mock(Terminal.class);
        Mockito.when(terminal.getVoltageLevel()).thenReturn(voltageLevel);
        Mockito.when(busbarSection.getTerminal()).thenReturn(terminal);
        Mockito.when(voltageLevel.getNominalV()).thenReturn(13.0);

        return Stream.of(
                // --- EQUALS --- //
                // VoltageLevel fields
                Arguments.of(EQUALS, FieldType.NOMINAL_VOLTAGE, 13.0, null, busbarSection, true),

                // --- GREATER_OR_EQUALS --- //
                // VoltageLevel fields
                Arguments.of(GREATER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 12.0, null, busbarSection, true),

                // --- GREATER --- //
                // VoltageLevel fields
                Arguments.of(GREATER, FieldType.NOMINAL_VOLTAGE, 12.0, null, busbarSection, true),

                // --- LOWER_OR_EQUALS --- //
                // VoltageLevel fields
                Arguments.of(LOWER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 14.0, null, busbarSection, true),

                // --- LOWER --- //
                // VoltageLevel fields
                Arguments.of(LOWER, FieldType.NOMINAL_VOLTAGE, 14.0, null, busbarSection, true),

                // --- BETWEEN --- //
                // VoltageLevel fields
                Arguments.of(BETWEEN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 14.0), busbarSection, true),

                // --- EXISTS --- //
                // VoltageLevel fields
                Arguments.of(EXISTS, FieldType.NOMINAL_VOLTAGE, null, null, busbarSection, true),

                // --- IN --- //
                // VoltageLevel fields
                Arguments.of(IN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 13.0, 14.0), busbarSection, true),

                // --- NOT_IN --- //
                // VoltageLevel fields
                Arguments.of(NOT_IN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 14.0), busbarSection, true)
        );
    }
}
