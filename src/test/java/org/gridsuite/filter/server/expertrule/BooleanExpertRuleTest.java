package org.gridsuite.filter.server.expertrule;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import org.gridsuite.filter.server.FilterService;
import org.gridsuite.filter.server.dto.expertfilter.expertrule.BooleanExpertRule;
import org.gridsuite.filter.server.utils.expertfilter.FieldType;
import org.gridsuite.filter.server.utils.expertfilter.OperatorType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.stream.Stream;

import static org.gridsuite.filter.server.utils.expertfilter.OperatorType.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@RunWith(SpringRunner.class)
@SpringBootTest
class BooleanExpertRuleTest {
    @Autowired
    private FilterService filterService;

    @ParameterizedTest
    @MethodSource({
        "provideArgumentsForTestWithException"
    })
    void testEvaluateRuleWithException(OperatorType operator, FieldType field, Identifiable<?> equipment, Class expectedException) {
        BooleanExpertRule rule = BooleanExpertRule.builder().operator(operator).field(field).build();
        assertThrows(expectedException, () -> rule.evaluateRule(equipment, filterService, new HashMap<>()));
    }

    static Stream<Arguments> provideArgumentsForTestWithException() {

        Network network = Mockito.mock(Network.class);
        Mockito.when(network.getType()).thenReturn(IdentifiableType.NETWORK);

        VoltageLevel voltageLevel = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel.getType()).thenReturn(IdentifiableType.VOLTAGE_LEVEL);

