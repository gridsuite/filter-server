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

        ShuntCompensator shuntCompensator = Mockito.mock(ShuntCompensator.class);
        Mockito.when(shuntCompensator.getType()).thenReturn(IdentifiableType.SHUNT_COMPENSATOR);

        Bus bus = Mockito.mock(Bus.class);
        Mockito.when(bus.getType()).thenReturn(IdentifiableType.BUS);

        BusbarSection busbarSection = Mockito.mock(BusbarSection.class);
        Mockito.when(busbarSection.getType()).thenReturn(IdentifiableType.BUSBAR_SECTION);

        return Stream.of(
                // --- Test an unsupported field for each equipment --- //
                Arguments.of(EQUALS, FieldType.RATED_S, network, PowsyblException.class),
                Arguments.of(EQUALS, FieldType.RATED_S, voltageLevel, PowsyblException.class),
                Arguments.of(EQUALS, FieldType.P0, generator, PowsyblException.class),
                Arguments.of(EQUALS, FieldType.RATED_S, load, PowsyblException.class),
                Arguments.of(EQUALS, FieldType.RATED_S, shuntCompensator, PowsyblException.class),
                Arguments.of(EQUALS, FieldType.RATED_S, bus, PowsyblException.class),
                Arguments.of(EQUALS, FieldType.RATED_S, busbarSection, PowsyblException.class),

                // --- Test an unsupported operator for this rule type --- //
                Arguments.of(IS, FieldType.MIN_P, generator, PowsyblException.class)
        );
    }

    @ParameterizedTest
    @MethodSource({
        "provideArgumentsForGeneratorTest",
        "provideArgumentsForLoadTest",
        "provideArgumentsForBusTest",
        "provideArgumentsForBusBarSectionTest",
        "provideArgumentsForShuntCompensatorTest",
        "provideArgumentsForBatteryTest",
        "provideArgumentsForVoltageLevelTest",
        "provideArgumentsForLinesTest"
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

        // for testing none EXISTS
        Generator gen1 = Mockito.mock(Generator.class);
        Mockito.when(gen1.getType()).thenReturn(IdentifiableType.GENERATOR);
        // Generator fields
        Mockito.when(gen1.getMinP()).thenReturn(Double.NaN);
        Mockito.when(gen1.getMaxP()).thenReturn(Double.NaN);
        Mockito.when(gen1.getTargetV()).thenReturn(Double.NaN);
        Mockito.when(gen1.getTargetP()).thenReturn(Double.NaN);
        Mockito.when(gen1.getTargetQ()).thenReturn(Double.NaN);
        Mockito.when(gen1.getRatedS()).thenReturn(Double.NaN);
        // GeneratorStartup extension fields
        GeneratorStartup genStartup1 = Mockito.mock(GeneratorStartup.class);
        Mockito.when(genStartup1.getPlannedActivePowerSetpoint()).thenReturn(Double.NaN);
        Mockito.when(genStartup1.getMarginalCost()).thenReturn(Double.NaN);
        Mockito.when(genStartup1.getPlannedOutageRate()).thenReturn(Double.NaN);
        Mockito.when(genStartup1.getForcedOutageRate()).thenReturn(Double.NaN);
        Mockito.when(gen1.getExtension(any())).thenReturn(genStartup1);
        // VoltageLevel fields
        VoltageLevel voltageLevel1 = Mockito.mock(VoltageLevel.class);
        Terminal terminal1 = Mockito.mock(Terminal.class);
        Mockito.when(terminal1.getVoltageLevel()).thenReturn(voltageLevel1);
        Mockito.when(gen1.getTerminal()).thenReturn(terminal1);
        Mockito.when(voltageLevel1.getNominalV()).thenReturn(Double.NaN);

        return Stream.of(
                // --- EQUALS --- //
                // Generator fields
                Arguments.of(EQUALS, FieldType.MIN_P, -500.0, null, gen, true),
                Arguments.of(EQUALS, FieldType.MIN_P, -400.0, null, gen, false),
                Arguments.of(EQUALS, FieldType.MAX_P, 100.0, null, gen, true),
                Arguments.of(EQUALS, FieldType.MAX_P, 90.0, null, gen, false),
                Arguments.of(EQUALS, FieldType.TARGET_V, 20.0, null, gen, true),
                Arguments.of(EQUALS, FieldType.TARGET_V, 10.0, null, gen, false),
                Arguments.of(EQUALS, FieldType.TARGET_P, 30.0, null, gen, true),
                Arguments.of(EQUALS, FieldType.TARGET_P, 20.0, null, gen, false),
                Arguments.of(EQUALS, FieldType.TARGET_Q, 40.0, null, gen, true),
                Arguments.of(EQUALS, FieldType.TARGET_Q, 30.0, null, gen, false),
                Arguments.of(EQUALS, FieldType.RATED_S, 60.0, null, gen, true),
                Arguments.of(EQUALS, FieldType.RATED_S, 50.0, null, gen, false),
                // GeneratorStartup extension fields
                Arguments.of(EQUALS, FieldType.PLANNED_ACTIVE_POWER_SET_POINT, 50.0, null, gen, true),
                Arguments.of(EQUALS, FieldType.PLANNED_ACTIVE_POWER_SET_POINT, 40.0, null, gen, false),
                Arguments.of(EQUALS, FieldType.MARGINAL_COST, 50.0, null, gen, true),
                Arguments.of(EQUALS, FieldType.MARGINAL_COST, 40.0, null, gen, false),
                Arguments.of(EQUALS, FieldType.PLANNED_OUTAGE_RATE, 50.0, null, gen, true),
                Arguments.of(EQUALS, FieldType.PLANNED_OUTAGE_RATE, 40.0, null, gen, false),
                Arguments.of(EQUALS, FieldType.FORCED_OUTAGE_RATE, 50.0, null, gen, true),
                Arguments.of(EQUALS, FieldType.FORCED_OUTAGE_RATE, 40.0, null, gen, false),
                // VoltageLevel fields
                Arguments.of(EQUALS, FieldType.NOMINAL_VOLTAGE, 13.0, null, gen, true),
                Arguments.of(EQUALS, FieldType.NOMINAL_VOLTAGE, 12.0, null, gen, false),

                // --- GREATER_OR_EQUALS --- //
                // Generator fields
                Arguments.of(GREATER_OR_EQUALS, FieldType.MIN_P, -600.0, null, gen, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.MIN_P, -500.0, null, gen, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.MIN_P, -400.0, null, gen, false),
                Arguments.of(GREATER_OR_EQUALS, FieldType.MAX_P, 90.0, null, gen, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.MAX_P, 100.0, null, gen, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.MAX_P, 110.0, null, gen, false),
                Arguments.of(GREATER_OR_EQUALS, FieldType.TARGET_V, 10.0, null, gen, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.TARGET_V, 20.0, null, gen, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.TARGET_V, 30.0, null, gen, false),
                Arguments.of(GREATER_OR_EQUALS, FieldType.TARGET_P, 20.0, null, gen, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.TARGET_P, 30.0, null, gen, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.TARGET_P, 40.0, null, gen, false),
                Arguments.of(GREATER_OR_EQUALS, FieldType.TARGET_Q, 30.0, null, gen, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.TARGET_Q, 40.0, null, gen, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.TARGET_Q, 50.0, null, gen, false),
                Arguments.of(GREATER_OR_EQUALS, FieldType.RATED_S, 50.0, null, gen, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.RATED_S, 60.0, null, gen, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.RATED_S, 70.0, null, gen, false),
                // GeneratorStartup extension fields
                Arguments.of(GREATER_OR_EQUALS, FieldType.PLANNED_ACTIVE_POWER_SET_POINT, 40.0, null, gen, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.PLANNED_ACTIVE_POWER_SET_POINT, 50.0, null, gen, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.PLANNED_ACTIVE_POWER_SET_POINT, 60.0, null, gen, false),
                Arguments.of(GREATER_OR_EQUALS, FieldType.MARGINAL_COST, 40.0, null, gen, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.MARGINAL_COST, 50.0, null, gen, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.MARGINAL_COST, 60.0, null, gen, false),
                Arguments.of(GREATER_OR_EQUALS, FieldType.PLANNED_OUTAGE_RATE, 40.0, null, gen, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.PLANNED_OUTAGE_RATE, 50.0, null, gen, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.PLANNED_OUTAGE_RATE, 60.0, null, gen, false),
                Arguments.of(GREATER_OR_EQUALS, FieldType.FORCED_OUTAGE_RATE, 40.0, null, gen, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.FORCED_OUTAGE_RATE, 50.0, null, gen, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.FORCED_OUTAGE_RATE, 60.0, null, gen, false),
                // VoltageLevel fields
                Arguments.of(GREATER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 12.0, null, gen, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 13.0, null, gen, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 14.0, null, gen, false),

                // --- GREATER --- //
                // Generator fields
                Arguments.of(GREATER, FieldType.MIN_P, -600.0, null, gen, true),
                Arguments.of(GREATER, FieldType.MIN_P, -500.0, null, gen, false),
                Arguments.of(GREATER, FieldType.MIN_P, -400.0, null, gen, false),
                Arguments.of(GREATER, FieldType.MAX_P, 90.0, null, gen, true),
                Arguments.of(GREATER, FieldType.MAX_P, 100.0, null, gen, false),
                Arguments.of(GREATER, FieldType.MAX_P, 110.0, null, gen, false),
                Arguments.of(GREATER, FieldType.TARGET_V, 10.0, null, gen, true),
                Arguments.of(GREATER, FieldType.TARGET_V, 20.0, null, gen, false),
                Arguments.of(GREATER, FieldType.TARGET_V, 30.0, null, gen, false),
                Arguments.of(GREATER, FieldType.TARGET_P, 20.0, null, gen, true),
                Arguments.of(GREATER, FieldType.TARGET_P, 30.0, null, gen, false),
                Arguments.of(GREATER, FieldType.TARGET_P, 40.0, null, gen, false),
                Arguments.of(GREATER, FieldType.TARGET_Q, 30.0, null, gen, true),
                Arguments.of(GREATER, FieldType.TARGET_Q, 40.0, null, gen, false),
                Arguments.of(GREATER, FieldType.TARGET_Q, 50.0, null, gen, false),
                Arguments.of(GREATER, FieldType.RATED_S, 50.0, null, gen, true),
                Arguments.of(GREATER, FieldType.RATED_S, 60.0, null, gen, false),
                Arguments.of(GREATER, FieldType.RATED_S, 70.0, null, gen, false),
                // GeneratorStartup extension fields
                Arguments.of(GREATER, FieldType.PLANNED_ACTIVE_POWER_SET_POINT, 40.0, null, gen, true),
                Arguments.of(GREATER, FieldType.PLANNED_ACTIVE_POWER_SET_POINT, 50.0, null, gen, false),
                Arguments.of(GREATER, FieldType.PLANNED_ACTIVE_POWER_SET_POINT, 60.0, null, gen, false),
                Arguments.of(GREATER, FieldType.MARGINAL_COST, 40.0, null, gen, true),
                Arguments.of(GREATER, FieldType.MARGINAL_COST, 50.0, null, gen, false),
                Arguments.of(GREATER, FieldType.MARGINAL_COST, 60.0, null, gen, false),
                Arguments.of(GREATER, FieldType.PLANNED_OUTAGE_RATE, 40.0, null, gen, true),
                Arguments.of(GREATER, FieldType.PLANNED_OUTAGE_RATE, 50.0, null, gen, false),
                Arguments.of(GREATER, FieldType.PLANNED_OUTAGE_RATE, 60.0, null, gen, false),
                Arguments.of(GREATER, FieldType.FORCED_OUTAGE_RATE, 40.0, null, gen, true),
                Arguments.of(GREATER, FieldType.FORCED_OUTAGE_RATE, 50.0, null, gen, false),
                Arguments.of(GREATER, FieldType.FORCED_OUTAGE_RATE, 60.0, null, gen, false),
                // VoltageLevel fields
                Arguments.of(GREATER, FieldType.NOMINAL_VOLTAGE, 12.0, null, gen, true),
                Arguments.of(GREATER, FieldType.NOMINAL_VOLTAGE, 13.0, null, gen, false),
                Arguments.of(GREATER, FieldType.NOMINAL_VOLTAGE, 14.0, null, gen, false),

                // --- LOWER_OR_EQUALS --- //
                // Generator fields
                Arguments.of(LOWER_OR_EQUALS, FieldType.MIN_P, -400.0, null, gen, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.MIN_P, -500.0, null, gen, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.MIN_P, -600.0, null, gen, false),
                Arguments.of(LOWER_OR_EQUALS, FieldType.MAX_P, 110.0, null, gen, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.MAX_P, 100.0, null, gen, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.MAX_P, 90.0, null, gen, false),
                Arguments.of(LOWER_OR_EQUALS, FieldType.TARGET_V, 30.0, null, gen, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.TARGET_V, 20.0, null, gen, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.TARGET_V, 10.0, null, gen, false),
                Arguments.of(LOWER_OR_EQUALS, FieldType.TARGET_P, 40.0, null, gen, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.TARGET_P, 30.0, null, gen, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.TARGET_P, 20.0, null, gen, false),
                Arguments.of(LOWER_OR_EQUALS, FieldType.TARGET_Q, 50.0, null, gen, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.TARGET_Q, 40.0, null, gen, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.TARGET_Q, 30.0, null, gen, false),
                Arguments.of(LOWER_OR_EQUALS, FieldType.RATED_S, 70.0, null, gen, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.RATED_S, 60.0, null, gen, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.RATED_S, 50.0, null, gen, false),
                // GeneratorStartup extension fields
                Arguments.of(LOWER_OR_EQUALS, FieldType.PLANNED_ACTIVE_POWER_SET_POINT, 60.0, null, gen, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.PLANNED_ACTIVE_POWER_SET_POINT, 50.0, null, gen, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.PLANNED_ACTIVE_POWER_SET_POINT, 40.0, null, gen, false),
                Arguments.of(LOWER_OR_EQUALS, FieldType.MARGINAL_COST, 60.0, null, gen, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.MARGINAL_COST, 50.0, null, gen, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.MARGINAL_COST, 40.0, null, gen, false),
                Arguments.of(LOWER_OR_EQUALS, FieldType.PLANNED_OUTAGE_RATE, 60.0, null, gen, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.PLANNED_OUTAGE_RATE, 50.0, null, gen, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.PLANNED_OUTAGE_RATE, 40.0, null, gen, false),
                Arguments.of(LOWER_OR_EQUALS, FieldType.FORCED_OUTAGE_RATE, 60.0, null, gen, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.FORCED_OUTAGE_RATE, 50.0, null, gen, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.FORCED_OUTAGE_RATE, 40.0, null, gen, false),
                // VoltageLevel fields
                Arguments.of(LOWER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 14.0, null, gen, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 13.0, null, gen, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 12.0, null, gen, false),

                // --- LOWER --- //
                // Generator fields
                Arguments.of(LOWER, FieldType.MIN_P, -400.0, null, gen, true),
                Arguments.of(LOWER, FieldType.MIN_P, -500.0, null, gen, false),
                Arguments.of(LOWER, FieldType.MIN_P, -600.0, null, gen, false),
                Arguments.of(LOWER, FieldType.MAX_P, 110.0, null, gen, true),
                Arguments.of(LOWER, FieldType.MAX_P, 100.0, null, gen, false),
                Arguments.of(LOWER, FieldType.MAX_P, 90.0, null, gen, false),
                Arguments.of(LOWER, FieldType.TARGET_V, 30.0, null, gen, true),
                Arguments.of(LOWER, FieldType.TARGET_V, 20.0, null, gen, false),
                Arguments.of(LOWER, FieldType.TARGET_V, 10.0, null, gen, false),
                Arguments.of(LOWER, FieldType.TARGET_P, 40.0, null, gen, true),
                Arguments.of(LOWER, FieldType.TARGET_P, 30.0, null, gen, false),
                Arguments.of(LOWER, FieldType.TARGET_P, 20.0, null, gen, false),
                Arguments.of(LOWER, FieldType.TARGET_Q, 50.0, null, gen, true),
                Arguments.of(LOWER, FieldType.TARGET_Q, 40.0, null, gen, false),
                Arguments.of(LOWER, FieldType.TARGET_Q, 30.0, null, gen, false),
                Arguments.of(LOWER, FieldType.RATED_S, 70.0, null, gen, true),
                Arguments.of(LOWER, FieldType.RATED_S, 60.0, null, gen, false),
                Arguments.of(LOWER, FieldType.RATED_S, 50.0, null, gen, false),
                // GeneratorStartup extension fields
                Arguments.of(LOWER, FieldType.PLANNED_ACTIVE_POWER_SET_POINT, 60.0, null, gen, true),
                Arguments.of(LOWER, FieldType.PLANNED_ACTIVE_POWER_SET_POINT, 50.0, null, gen, false),
                Arguments.of(LOWER, FieldType.PLANNED_ACTIVE_POWER_SET_POINT, 40.0, null, gen, false),
                Arguments.of(LOWER, FieldType.MARGINAL_COST, 60.0, null, gen, true),
                Arguments.of(LOWER, FieldType.MARGINAL_COST, 50.0, null, gen, false),
                Arguments.of(LOWER, FieldType.MARGINAL_COST, 40.0, null, gen, false),
                Arguments.of(LOWER, FieldType.PLANNED_OUTAGE_RATE, 60.0, null, gen, true),
                Arguments.of(LOWER, FieldType.PLANNED_OUTAGE_RATE, 50.0, null, gen, false),
                Arguments.of(LOWER, FieldType.PLANNED_OUTAGE_RATE, 40.0, null, gen, false),
                Arguments.of(LOWER, FieldType.FORCED_OUTAGE_RATE, 60.0, null, gen, true),
                Arguments.of(LOWER, FieldType.FORCED_OUTAGE_RATE, 50.0, null, gen, false),
                Arguments.of(LOWER, FieldType.FORCED_OUTAGE_RATE, 40.0, null, gen, false),
                // VoltageLevel fields
                Arguments.of(LOWER, FieldType.NOMINAL_VOLTAGE, 14.0, null, gen, true),
                Arguments.of(LOWER, FieldType.NOMINAL_VOLTAGE, 13.0, null, gen, false),
                Arguments.of(LOWER, FieldType.NOMINAL_VOLTAGE, 12.0, null, gen, false),

                // --- BETWEEN --- //
                // Generator fields
                Arguments.of(BETWEEN, FieldType.MIN_P, null, Set.of(-600.0, -400.0), gen, true),
                Arguments.of(BETWEEN, FieldType.MIN_P, null, Set.of(-450.0, -400.0), gen, false),
                Arguments.of(BETWEEN, FieldType.MAX_P, null, Set.of(90.0, 110.0), gen, true),
                Arguments.of(BETWEEN, FieldType.MAX_P, null, Set.of(105.0, 110.0), gen, false),
                Arguments.of(BETWEEN, FieldType.TARGET_V, null, Set.of(10.0, 30.0), gen, true),
                Arguments.of(BETWEEN, FieldType.TARGET_V, null, Set.of(25.0, 30.0), gen, false),
                Arguments.of(BETWEEN, FieldType.TARGET_P, null, Set.of(20.0, 40.0), gen, true),
                Arguments.of(BETWEEN, FieldType.TARGET_P, null, Set.of(35.0, 40.0), gen, false),
                Arguments.of(BETWEEN, FieldType.TARGET_Q, null, Set.of(30.0, 50.0), gen, true),
                Arguments.of(BETWEEN, FieldType.TARGET_Q, null, Set.of(45.0, 50.0), gen, false),
                Arguments.of(BETWEEN, FieldType.RATED_S, null, Set.of(50.0, 70.0), gen, true),
                Arguments.of(BETWEEN, FieldType.RATED_S, null, Set.of(65.0, 70.0), gen, false),
                // GeneratorStartup extension fields
                Arguments.of(BETWEEN, FieldType.PLANNED_ACTIVE_POWER_SET_POINT, null, Set.of(40.0, 60.0), gen, true),
                Arguments.of(BETWEEN, FieldType.PLANNED_ACTIVE_POWER_SET_POINT, null, Set.of(55.0, 60.0), gen, false),
                Arguments.of(BETWEEN, FieldType.MARGINAL_COST, null, Set.of(40.0, 60.0), gen, true),
                Arguments.of(BETWEEN, FieldType.MARGINAL_COST, null, Set.of(55.0, 60.0), gen, false),
                Arguments.of(BETWEEN, FieldType.PLANNED_OUTAGE_RATE, null, Set.of(40.0, 60.0), gen, true),
                Arguments.of(BETWEEN, FieldType.PLANNED_OUTAGE_RATE, null, Set.of(55.0, 60.0), gen, false),
                Arguments.of(BETWEEN, FieldType.FORCED_OUTAGE_RATE, null, Set.of(40.0, 60.0), gen, true),
                Arguments.of(BETWEEN, FieldType.FORCED_OUTAGE_RATE, null, Set.of(55.0, 60.0), gen, false),
                // VoltageLevel fields
                Arguments.of(BETWEEN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 14.0), gen, true),
                Arguments.of(BETWEEN, FieldType.NOMINAL_VOLTAGE, null, Set.of(13.5, 14.0), gen, false),

                // --- EXISTS --- //
                // Generator fields
                Arguments.of(EXISTS, FieldType.MIN_P, null, null, gen, true),
                Arguments.of(EXISTS, FieldType.MIN_P, null, null, gen1, false),
                Arguments.of(EXISTS, FieldType.MAX_P, null, null, gen, true),
                Arguments.of(EXISTS, FieldType.MAX_P, null, null, gen1, false),
                Arguments.of(EXISTS, FieldType.TARGET_V, null, null, gen, true),
                Arguments.of(EXISTS, FieldType.TARGET_V, null, null, gen1, false),
                Arguments.of(EXISTS, FieldType.TARGET_P, null, null, gen, true),
                Arguments.of(EXISTS, FieldType.TARGET_P, null, null, gen1, false),
                Arguments.of(EXISTS, FieldType.TARGET_Q, null, null, gen, true),
                Arguments.of(EXISTS, FieldType.TARGET_Q, null, null, gen1, false),
                Arguments.of(EXISTS, FieldType.RATED_S, null, null, gen, true),
                Arguments.of(EXISTS, FieldType.RATED_S, null, null, gen1, false),
                // GeneratorStartup extension fields
                Arguments.of(EXISTS, FieldType.PLANNED_ACTIVE_POWER_SET_POINT, null, null, gen, true),
                Arguments.of(EXISTS, FieldType.PLANNED_ACTIVE_POWER_SET_POINT, null, null, gen1, false),
                Arguments.of(EXISTS, FieldType.MARGINAL_COST, null, null, gen, true),
                Arguments.of(EXISTS, FieldType.MARGINAL_COST, null, null, gen1, false),
                Arguments.of(EXISTS, FieldType.PLANNED_OUTAGE_RATE, null, null, gen, true),
                Arguments.of(EXISTS, FieldType.PLANNED_OUTAGE_RATE, null, null, gen1, false),
                Arguments.of(EXISTS, FieldType.FORCED_OUTAGE_RATE, null, null, gen, true),
                Arguments.of(EXISTS, FieldType.FORCED_OUTAGE_RATE, null, null, gen1, false),
                // VoltageLevel fields
                Arguments.of(EXISTS, FieldType.NOMINAL_VOLTAGE, null, null, gen, true),
                Arguments.of(EXISTS, FieldType.NOMINAL_VOLTAGE, null, null, gen1, false),

                // --- IN --- //
                // Generator fields
                Arguments.of(IN, FieldType.MIN_P, null, Set.of(-600.0, -500.0, -400.0), gen, true),
                Arguments.of(IN, FieldType.MIN_P, null, Set.of(-600.0, -400.0), gen, false),
                Arguments.of(IN, FieldType.MAX_P, null, Set.of(90.0, 100.0, 110.0), gen, true),
                Arguments.of(IN, FieldType.MAX_P, null, Set.of(90.0, 110.0), gen, false),
                Arguments.of(IN, FieldType.TARGET_V, null, Set.of(10.0, 20.0, 30.0), gen, true),
                Arguments.of(IN, FieldType.TARGET_V, null, Set.of(10.0, 30.0), gen, false),
                Arguments.of(IN, FieldType.TARGET_P, null, Set.of(20.0, 30.0, 40.0), gen, true),
                Arguments.of(IN, FieldType.TARGET_P, null, Set.of(20.0, 40.0), gen, false),
                Arguments.of(IN, FieldType.TARGET_Q, null, Set.of(30.0, 40.0, 50.0), gen, true),
                Arguments.of(IN, FieldType.TARGET_Q, null, Set.of(30.0, 50.0), gen, false),
                Arguments.of(IN, FieldType.RATED_S, null, Set.of(50.0, 60.0, 70.0), gen, true),
                Arguments.of(IN, FieldType.RATED_S, null, Set.of(50.0, 70.0), gen, false),
                // GeneratorStartup extension fields
                Arguments.of(IN, FieldType.PLANNED_ACTIVE_POWER_SET_POINT, null, Set.of(40.0, 50.0, 60.0), gen, true),
                Arguments.of(IN, FieldType.PLANNED_ACTIVE_POWER_SET_POINT, null, Set.of(40.0, 60.0), gen, false),
                Arguments.of(IN, FieldType.MARGINAL_COST, null, Set.of(40.0, 50.0, 60.0), gen, true),
                Arguments.of(IN, FieldType.MARGINAL_COST, null, Set.of(40.0, 60.0), gen, false),
                Arguments.of(IN, FieldType.PLANNED_OUTAGE_RATE, null, Set.of(40.0, 50.0, 60.0), gen, true),
                Arguments.of(IN, FieldType.PLANNED_OUTAGE_RATE, null, Set.of(40.0, 60.0), gen, false),
                Arguments.of(IN, FieldType.FORCED_OUTAGE_RATE, null, Set.of(40.0, 50.0, 60.0), gen, true),
                Arguments.of(IN, FieldType.FORCED_OUTAGE_RATE, null, Set.of(40.0, 60.0), gen, false),
                // VoltageLevel fields
                Arguments.of(IN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 13.0, 14.0), gen, true),
                Arguments.of(IN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 14.0), gen, false),

                // --- NOT_IN --- //
                // Generator fields
                Arguments.of(NOT_IN, FieldType.MIN_P, null, Set.of(-600.0, -400.0), gen, true),
                Arguments.of(NOT_IN, FieldType.MIN_P, null, Set.of(-600.0, -500.0, -400.0), gen, false),
                Arguments.of(NOT_IN, FieldType.MAX_P, null, Set.of(90.0, 110.0), gen, true),
                Arguments.of(NOT_IN, FieldType.MAX_P, null, Set.of(90.0, 100.0, 110.0), gen, false),
                Arguments.of(NOT_IN, FieldType.TARGET_V, null, Set.of(10.0, 30.0), gen, true),
                Arguments.of(NOT_IN, FieldType.TARGET_V, null, Set.of(10.0, 20.0, 30.0), gen, false),
                Arguments.of(NOT_IN, FieldType.TARGET_P, null, Set.of(20.0, 40.0), gen, true),
                Arguments.of(NOT_IN, FieldType.TARGET_P, null, Set.of(20.0, 30.0, 40.0), gen, false),
                Arguments.of(NOT_IN, FieldType.TARGET_Q, null, Set.of(30.0, 50.0), gen, true),
                Arguments.of(NOT_IN, FieldType.TARGET_Q, null, Set.of(30.0, 40.0, 50.0), gen, false),
                Arguments.of(NOT_IN, FieldType.RATED_S, null, Set.of(50.0, 70.0), gen, true),
                Arguments.of(NOT_IN, FieldType.RATED_S, null, Set.of(50.0, 60.0, 70.0), gen, false),
                // GeneratorStartup extension fields
                Arguments.of(NOT_IN, FieldType.PLANNED_ACTIVE_POWER_SET_POINT, null, Set.of(40.0, 60.0), gen, true),
                Arguments.of(NOT_IN, FieldType.PLANNED_ACTIVE_POWER_SET_POINT, null, Set.of(40.0, 50.0, 60.0), gen, false),
                Arguments.of(NOT_IN, FieldType.MARGINAL_COST, null, Set.of(40.0, 60.0), gen, true),
                Arguments.of(NOT_IN, FieldType.MARGINAL_COST, null, Set.of(40.0, 50.0, 60.0), gen, false),
                Arguments.of(NOT_IN, FieldType.PLANNED_OUTAGE_RATE, null, Set.of(40.0, 60.0), gen, true),
                Arguments.of(NOT_IN, FieldType.PLANNED_OUTAGE_RATE, null, Set.of(40.0, 50.0, 60.0), gen, false),
                Arguments.of(NOT_IN, FieldType.FORCED_OUTAGE_RATE, null, Set.of(40.0, 60.0), gen, true),
                Arguments.of(NOT_IN, FieldType.FORCED_OUTAGE_RATE, null, Set.of(40.0, 50.0, 60.0), gen, false),
                // VoltageLevel fields
                Arguments.of(NOT_IN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 14.0), gen, true),
                Arguments.of(NOT_IN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 13.0, 14.0), gen, false)
        );
    }

    private static Stream<Arguments> provideArgumentsForLoadTest() {

        Load load = Mockito.mock(Load.class);
        Mockito.when(load.getType()).thenReturn(IdentifiableType.LOAD);
        Mockito.when(load.getP0()).thenReturn(77.0);
        Mockito.when(load.getQ0()).thenReturn(277.0);
        // VoltageLevel fields
        VoltageLevel voltageLevel = Mockito.mock(VoltageLevel.class);
        Terminal terminal = Mockito.mock(Terminal.class);
        Mockito.when(terminal.getVoltageLevel()).thenReturn(voltageLevel);
        Mockito.when(load.getTerminal()).thenReturn(terminal);
        Mockito.when(voltageLevel.getNominalV()).thenReturn(13.0);

        // for testing none EXISTS
        Load load1 = Mockito.mock(Load.class);
        Mockito.when(load1.getType()).thenReturn(IdentifiableType.LOAD);
        Mockito.when(load1.getP0()).thenReturn(Double.NaN);
        Mockito.when(load1.getQ0()).thenReturn(Double.NaN);
        // VoltageLevel fields
        VoltageLevel voltageLevel1 = Mockito.mock(VoltageLevel.class);
        Terminal terminal1 = Mockito.mock(Terminal.class);
        Mockito.when(terminal1.getVoltageLevel()).thenReturn(voltageLevel1);
        Mockito.when(load1.getTerminal()).thenReturn(terminal1);
        Mockito.when(voltageLevel1.getNominalV()).thenReturn(Double.NaN);

        return Stream.of(
                // --- EQUALS --- //
                // VoltageLevel fields
                Arguments.of(EQUALS, FieldType.NOMINAL_VOLTAGE, 13.0, null, load, true),
                Arguments.of(EQUALS, FieldType.NOMINAL_VOLTAGE, 14.0, null, load, false),
                // Load fields
                Arguments.of(EQUALS, FieldType.P0, 77.0, null, load, true),
                Arguments.of(EQUALS, FieldType.P0, 50.0, null, load, false),
                Arguments.of(EQUALS, FieldType.Q0, 277.0, null, load, true),
                Arguments.of(EQUALS, FieldType.Q0, 300.0, null, load, false),

                // --- GREATER_OR_EQUALS --- //
                // VoltageLevel fields
                Arguments.of(GREATER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 12.0, null, load, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 13.0, null, load, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 14.0, null, load, false),
                // Load fields
                Arguments.of(GREATER_OR_EQUALS, FieldType.P0, 72.0, null, load, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.P0, 77.0, null, load, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.P0, 78.0, null, load, false),
                Arguments.of(GREATER_OR_EQUALS, FieldType.Q0, 272.0, null, load, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.Q0, 277.0, null, load, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.Q0, 278.0, null, load, false),

                // --- GREATER --- //
                // VoltageLevel fields
                Arguments.of(GREATER, FieldType.NOMINAL_VOLTAGE, 12.0, null, load, true),
                Arguments.of(GREATER, FieldType.NOMINAL_VOLTAGE, 13.0, null, load, false),
                Arguments.of(GREATER, FieldType.NOMINAL_VOLTAGE, 14.0, null, load, false),
                // Load fields
                Arguments.of(GREATER, FieldType.P0, 70.0, null, load, true),
                Arguments.of(GREATER, FieldType.P0, 77.0, null, load, false),
                Arguments.of(GREATER, FieldType.P0, 78.0, null, load, false),
                Arguments.of(GREATER, FieldType.Q0, 270.0, null, load, true),
                Arguments.of(GREATER, FieldType.Q0, 277.0, null, load, false),
                Arguments.of(GREATER, FieldType.Q0, 278.0, null, load, false),

                // --- LOWER_OR_EQUALS --- //
                // VoltageLevel fields
                Arguments.of(LOWER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 14.0, null, load, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 13.0, null, load, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 12.0, null, load, false),
                // Load fields
                Arguments.of(LOWER_OR_EQUALS, FieldType.P0, 80.0, null, load, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.P0, 77.0, null, load, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.P0, 70.0, null, load, false),
                Arguments.of(LOWER_OR_EQUALS, FieldType.Q0, 300.0, null, load, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.Q0, 277.0, null, load, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.Q0, 77.0, null, load, false),

                // --- LOWER --- //
                // VoltageLevel fields
                Arguments.of(LOWER, FieldType.NOMINAL_VOLTAGE, 14.0, null, load, true),
                Arguments.of(LOWER, FieldType.NOMINAL_VOLTAGE, 13.0, null, load, false),
                Arguments.of(LOWER, FieldType.NOMINAL_VOLTAGE, 12.0, null, load, false),
                // Load fields
                Arguments.of(LOWER, FieldType.P0, 99.0, null, load, true),
                Arguments.of(LOWER, FieldType.P0, 77.0, null, load, false),
                Arguments.of(LOWER, FieldType.P0, 70.0, null, load, false),
                Arguments.of(LOWER, FieldType.Q0, 300.0, null, load, true),
                Arguments.of(LOWER, FieldType.Q0, 277.0, null, load, false),
                Arguments.of(LOWER, FieldType.Q0, 270.0, null, load, false),

                // --- BETWEEN --- //
                // VoltageLevel fields
                Arguments.of(BETWEEN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 14.0), load, true),
                Arguments.of(BETWEEN, FieldType.NOMINAL_VOLTAGE, null, Set.of(13.5, 14.0), load, false),
                // Load fields
                Arguments.of(BETWEEN, FieldType.P0, null, Set.of(60.0, 80.0), load, true),
                Arguments.of(BETWEEN, FieldType.P0, null, Set.of(60.0, 70.0), load, false),
                Arguments.of(BETWEEN, FieldType.Q0, null, Set.of(270.0, 280.0), load, true),
                Arguments.of(BETWEEN, FieldType.Q0, null, Set.of(100.0, 260.0), load, false),

                // --- EXISTS --- //
                // VoltageLevel fields
                Arguments.of(EXISTS, FieldType.NOMINAL_VOLTAGE, null, null, load, true),
                Arguments.of(EXISTS, FieldType.NOMINAL_VOLTAGE, null, null, load1, false),
                // Load fields
                Arguments.of(EXISTS, FieldType.P0, null, null, load, true),
                Arguments.of(EXISTS, FieldType.P0, null, null, load1, false),
                Arguments.of(EXISTS, FieldType.Q0, null, null, load, true),
                Arguments.of(EXISTS, FieldType.Q0, null, null, load1, false),

                // --- IN --- //
                // VoltageLevel fields
                Arguments.of(IN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 13.0, 14.0), load, true),
                Arguments.of(IN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 14.0), load, false),
                // Load fields
                Arguments.of(IN, FieldType.P0, null, Set.of(12.0, 77.0, 100.0), load, true),
                Arguments.of(IN, FieldType.P0, null, Set.of(12.0, 100.0), load, false),
                Arguments.of(IN, FieldType.Q0, null, Set.of(120.0, 277.0, 300.0), load, true),
                Arguments.of(IN, FieldType.Q0, null, Set.of(120.0, 300.0), load, false),

                // --- NOT_IN --- //
                // VoltageLevel fields
                Arguments.of(NOT_IN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 14.0), load, true),
                Arguments.of(NOT_IN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 13.0, 14.0), load, false),
                // Load fields
                Arguments.of(NOT_IN, FieldType.P0, null, Set.of(120.0, 140.0), load, true),
                Arguments.of(NOT_IN, FieldType.P0, null, Set.of(70.0, 77.0, 140.0), load, false),
                Arguments.of(NOT_IN, FieldType.Q0, null, Set.of(120.0, 300.0), load, true),
                Arguments.of(NOT_IN, FieldType.Q0, null, Set.of(120.0, 277.0, 300.0), load, false)
        );
    }

    private static Stream<Arguments> provideArgumentsForBusTest() {

        Bus bus = Mockito.mock(Bus.class);
        Mockito.when(bus.getType()).thenReturn(IdentifiableType.BUS);
        // VoltageLevel fields
        VoltageLevel voltageLevel = Mockito.mock(VoltageLevel.class);
        Mockito.when(bus.getVoltageLevel()).thenReturn(voltageLevel);
        Mockito.when(voltageLevel.getNominalV()).thenReturn(13.0);

        // for testing none EXISTS
        Bus bus1 = Mockito.mock(Bus.class);
        Mockito.when(bus1.getType()).thenReturn(IdentifiableType.BUS);
        // VoltageLevel fields
        VoltageLevel voltageLevel1 = Mockito.mock(VoltageLevel.class);
        Mockito.when(bus1.getVoltageLevel()).thenReturn(voltageLevel1);
        Mockito.when(voltageLevel1.getNominalV()).thenReturn(Double.NaN);

        return Stream.of(
                // --- EQUALS --- //
                // VoltageLevel fields
                Arguments.of(EQUALS, FieldType.NOMINAL_VOLTAGE, 13.0, null, bus, true),
                Arguments.of(EQUALS, FieldType.NOMINAL_VOLTAGE, 14.0, null, bus, false),

                // --- GREATER_OR_EQUALS --- //
                // VoltageLevel fields
                Arguments.of(GREATER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 12.0, null, bus, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 13.0, null, bus, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 14.0, null, bus, false),

                // --- GREATER --- //
                // VoltageLevel fields
                Arguments.of(GREATER, FieldType.NOMINAL_VOLTAGE, 12.0, null, bus, true),
                Arguments.of(GREATER, FieldType.NOMINAL_VOLTAGE, 13.0, null, bus, false),
                Arguments.of(GREATER, FieldType.NOMINAL_VOLTAGE, 14.0, null, bus, false),

                // --- LOWER_OR_EQUALS --- //
                // VoltageLevel fields
                Arguments.of(LOWER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 14.0, null, bus, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 13.0, null, bus, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 12.0, null, bus, false),

                // --- LOWER --- //
                // VoltageLevel fields
                Arguments.of(LOWER, FieldType.NOMINAL_VOLTAGE, 14.0, null, bus, true),
                Arguments.of(LOWER, FieldType.NOMINAL_VOLTAGE, 13.0, null, bus, false),
                Arguments.of(LOWER, FieldType.NOMINAL_VOLTAGE, 12.0, null, bus, false),

                // --- BETWEEN --- //
                // VoltageLevel fields
                Arguments.of(BETWEEN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 14.0), bus, true),
                Arguments.of(BETWEEN, FieldType.NOMINAL_VOLTAGE, null, Set.of(13.5, 14.0), bus, false),

                // --- EXISTS --- //
                // VoltageLevel fields
                Arguments.of(EXISTS, FieldType.NOMINAL_VOLTAGE, null, null, bus, true),
                Arguments.of(EXISTS, FieldType.NOMINAL_VOLTAGE, null, null, bus1, false),

                // --- IN --- //
                // VoltageLevel fields
                Arguments.of(IN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 13.0, 14.0), bus, true),
                Arguments.of(IN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 14.0), bus, false),

                // --- NOT_IN --- //
                // VoltageLevel fields
                Arguments.of(NOT_IN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 14.0), bus, true),
                Arguments.of(NOT_IN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 13.0, 14.0), bus, false)
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

        // for testing none EXISTS
        BusbarSection busbarSection1 = Mockito.mock(BusbarSection.class);
        Mockito.when(busbarSection1.getType()).thenReturn(IdentifiableType.BUSBAR_SECTION);
        // VoltageLevel fields
        VoltageLevel voltageLevel1 = Mockito.mock(VoltageLevel.class);
        Terminal terminal1 = Mockito.mock(Terminal.class);
        Mockito.when(terminal1.getVoltageLevel()).thenReturn(voltageLevel1);
        Mockito.when(busbarSection1.getTerminal()).thenReturn(terminal1);
        Mockito.when(voltageLevel1.getNominalV()).thenReturn(Double.NaN);

        return Stream.of(
                // --- EQUALS --- //
                // VoltageLevel fields
                Arguments.of(EQUALS, FieldType.NOMINAL_VOLTAGE, 13.0, null, busbarSection, true),
                Arguments.of(EQUALS, FieldType.NOMINAL_VOLTAGE, 14.0, null, busbarSection, false),

                // --- GREATER_OR_EQUALS --- //
                // VoltageLevel fields
                Arguments.of(GREATER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 13.0, null, busbarSection, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 12.0, null, busbarSection, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 14.0, null, busbarSection, false),

                // --- GREATER --- //
                // VoltageLevel fields
                Arguments.of(GREATER, FieldType.NOMINAL_VOLTAGE, 12.0, null, busbarSection, true),
                Arguments.of(GREATER, FieldType.NOMINAL_VOLTAGE, 13.0, null, busbarSection, false),
                Arguments.of(GREATER, FieldType.NOMINAL_VOLTAGE, 14.0, null, busbarSection, false),

                // --- LOWER_OR_EQUALS --- //
                // VoltageLevel fields
                Arguments.of(LOWER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 13.0, null, busbarSection, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 14.0, null, busbarSection, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 12.0, null, busbarSection, false),

                // --- LOWER --- //
                // VoltageLevel fields
                Arguments.of(LOWER, FieldType.NOMINAL_VOLTAGE, 14.0, null, busbarSection, true),
                Arguments.of(LOWER, FieldType.NOMINAL_VOLTAGE, 13.0, null, busbarSection, false),
                Arguments.of(LOWER, FieldType.NOMINAL_VOLTAGE, 12.0, null, busbarSection, false),

                // --- BETWEEN --- //
                // VoltageLevel fields
                Arguments.of(BETWEEN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 14.0), busbarSection, true),
                Arguments.of(BETWEEN, FieldType.NOMINAL_VOLTAGE, null, Set.of(13.5, 14.0), busbarSection, false),

                // --- EXISTS --- //
                // VoltageLevel fields
                Arguments.of(EXISTS, FieldType.NOMINAL_VOLTAGE, null, null, busbarSection, true),
                Arguments.of(EXISTS, FieldType.NOMINAL_VOLTAGE, null, null, busbarSection1, false),

                // --- IN --- //
                // VoltageLevel fields
                Arguments.of(IN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 13.0, 14.0), busbarSection, true),
                Arguments.of(IN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 14.0), busbarSection, false),

                // --- NOT_IN --- //
                // VoltageLevel fields
                Arguments.of(NOT_IN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 14.0), busbarSection, true),
                Arguments.of(NOT_IN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 13.0, 14.0), busbarSection, false)
        );
    }

    private static Stream<Arguments> provideArgumentsForShuntCompensatorTest() {

        ShuntCompensator shuntCompensator = Mockito.mock(ShuntCompensator.class);
        Mockito.when(shuntCompensator.getType()).thenReturn(IdentifiableType.SHUNT_COMPENSATOR);

        Mockito.when(shuntCompensator.getMaximumSectionCount()).thenReturn(3);
        Mockito.when(shuntCompensator.getSectionCount()).thenReturn(3);
        Mockito.when(shuntCompensator.getModel(ShuntCompensatorLinearModel.class)).thenReturn(new ShuntCompensatorLinearModel() {
            @Override
            public double getBPerSection() {
                return 1.;
            }

            @Override
            public ShuntCompensatorLinearModel setBPerSection(double v) {
                return null;
            }

            @Override
            public double getGPerSection() {
                return 0;
            }

            @Override
            public ShuntCompensatorLinearModel setGPerSection(double v) {
                return null;
            }

            @Override
            public ShuntCompensatorLinearModel setMaximumSectionCount(int i) {
                return null;
            }
        });
        // VoltageLevel fields
        VoltageLevel voltageLevel = Mockito.mock(VoltageLevel.class);
        Terminal terminal = Mockito.mock(Terminal.class);
        Mockito.when(terminal.getVoltageLevel()).thenReturn(voltageLevel);
        Mockito.when(shuntCompensator.getTerminal()).thenReturn(terminal);
        Mockito.when(voltageLevel.getNominalV()).thenReturn(13.0);

        // for testing none EXISTS
        ShuntCompensator shuntCompensator1 = Mockito.mock(ShuntCompensator.class);
        Mockito.when(shuntCompensator1.getType()).thenReturn(IdentifiableType.SHUNT_COMPENSATOR);
        // VoltageLevel fields
        VoltageLevel voltageLevel1 = Mockito.mock(VoltageLevel.class);
        Terminal terminal1 = Mockito.mock(Terminal.class);
        Mockito.when(terminal1.getVoltageLevel()).thenReturn(voltageLevel1);
        Mockito.when(shuntCompensator1.getTerminal()).thenReturn(terminal1);
        Mockito.when(voltageLevel1.getNominalV()).thenReturn(Double.NaN);

        return Stream.of(
                // --- EQUALS --- //
                // VoltageLevel fields
                Arguments.of(EQUALS, FieldType.NOMINAL_VOLTAGE, 13.0, null, shuntCompensator, true),
                Arguments.of(EQUALS, FieldType.NOMINAL_VOLTAGE, 14.0, null, shuntCompensator, false),
                // Shunt Compensator Fields
                Arguments.of(EQUALS, FieldType.MAXIMUM_SECTION_COUNT, 3., null, shuntCompensator, true),
                Arguments.of(EQUALS, FieldType.MAXIMUM_SECTION_COUNT, 2., null, shuntCompensator, false),
                Arguments.of(EQUALS, FieldType.SECTION_COUNT, 3., null, shuntCompensator, true),
                Arguments.of(EQUALS, FieldType.SECTION_COUNT, 2., null, shuntCompensator, false),
                Arguments.of(EQUALS, FieldType.MAX_Q_AT_NOMINAL_V, 507., null, shuntCompensator, true),
                Arguments.of(EQUALS, FieldType.MAX_Q_AT_NOMINAL_V, 2., null, shuntCompensator, false),
                Arguments.of(EQUALS, FieldType.SWITCHED_ON_Q_AT_NOMINAL_V, 507., null, shuntCompensator, true),
                Arguments.of(EQUALS, FieldType.SWITCHED_ON_Q_AT_NOMINAL_V, 2., null, shuntCompensator, false),
                Arguments.of(EQUALS, FieldType.MAX_SUSCEPTANCE, 3., null, shuntCompensator, true),
                Arguments.of(EQUALS, FieldType.MAX_SUSCEPTANCE, 2., null, shuntCompensator, false),
                Arguments.of(EQUALS, FieldType.SWITCHED_ON_SUSCEPTANCE, 3., null, shuntCompensator, true),
                Arguments.of(EQUALS, FieldType.SWITCHED_ON_SUSCEPTANCE, 2., null, shuntCompensator, false),

                // --- GREATER_OR_EQUALS --- //
                // VoltageLevel fields
                Arguments.of(GREATER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 13.0, null, shuntCompensator, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 12.0, null, shuntCompensator, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 14.0, null, shuntCompensator, false),

                // Shunt Compensator Fields
                Arguments.of(GREATER_OR_EQUALS, FieldType.MAXIMUM_SECTION_COUNT, 3., null, shuntCompensator, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.MAXIMUM_SECTION_COUNT, 2., null, shuntCompensator, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.MAXIMUM_SECTION_COUNT, 4., null, shuntCompensator, false),
                Arguments.of(GREATER_OR_EQUALS, FieldType.SECTION_COUNT, 3., null, shuntCompensator, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.SECTION_COUNT, 2., null, shuntCompensator, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.SECTION_COUNT, 4., null, shuntCompensator, false),

                Arguments.of(GREATER_OR_EQUALS, FieldType.MAX_Q_AT_NOMINAL_V, 507., null, shuntCompensator, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.MAX_Q_AT_NOMINAL_V, 506., null, shuntCompensator, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.MAX_Q_AT_NOMINAL_V, 508., null, shuntCompensator, false),
                Arguments.of(GREATER_OR_EQUALS, FieldType.SWITCHED_ON_Q_AT_NOMINAL_V, 507., null, shuntCompensator, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.SWITCHED_ON_Q_AT_NOMINAL_V, 506., null, shuntCompensator, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.SWITCHED_ON_Q_AT_NOMINAL_V, 508., null, shuntCompensator, false),
                Arguments.of(GREATER_OR_EQUALS, FieldType.MAX_SUSCEPTANCE, 3., null, shuntCompensator, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.MAX_SUSCEPTANCE, 2., null, shuntCompensator, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.MAX_SUSCEPTANCE, 4., null, shuntCompensator, false),
                Arguments.of(GREATER_OR_EQUALS, FieldType.SWITCHED_ON_SUSCEPTANCE, 3., null, shuntCompensator, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.SWITCHED_ON_SUSCEPTANCE, 2., null, shuntCompensator, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.SWITCHED_ON_SUSCEPTANCE, 4., null, shuntCompensator, false),

                // --- GREATER --- //
                // VoltageLevel fields
                Arguments.of(GREATER, FieldType.NOMINAL_VOLTAGE, 12.0, null, shuntCompensator, true),
                Arguments.of(GREATER, FieldType.NOMINAL_VOLTAGE, 13.0, null, shuntCompensator, false),
                Arguments.of(GREATER, FieldType.NOMINAL_VOLTAGE, 14.0, null, shuntCompensator, false),

                // Shunt Compensator Fields
                Arguments.of(GREATER, FieldType.MAXIMUM_SECTION_COUNT, 2., null, shuntCompensator, true),
                Arguments.of(GREATER, FieldType.MAXIMUM_SECTION_COUNT, 3., null, shuntCompensator, false),
                Arguments.of(GREATER, FieldType.MAXIMUM_SECTION_COUNT, 4., null, shuntCompensator, false),
                Arguments.of(GREATER, FieldType.SECTION_COUNT, 2., null, shuntCompensator, true),
                Arguments.of(GREATER, FieldType.SECTION_COUNT, 3., null, shuntCompensator, false),
                Arguments.of(GREATER, FieldType.SECTION_COUNT, 4., null, shuntCompensator, false),

                Arguments.of(GREATER, FieldType.MAX_Q_AT_NOMINAL_V, 506., null, shuntCompensator, true),
                Arguments.of(GREATER, FieldType.MAX_Q_AT_NOMINAL_V, 507., null, shuntCompensator, false),
                Arguments.of(GREATER, FieldType.MAX_Q_AT_NOMINAL_V, 508., null, shuntCompensator, false),
                Arguments.of(GREATER, FieldType.SWITCHED_ON_Q_AT_NOMINAL_V, 506., null, shuntCompensator, true),
                Arguments.of(GREATER, FieldType.SWITCHED_ON_Q_AT_NOMINAL_V, 507., null, shuntCompensator, false),
                Arguments.of(GREATER, FieldType.SWITCHED_ON_Q_AT_NOMINAL_V, 508., null, shuntCompensator, false),
                Arguments.of(GREATER, FieldType.MAX_SUSCEPTANCE, 2., null, shuntCompensator, true),
                Arguments.of(GREATER, FieldType.MAX_SUSCEPTANCE, 3., null, shuntCompensator, false),
                Arguments.of(GREATER, FieldType.MAX_SUSCEPTANCE, 4., null, shuntCompensator, false),
                Arguments.of(GREATER, FieldType.SWITCHED_ON_SUSCEPTANCE, 2., null, shuntCompensator, true),
                Arguments.of(GREATER, FieldType.SWITCHED_ON_SUSCEPTANCE, 3., null, shuntCompensator, false),
                Arguments.of(GREATER, FieldType.SWITCHED_ON_SUSCEPTANCE, 4., null, shuntCompensator, false),

                // --- LOWER_OR_EQUALS --- //
                // VoltageLevel fields
                Arguments.of(LOWER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 13.0, null, shuntCompensator, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 14.0, null, shuntCompensator, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 12.0, null, shuntCompensator, false),

                // Shunt Compensator Fields
                Arguments.of(LOWER_OR_EQUALS, FieldType.MAXIMUM_SECTION_COUNT, 4., null, shuntCompensator, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.MAXIMUM_SECTION_COUNT, 3., null, shuntCompensator, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.MAXIMUM_SECTION_COUNT, 2., null, shuntCompensator, false),
                Arguments.of(LOWER_OR_EQUALS, FieldType.SECTION_COUNT, 4., null, shuntCompensator, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.SECTION_COUNT, 3., null, shuntCompensator, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.SECTION_COUNT, 2., null, shuntCompensator, false),

                Arguments.of(LOWER_OR_EQUALS, FieldType.MAX_Q_AT_NOMINAL_V, 507., null, shuntCompensator, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.MAX_Q_AT_NOMINAL_V, 508., null, shuntCompensator, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.MAX_Q_AT_NOMINAL_V, 506., null, shuntCompensator, false),
                Arguments.of(LOWER_OR_EQUALS, FieldType.SWITCHED_ON_Q_AT_NOMINAL_V, 507., null, shuntCompensator, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.SWITCHED_ON_Q_AT_NOMINAL_V, 508., null, shuntCompensator, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.SWITCHED_ON_Q_AT_NOMINAL_V, 506., null, shuntCompensator, false),
                Arguments.of(LOWER_OR_EQUALS, FieldType.MAX_SUSCEPTANCE, 3., null, shuntCompensator, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.MAX_SUSCEPTANCE, 4., null, shuntCompensator, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.MAX_SUSCEPTANCE, 2., null, shuntCompensator, false),
                Arguments.of(LOWER_OR_EQUALS, FieldType.SWITCHED_ON_SUSCEPTANCE, 3., null, shuntCompensator, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.SWITCHED_ON_SUSCEPTANCE, 4., null, shuntCompensator, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.SWITCHED_ON_SUSCEPTANCE, 2., null, shuntCompensator, false),

                // --- LOWER --- //
                // VoltageLevel fields
                Arguments.of(LOWER, FieldType.NOMINAL_VOLTAGE, 14.0, null, shuntCompensator, true),
                Arguments.of(LOWER, FieldType.NOMINAL_VOLTAGE, 13.0, null, shuntCompensator, false),
                Arguments.of(LOWER, FieldType.NOMINAL_VOLTAGE, 12.0, null, shuntCompensator, false),

                // Shunt Compensator Fields
                Arguments.of(LOWER, FieldType.MAXIMUM_SECTION_COUNT, 4., null, shuntCompensator, true),
                Arguments.of(LOWER, FieldType.MAXIMUM_SECTION_COUNT, 3., null, shuntCompensator, false),
                Arguments.of(LOWER, FieldType.MAXIMUM_SECTION_COUNT, 2., null, shuntCompensator, false),
                Arguments.of(LOWER, FieldType.SECTION_COUNT, 4., null, shuntCompensator, true),
                Arguments.of(LOWER, FieldType.SECTION_COUNT, 3., null, shuntCompensator, false),
                Arguments.of(LOWER, FieldType.SECTION_COUNT, 2., null, shuntCompensator, false),

                Arguments.of(LOWER, FieldType.MAX_Q_AT_NOMINAL_V, 508., null, shuntCompensator, true),
                Arguments.of(LOWER, FieldType.MAX_Q_AT_NOMINAL_V, 507., null, shuntCompensator, false),
                Arguments.of(LOWER, FieldType.MAX_Q_AT_NOMINAL_V, 506., null, shuntCompensator, false),
                Arguments.of(LOWER, FieldType.SWITCHED_ON_Q_AT_NOMINAL_V, 508., null, shuntCompensator, true),
                Arguments.of(LOWER, FieldType.SWITCHED_ON_Q_AT_NOMINAL_V, 507., null, shuntCompensator, false),
                Arguments.of(LOWER, FieldType.SWITCHED_ON_Q_AT_NOMINAL_V, 506., null, shuntCompensator, false),
                Arguments.of(LOWER, FieldType.MAX_SUSCEPTANCE, 4., null, shuntCompensator, true),
                Arguments.of(LOWER, FieldType.MAX_SUSCEPTANCE, 3., null, shuntCompensator, false),
                Arguments.of(LOWER, FieldType.MAX_SUSCEPTANCE, 2., null, shuntCompensator, false),
                Arguments.of(LOWER, FieldType.SWITCHED_ON_SUSCEPTANCE, 4., null, shuntCompensator, true),
                Arguments.of(LOWER, FieldType.SWITCHED_ON_SUSCEPTANCE, 3., null, shuntCompensator, false),
                Arguments.of(LOWER, FieldType.SWITCHED_ON_SUSCEPTANCE, 2., null, shuntCompensator, false),

                // --- BETWEEN --- //
                // VoltageLevel fields
                Arguments.of(BETWEEN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 14.0), shuntCompensator, true),
                Arguments.of(BETWEEN, FieldType.NOMINAL_VOLTAGE, null, Set.of(13.5, 14.0), shuntCompensator, false),

                // Shunt Compensator Fields
                Arguments.of(BETWEEN, FieldType.MAXIMUM_SECTION_COUNT, null, Set.of(2.0, 4.0), shuntCompensator, true),
                Arguments.of(BETWEEN, FieldType.MAXIMUM_SECTION_COUNT, null, Set.of(3.5, 4.0), shuntCompensator, false),
                Arguments.of(BETWEEN, FieldType.SECTION_COUNT, null, Set.of(2.0, 4.0), shuntCompensator, true),
                Arguments.of(BETWEEN, FieldType.SECTION_COUNT, null, Set.of(3.5, 4.0), shuntCompensator, false),

                Arguments.of(BETWEEN, FieldType.MAX_Q_AT_NOMINAL_V, null, Set.of(506., 508.), shuntCompensator, true),
                Arguments.of(BETWEEN, FieldType.MAX_Q_AT_NOMINAL_V, null, Set.of(508., 509.), shuntCompensator, false),
                Arguments.of(BETWEEN, FieldType.SWITCHED_ON_Q_AT_NOMINAL_V, null, Set.of(506., 508.), shuntCompensator, true),
                Arguments.of(BETWEEN, FieldType.SWITCHED_ON_Q_AT_NOMINAL_V, null, Set.of(508., 509.), shuntCompensator, false),
                Arguments.of(BETWEEN, FieldType.MAX_SUSCEPTANCE, null, Set.of(2., 4.), shuntCompensator, true),
                Arguments.of(BETWEEN, FieldType.MAX_SUSCEPTANCE, null, Set.of(4., 6.), shuntCompensator, false),
                Arguments.of(BETWEEN, FieldType.SWITCHED_ON_SUSCEPTANCE, null, Set.of(2., 4.), shuntCompensator, true),
                Arguments.of(BETWEEN, FieldType.SWITCHED_ON_SUSCEPTANCE, null, Set.of(4., 6.), shuntCompensator, false),

                // --- EXISTS --- //
                // VoltageLevel fields
                Arguments.of(EXISTS, FieldType.NOMINAL_VOLTAGE, null, null, shuntCompensator, true),
                Arguments.of(EXISTS, FieldType.NOMINAL_VOLTAGE, null, null, shuntCompensator1, false),

                // Shunt Compensator Fields

                // --- IN --- //
                // VoltageLevel fields
                Arguments.of(IN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 13.0, 14.0), shuntCompensator, true),
                Arguments.of(IN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 14.0), shuntCompensator, false),

                // Shunt Compensator Fields
                Arguments.of(IN, FieldType.MAXIMUM_SECTION_COUNT, null, Set.of(2., 3., 4.), shuntCompensator, true),
                Arguments.of(IN, FieldType.MAXIMUM_SECTION_COUNT, null, Set.of(2., 4.), shuntCompensator, false),
                Arguments.of(IN, FieldType.SECTION_COUNT, null, Set.of(2., 3., 4.), shuntCompensator, true),
                Arguments.of(IN, FieldType.SECTION_COUNT, null, Set.of(2., 4.), shuntCompensator, false),

                Arguments.of(IN, FieldType.MAX_Q_AT_NOMINAL_V, null, Set.of(507., 508.), shuntCompensator, true),
                Arguments.of(IN, FieldType.MAX_Q_AT_NOMINAL_V, null, Set.of(508., 509.), shuntCompensator, false),
                Arguments.of(IN, FieldType.SWITCHED_ON_Q_AT_NOMINAL_V, null, Set.of(507., 508.), shuntCompensator, true),
                Arguments.of(IN, FieldType.SWITCHED_ON_Q_AT_NOMINAL_V, null, Set.of(508., 509.), shuntCompensator, false),
                Arguments.of(IN, FieldType.MAX_SUSCEPTANCE, null, Set.of(3., 4.), shuntCompensator, true),
                Arguments.of(IN, FieldType.MAX_SUSCEPTANCE, null, Set.of(4., 6.), shuntCompensator, false),
                Arguments.of(IN, FieldType.SWITCHED_ON_SUSCEPTANCE, null, Set.of(3., 4.), shuntCompensator, true),
                Arguments.of(IN, FieldType.SWITCHED_ON_SUSCEPTANCE, null, Set.of(4., 6.), shuntCompensator, false),
                // --- NOT_IN --- //
                // VoltageLevel fields
                Arguments.of(NOT_IN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 14.0), shuntCompensator, true),
                Arguments.of(NOT_IN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 13.0, 14.0), shuntCompensator, false),

                // Shunt Compensator Fields
                Arguments.of(NOT_IN, FieldType.MAXIMUM_SECTION_COUNT, null, Set.of(2., 4.), shuntCompensator, true),
                Arguments.of(NOT_IN, FieldType.MAXIMUM_SECTION_COUNT, null, Set.of(2., 3., 4.), shuntCompensator, false),
                Arguments.of(NOT_IN, FieldType.SECTION_COUNT, null, Set.of(2., 4.), shuntCompensator, true),
                Arguments.of(NOT_IN, FieldType.SECTION_COUNT, null, Set.of(2., 3., 4.), shuntCompensator, false),

                Arguments.of(NOT_IN, FieldType.MAX_Q_AT_NOMINAL_V, null, Set.of(506., 508.), shuntCompensator, true),
                Arguments.of(NOT_IN, FieldType.MAX_Q_AT_NOMINAL_V, null, Set.of(507., 509.), shuntCompensator, false),
                Arguments.of(NOT_IN, FieldType.SWITCHED_ON_Q_AT_NOMINAL_V, null, Set.of(506., 508.), shuntCompensator, true),
                Arguments.of(NOT_IN, FieldType.SWITCHED_ON_Q_AT_NOMINAL_V, null, Set.of(507., 509.), shuntCompensator, false),
                Arguments.of(NOT_IN, FieldType.MAX_SUSCEPTANCE, null, Set.of(6., 4.), shuntCompensator, true),
                Arguments.of(NOT_IN, FieldType.MAX_SUSCEPTANCE, null, Set.of(3., 6.), shuntCompensator, false),
                Arguments.of(NOT_IN, FieldType.SWITCHED_ON_SUSCEPTANCE, null, Set.of(6., 4.), shuntCompensator, true),
                Arguments.of(NOT_IN, FieldType.SWITCHED_ON_SUSCEPTANCE, null, Set.of(3., 6.), shuntCompensator, false)
        );
    }

    private static Stream<Arguments> provideArgumentsForLinesTest() {

        Line line = Mockito.mock(Line.class);
        Mockito.when(line.getType()).thenReturn(IdentifiableType.LINE);
        // Line Fields

        Mockito.when(line.getR()).thenReturn(150.);
        Mockito.when(line.getX()).thenReturn(50.);
        Mockito.when(line.getG1()).thenReturn(10.e-6);
        Mockito.when(line.getG2()).thenReturn(5.e-6);
        Mockito.when(line.getB1()).thenReturn(200.e-6);
        Mockito.when(line.getB2()).thenReturn(250.e-6);

        // VoltageLevel fields
        VoltageLevel voltageLevel1 = Mockito.mock(VoltageLevel.class);
        Terminal terminal1 = Mockito.mock(Terminal.class);
        Mockito.when(terminal1.getVoltageLevel()).thenReturn(voltageLevel1);
        Mockito.when(line.getTerminal(TwoSides.ONE)).thenReturn(terminal1);
        Mockito.when(voltageLevel1.getNominalV()).thenReturn(13.);

        VoltageLevel voltageLevel2 = Mockito.mock(VoltageLevel.class);
        Terminal terminal2 = Mockito.mock(Terminal.class);
        Mockito.when(terminal2.getVoltageLevel()).thenReturn(voltageLevel2);
        Mockito.when(line.getTerminal(TwoSides.TWO)).thenReturn(terminal2);
        Mockito.when(voltageLevel2.getNominalV()).thenReturn(17.);

        // for testing none EXISTS
        Line line1 = Mockito.mock(Line.class);
        Mockito.when(line1.getType()).thenReturn(IdentifiableType.LINE);
        // VoltageLevel fields
        VoltageLevel voltageLevelNone = Mockito.mock(VoltageLevel.class);
        Terminal terminalNone = Mockito.mock(Terminal.class);
        Mockito.when(terminalNone.getVoltageLevel()).thenReturn(voltageLevelNone);
        Mockito.when(line1.getTerminal(TwoSides.ONE)).thenReturn(terminalNone);
        Mockito.when(voltageLevelNone.getNominalV()).thenReturn(Double.NaN);

        VoltageLevel voltageLevelNone2 = Mockito.mock(VoltageLevel.class);
        Terminal terminalNone2 = Mockito.mock(Terminal.class);
        Mockito.when(terminalNone2.getVoltageLevel()).thenReturn(voltageLevelNone2);
        Mockito.when(line1.getTerminal(TwoSides.TWO)).thenReturn(terminalNone2);
        Mockito.when(voltageLevelNone2.getNominalV()).thenReturn(Double.NaN);

        return Stream.of(
                // --- EQUALS --- //
                // VoltageLevel fields
                Arguments.of(EQUALS, FieldType.NOMINAL_VOLTAGE_1, 13., null, line, true),
                Arguments.of(EQUALS, FieldType.NOMINAL_VOLTAGE_1, 14., null, line, false),

                Arguments.of(EQUALS, FieldType.NOMINAL_VOLTAGE_2, 17., null, line, true),
                Arguments.of(EQUALS, FieldType.NOMINAL_VOLTAGE_2, 14., null, line, false),

                // Line fields
                Arguments.of(EQUALS, FieldType.SERIE_RESISTANCE, 150., null, line, true),
                Arguments.of(EQUALS, FieldType.SERIE_RESISTANCE, 149., null, line, false),
                Arguments.of(EQUALS, FieldType.SERIE_REACTANCE, 50., null, line, true),
                Arguments.of(EQUALS, FieldType.SERIE_REACTANCE, 49., null, line, false),
                Arguments.of(EQUALS, FieldType.SHUNT_CONDUCTANCE_1, 10., null, line, true),
                Arguments.of(EQUALS, FieldType.SHUNT_CONDUCTANCE_1, 14., null, line, false),
                Arguments.of(EQUALS, FieldType.SHUNT_CONDUCTANCE_2, 5., null, line, true),
                Arguments.of(EQUALS, FieldType.SHUNT_CONDUCTANCE_2, 14., null, line, false),
                Arguments.of(EQUALS, FieldType.SHUNT_SUSCEPTANCE_1, 200., null, line, true),
                Arguments.of(EQUALS, FieldType.SHUNT_SUSCEPTANCE_1, 14., null, line, false),
                Arguments.of(EQUALS, FieldType.SHUNT_SUSCEPTANCE_2, 250., null, line, true),
                Arguments.of(EQUALS, FieldType.SHUNT_SUSCEPTANCE_2, 14., null, line, false),

                // --- GREATER_OR_EQUALS --- //
                // VoltageLevel fields
                Arguments.of(GREATER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE_1, 12., null, line, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE_1, 13., null, line, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE_1, 14., null, line, false),

                Arguments.of(GREATER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE_2, 12., null, line, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE_2, 17., null, line, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE_2, 18., null, line, false),

                // Line fields
                Arguments.of(GREATER_OR_EQUALS, FieldType.SERIE_RESISTANCE, 149., null, line, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.SERIE_RESISTANCE, 150., null, line, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.SERIE_RESISTANCE, 151., null, line, false),
                Arguments.of(GREATER_OR_EQUALS, FieldType.SERIE_REACTANCE, 49., null, line, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.SERIE_REACTANCE, 50., null, line, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.SERIE_REACTANCE, 51., null, line, false),
                Arguments.of(GREATER_OR_EQUALS, FieldType.SHUNT_CONDUCTANCE_1, 9., null, line, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.SHUNT_CONDUCTANCE_1, 10., null, line, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.SHUNT_CONDUCTANCE_1, 14., null, line, false),
                Arguments.of(GREATER_OR_EQUALS, FieldType.SHUNT_CONDUCTANCE_2, 4., null, line, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.SHUNT_CONDUCTANCE_2, 5., null, line, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.SHUNT_CONDUCTANCE_2, 14., null, line, false),
                Arguments.of(GREATER_OR_EQUALS, FieldType.SHUNT_SUSCEPTANCE_1, 199., null, line, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.SHUNT_SUSCEPTANCE_1, 200., null, line, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.SHUNT_SUSCEPTANCE_1, 201., null, line, false),
                Arguments.of(GREATER_OR_EQUALS, FieldType.SHUNT_SUSCEPTANCE_2, 249., null, line, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.SHUNT_SUSCEPTANCE_2, 250., null, line, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.SHUNT_SUSCEPTANCE_2, 251., null, line, false),

                // --- GREATER --- //
                // VoltageLevel fields
                Arguments.of(GREATER, FieldType.NOMINAL_VOLTAGE_1, 12., null, line, true),
                Arguments.of(GREATER, FieldType.NOMINAL_VOLTAGE_1, 13., null, line, false),
                Arguments.of(GREATER, FieldType.NOMINAL_VOLTAGE_1, 14., null, line, false),

                Arguments.of(GREATER, FieldType.NOMINAL_VOLTAGE_2, 16., null, line, true),
                Arguments.of(GREATER, FieldType.NOMINAL_VOLTAGE_2, 17., null, line, false),
                Arguments.of(GREATER, FieldType.NOMINAL_VOLTAGE_2, 18., null, line, false),

                // Line fields
                Arguments.of(GREATER, FieldType.SERIE_RESISTANCE, 149., null, line, true),
                Arguments.of(GREATER, FieldType.SERIE_RESISTANCE, 150., null, line, false),
                Arguments.of(GREATER, FieldType.SERIE_RESISTANCE, 151., null, line, false),
                Arguments.of(GREATER, FieldType.SERIE_REACTANCE, 49., null, line, true),
                Arguments.of(GREATER, FieldType.SERIE_REACTANCE, 50., null, line, false),
                Arguments.of(GREATER, FieldType.SERIE_REACTANCE, 51., null, line, false),
                Arguments.of(GREATER, FieldType.SHUNT_CONDUCTANCE_1, 9., null, line, true),
                Arguments.of(GREATER, FieldType.SHUNT_CONDUCTANCE_1, 10., null, line, false),
                Arguments.of(GREATER, FieldType.SHUNT_CONDUCTANCE_1, 14., null, line, false),
                Arguments.of(GREATER, FieldType.SHUNT_CONDUCTANCE_2, 4., null, line, true),
                Arguments.of(GREATER, FieldType.SHUNT_CONDUCTANCE_2, 5., null, line, false),
                Arguments.of(GREATER, FieldType.SHUNT_CONDUCTANCE_2, 14., null, line, false),
                Arguments.of(GREATER, FieldType.SHUNT_SUSCEPTANCE_1, 199., null, line, true),
                Arguments.of(GREATER, FieldType.SHUNT_SUSCEPTANCE_1, 200., null, line, false),
                Arguments.of(GREATER, FieldType.SHUNT_SUSCEPTANCE_1, 201., null, line, false),
                Arguments.of(GREATER, FieldType.SHUNT_SUSCEPTANCE_2, 249., null, line, true),
                Arguments.of(GREATER, FieldType.SHUNT_SUSCEPTANCE_2, 250., null, line, false),
                Arguments.of(GREATER, FieldType.SHUNT_SUSCEPTANCE_2, 251., null, line, false),

                // --- LOWER_OR_EQUALS --- //
                // VoltageLevel fields
                Arguments.of(LOWER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE_1, 14., null, line, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE_1, 13., null, line, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE_1, 12., null, line, false),

                Arguments.of(LOWER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE_2, 18., null, line, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE_2, 17., null, line, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE_2, 12., null, line, false),

                Arguments.of(LOWER_OR_EQUALS, FieldType.SERIE_RESISTANCE, 151., null, line, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.SERIE_RESISTANCE, 150., null, line, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.SERIE_RESISTANCE, 149., null, line, false),
                Arguments.of(LOWER_OR_EQUALS, FieldType.SERIE_REACTANCE, 51., null, line, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.SERIE_REACTANCE, 50., null, line, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.SERIE_REACTANCE, 49., null, line, false),
                Arguments.of(LOWER_OR_EQUALS, FieldType.SHUNT_CONDUCTANCE_1, 11., null, line, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.SHUNT_CONDUCTANCE_1, 10., null, line, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.SHUNT_CONDUCTANCE_1, 9., null, line, false),
                Arguments.of(LOWER_OR_EQUALS, FieldType.SHUNT_CONDUCTANCE_2, 6., null, line, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.SHUNT_CONDUCTANCE_2, 5., null, line, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.SHUNT_CONDUCTANCE_2, 4., null, line, false),
                Arguments.of(LOWER_OR_EQUALS, FieldType.SHUNT_SUSCEPTANCE_1, 201., null, line, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.SHUNT_SUSCEPTANCE_1, 200., null, line, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.SHUNT_SUSCEPTANCE_1, 199., null, line, false),
                Arguments.of(LOWER_OR_EQUALS, FieldType.SHUNT_SUSCEPTANCE_2, 251., null, line, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.SHUNT_SUSCEPTANCE_2, 250., null, line, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.SHUNT_SUSCEPTANCE_2, 249., null, line, false),

                // --- LOWER --- //
                // VoltageLevel fields
                Arguments.of(LOWER, FieldType.NOMINAL_VOLTAGE_1, 14., null, line, true),
                Arguments.of(LOWER, FieldType.NOMINAL_VOLTAGE_1, 13., null, line, false),
                Arguments.of(LOWER, FieldType.NOMINAL_VOLTAGE_1, 12., null, line, false),

                Arguments.of(LOWER, FieldType.NOMINAL_VOLTAGE_2, 18., null, line, true),
                Arguments.of(LOWER, FieldType.NOMINAL_VOLTAGE_2, 17., null, line, false),
                Arguments.of(LOWER, FieldType.NOMINAL_VOLTAGE_2, 12., null, line, false),

                Arguments.of(LOWER, FieldType.SERIE_RESISTANCE, 151., null, line, true),
                Arguments.of(LOWER, FieldType.SERIE_RESISTANCE, 150., null, line, false),
                Arguments.of(LOWER, FieldType.SERIE_RESISTANCE, 149., null, line, false),
                Arguments.of(LOWER, FieldType.SERIE_REACTANCE, 51., null, line, true),
                Arguments.of(LOWER, FieldType.SERIE_REACTANCE, 50., null, line, false),
                Arguments.of(LOWER, FieldType.SERIE_REACTANCE, 49., null, line, false),
                Arguments.of(LOWER, FieldType.SHUNT_CONDUCTANCE_1, 11., null, line, true),
                Arguments.of(LOWER, FieldType.SHUNT_CONDUCTANCE_1, 10., null, line, false),
                Arguments.of(LOWER, FieldType.SHUNT_CONDUCTANCE_1, 9., null, line, false),
                Arguments.of(LOWER, FieldType.SHUNT_CONDUCTANCE_2, 6., null, line, true),
                Arguments.of(LOWER, FieldType.SHUNT_CONDUCTANCE_2, 5., null, line, false),
                Arguments.of(LOWER, FieldType.SHUNT_CONDUCTANCE_2, 4., null, line, false),
                Arguments.of(LOWER, FieldType.SHUNT_SUSCEPTANCE_1, 201., null, line, true),
                Arguments.of(LOWER, FieldType.SHUNT_SUSCEPTANCE_1, 200., null, line, false),
                Arguments.of(LOWER, FieldType.SHUNT_SUSCEPTANCE_1, 199., null, line, false),
                Arguments.of(LOWER, FieldType.SHUNT_SUSCEPTANCE_2, 251., null, line, true),
                Arguments.of(LOWER, FieldType.SHUNT_SUSCEPTANCE_2, 250., null, line, false),
                Arguments.of(LOWER, FieldType.SHUNT_SUSCEPTANCE_2, 249., null, line, false),

                // --- BETWEEN --- //
                // VoltageLevel fields
                Arguments.of(BETWEEN, FieldType.NOMINAL_VOLTAGE_1, null, Set.of(12., 14.), line, true),
                Arguments.of(BETWEEN, FieldType.NOMINAL_VOLTAGE_1, null, Set.of(13.5, 14.), line, false),

                Arguments.of(BETWEEN, FieldType.NOMINAL_VOLTAGE_2, null, Set.of(16., 18.), line, true),
                Arguments.of(BETWEEN, FieldType.NOMINAL_VOLTAGE_2, null, Set.of(17.5, 19.), line, false),

                Arguments.of(BETWEEN, FieldType.SERIE_RESISTANCE, null, Set.of(120., 160.), line, true),
                Arguments.of(BETWEEN, FieldType.SERIE_RESISTANCE, null, Set.of(120., 140.), line, false),
                Arguments.of(BETWEEN, FieldType.SERIE_REACTANCE, null, Set.of(49., 51.), line, true),
                Arguments.of(BETWEEN, FieldType.SERIE_REACTANCE, null, Set.of(12., 14.), line, false),
                Arguments.of(BETWEEN, FieldType.SHUNT_CONDUCTANCE_1, null, Set.of(9., 14.), line, true),
                Arguments.of(BETWEEN, FieldType.SHUNT_CONDUCTANCE_1, null, Set.of(12., 14.), line, false),
                Arguments.of(BETWEEN, FieldType.SHUNT_CONDUCTANCE_2, null, Set.of(2., 6.), line, true),
                Arguments.of(BETWEEN, FieldType.SHUNT_CONDUCTANCE_2, null, Set.of(12., 14.), line, false),
                Arguments.of(BETWEEN, FieldType.SHUNT_SUSCEPTANCE_1, null, Set.of(199., 200.), line, true),
                Arguments.of(BETWEEN, FieldType.SHUNT_SUSCEPTANCE_1, null, Set.of(201., 204.), line, false),
                Arguments.of(BETWEEN, FieldType.SHUNT_SUSCEPTANCE_2, null, Set.of(245., 255.), line, true),
                Arguments.of(BETWEEN, FieldType.SHUNT_SUSCEPTANCE_2, null, Set.of(240., 249.), line, false),

                // --- EXISTS --- //
                // VoltageLevel fields
                Arguments.of(EXISTS, FieldType.NOMINAL_VOLTAGE_1, null, null, line, true),
                Arguments.of(EXISTS, FieldType.NOMINAL_VOLTAGE_1, null, null, line1, false),

                Arguments.of(EXISTS, FieldType.NOMINAL_VOLTAGE_2, null, null, line, true),
                Arguments.of(EXISTS, FieldType.NOMINAL_VOLTAGE_2, null, null, line1, false),

                // --- IN --- //
                // VoltageLevel fields
                Arguments.of(IN, FieldType.NOMINAL_VOLTAGE_1, null, Set.of(12., 13., 14.), line, true),
                Arguments.of(IN, FieldType.NOMINAL_VOLTAGE_1, null, Set.of(12., 14.), line, false),

                Arguments.of(IN, FieldType.NOMINAL_VOLTAGE_2, null, Set.of(12., 17., 14.), line, true),
                Arguments.of(IN, FieldType.NOMINAL_VOLTAGE_2, null, Set.of(12., 14.), line, false),

                Arguments.of(IN, FieldType.SERIE_RESISTANCE, null, Set.of(120., 150.), line, true),
                Arguments.of(IN, FieldType.SERIE_RESISTANCE, null, Set.of(120., 140.), line, false),
                Arguments.of(IN, FieldType.SERIE_REACTANCE, null, Set.of(49., 50.), line, true),
                Arguments.of(IN, FieldType.SERIE_REACTANCE, null, Set.of(12., 14.), line, false),
                Arguments.of(IN, FieldType.SHUNT_CONDUCTANCE_1, null, Set.of(9., 10.), line, true),
                Arguments.of(IN, FieldType.SHUNT_CONDUCTANCE_1, null, Set.of(12., 14.), line, false),
                Arguments.of(IN, FieldType.SHUNT_CONDUCTANCE_2, null, Set.of(2., 5., 14.), line, true),
                Arguments.of(IN, FieldType.SHUNT_CONDUCTANCE_2, null, Set.of(2., 14.), line, false),
                Arguments.of(IN, FieldType.SHUNT_SUSCEPTANCE_1, null, Set.of(199., 200.), line, true),
                Arguments.of(IN, FieldType.SHUNT_SUSCEPTANCE_1, null, Set.of(199., 204.), line, false),
                Arguments.of(IN, FieldType.SHUNT_SUSCEPTANCE_2, null, Set.of(250., 240.), line, true),
                Arguments.of(IN, FieldType.SHUNT_SUSCEPTANCE_2, null, Set.of(240., 249.), line, false),

                // --- NOT_IN --- //
                // VoltageLevel fields
                Arguments.of(NOT_IN, FieldType.NOMINAL_VOLTAGE_1, null, Set.of(12., 14.), line, true),
                Arguments.of(NOT_IN, FieldType.NOMINAL_VOLTAGE_1, null, Set.of(12., 13., 14.), line, false),

                Arguments.of(NOT_IN, FieldType.NOMINAL_VOLTAGE_2, null, Set.of(12., 14.), line, true),
                Arguments.of(NOT_IN, FieldType.NOMINAL_VOLTAGE_2, null, Set.of(12., 17., 14.), line, false),

                Arguments.of(NOT_IN, FieldType.SERIE_RESISTANCE, null, Set.of(120., 149.), line, true),
                Arguments.of(NOT_IN, FieldType.SERIE_RESISTANCE, null, Set.of(120., 140., 150.), line, false),
                Arguments.of(NOT_IN, FieldType.SERIE_REACTANCE, null, Set.of(49., 51.), line, true),
                Arguments.of(NOT_IN, FieldType.SERIE_REACTANCE, null, Set.of(50., 49., 51.), line, false),
                Arguments.of(NOT_IN, FieldType.SHUNT_CONDUCTANCE_1, null, Set.of(9., 11.), line, true),
                Arguments.of(NOT_IN, FieldType.SHUNT_CONDUCTANCE_1, null, Set.of(9., 10., 11.), line, false),
                Arguments.of(NOT_IN, FieldType.SHUNT_CONDUCTANCE_2, null, Set.of(4., 6.), line, true),
                Arguments.of(NOT_IN, FieldType.SHUNT_CONDUCTANCE_2, null, Set.of(4., 5., 6.), line, false),
                Arguments.of(NOT_IN, FieldType.SHUNT_SUSCEPTANCE_1, null, Set.of(199., 201.), line, true),
                Arguments.of(NOT_IN, FieldType.SHUNT_SUSCEPTANCE_1, null, Set.of(199., 200., 201.), line, false),
                Arguments.of(NOT_IN, FieldType.SHUNT_SUSCEPTANCE_2, null, Set.of(249., 251.), line, true),
                Arguments.of(NOT_IN, FieldType.SHUNT_SUSCEPTANCE_2, null, Set.of(249., 250., 251.), line, false)
        );
    }

    private static Stream<Arguments> provideArgumentsForBatteryTest() {

        Battery battery = Mockito.mock(Battery.class);
        Mockito.when(battery.getType()).thenReturn(IdentifiableType.BATTERY);
        Mockito.when(battery.getMinP()).thenReturn(-5.0);
        Mockito.when(battery.getMaxP()).thenReturn(5.0);
        Mockito.when(battery.getTargetP()).thenReturn(3.0);
        Mockito.when(battery.getTargetQ()).thenReturn(1.0);
        // VoltageLevel fields
        VoltageLevel voltageLevel = Mockito.mock(VoltageLevel.class);
        Terminal terminal = Mockito.mock(Terminal.class);
        Mockito.when(terminal.getVoltageLevel()).thenReturn(voltageLevel);
        Mockito.when(battery.getTerminal()).thenReturn(terminal);
        Mockito.when(voltageLevel.getNominalV()).thenReturn(13.0);

        // for testing none EXISTS
        Battery battery1 = Mockito.mock(Battery.class);

        Mockito.when(battery1.getType()).thenReturn(IdentifiableType.BATTERY);
        Mockito.when(battery1.getMinP()).thenReturn(Double.NaN);
        Mockito.when(battery1.getMaxP()).thenReturn(Double.NaN);
        Mockito.when(battery1.getTargetP()).thenReturn(Double.NaN);
        Mockito.when(battery1.getTargetQ()).thenReturn(Double.NaN);
        // VoltageLevel fields
        VoltageLevel voltageLevel1 = Mockito.mock(VoltageLevel.class);
        Terminal terminal1 = Mockito.mock(Terminal.class);
        Mockito.when(terminal1.getVoltageLevel()).thenReturn(voltageLevel1);
        Mockito.when(battery1.getTerminal()).thenReturn(terminal1);
        Mockito.when(voltageLevel1.getNominalV()).thenReturn(Double.NaN);

        return Stream.of(
                // --- EQUALS --- //
                // VoltageLevel fields
                Arguments.of(EQUALS, FieldType.NOMINAL_VOLTAGE, 13.0, null, battery, true),
                Arguments.of(EQUALS, FieldType.NOMINAL_VOLTAGE, 14.0, null, battery, false),
                //Battery fields
                Arguments.of(EQUALS, FieldType.MIN_P, -5.0, null, battery, true),
                Arguments.of(EQUALS, FieldType.MIN_P, -4.0, null, battery, false),
                Arguments.of(EQUALS, FieldType.MAX_P, 5.0, null, battery, true),
                Arguments.of(EQUALS, FieldType.MAX_P, 4.0, null, battery, false),
                Arguments.of(EQUALS, FieldType.TARGET_P, 3.0, null, battery, true),
                Arguments.of(EQUALS, FieldType.TARGET_P, 4.0, null, battery, false),
                Arguments.of(EQUALS, FieldType.TARGET_Q, 1.0, null, battery, true),
                Arguments.of(EQUALS, FieldType.TARGET_Q, 0.0, null, battery, false),

                // --- GREATER_OR_EQUALS --- //
                // VoltageLevel fields
                Arguments.of(GREATER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 12.0, null, battery, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 13.0, null, battery, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 14.0, null, battery, false),
                //Battery fields
                Arguments.of(GREATER_OR_EQUALS, FieldType.MIN_P, -6.0, null, battery, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.MIN_P, -5.0, null, battery, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.MIN_P, -3.0, null, battery, false),
                Arguments.of(GREATER_OR_EQUALS, FieldType.MAX_P, 4.0, null, battery, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.MAX_P, 5.0, null, battery, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.MAX_P, 6.0, null, battery, false),
                Arguments.of(GREATER_OR_EQUALS, FieldType.TARGET_P, 2.0, null, battery, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.TARGET_P, 3.0, null, battery, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.TARGET_P, 6.0, null, battery, false),
                Arguments.of(GREATER_OR_EQUALS, FieldType.TARGET_Q, 0.0, null, battery, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.TARGET_Q, 1.0, null, battery, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.TARGET_Q, 2.0, null, battery, false),
                // --- GREATER --- //
                // VoltageLevel fields
                Arguments.of(GREATER, FieldType.NOMINAL_VOLTAGE, 12.0, null, battery, true),
                Arguments.of(GREATER, FieldType.NOMINAL_VOLTAGE, 13.0, null, battery, false),
                Arguments.of(GREATER, FieldType.NOMINAL_VOLTAGE, 14.0, null, battery, false),
                //Battery fields
                Arguments.of(GREATER, FieldType.MIN_P, -6.0, null, battery, true),
                Arguments.of(GREATER, FieldType.MIN_P, -5.0, null, battery, false),
                Arguments.of(GREATER, FieldType.MIN_P, -4.0, null, battery, false),
                Arguments.of(GREATER, FieldType.MAX_P, 2.0, null, battery, true),
                Arguments.of(GREATER, FieldType.MAX_P, 5.0, null, battery, false),
                Arguments.of(GREATER, FieldType.MAX_P, 6.0, null, battery, false),
                Arguments.of(GREATER, FieldType.TARGET_P, 2.0, null, battery, true),
                Arguments.of(GREATER, FieldType.TARGET_P, 5.0, null, battery, false),
                Arguments.of(GREATER, FieldType.TARGET_P, 3.0, null, battery, false),
                Arguments.of(GREATER, FieldType.TARGET_Q, 0.0, null, battery, true),
                Arguments.of(GREATER, FieldType.TARGET_Q, 2.0, null, battery, false),
                Arguments.of(GREATER, FieldType.TARGET_Q, 3.0, null, battery, false),

                // --- LOWER_OR_EQUALS --- //
                // VoltageLevel fields
                Arguments.of(LOWER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 14.0, null, battery, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 13.0, null, battery, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 12.0, null, battery, false),
                //Battery fields
                Arguments.of(LOWER_OR_EQUALS, FieldType.MIN_P, -4.0, null, battery, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.MIN_P, -5.0, null, battery, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.MIN_P, -6.0, null, battery, false),
                Arguments.of(LOWER_OR_EQUALS, FieldType.MAX_P, 7.0, null, battery, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.MAX_P, 5.0, null, battery, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.MAX_P, 2.0, null, battery, false),
                Arguments.of(LOWER_OR_EQUALS, FieldType.TARGET_P, 5.0, null, battery, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.TARGET_P, 3.0, null, battery, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.TARGET_P, 2.0, null, battery, false),
                Arguments.of(LOWER_OR_EQUALS, FieldType.TARGET_Q, 2.0, null, battery, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.TARGET_Q, 1.0, null, battery, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.TARGET_Q, 0.0, null, battery, false),

                // --- LOWER --- //
                // VoltageLevel fields
                Arguments.of(LOWER, FieldType.NOMINAL_VOLTAGE, 14.0, null, battery, true),
                Arguments.of(LOWER, FieldType.NOMINAL_VOLTAGE, 13.0, null, battery, false),
                Arguments.of(LOWER, FieldType.NOMINAL_VOLTAGE, 12.0, null, battery, false),
                //Battery fields
                Arguments.of(LOWER, FieldType.MIN_P, -4.0, null, battery, true),
                Arguments.of(LOWER, FieldType.MIN_P, -5.0, null, battery, false),
                Arguments.of(LOWER, FieldType.MIN_P, -6.0, null, battery, false),
                Arguments.of(LOWER, FieldType.MAX_P, 7.0, null, battery, true),
                Arguments.of(LOWER, FieldType.MAX_P, 5.0, null, battery, false),
                Arguments.of(LOWER, FieldType.MAX_P, 2.0, null, battery, false),
                Arguments.of(LOWER, FieldType.TARGET_P, 5.0, null, battery, true),
                Arguments.of(LOWER, FieldType.TARGET_P, 3.0, null, battery, false),
                Arguments.of(LOWER, FieldType.TARGET_P, 2.0, null, battery, false),
                Arguments.of(LOWER, FieldType.TARGET_Q, 2.0, null, battery, true),
                Arguments.of(LOWER, FieldType.TARGET_Q, 1.0, null, battery, false),
                Arguments.of(LOWER, FieldType.TARGET_Q, 0.0, null, battery, false),
                // --- BETWEEN --- //
                // VoltageLevel fields
                Arguments.of(BETWEEN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 14.0), battery, true),
                Arguments.of(BETWEEN, FieldType.NOMINAL_VOLTAGE, null, Set.of(13.5, 14.0), battery, false),
                //Battery fields
                Arguments.of(BETWEEN, FieldType.MIN_P, null, Set.of(-3.0, -14.0), battery, true),
                Arguments.of(BETWEEN, FieldType.MIN_P, null, Set.of(-12.0, -14.0), battery, false),
                Arguments.of(BETWEEN, FieldType.MAX_P, null, Set.of(2.0, 14.0), battery, true),
                Arguments.of(BETWEEN, FieldType.MAX_P, null, Set.of(12.0, 14.0), battery, false),
                Arguments.of(BETWEEN, FieldType.TARGET_P, null, Set.of(2.0, 14.0), battery, true),
                Arguments.of(BETWEEN, FieldType.TARGET_P, null, Set.of(12.0, 14.0), battery, false),
                Arguments.of(BETWEEN, FieldType.TARGET_Q, null, Set.of(0.0, 14.0), battery, true),
                Arguments.of(BETWEEN, FieldType.TARGET_Q, null, Set.of(2.0, 14.0), battery, false),
                // --- EXISTS --- //
                // VoltageLevel fields
                Arguments.of(EXISTS, FieldType.NOMINAL_VOLTAGE, null, null, battery, true),
                Arguments.of(EXISTS, FieldType.NOMINAL_VOLTAGE, null, null, battery1, false),
                //Battery fields
                Arguments.of(EXISTS, FieldType.MIN_P, null, null, battery, true),
                Arguments.of(EXISTS, FieldType.MIN_P, null, null, battery1, false),
                Arguments.of(EXISTS, FieldType.MAX_P, null, null, battery, true),
                Arguments.of(EXISTS, FieldType.MAX_P, null, null, battery1, false),
                Arguments.of(EXISTS, FieldType.TARGET_P, null, null, battery, true),
                Arguments.of(EXISTS, FieldType.TARGET_P, null, null, battery1, false),
                Arguments.of(EXISTS, FieldType.TARGET_Q, null, null, battery, true),
                Arguments.of(EXISTS, FieldType.TARGET_Q, null, null, battery1, false),

                // --- IN --- //
                // VoltageLevel fields
                Arguments.of(IN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 13.0, 14.0), battery, true),
                Arguments.of(IN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 14.0), battery, false),
                //Battery fields
                Arguments.of(IN, FieldType.MIN_P, null, Set.of(-3.0, -5.0, -14.0), battery, true),
                Arguments.of(IN, FieldType.MIN_P, null, Set.of(-12.0, -6.0, -14.0), battery, false),
                Arguments.of(IN, FieldType.MAX_P, null, Set.of(2.0, 5.0, 14.0), battery, true),
                Arguments.of(IN, FieldType.MAX_P, null, Set.of(12.0, 6.0, 14.0), battery, false),
                Arguments.of(IN, FieldType.TARGET_P, null, Set.of(2.0, 3.0, 14.0), battery, true),
                Arguments.of(IN, FieldType.TARGET_P, null, Set.of(12.0, 4.0, 14.0), battery, false),
                Arguments.of(IN, FieldType.TARGET_Q, null, Set.of(0.0, 1.0, 14.0), battery, true),
                Arguments.of(IN, FieldType.TARGET_Q, null, Set.of(2.0, 3.0, 14.0), battery, false),
                // --- NOT_IN --- //
                // VoltageLevel fields
                Arguments.of(NOT_IN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 14.0), battery, true),
                Arguments.of(NOT_IN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 13.0, 14.0), battery, false),
                //Battery fields
                Arguments.of(NOT_IN, FieldType.MIN_P, null, Set.of(-3.0, -14.0), battery, true),
                Arguments.of(NOT_IN, FieldType.MIN_P, null, Set.of(-12.0, -5.0, -14.0), battery, false),
                Arguments.of(NOT_IN, FieldType.MAX_P, null, Set.of(2.0, 14.0), battery, true),
                Arguments.of(NOT_IN, FieldType.MAX_P, null, Set.of(12.0, 5.0, 14.0), battery, false),
                Arguments.of(NOT_IN, FieldType.TARGET_P, null, Set.of(2.0, 14.0), battery, true),
                Arguments.of(NOT_IN, FieldType.TARGET_P, null, Set.of(12.0, 3.0, 14.0), battery, false),
                Arguments.of(NOT_IN, FieldType.TARGET_Q, null, Set.of(0.0, 14.0), battery, true),
                Arguments.of(NOT_IN, FieldType.TARGET_Q, null, Set.of(2.0, 1.0, 14.0), battery, false)
        );
    }

    private static Stream<Arguments> provideArgumentsForVoltageLevelTest() {

        VoltageLevel voltageLevel = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel.getType()).thenReturn(IdentifiableType.VOLTAGE_LEVEL);
        Mockito.when(voltageLevel.getNominalV()).thenReturn(13.0);
        Mockito.when(voltageLevel.getLowVoltageLimit()).thenReturn(40.0);
        Mockito.when(voltageLevel.getHighVoltageLimit()).thenReturn(400.0);

        // for testing none EXISTS
        VoltageLevel voltageLevel1 = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel1.getType()).thenReturn(IdentifiableType.VOLTAGE_LEVEL);
        Mockito.when(voltageLevel1.getNominalV()).thenReturn(Double.NaN);
        Mockito.when(voltageLevel1.getLowVoltageLimit()).thenReturn(Double.NaN);
        Mockito.when(voltageLevel1.getHighVoltageLimit()).thenReturn(Double.NaN);

        return Stream.of(
            // --- EQUALS --- //
            Arguments.of(EQUALS, FieldType.NOMINAL_VOLTAGE, 13.0, null, voltageLevel, true),
            Arguments.of(EQUALS, FieldType.NOMINAL_VOLTAGE, 12.0, null, voltageLevel, false),
            Arguments.of(EQUALS, FieldType.LOW_VOLTAGE_LIMIT, 40.0, null, voltageLevel, true),
            Arguments.of(EQUALS, FieldType.LOW_VOLTAGE_LIMIT, 50.0, null, voltageLevel, false),
            Arguments.of(EQUALS, FieldType.HIGH_VOLTAGE_LIMIT, 400.0, null, voltageLevel, true),
            Arguments.of(EQUALS, FieldType.HIGH_VOLTAGE_LIMIT, 500.0, null, voltageLevel, false),

            // --- GREATER_OR_EQUALS --- //
            Arguments.of(GREATER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 12.0, null, voltageLevel, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 13.0, null, voltageLevel, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 14.0, null, voltageLevel, false),
            Arguments.of(GREATER_OR_EQUALS, FieldType.LOW_VOLTAGE_LIMIT, 30.0, null, voltageLevel, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.LOW_VOLTAGE_LIMIT, 40.0, null, voltageLevel, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.LOW_VOLTAGE_LIMIT, 50.0, null, voltageLevel, false),
            Arguments.of(GREATER_OR_EQUALS, FieldType.HIGH_VOLTAGE_LIMIT, 300.0, null, voltageLevel, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.HIGH_VOLTAGE_LIMIT, 400.0, null, voltageLevel, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.HIGH_VOLTAGE_LIMIT, 500.0, null, voltageLevel, false),

            // --- GREATER --- //
            Arguments.of(GREATER, FieldType.NOMINAL_VOLTAGE, 12.0, null, voltageLevel, true),
            Arguments.of(GREATER, FieldType.NOMINAL_VOLTAGE, 13.0, null, voltageLevel, false),
            Arguments.of(GREATER, FieldType.NOMINAL_VOLTAGE, 14.0, null, voltageLevel, false),
            Arguments.of(GREATER, FieldType.LOW_VOLTAGE_LIMIT, 30.0, null, voltageLevel, true),
            Arguments.of(GREATER, FieldType.LOW_VOLTAGE_LIMIT, 40.0, null, voltageLevel, false),
            Arguments.of(GREATER, FieldType.LOW_VOLTAGE_LIMIT, 50.0, null, voltageLevel, false),
            Arguments.of(GREATER, FieldType.HIGH_VOLTAGE_LIMIT, 300.0, null, voltageLevel, true),
            Arguments.of(GREATER, FieldType.HIGH_VOLTAGE_LIMIT, 400.0, null, voltageLevel, false),
            Arguments.of(GREATER, FieldType.HIGH_VOLTAGE_LIMIT, 500.0, null, voltageLevel, false),

            // --- LOWER_OR_EQUALS --- //
            Arguments.of(LOWER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 14.0, null, voltageLevel, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 13.0, null, voltageLevel, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 12.0, null, voltageLevel, false),
            Arguments.of(LOWER_OR_EQUALS, FieldType.LOW_VOLTAGE_LIMIT, 50.0, null, voltageLevel, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.LOW_VOLTAGE_LIMIT, 40.0, null, voltageLevel, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.LOW_VOLTAGE_LIMIT, 30.0, null, voltageLevel, false),
            Arguments.of(LOWER_OR_EQUALS, FieldType.HIGH_VOLTAGE_LIMIT, 500.0, null, voltageLevel, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.HIGH_VOLTAGE_LIMIT, 400.0, null, voltageLevel, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.HIGH_VOLTAGE_LIMIT, 300.0, null, voltageLevel, false),

            // --- LOWER --- //
            // voltageLevelerator fields
            Arguments.of(LOWER, FieldType.NOMINAL_VOLTAGE, 14.0, null, voltageLevel, true),
            Arguments.of(LOWER, FieldType.NOMINAL_VOLTAGE, 13.0, null, voltageLevel, false),
            Arguments.of(LOWER, FieldType.NOMINAL_VOLTAGE, 12.0, null, voltageLevel, false),
            Arguments.of(LOWER, FieldType.LOW_VOLTAGE_LIMIT, 50.0, null, voltageLevel, true),
            Arguments.of(LOWER, FieldType.LOW_VOLTAGE_LIMIT, 40.0, null, voltageLevel, false),
            Arguments.of(LOWER, FieldType.LOW_VOLTAGE_LIMIT, 30.0, null, voltageLevel, false),
            Arguments.of(LOWER, FieldType.HIGH_VOLTAGE_LIMIT, 500.0, null, voltageLevel, true),
            Arguments.of(LOWER, FieldType.HIGH_VOLTAGE_LIMIT, 400.0, null, voltageLevel, false),
            Arguments.of(LOWER, FieldType.HIGH_VOLTAGE_LIMIT, 300.0, null, voltageLevel, false),

            // --- BETWEEN --- //
            Arguments.of(BETWEEN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 14.0), voltageLevel, true),
            Arguments.of(BETWEEN, FieldType.NOMINAL_VOLTAGE, null, Set.of(13.5, 14.0), voltageLevel, false),
            Arguments.of(BETWEEN, FieldType.LOW_VOLTAGE_LIMIT, null, Set.of(30.0, 50.0), voltageLevel, true),
            Arguments.of(BETWEEN, FieldType.LOW_VOLTAGE_LIMIT, null, Set.of(50.0, 60.0), voltageLevel, false),
            Arguments.of(BETWEEN, FieldType.LOW_VOLTAGE_LIMIT, null, Set.of(20.0, 30.0), voltageLevel, false),
            Arguments.of(BETWEEN, FieldType.HIGH_VOLTAGE_LIMIT, null, Set.of(300.0, 500.0), voltageLevel, true),
            Arguments.of(BETWEEN, FieldType.HIGH_VOLTAGE_LIMIT, null, Set.of(500.0, 600.0), voltageLevel, false),
            Arguments.of(BETWEEN, FieldType.HIGH_VOLTAGE_LIMIT, null, Set.of(200.0, 300.0), voltageLevel, false),

            // --- EXISTS --- //
            Arguments.of(EXISTS, FieldType.NOMINAL_VOLTAGE, null, null, voltageLevel, true),
            Arguments.of(EXISTS, FieldType.NOMINAL_VOLTAGE, null, null, voltageLevel1, false),
            Arguments.of(EXISTS, FieldType.LOW_VOLTAGE_LIMIT, null, null, voltageLevel, true),
            Arguments.of(EXISTS, FieldType.LOW_VOLTAGE_LIMIT, null, null, voltageLevel1, false),
            Arguments.of(EXISTS, FieldType.HIGH_VOLTAGE_LIMIT, null, null, voltageLevel, true),
            Arguments.of(EXISTS, FieldType.HIGH_VOLTAGE_LIMIT, null, null, voltageLevel1, false),

            // --- IN --- //
            Arguments.of(IN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 13.0, 14.0), voltageLevel, true),
            Arguments.of(IN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 14.0), voltageLevel, false),
            Arguments.of(IN, FieldType.LOW_VOLTAGE_LIMIT, null, Set.of(30.0, 40.0, 50.0), voltageLevel, true),
            Arguments.of(IN, FieldType.LOW_VOLTAGE_LIMIT, null, Set.of(30.0, 50.0), voltageLevel, false),
            Arguments.of(IN, FieldType.HIGH_VOLTAGE_LIMIT, null, Set.of(300.0, 400.0, 500.0), voltageLevel, true),
            Arguments.of(IN, FieldType.HIGH_VOLTAGE_LIMIT, null, Set.of(300.0, 500.0), voltageLevel, false),

            // --- NOT_IN --- //
            Arguments.of(NOT_IN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 14.0), voltageLevel, true),
            Arguments.of(NOT_IN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 13.0, 14.0), voltageLevel, false),
            Arguments.of(NOT_IN, FieldType.LOW_VOLTAGE_LIMIT, null, Set.of(30.0, 50.0), voltageLevel, true),
            Arguments.of(NOT_IN, FieldType.LOW_VOLTAGE_LIMIT, null, Set.of(30.0, 40.0, 50.0), voltageLevel, false),
            Arguments.of(NOT_IN, FieldType.HIGH_VOLTAGE_LIMIT, null, Set.of(300.0, 500.0), voltageLevel, true),
            Arguments.of(NOT_IN, FieldType.HIGH_VOLTAGE_LIMIT, null, Set.of(300.0, 400.0, 500.0), voltageLevel, false)
        );
    }
}
