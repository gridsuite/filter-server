package org.gridsuite.filter.server.expertrule;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import org.gridsuite.filter.server.dto.expertfilter.expertrule.BooleanExpertRule;
import org.gridsuite.filter.server.utils.expertfilter.FieldType;
import org.gridsuite.filter.server.utils.expertfilter.OperatorType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.util.stream.Stream;

import static org.gridsuite.filter.server.utils.expertfilter.OperatorType.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BooleanExpertRuleTest {

    @ParameterizedTest
    @MethodSource({
        "provideArgumentsForTestWithException"
    })
    void testEvaluateRuleWithException(OperatorType operator, FieldType field, Identifiable<?> equipment, Class expectedException) {
        BooleanExpertRule rule = BooleanExpertRule.builder().operator(operator).field(field).build();
        assertThrows(expectedException, () -> rule.evaluateRule(equipment));
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
                Arguments.of(IS, FieldType.VOLTAGE_REGULATOR_ON, generator, PowsyblException.class)
        );
    }

    @ParameterizedTest
    @MethodSource({"provideArgumentsForGeneratorTest", "provideArgumentsForBatteryTest", "provideArgumentsForTwoWindingTransformerTest"
    })
    void testEvaluateRule(OperatorType operator, FieldType field, boolean value, Identifiable<?> equipment, boolean expected) {
        BooleanExpertRule rule = BooleanExpertRule.builder().operator(operator).field(field).value(value).build();
        assertEquals(expected, rule.evaluateRule(equipment));
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
                Arguments.of(NOT_EQUALS, FieldType.RATIO_REGULATING, false, twoWindingsTransformer2, false),
                Arguments.of(NOT_EQUALS, FieldType.RATIO_REGULATING, true, twoWindingsTransformer2, true)
        );
    }
}