        Generator generator = Mockito.mock(Generator.class);
        Mockito.when(generator.getType()).thenReturn(IdentifiableType.GENERATOR);
        Mockito.when(generator.isVoltageRegulatorOn()).thenReturn(true);

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
                Arguments.of(IS, FieldType.VOLTAGE_REGULATOR_ON, generator, PowsyblException.class)
        );
    }

    @ParameterizedTest
    @MethodSource({
        "provideArgumentsForGeneratorTest",
        "provideArgumentsForShuntCompensatorTest",
        "provideArgumentsForBatteryTest",
        "provideArgumentsForLinesTest",
        "provideArgumentsForLoadTest",
        "provideArgumentsForTwoWindingTransformerTest"
    })
    void testEvaluateRule(OperatorType operator, FieldType field, boolean value, Identifiable<?> equipment, boolean expected) {
        BooleanExpertRule rule = BooleanExpertRule.builder().operator(operator).field(field).value(value).build();
        assertEquals(expected, rule.evaluateRule(equipment, filterService, new HashMap<>()));
    }

    private static Stream<Arguments> provideArgumentsForGeneratorTest() {

        Generator gen = Mockito.mock(Generator.class);
        Mockito.when(gen.getType()).thenReturn(IdentifiableType.GENERATOR);
        //Generator fields
        Mockito.when(gen.isVoltageRegulatorOn()).thenReturn(true);
        // Terminal fields
        Terminal terminal = Mockito.mock(Terminal.class);
        Mockito.when(terminal.isConnected()).thenReturn(true);
        Mockito.when(gen.getTerminal()).thenReturn(terminal);

        return Stream.of(
                // --- EQUALS--- //
                //Generator fields
                Arguments.of(EQUALS, FieldType.VOLTAGE_REGULATOR_ON, true, gen, true),
                Arguments.of(EQUALS, FieldType.VOLTAGE_REGULATOR_ON, false, gen, false),
                // Terminal fields
                Arguments.of(EQUALS, FieldType.CONNECTED, true, gen, true),
                Arguments.of(EQUALS, FieldType.CONNECTED, false, gen, false),

                // --- NOT_EQUALS--- //
                //Generator fields
                Arguments.of(NOT_EQUALS, FieldType.VOLTAGE_REGULATOR_ON, false, gen, true),
                Arguments.of(NOT_EQUALS, FieldType.VOLTAGE_REGULATOR_ON, true, gen, false),
                // Terminal fields
                Arguments.of(NOT_EQUALS, FieldType.CONNECTED, false, gen, true),
                Arguments.of(NOT_EQUALS, FieldType.CONNECTED, true, gen, false)
        );
    }

    private static Stream<Arguments> provideArgumentsForLoadTest() {

        Load gen = Mockito.mock(Load.class);
        Mockito.when(gen.getType()).thenReturn(IdentifiableType.LOAD);
        // Terminal fields
        Terminal terminal = Mockito.mock(Terminal.class);
        Mockito.when(terminal.isConnected()).thenReturn(true);
        Mockito.when(gen.getTerminal()).thenReturn(terminal);

        return Stream.of(
                // --- EQUALS--- //
                // Terminal fields
                Arguments.of(EQUALS, FieldType.CONNECTED, true, gen, true),
                Arguments.of(EQUALS, FieldType.CONNECTED, false, gen, false),

                // --- NOT_EQUALS--- //
                // Terminal fields
                Arguments.of(NOT_EQUALS, FieldType.CONNECTED, false, gen, true),
                Arguments.of(NOT_EQUALS, FieldType.CONNECTED, true, gen, false)
        );
    }

    private static Stream<Arguments> provideArgumentsForShuntCompensatorTest() {

        ShuntCompensator gen = Mockito.mock(ShuntCompensator.class);
        Mockito.when(gen.getType()).thenReturn(IdentifiableType.SHUNT_COMPENSATOR);
        // Terminal fields
        Terminal terminal = Mockito.mock(Terminal.class);
        Mockito.when(terminal.isConnected()).thenReturn(true);
        Mockito.when(gen.getTerminal()).thenReturn(terminal);

        return Stream.of(
                // --- EQUALS--- //
                // Terminal fields
                Arguments.of(EQUALS, FieldType.CONNECTED, true, gen, true),
                Arguments.of(EQUALS, FieldType.CONNECTED, false, gen, false),

                // --- NOT_EQUALS--- //
                // Terminal fields
                Arguments.of(NOT_EQUALS, FieldType.CONNECTED, false, gen, true),
                Arguments.of(NOT_EQUALS, FieldType.CONNECTED, true, gen, false)
        );
    }

    private static Stream<Arguments> provideArgumentsForBatteryTest() {

        Battery battery = Mockito.mock(Battery.class);
        Mockito.when(battery.getType()).thenReturn(IdentifiableType.BATTERY);
        // Terminal fields
        Terminal terminal = Mockito.mock(Terminal.class);
        Mockito.when(terminal.isConnected()).thenReturn(true);
        Mockito.when(battery.getTerminal()).thenReturn(terminal);

        return Stream.of(
                // --- EQUALS--- //
                // Terminal fields
                Arguments.of(EQUALS, FieldType.CONNECTED, true, battery, true),
                Arguments.of(EQUALS, FieldType.CONNECTED, false, battery, false),

                // --- NOT_EQUALS--- //
                // Terminal fields
                Arguments.of(NOT_EQUALS, FieldType.CONNECTED, false, battery, true),
                Arguments.of(NOT_EQUALS, FieldType.CONNECTED, true, battery, false)
        );
    }

    private static Stream<Arguments> provideArgumentsForLinesTest() {

        Line line = Mockito.mock(Line.class);
        Mockito.when(line.getType()).thenReturn(IdentifiableType.LINE);
        // Terminal fields
        Terminal terminal1 = Mockito.mock(Terminal.class);
        Mockito.when(terminal1.isConnected()).thenReturn(true);
        Mockito.when(line.getTerminal(TwoSides.ONE)).thenReturn(terminal1);

        Terminal terminal2 = Mockito.mock(Terminal.class);
        Mockito.when(terminal2.isConnected()).thenReturn(true);
        Mockito.when(line.getTerminal(TwoSides.TWO)).thenReturn(terminal2);

        return Stream.of(
                // --- EQUALS--- //
                // Terminal fields
                Arguments.of(EQUALS, FieldType.CONNECTED_1, true, line, true),
                Arguments.of(EQUALS, FieldType.CONNECTED_1, false, line, false),
                Arguments.of(EQUALS, FieldType.CONNECTED_2, true, line, true),
                Arguments.of(EQUALS, FieldType.CONNECTED_2, false, line, false),

                // --- NOT_EQUALS--- //
                // Terminal fields
                Arguments.of(NOT_EQUALS, FieldType.CONNECTED_1, false, line, true),
                Arguments.of(NOT_EQUALS, FieldType.CONNECTED_1, true, line, false),
                Arguments.of(NOT_EQUALS, FieldType.CONNECTED_2, false, line, true),
                Arguments.of(NOT_EQUALS, FieldType.CONNECTED_2, true, line, false)
        );
    }

    private static Stream<Arguments> provideArgumentsForTwoWindingTransformerTest() {

        TwoWindingsTransformer twoWindingsTransformer = Mockito.mock(TwoWindingsTransformer.class);
        Mockito.when(twoWindingsTransformer.getType()).thenReturn(IdentifiableType.TWO_WINDINGS_TRANSFORMER);
        // Terminal fields
        Terminal terminal = Mockito.mock(Terminal.class);
        Mockito.when(terminal.isConnected()).thenReturn(true);
        Mockito.when(twoWindingsTransformer.getTerminal1()).thenReturn(terminal);
        Mockito.when(twoWindingsTransformer.getTerminal2()).thenReturn(terminal);

        // RatioTapChanger fields
        RatioTapChanger ratioTapChanger = Mockito.mock(RatioTapChanger.class);
        Mockito.when(ratioTapChanger.isRegulating()).thenReturn(true);
        Mockito.when(ratioTapChanger.hasLoadTapChangingCapabilities()).thenReturn(true);
        Mockito.when(twoWindingsTransformer.getRatioTapChanger()).thenReturn(ratioTapChanger);

        // null RatioTapChanger
        TwoWindingsTransformer twoWindingsTransformer2 = Mockito.mock(TwoWindingsTransformer.class);
        Mockito.when(twoWindingsTransformer2.getType()).thenReturn(IdentifiableType.TWO_WINDINGS_TRANSFORMER);
        Mockito.when(twoWindingsTransformer2.getRatioTapChanger()).thenReturn(null);

        return Stream.of(
                // --- EQUALS--- //
                // Terminal fields
                Arguments.of(EQUALS, FieldType.CONNECTED_1, true, twoWindingsTransformer, true),
                Arguments.of(EQUALS, FieldType.CONNECTED_1, false, twoWindingsTransformer, false),
                Arguments.of(EQUALS, FieldType.CONNECTED_2, true, twoWindingsTransformer, true),
                Arguments.of(EQUALS, FieldType.CONNECTED_2, false, twoWindingsTransformer, false),

                // RatioTapChanger fields
                Arguments.of(EQUALS, FieldType.RATIO_REGULATING, true, twoWindingsTransformer, true),
                Arguments.of(EQUALS, FieldType.RATIO_REGULATING, false, twoWindingsTransformer, false),
                Arguments.of(EQUALS, FieldType.LOAD_TAP_CHANGING_CAPABILITIES, true, twoWindingsTransformer, true),
                Arguments.of(EQUALS, FieldType.LOAD_TAP_CHANGING_CAPABILITIES, false, twoWindingsTransformer, false),

                // --- NOT_EQUALS--- //
                // Terminal fields
                Arguments.of(NOT_EQUALS, FieldType.CONNECTED_1, false, twoWindingsTransformer, true),
                Arguments.of(NOT_EQUALS, FieldType.CONNECTED_1, true, twoWindingsTransformer, false),
                Arguments.of(NOT_EQUALS, FieldType.CONNECTED_2, false, twoWindingsTransformer, true),
                Arguments.of(NOT_EQUALS, FieldType.CONNECTED_2, true, twoWindingsTransformer, false),

                // RatioTapChanger fields
                Arguments.of(NOT_EQUALS, FieldType.RATIO_REGULATING, false, twoWindingsTransformer, true),
                Arguments.of(NOT_EQUALS, FieldType.RATIO_REGULATING, true, twoWindingsTransformer, false),
                Arguments.of(NOT_EQUALS, FieldType.LOAD_TAP_CHANGING_CAPABILITIES, false, twoWindingsTransformer, true),
                Arguments.of(NOT_EQUALS, FieldType.LOAD_TAP_CHANGING_CAPABILITIES, true, twoWindingsTransformer, false),

                // null RatioTapChanger
                Arguments.of(NOT_EQUALS, FieldType.RATIO_REGULATING, false, twoWindingsTransformer2, false)
        );
    }
}
