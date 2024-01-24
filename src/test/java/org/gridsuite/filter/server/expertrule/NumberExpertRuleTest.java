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
                // --- Test an unsupported field for each equipment --- //
                Arguments.of(EQUALS, FieldType.RATED_S, network, PowsyblException.class),
                Arguments.of(EQUALS, FieldType.RATED_S, voltageLevel, PowsyblException.class),
                Arguments.of(EQUALS, FieldType.P0, generator, PowsyblException.class),
                Arguments.of(EQUALS, FieldType.RATED_S, load, PowsyblException.class),
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
        // VoltageLevel fields
        VoltageLevel voltageLevel = Mockito.mock(VoltageLevel.class);
        Terminal terminal = Mockito.mock(Terminal.class);
        Mockito.when(terminal.getVoltageLevel()).thenReturn(voltageLevel);
        Mockito.when(load.getTerminal()).thenReturn(terminal);
        Mockito.when(voltageLevel.getNominalV()).thenReturn(13.0);

        // for testing none EXISTS
        Load load1 = Mockito.mock(Load.class);
        Mockito.when(load1.getType()).thenReturn(IdentifiableType.LOAD);
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

                // --- GREATER_OR_EQUALS --- //
                // VoltageLevel fields
                Arguments.of(GREATER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 12.0, null, load, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 13.0, null, load, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 14.0, null, load, false),

                // --- GREATER --- //
                // VoltageLevel fields
                Arguments.of(GREATER, FieldType.NOMINAL_VOLTAGE, 12.0, null, load, true),
                Arguments.of(GREATER, FieldType.NOMINAL_VOLTAGE, 13.0, null, load, false),
                Arguments.of(GREATER, FieldType.NOMINAL_VOLTAGE, 14.0, null, load, false),

                // --- LOWER_OR_EQUALS --- //
                // VoltageLevel fields
                Arguments.of(LOWER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 14.0, null, load, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 13.0, null, load, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 12.0, null, load, false),

                // --- LOWER --- //
                // VoltageLevel fields
                Arguments.of(LOWER, FieldType.NOMINAL_VOLTAGE, 14.0, null, load, true),
                Arguments.of(LOWER, FieldType.NOMINAL_VOLTAGE, 13.0, null, load, false),
                Arguments.of(LOWER, FieldType.NOMINAL_VOLTAGE, 12.0, null, load, false),

                // --- BETWEEN --- //
                // VoltageLevel fields
                Arguments.of(BETWEEN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 14.0), load, true),
                Arguments.of(BETWEEN, FieldType.NOMINAL_VOLTAGE, null, Set.of(13.5, 14.0), load, false),

                // --- EXISTS --- //
                // VoltageLevel fields
                Arguments.of(EXISTS, FieldType.NOMINAL_VOLTAGE, null, null, load, true),
                Arguments.of(EXISTS, FieldType.NOMINAL_VOLTAGE, null, null, load1, false),

                // --- IN --- //
                // VoltageLevel fields
                Arguments.of(IN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 13.0, 14.0), load, true),
                Arguments.of(IN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 14.0), load, false),

                // --- NOT_IN --- //
                // VoltageLevel fields
                Arguments.of(NOT_IN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 14.0), load, true),
                Arguments.of(NOT_IN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 13.0, 14.0), load, false)
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
}
