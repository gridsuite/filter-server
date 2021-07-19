/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.server;

import org.gridsuite.filter.server.dto.LineFilter;
import org.gridsuite.filter.server.dto.NumericalFilter;
import org.gridsuite.filter.server.utils.RangeType;
import org.junit.Test;

import java.util.LinkedHashSet;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class GenerateScriptFromFiltersTest {

    private static final UUID FILTER1_UUID = UUID.fromString("11111111-f60e-4766-bc5c-8f312c1984e4");

    @Test
    public void generateScriptTest() {
        FiltersToGroovyScript filtersToScript = new FiltersToGroovyScript();

        LinkedHashSet<String> countries1 = new LinkedHashSet<>();
        countries1.add("FR");
        countries1.add("ES");
        LinkedHashSet<String> countries2 = new LinkedHashSet<>();
        countries2.add("IT");
        countries2.add("PT");

        assertEquals("import org.gridsuite.filter.server.utils.FiltersUtils;\n" +
                "\n" +
                "for (equipment in network.lines) {\n" +
                "  if (\n" +
                "      (FiltersUtils.matchID('lineId1', equipment) || FiltersUtils.matchName('lineName1', equipment))\n" +
                "      && FiltersUtils.isLocatedIn(['FR','ES'], equipment.terminal1)\n" +
                "      && FiltersUtils.isLocatedIn(['IT','PT'], equipment.terminal2)\n" +
                "      && FiltersUtils.isRangeNominalVoltage(equipment.terminal1, 225.0, 250.0)\n" +
                "      && FiltersUtils.isApproxNominalVoltage(equipment.terminal2, 385.0, 5.0)\n" +
                "      && equipment.terminal1.voltageLevel.substation.name.equals('s1')\n" +
                "      && equipment.terminal2.voltageLevel.substation.name.equals('s2')\n" +
                "     ) {\n" +
                "           filter(equipment.id) { equipments equipment.id }\n" +
                "     }\n" +
                "}\n",
            filtersToScript.generateGroovyScriptFromFilters(LineFilter.builder()
                    .id(FILTER1_UUID)
                .name("filter1")
                .equipmentID("lineId1")
                .equipmentName("lineName1")
                .substationName1("s1")
                .substationName2("s2")
                .countries1(countries1)
                .countries2(countries2)
                .nominalVoltage1(NumericalFilter.builder().type(RangeType.RANGE).value1(225.).value2(250.).build())
                .nominalVoltage2(NumericalFilter.builder().type(RangeType.APPROX).value1(385.).value2(5.).build())
                .build()));

        assertEquals("import org.gridsuite.filter.server.utils.FiltersUtils;\n" +
                "\n" +
                "for (equipment in network.lines) {\n" +
                "  if (\n" +
                "      (FiltersUtils.matchName('lineName2', equipment))\n" +
                "      && FiltersUtils.isLocatedIn(['IT','PT'], equipment.terminal2)\n" +
                "      && FiltersUtils.isEqualityNominalVoltage(equipment.terminal2, 380.0)\n" +
                "      && equipment.terminal1.voltageLevel.substation.name.equals('s1')\n" +
                "     ) {\n" +
                "           filter(equipment.id) { equipments equipment.id }\n" +
                "     }\n" +
                "}\n",
            filtersToScript.generateGroovyScriptFromFilters(LineFilter.builder()
                .id(FILTER1_UUID)
                .name("filter2")
                .equipmentName("lineName2")
                .substationName1("s1")
                .countries2(countries2)
                .nominalVoltage2(NumericalFilter.builder().type(RangeType.EQUALITY).value1(380.).build())
                .build()));

        assertEquals("import org.gridsuite.filter.server.utils.FiltersUtils;\n" +
                "\n" +
                "for (equipment in network.lines) {\n" +
                "           filter(equipment.id) { equipments equipment.id }\n" +
                "}\n",
            filtersToScript.generateGroovyScriptFromFilters(LineFilter.builder()
                .id(FILTER1_UUID)
                .name("filter2")
                .build()));

        assertEquals("import org.gridsuite.filter.server.utils.FiltersUtils;\n" +
                "\n" +
                "for (equipment in network.lines) {\n" +
                "  if (\n" +
                "      (FiltersUtils.matchName('lineName2', equipment))\n" +
                "      && FiltersUtils.isLocatedIn(['IT','PT'], equipment.terminal2)\n" +
                "      && FiltersUtils.isRangeNominalVoltage(equipment.terminal2, 380.0, null)\n" +
                "      && equipment.terminal1.voltageLevel.substation.name.equals('s1')\n" +
                "     ) {\n" +
                "           filter(equipment.id) { equipments equipment.id }\n" +
                "     }\n" +
                "}\n",
            filtersToScript.generateGroovyScriptFromFilters(LineFilter.builder()
                .id(FILTER1_UUID)
                .name("filter2")
                .equipmentName("lineName2")
                .substationName1("s1")
                .countries2(countries2)
                .nominalVoltage2(NumericalFilter.builder().type(RangeType.RANGE).value1(380.).build())
                .build()));
    }
}
