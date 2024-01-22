package org.gridsuite.filter.server.expertrule;

import com.powsybl.iidm.network.*;
import org.gridsuite.filter.server.dto.expertfilter.expertrule.EnumExpertRule;
import org.gridsuite.filter.server.utils.expertfilter.FieldType;
import org.gridsuite.filter.server.utils.expertfilter.OperatorType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.gridsuite.filter.server.utils.expertfilter.OperatorType.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class EnumExpertRuleTest {

    @ParameterizedTest
    @MethodSource({
        "provideArgumentsForGeneratorTest",
        "provideArgumentsForLoadTest",
        "provideArgumentsForBusTest",
        "provideArgumentsForBusBarSectionTest"})
    void testEvaluateRule(OperatorType operator, FieldType field, String value, Set<String> values, Identifiable<?> equipment, boolean expected) {
        EnumExpertRule rule = EnumExpertRule.builder().operator(operator).field(field).value(value).values(values).build();
        assertEquals(expected, rule.evaluateRule(equipment));
    }

    private static Stream<Arguments> provideArgumentsForGeneratorTest() {

        Generator gen = Mockito.mock(Generator.class);
        Mockito.when(gen.getType()).thenReturn(IdentifiableType.GENERATOR);
        // Generator fields
        Mockito.when(gen.getEnergySource()).thenReturn(EnergySource.HYDRO);
        // VoltageLevel fields
        Substation substation = Mockito.mock(Substation.class);
        VoltageLevel voltageLevel = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel.getSubstation()).thenReturn(Optional.of(substation));
        Terminal terminal = Mockito.mock(Terminal.class);
        Mockito.when(terminal.getVoltageLevel()).thenReturn(voltageLevel);
        Mockito.when(gen.getTerminal()).thenReturn(terminal);
        Mockito.when(substation.getCountry()).thenReturn(Optional.of(Country.FR));

        return Stream.of(
                // --- EQUALS --- //
                // Generator fields
                Arguments.of(EQUALS, FieldType.ENERGY_SOURCE, EnergySource.HYDRO.name(), null, gen, true),
                // VoltageLevel fields
                Arguments.of(EQUALS, FieldType.COUNTRY, Country.FR.name(), null, gen, true),

                // --- NOT_EQUALS --- //
                // Generator fields
                Arguments.of(NOT_EQUALS, FieldType.ENERGY_SOURCE, EnergySource.THERMAL.name(), null, gen, true),
                // VoltageLevel fields
                Arguments.of(NOT_EQUALS, FieldType.COUNTRY, Country.DE.name(), null, gen, true),

                // --- IN --- //
                // Generator fields
                Arguments.of(IN, FieldType.ENERGY_SOURCE, null, Set.of(EnergySource.HYDRO.name(), EnergySource.THERMAL.name()), gen, true),
                // VoltageLevel fields
                Arguments.of(IN, FieldType.COUNTRY, null, Set.of(Country.FR.name(), Country.DE.name()), gen, true),

                // --- NOT_IN --- //
                // Generator fields
                Arguments.of(NOT_IN, FieldType.ENERGY_SOURCE, null, Set.of(EnergySource.NUCLEAR.name(), EnergySource.THERMAL.name()), gen, true),
                // VoltageLevel fields
                Arguments.of(NOT_IN, FieldType.COUNTRY, null, Set.of(Country.BE.name(), Country.DE.name()), gen, true)
        );
    }

    private static Stream<Arguments> provideArgumentsForLoadTest() {

        Load load = Mockito.mock(Load.class);
        Mockito.when(load.getType()).thenReturn(IdentifiableType.LOAD);
        // VoltageLevel fields
        Substation substation = Mockito.mock(Substation.class);
        VoltageLevel voltageLevel = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel.getSubstation()).thenReturn(Optional.of(substation));
        Terminal terminal = Mockito.mock(Terminal.class);
        Mockito.when(terminal.getVoltageLevel()).thenReturn(voltageLevel);
        Mockito.when(load.getTerminal()).thenReturn(terminal);
        Mockito.when(substation.getCountry()).thenReturn(Optional.of(Country.FR));

        return Stream.of(
                // --- EQUALS --- //
                // VoltageLevel fields
                Arguments.of(EQUALS, FieldType.COUNTRY, Country.FR.name(), null, load, true),

                // --- NOT_EQUALS --- //
                // VoltageLevel fields
                Arguments.of(NOT_EQUALS, FieldType.COUNTRY, Country.DE.name(), null, load, true),

                // --- IN --- //
                 // VoltageLevel fields
                Arguments.of(IN, FieldType.COUNTRY, null, Set.of(Country.FR.name(), Country.DE.name()), load, true),

                // --- NOT_IN --- //
                // VoltageLevel fields
                Arguments.of(NOT_IN, FieldType.COUNTRY, null, Set.of(Country.BE.name(), Country.DE.name()), load, true)
        );
    }

    private static Stream<Arguments> provideArgumentsForBusTest() {

        Bus bus = Mockito.mock(Bus.class);
        Mockito.when(bus.getType()).thenReturn(IdentifiableType.BUS);
        // VoltageLevel fields
        Substation substation = Mockito.mock(Substation.class);
        VoltageLevel voltageLevel = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel.getSubstation()).thenReturn(Optional.of(substation));
        Mockito.when(bus.getVoltageLevel()).thenReturn(voltageLevel);
        Mockito.when(substation.getCountry()).thenReturn(Optional.of(Country.FR));

        return Stream.of(
                // --- EQUALS --- //
                // VoltageLevel fields
                Arguments.of(EQUALS, FieldType.COUNTRY, Country.FR.name(), null, bus, true),

                // --- NOT_EQUALS --- //
                // VoltageLevel fields
                Arguments.of(NOT_EQUALS, FieldType.COUNTRY, Country.DE.name(), null, bus, true),

                // --- IN --- //
                // VoltageLevel fields
                Arguments.of(IN, FieldType.COUNTRY, null, Set.of(Country.FR.name(), Country.DE.name()), bus, true),

                // --- NOT_IN --- //
                // VoltageLevel fields
                Arguments.of(NOT_IN, FieldType.COUNTRY, null, Set.of(Country.BE.name(), Country.DE.name()), bus, true)
        );
    }

    private static Stream<Arguments> provideArgumentsForBusBarSectionTest() {

        BusbarSection busbarSection = Mockito.mock(BusbarSection.class);
        Mockito.when(busbarSection.getType()).thenReturn(IdentifiableType.BUSBAR_SECTION);
        // VoltageLevel fields
        Substation substation = Mockito.mock(Substation.class);
        VoltageLevel voltageLevel = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel.getSubstation()).thenReturn(Optional.of(substation));
        Terminal terminal = Mockito.mock(Terminal.class);
        Mockito.when(terminal.getVoltageLevel()).thenReturn(voltageLevel);
        Mockito.when(busbarSection.getTerminal()).thenReturn(terminal);
        Mockito.when(substation.getCountry()).thenReturn(Optional.of(Country.FR));

        return Stream.of(
                // --- EQUALS --- //
                // VoltageLevel fields
                Arguments.of(EQUALS, FieldType.COUNTRY, Country.FR.name(), null, busbarSection, true),

                // --- NOT_EQUALS --- //
                // VoltageLevel fields
                Arguments.of(NOT_EQUALS, FieldType.COUNTRY, Country.DE.name(), null, busbarSection, true),

                // --- IN --- //
                // VoltageLevel fields
                Arguments.of(IN, FieldType.COUNTRY, null, Set.of(Country.FR.name(), Country.DE.name()), busbarSection, true),

                // --- NOT_IN --- //
                // VoltageLevel fields
                Arguments.of(NOT_IN, FieldType.COUNTRY, null, Set.of(Country.BE.name(), Country.DE.name()), busbarSection, true)
        );
    }
}
