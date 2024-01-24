package org.gridsuite.filter.server.expertrule;

import com.powsybl.commons.PowsyblException;
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
import static org.junit.jupiter.api.Assertions.assertThrows;

class EnumExpertRuleTest {

    @ParameterizedTest
    @MethodSource({
        "provideArgumentsForTestWithException"
    })
    void testEvaluateRuleWithException(OperatorType operator, FieldType field, Identifiable<?> equipment, Class expectedException) {
        EnumExpertRule rule = EnumExpertRule.builder().operator(operator).field(field).build();
        assertThrows(expectedException, () -> rule.evaluateRule(equipment));
    }

    static Stream<Arguments> provideArgumentsForTestWithException() {

        Network network = Mockito.mock(Network.class);
        Mockito.when(network.getType()).thenReturn(IdentifiableType.NETWORK);

        VoltageLevel voltageLevel = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel.getType()).thenReturn(IdentifiableType.VOLTAGE_LEVEL);

        Generator generator = Mockito.mock(Generator.class);
        Mockito.when(generator.getType()).thenReturn(IdentifiableType.GENERATOR);
        Mockito.when(generator.getEnergySource()).thenReturn(EnergySource.HYDRO);

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
                Arguments.of(EQUALS, FieldType.RATED_S, network, PowsyblException.class),
                Arguments.of(EQUALS, FieldType.RATED_S, voltageLevel, PowsyblException.class),
                Arguments.of(EQUALS, FieldType.P0, generator, PowsyblException.class),
                Arguments.of(EQUALS, FieldType.RATED_S, load, PowsyblException.class),
                Arguments.of(EQUALS, FieldType.RATED_S, bus, PowsyblException.class),
                Arguments.of(EQUALS, FieldType.RATED_S, busbarSection, PowsyblException.class),
                Arguments.of(EQUALS, FieldType.RATED_S, battery, PowsyblException.class),

