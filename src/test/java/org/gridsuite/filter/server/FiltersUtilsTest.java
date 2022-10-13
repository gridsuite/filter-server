/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.server;

import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VoltageLevel;
import org.gridsuite.filter.server.utils.FiltersUtils;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

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
        Terminal t1 = Mockito.mock(Terminal.class);
        Terminal t2 = Mockito.mock(Terminal.class);

        Mockito.when(s1.getCountry()).thenReturn(Optional.of(Country.FR));
        Mockito.when(s2.getCountry()).thenReturn(Optional.of(Country.ES));
        Mockito.when(vl1.getSubstation()).thenReturn(Optional.of(s1));
        Mockito.when(vl2.getSubstation()).thenReturn(Optional.of(s2));
        Mockito.when(t1.getVoltageLevel()).thenReturn(vl1);
        Mockito.when(t2.getVoltageLevel()).thenReturn(vl2);
        Mockito.when(vl1.getNominalV()).thenReturn(225.);
        Mockito.when(vl2.getNominalV()).thenReturn(380.);

        assertTrue(FiltersUtils.isLocatedIn(List.of("FR", "ES"), t1));
        assertTrue(FiltersUtils.isLocatedIn(List.of("ES", "IT"), t2));
        assertFalse(FiltersUtils.isLocatedIn(List.of("PT", "IT"), t1));
        assertFalse(FiltersUtils.isLocatedIn(List.of("PT", "IT"), t2));

        assertTrue(FiltersUtils.isEqualityNominalVoltage(t1, 225.));
        assertFalse(FiltersUtils.isEqualityNominalVoltage(t1, 380.));
        assertTrue(FiltersUtils.isEqualityNominalVoltage(t2, 380.));
        assertFalse(FiltersUtils.isEqualityNominalVoltage(t2, 225.));

        assertTrue(FiltersUtils.isRangeNominalVoltage(t1, 200., 250.));
        assertFalse(FiltersUtils.isRangeNominalVoltage(t1, 250., 300.));
    }
}
