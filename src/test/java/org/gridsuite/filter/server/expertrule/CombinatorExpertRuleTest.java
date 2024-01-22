package org.gridsuite.filter.server.expertrule;

import com.powsybl.iidm.network.EnergySource;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.IdentifiableType;
import org.gridsuite.filter.server.dto.expertfilter.expertrule.AbstractExpertRule;
import org.gridsuite.filter.server.dto.expertfilter.expertrule.CombinatorExpertRule;
import org.gridsuite.filter.server.dto.expertfilter.expertrule.EnumExpertRule;
import org.gridsuite.filter.server.utils.expertfilter.CombinatorType;
import org.gridsuite.filter.server.utils.expertfilter.FieldType;
import org.gridsuite.filter.server.utils.expertfilter.OperatorType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CombinatorExpertRuleTest {

    @ParameterizedTest
    @MethodSource({"provideArgumentsForTest"})
    void testEvaluateRule(CombinatorType combinatorType, List<AbstractExpertRule> rules, Identifiable<?> equipment, boolean expected) {
        CombinatorExpertRule filter = CombinatorExpertRule.builder().combinator(combinatorType).rules(rules).build();
        assertEquals(expected, filter.evaluateRule(equipment));
    }

    private static Stream<Arguments> provideArgumentsForTest() {
        Generator gen = Mockito.mock(Generator.class);
        Mockito.when(gen.getType()).thenReturn(IdentifiableType.GENERATOR);
        // Generator fields
        Mockito.when(gen.getEnergySource()).thenReturn(EnergySource.HYDRO);

        return Stream.of(
                Arguments.of(CombinatorType.AND, List.of(
                    EnumExpertRule.builder().operator(OperatorType.EQUALS).field(FieldType.ENERGY_SOURCE).value(EnergySource.HYDRO.name()).build()
                ), gen, true),
                Arguments.of(CombinatorType.AND, List.of(
                    EnumExpertRule.builder().operator(OperatorType.EQUALS).field(FieldType.ENERGY_SOURCE).value(EnergySource.THERMAL.name()).build()
                ), gen, false),
                Arguments.of(CombinatorType.OR, List.of(
                    EnumExpertRule.builder().operator(OperatorType.EQUALS).field(FieldType.ENERGY_SOURCE).value(EnergySource.HYDRO.name()).build()
                ), gen, true),
                Arguments.of(CombinatorType.OR, List.of(
                    EnumExpertRule.builder().operator(OperatorType.EQUALS).field(FieldType.ENERGY_SOURCE).value(EnergySource.THERMAL.name()).build()
                ), gen, false)
        );
    }
}
