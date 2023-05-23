/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.server;

import com.powsybl.iidm.network.*;
import org.gridsuite.filter.server.utils.FiltersUtils;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class FiltersUtilsTest {
    @Test
    public void test() {
        Identifiable<Generator> ident = Mockito.mock(Identifiable.class);
        Mockito.when(ident.getId()).thenReturn("id1");
        Mockito.when(ident.getOptionalName()).thenReturn(Optional.of("name1"));

        assertTrue(FiltersUtils.matchID("id1", ident));
        assertFalse(FiltersUtils.matchID("id2", ident));
        assertTrue(FiltersUtils.matchName("name1", ident));
        assertFalse(FiltersUtils.matchName("name2", ident));

        Substation s1 = Mockito.mock(Substation.class);
        Substation s2 = Mockito.mock(Substation.class);
        VoltageLevel vl1 = Mockito.mock(VoltageLevel.class);
        VoltageLevel vl2 = Mockito.mock(VoltageLevel.class);
        VoltageLevel vl3 = Mockito.mock(VoltageLevel.class);
        Terminal t1 = Mockito.mock(Terminal.class);
        Terminal t2 = Mockito.mock(Terminal.class);
        Terminal t3 = Mockito.mock(Terminal.class);
        Generator g = Mockito.mock(Generator.class);

        Mockito.when(s1.getCountry()).thenReturn(Optional.of(Country.FR));
        Mockito.when(s2.getCountry()).thenReturn(Optional.of(Country.ES));
        Mockito.when(s1.getPropertyNames()).thenReturn(Set.of("region"));
        Mockito.when(s1.getProperty("region")).thenReturn("north");
        Mockito.when(vl1.getSubstation()).thenReturn(Optional.of(s1));
        Mockito.when(vl2.getSubstation()).thenReturn(Optional.of(s2));
        Mockito.when(t1.getVoltageLevel()).thenReturn(vl1);
        Mockito.when(t2.getVoltageLevel()).thenReturn(vl2);
        Mockito.when(t3.getVoltageLevel()).thenReturn(vl3);
        Mockito.when(vl1.getNominalV()).thenReturn(225.);
        Mockito.when(vl2.getNominalV()).thenReturn(380.);
        Mockito.when(g.getEnergySource()).thenReturn(EnergySource.NUCLEAR);

        assertTrue(FiltersUtils.isLocatedIn(List.of("FR", "ES"), t1));
        assertTrue(FiltersUtils.isLocatedIn(List.of("ES", "IT"), t2));
        assertFalse(FiltersUtils.isLocatedIn(List.of("PT", "IT"), t1));
        assertFalse(FiltersUtils.isLocatedIn(List.of("PT", "IT"), t2));

        freePropsMatching(t1, t2, t3);

        assertTrue(FiltersUtils.isEqualityNominalVoltage(t1, 225.));
        assertFalse(FiltersUtils.isEqualityNominalVoltage(t1, 380.));
        assertTrue(FiltersUtils.isEqualityNominalVoltage(t2, 380.));
        assertFalse(FiltersUtils.isEqualityNominalVoltage(t2, 225.));

        assertTrue(FiltersUtils.isRangeNominalVoltage(t1, 200., 250.));
        assertFalse(FiltersUtils.isRangeNominalVoltage(t1, 250., 300.));

        assertTrue(FiltersUtils.isLocatedIn(List.of("FR", "ES"), vl1));
        assertFalse(FiltersUtils.isLocatedIn(List.of("PT", "ES"), vl1));
        assertFalse(FiltersUtils.isLocatedIn(List.of("FR"), vl2));
        assertTrue(FiltersUtils.isLocatedIn(List.of("PT", "ES"), vl2));

        assertTrue(FiltersUtils.isLocatedIn(List.of("FR"), s1));
        assertFalse(FiltersUtils.isLocatedIn(List.of("ES"), s1));
        assertTrue(FiltersUtils.isLocatedIn(List.of("ES"), s2));
        assertFalse(FiltersUtils.isLocatedIn(List.of("PT"), s2));

        assertFalse(FiltersUtils.isEnergySource(g, "WIND"));
        assertTrue(FiltersUtils.isEnergySource(g, "NUCLEAR"));
    }

    private static void freePropsMatching(Terminal t1, Terminal t2, Terminal t3) {
        assertTrue(FiltersUtils.matchesFreeProps(null, t1));
        assertTrue(FiltersUtils.matchesFreeProps(Map.of(), t1));
        assertTrue(FiltersUtils.matchesFreeProps(null, t2));
        assertTrue(FiltersUtils.matchesFreeProps(Map.of(), t2));
        assertTrue(FiltersUtils.matchesFreeProps(null, t3));
        assertTrue(FiltersUtils.matchesFreeProps(Map.of(), t3));
        assertTrue(FiltersUtils.matchesFreeProps(Map.of("region", List.of("north")), t1));
        assertFalse(FiltersUtils.matchesFreeProps(Map.of("region", List.of("south")), t1));
    }
}