                // --- Test an unsupported operator for this rule type --- //
                Arguments.of(IS, FieldType.ENERGY_SOURCE, generator, PowsyblException.class)
                );
    }

    @ParameterizedTest
    @MethodSource({
        "provideArgumentsForGeneratorTest",
        "provideArgumentsForLoadTest",
        "provideArgumentsForBusTest",
        "provideArgumentsForBusBarSectionTest",
        "provideArgumentsForBatteryTest"})
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
                Arguments.of(EQUALS, FieldType.ENERGY_SOURCE, EnergySource.THERMAL.name(), null, gen, false),
                // VoltageLevel fields
                Arguments.of(EQUALS, FieldType.COUNTRY, Country.FR.name(), null, gen, true),
                Arguments.of(EQUALS, FieldType.COUNTRY, Country.DE.name(), null, gen, false),

                // --- NOT_EQUALS --- //
                // Generator fields
                Arguments.of(NOT_EQUALS, FieldType.ENERGY_SOURCE, EnergySource.THERMAL.name(), null, gen, true),
                Arguments.of(NOT_EQUALS, FieldType.ENERGY_SOURCE, EnergySource.HYDRO.name(), null, gen, false),
                // VoltageLevel fields
                Arguments.of(NOT_EQUALS, FieldType.COUNTRY, Country.DE.name(), null, gen, true),
                Arguments.of(NOT_EQUALS, FieldType.COUNTRY, Country.FR.name(), null, gen, false),

                // --- IN --- //
                // Generator fields
                Arguments.of(IN, FieldType.ENERGY_SOURCE, null, Set.of(EnergySource.HYDRO.name(), EnergySource.THERMAL.name()), gen, true),
                Arguments.of(IN, FieldType.ENERGY_SOURCE, null, Set.of(EnergySource.NUCLEAR.name(), EnergySource.THERMAL.name()), gen, false),
                // VoltageLevel fields
                Arguments.of(IN, FieldType.COUNTRY, null, Set.of(Country.FR.name(), Country.DE.name()), gen, true),
                Arguments.of(IN, FieldType.COUNTRY, null, Set.of(Country.BE.name(), Country.DE.name()), gen, false),

                // --- NOT_IN --- //
                // Generator fields
                Arguments.of(NOT_IN, FieldType.ENERGY_SOURCE, null, Set.of(EnergySource.NUCLEAR.name(), EnergySource.THERMAL.name()), gen, true),
                Arguments.of(NOT_IN, FieldType.ENERGY_SOURCE, null, Set.of(EnergySource.HYDRO.name(), EnergySource.THERMAL.name()), gen, false),
                // VoltageLevel fields
                Arguments.of(NOT_IN, FieldType.COUNTRY, null, Set.of(Country.BE.name(), Country.DE.name()), gen, true),
                Arguments.of(NOT_IN, FieldType.COUNTRY, null, Set.of(Country.FR.name(), Country.DE.name()), gen, false)
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
                Arguments.of(EQUALS, FieldType.COUNTRY, Country.DE.name(), null, load, false),

                // --- NOT_EQUALS --- //
                // VoltageLevel fields
                Arguments.of(NOT_EQUALS, FieldType.COUNTRY, Country.DE.name(), null, load, true),
                Arguments.of(NOT_EQUALS, FieldType.COUNTRY, Country.FR.name(), null, load, false),

                // --- IN --- //
                 // VoltageLevel fields
                Arguments.of(IN, FieldType.COUNTRY, null, Set.of(Country.FR.name(), Country.DE.name()), load, true),
                Arguments.of(IN, FieldType.COUNTRY, null, Set.of(Country.BE.name(), Country.DE.name()), load, false),

                // --- NOT_IN --- //
                // VoltageLevel fields
                Arguments.of(NOT_IN, FieldType.COUNTRY, null, Set.of(Country.BE.name(), Country.DE.name()), load, true),
                Arguments.of(NOT_IN, FieldType.COUNTRY, null, Set.of(Country.FR.name(), Country.DE.name()), load, false)
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
                Arguments.of(EQUALS, FieldType.COUNTRY, Country.DE.name(), null, bus, false),

                // --- NOT_EQUALS --- //
                // VoltageLevel fields
                Arguments.of(NOT_EQUALS, FieldType.COUNTRY, Country.DE.name(), null, bus, true),
                Arguments.of(NOT_EQUALS, FieldType.COUNTRY, Country.FR.name(), null, bus, false),

                // --- IN --- //
                // VoltageLevel fields
                Arguments.of(IN, FieldType.COUNTRY, null, Set.of(Country.FR.name(), Country.DE.name()), bus, true),
                Arguments.of(IN, FieldType.COUNTRY, null, Set.of(Country.BE.name(), Country.DE.name()), bus, false),

                // --- NOT_IN --- //
                // VoltageLevel fields
                Arguments.of(NOT_IN, FieldType.COUNTRY, null, Set.of(Country.BE.name(), Country.DE.name()), bus, true),
                Arguments.of(NOT_IN, FieldType.COUNTRY, null, Set.of(Country.FR.name(), Country.DE.name()), bus, false)
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
                Arguments.of(EQUALS, FieldType.COUNTRY, Country.DE.name(), null, busbarSection, false),

                // --- NOT_EQUALS --- //
                // VoltageLevel fields
                Arguments.of(NOT_EQUALS, FieldType.COUNTRY, Country.DE.name(), null, busbarSection, true),
                Arguments.of(NOT_EQUALS, FieldType.COUNTRY, Country.FR.name(), null, busbarSection, false),

                // --- IN --- //
                // VoltageLevel fields
                Arguments.of(IN, FieldType.COUNTRY, null, Set.of(Country.FR.name(), Country.DE.name()), busbarSection, true),
                Arguments.of(IN, FieldType.COUNTRY, null, Set.of(Country.BE.name(), Country.DE.name()), busbarSection, false),

                // --- NOT_IN --- //
                // VoltageLevel fields
                Arguments.of(NOT_IN, FieldType.COUNTRY, null, Set.of(Country.BE.name(), Country.DE.name()), busbarSection, true),
                Arguments.of(NOT_IN, FieldType.COUNTRY, null, Set.of(Country.FR.name(), Country.DE.name()), busbarSection, false)
        );
    }

    private static Stream<Arguments> provideArgumentsForBatteryTest() {

        Battery battery = Mockito.mock(Battery.class);
        Mockito.when(battery.getType()).thenReturn(IdentifiableType.BATTERY);
        // VoltageLevel fields
        Substation substation = Mockito.mock(Substation.class);
        VoltageLevel voltageLevel = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel.getSubstation()).thenReturn(Optional.of(substation));
        Terminal terminal = Mockito.mock(Terminal.class);
        Mockito.when(terminal.getVoltageLevel()).thenReturn(voltageLevel);
        Mockito.when(battery.getTerminal()).thenReturn(terminal);
        Mockito.when(substation.getCountry()).thenReturn(Optional.of(Country.FR));

        return Stream.of(
                // --- EQUALS --- //
                // VoltageLevel fields
                Arguments.of(EQUALS, FieldType.COUNTRY, Country.FR.name(), null, battery, true),
                Arguments.of(EQUALS, FieldType.COUNTRY, Country.DE.name(), null, battery, false),

                // --- NOT_EQUALS --- //
                // VoltageLevel fields
                Arguments.of(NOT_EQUALS, FieldType.COUNTRY, Country.DE.name(), null, battery, true),
                Arguments.of(NOT_EQUALS, FieldType.COUNTRY, Country.FR.name(), null, battery, false),

                // --- IN --- //
                // VoltageLevel fields
                Arguments.of(IN, FieldType.COUNTRY, null, Set.of(Country.FR.name(), Country.DE.name()), battery, true),
                Arguments.of(IN, FieldType.COUNTRY, null, Set.of(Country.BE.name(), Country.DE.name()), battery, false),

                // --- NOT_IN --- //
                // VoltageLevel fields
                Arguments.of(NOT_IN, FieldType.COUNTRY, null, Set.of(Country.BE.name(), Country.DE.name()), battery, true),
                Arguments.of(NOT_IN, FieldType.COUNTRY, null, Set.of(Country.FR.name(), Country.DE.name()), battery, false)
        );
    }
}
