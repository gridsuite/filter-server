package org.gridsuite.filter.server.expertrule;

import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.IdentifiableType;
import com.powsybl.iidm.network.Terminal;
import org.gridsuite.filter.server.dto.expertfilter.expertrule.BooleanExpertRule;
import org.gridsuite.filter.server.utils.expertfilter.FieldType;
import org.gridsuite.filter.server.utils.expertfilter.OperatorType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.util.stream.Stream;

import static org.gridsuite.filter.server.utils.expertfilter.OperatorType.EQUALS;
import static org.gridsuite.filter.server.utils.expertfilter.OperatorType.NOT_EQUALS;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BooleanExpertRuleTest {

    @ParameterizedTest
    @MethodSource({"provideArgumentsForGeneratorTest"})
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
                // Terminal fields
                Arguments.of(EQUALS, FieldType.CONNECTED, true, gen, true),

                // --- NOT_EQUALS--- //
                //Generator fields
                Arguments.of(NOT_EQUALS, FieldType.VOLTAGE_REGULATOR_ON, false, gen, true),
                // Terminal fields
                Arguments.of(NOT_EQUALS, FieldType.CONNECTED, false, gen, true)
        );
    }
}
