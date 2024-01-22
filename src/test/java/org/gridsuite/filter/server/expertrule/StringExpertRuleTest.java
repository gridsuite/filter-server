package org.gridsuite.filter.server.expertrule;

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

class StringExpertRuleTest {

    @ParameterizedTest
    @MethodSource({"provideArgumentsForGeneratorTest"})
    void testEvaluateRule(OperatorType operator, FieldType field, String value, Set<String> values, Identifiable<?> equipment, boolean expected) {
        StringExpertRule rule = StringExpertRule.builder().operator(operator).field(field).value(value).values(values).build();
        assertEquals(expected, rule.evaluateRule(equipment));
    }

    private static Stream<Arguments> provideArgumentsForGeneratorTest() {

        // --- Generator --- //
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
}
