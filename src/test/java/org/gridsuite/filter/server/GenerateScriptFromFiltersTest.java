/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.server;

import org.gridsuite.filter.server.dto.BatteryFilter;
import org.gridsuite.filter.server.dto.BusBarSectionFilter;
import org.gridsuite.filter.server.dto.DanglingLineFilter;
import org.gridsuite.filter.server.dto.GeneratorFilter;
import org.gridsuite.filter.server.dto.HvdcLineFilter;
import org.gridsuite.filter.server.dto.LccConverterStationFilter;
import org.gridsuite.filter.server.dto.LineFilter;
import org.gridsuite.filter.server.dto.LoadFilter;
import org.gridsuite.filter.server.dto.NumericalFilter;
import org.gridsuite.filter.server.dto.ShuntCompensatorFilter;
import org.gridsuite.filter.server.dto.StaticVarCompensatorFilter;
import org.gridsuite.filter.server.dto.ThreeWindingsTransformerFilter;
import org.gridsuite.filter.server.dto.TwoWindingsTransformerFilter;
import org.gridsuite.filter.server.dto.VscConverterStationFilter;
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

    FiltersToGroovyScript filtersToScript = new FiltersToGroovyScript();

    @Test
    public void generateScriptLineFilterTest() {
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

    @Test
    public void generateScriptGeneratorFilterTest() {
        LinkedHashSet<String> countries = new LinkedHashSet<>();
        countries.add("FR");
        countries.add("ES");

        assertEquals("import org.gridsuite.filter.server.utils.FiltersUtils;\n" +
            "\n" +
            "for (equipment in network.generators) {\n" +
            "  if (\n" +
            "      (FiltersUtils.matchID('genId1', equipment) || FiltersUtils.matchName('genName1', equipment))\n" +
            "      && FiltersUtils.isLocatedIn(['FR','ES'], equipment.terminal)\n" +
            "      && FiltersUtils.isRangeNominalVoltage(equipment.terminal, 225.0, 250.0)\n" +
            "      && equipment.terminal.voltageLevel.substation.name.equals('s1')\n" +
            "     ) {\n" +
            "           filter(equipment.id) { equipments equipment.id }\n" +
            "     }\n" +
            "}\n", filtersToScript.generateGroovyScriptFromFilters(GeneratorFilter.builder()
            .id(FILTER1_UUID)
            .name("filter1")
            .equipmentID("genId1")
            .equipmentName("genName1")
            .substationName("s1")
            .countries(countries)
            .nominalVoltage(NumericalFilter.builder().type(RangeType.RANGE).value1(225.).value2(250.).build())
            .build()));
    }

    @Test
    public void generateScriptLoadFilterTest() {
        LinkedHashSet<String> countries = new LinkedHashSet<>();
        countries.add("IT");

        assertEquals("import org.gridsuite.filter.server.utils.FiltersUtils;\n" +
            "\n" +
            "for (equipment in network.loads) {\n" +
            "  if (\n" +
            "      (FiltersUtils.matchID('loadId1', equipment) || FiltersUtils.matchName('loadName1', equipment))\n" +
            "      && FiltersUtils.isLocatedIn(['IT'], equipment.terminal)\n" +
            "      && FiltersUtils.isApproxNominalVoltage(equipment.terminal, 390.0, 5.0)\n" +
            "      && equipment.terminal.voltageLevel.substation.name.equals('s3')\n" +
            "     ) {\n" +
            "           filter(equipment.id) { equipments equipment.id }\n" +
            "     }\n" +
            "}\n", filtersToScript.generateGroovyScriptFromFilters(LoadFilter.builder()
            .id(FILTER1_UUID)
            .name("filter2")
            .equipmentID("loadId1")
            .equipmentName("loadName1")
            .substationName("s3")
            .countries(countries)
            .nominalVoltage(NumericalFilter.builder().type(RangeType.APPROX).value1(390.).value2(5.).build())
            .build()));
    }

    @Test
    public void generateScriptShuntCompensatorFilterTest() {
        assertEquals("import org.gridsuite.filter.server.utils.FiltersUtils;\n" +
            "\n" +
            "for (equipment in network.shuntCompensators) {\n" +
            "  if (\n" +
            "      (FiltersUtils.matchName('shuntName1', equipment))\n" +
            "      && FiltersUtils.isEqualityNominalVoltage(equipment.terminal, 380.0)\n" +
            "     ) {\n" +
            "           filter(equipment.id) { equipments equipment.id }\n" +
            "     }\n" +
            "}\n", filtersToScript.generateGroovyScriptFromFilters(ShuntCompensatorFilter.builder()
            .id(FILTER1_UUID)
            .name("filter3")
            .equipmentName("shuntName1")
            .nominalVoltage(NumericalFilter.builder().type(RangeType.EQUALITY).value1(380.).build())
            .build()));
    }

    @Test
    public void generateScriptStaticVarCompensatorFilterTest() {
        LinkedHashSet<String> countries = new LinkedHashSet<>();
        countries.add("DE");

        assertEquals("import org.gridsuite.filter.server.utils.FiltersUtils;\n" +
            "\n" +
            "for (equipment in network.staticVarCompensators) {\n" +
            "  if (\n" +
            "      (FiltersUtils.matchID('staticVarCompensatorId1', equipment))\n" +
            "      && FiltersUtils.isLocatedIn(['DE'], equipment.terminal)\n" +
            "      && equipment.terminal.voltageLevel.substation.name.equals('s4')\n" +
            "     ) {\n" +
            "           filter(equipment.id) { equipments equipment.id }\n" +
            "     }\n" +
            "}\n", filtersToScript.generateGroovyScriptFromFilters(StaticVarCompensatorFilter.builder()
            .id(FILTER1_UUID)
            .name("filter2")
            .equipmentID("staticVarCompensatorId1")
            .substationName("s4")
            .countries(countries)
            .build()));
    }

    @Test
    public void generateScriptBatteryFilterTest() {
        assertEquals("import org.gridsuite.filter.server.utils.FiltersUtils;\n" +
            "\n" +
            "for (equipment in network.batteries) {\n" +
            "  if (\n" +
            "      (FiltersUtils.matchID('batteryId1', equipment) || FiltersUtils.matchName('batteryName1', equipment))\n" +
            "      && FiltersUtils.isApproxNominalVoltage(equipment.terminal, 150.0, 5.0)\n" +
            "      && equipment.terminal.voltageLevel.substation.name.equals('s5')\n" +
            "     ) {\n" +
            "           filter(equipment.id) { equipments equipment.id }\n" +
            "     }\n" +
            "}\n", filtersToScript.generateGroovyScriptFromFilters(BatteryFilter.builder()
            .id(FILTER1_UUID)
            .name("filter5")
            .equipmentID("batteryId1")
            .equipmentName("batteryName1")
            .substationName("s5")
            .nominalVoltage(NumericalFilter.builder().type(RangeType.APPROX).value1(150.).value2(5.).build())
            .build()));
    }

    @Test
    public void generateScriptBusBarSectionFilterTest() {
        assertEquals("import org.gridsuite.filter.server.utils.FiltersUtils;\n" +
            "\n" +
            "for (equipment in network.busbarSections) {\n" +
            "           filter(equipment.id) { equipments equipment.id }\n" +
            "}\n", filtersToScript.generateGroovyScriptFromFilters(BusBarSectionFilter.builder()
            .id(FILTER1_UUID)
            .name("filter5")
            .build()));
    }

    @Test
    public void generateScriptDanglingLineFilterTest() {
        LinkedHashSet<String> countries = new LinkedHashSet<>();
        countries.add("FR");
        countries.add("IT");

        assertEquals("import org.gridsuite.filter.server.utils.FiltersUtils;\n" +
            "\n" +
            "for (equipment in network.danglingLines) {\n" +
            "  if (\n" +
            "      (FiltersUtils.matchID('danglingId1', equipment) || FiltersUtils.matchName('danglingName1', equipment))\n" +
            "      && FiltersUtils.isLocatedIn(['FR','IT'], equipment.terminal)\n" +
            "      && FiltersUtils.isRangeNominalVoltage(equipment.terminal, 360.0, 400.0)\n" +
            "      && equipment.terminal.voltageLevel.substation.name.equals('s3')\n" +
            "     ) {\n" +
            "           filter(equipment.id) { equipments equipment.id }\n" +
            "     }\n" +
            "}\n", filtersToScript.generateGroovyScriptFromFilters(DanglingLineFilter.builder()
            .id(FILTER1_UUID)
            .name("filter1")
            .equipmentID("danglingId1")
            .equipmentName("danglingName1")
            .substationName("s3")
            .countries(countries)
            .nominalVoltage(NumericalFilter.builder().type(RangeType.RANGE).value1(360.).value2(400.).build())
            .build()));
    }

    @Test
    public void generateScriptLccConverterStationFilterTest() {
        LinkedHashSet<String> countries = new LinkedHashSet<>();
        countries.add("IT");
        countries.add("CH");

        assertEquals("import org.gridsuite.filter.server.utils.FiltersUtils;\n" +
            "\n" +
            "for (equipment in network.lccConverterStations) {\n" +
            "  if (\n" +
            "      (FiltersUtils.matchID('lccId1', equipment) || FiltersUtils.matchName('lccName1', equipment))\n" +
            "      && FiltersUtils.isLocatedIn(['IT','CH'], equipment.terminal)\n" +
            "     ) {\n" +
            "           filter(equipment.id) { equipments equipment.id }\n" +
            "     }\n" +
            "}\n", filtersToScript.generateGroovyScriptFromFilters(LccConverterStationFilter.builder()
            .id(FILTER1_UUID)
            .name("filter6")
            .equipmentID("lccId1")
            .equipmentName("lccName1")
            .countries(countries)
            .build()));
    }

    @Test
    public void generateScriptVscConverterStationFilterTest() {
        LinkedHashSet<String> countries = new LinkedHashSet<>();
        countries.add("BE");

        assertEquals("import org.gridsuite.filter.server.utils.FiltersUtils;\n" +
            "\n" +
            "for (equipment in network.vscConverterStations) {\n" +
            "  if (\n" +
            "      (FiltersUtils.matchID('vscId1', equipment))\n" +
            "      && FiltersUtils.isLocatedIn(['BE'], equipment.terminal)\n" +
            "      && FiltersUtils.isEqualityNominalVoltage(equipment.terminal, 225.0)\n" +
            "     ) {\n" +
            "           filter(equipment.id) { equipments equipment.id }\n" +
            "     }\n" +
            "}\n", filtersToScript.generateGroovyScriptFromFilters(VscConverterStationFilter.builder()
            .id(FILTER1_UUID)
            .name("filter7")
            .equipmentID("vscId1")
            .countries(countries)
            .nominalVoltage(NumericalFilter.builder().type(RangeType.EQUALITY).value1(225.).build())
            .build()));
    }

    @Test
    public void generateScriptTwoWindingsTransformerFilterTest() {
        LinkedHashSet<String> countries = new LinkedHashSet<>();
        countries.add("FR");
        countries.add("IT");

        assertEquals("import org.gridsuite.filter.server.utils.FiltersUtils;\n" +
            "\n" +
            "for (equipment in network.twoWindingsTransformers) {\n" +
            "  if (\n" +
            "      (FiltersUtils.matchID('2wtId1', equipment) || FiltersUtils.matchName('2wtName1', equipment))\n" +
            "      && (FiltersUtils.isLocatedIn(['FR','IT'], equipment.terminal1) || FiltersUtils.isLocatedIn(['FR','IT'], equipment.terminal2))\n" +
            "      && FiltersUtils.isRangeNominalVoltage(equipment.terminal1, 370.0, 390.0)\n" +
            "      && FiltersUtils.isEqualityNominalVoltage(equipment.terminal2, 225.0)\n" +
            "      && (equipment.terminal1.voltageLevel.substation.name.equals('s2') || equipment.terminal2.voltageLevel.substation.name.equals('s2'))\n" +
            "     ) {\n" +
            "           filter(equipment.id) { equipments equipment.id }\n" +
            "     }\n" +
            "}\n", filtersToScript.generateGroovyScriptFromFilters(TwoWindingsTransformerFilter.builder()
            .id(FILTER1_UUID)
            .name("filter1")
            .equipmentID("2wtId1")
            .equipmentName("2wtName1")
            .substationName("s2")
            .countries(countries)
            .nominalVoltage1(NumericalFilter.builder().type(RangeType.RANGE).value1(370.).value2(390.).build())
            .nominalVoltage2(NumericalFilter.builder().type(RangeType.EQUALITY).value1(225.).build())
            .build()));
    }

    @Test
    public void generateScriptThreeWindingsTransformerFilterTest() {
        LinkedHashSet<String> countries = new LinkedHashSet<>();
        countries.add("NL");

        assertEquals("import org.gridsuite.filter.server.utils.FiltersUtils;\n" +
            "\n" +
            "for (equipment in network.threeWindingsTransformers) {\n" +
            "  if (\n" +
            "      (FiltersUtils.matchID('3wtId1', equipment) || FiltersUtils.matchName('3wtName1', equipment))\n" +
            "      && (FiltersUtils.isLocatedIn(['NL'], equipment.terminal1) || FiltersUtils.isLocatedIn(['NL'], equipment.terminal2) || FiltersUtils.isLocatedIn(['NL'], equipment.terminal3))\n" +
            "      && FiltersUtils.isRangeNominalVoltage(equipment.terminal1, 210.0, 230.0)\n" +
            "      && FiltersUtils.isEqualityNominalVoltage(equipment.terminal2, 150.0)\n" +
            "      && FiltersUtils.isApproxNominalVoltage(equipment.terminal3, 380.0, 5.0)\n" +
            "      && (equipment.terminal1.voltageLevel.substation.name.equals('s3') || equipment.terminal2.voltageLevel.substation.name.equals('s3') || equipment.terminal3.voltageLevel.substation.name.equals('s3'))\n" +
            "     ) {\n" +
            "           filter(equipment.id) { equipments equipment.id }\n" +
            "     }\n" +
            "}\n", filtersToScript.generateGroovyScriptFromFilters(ThreeWindingsTransformerFilter.builder()
            .id(FILTER1_UUID)
            .name("filter1=2")
            .equipmentID("3wtId1")
            .equipmentName("3wtName1")
            .substationName("s3")
            .countries(countries)
            .nominalVoltage1(NumericalFilter.builder().type(RangeType.RANGE).value1(210.).value2(230.).build())
            .nominalVoltage2(NumericalFilter.builder().type(RangeType.EQUALITY).value1(150.).build())
            .nominalVoltage3(NumericalFilter.builder().type(RangeType.APPROX).value1(380.).value2(5.).build())
            .build()));
    }

    @Test
    public void generateScriptHvdcLineFilterTest() {
        LinkedHashSet<String> countries1 = new LinkedHashSet<>();
        countries1.add("FR");
        LinkedHashSet<String> countries2 = new LinkedHashSet<>();
        countries2.add("IT");

        assertEquals("import org.gridsuite.filter.server.utils.FiltersUtils;\n" +
            "\n" +
            "for (equipment in network.hvdcLines) {\n" +
            "  if (\n" +
            "      (FiltersUtils.matchID('hvdcId1', equipment) || FiltersUtils.matchName('hvdcName1', equipment))\n" +
            "      && FiltersUtils.isLocatedIn(['FR'], equipment.converterStation1.terminal)\n" +
            "      && FiltersUtils.isLocatedIn(['IT'], equipment.converterStation2.terminal)\n" +
            "      && FiltersUtils.isRangeNominalVoltage(equipment.converterStation1.terminal, 200.0, 400.0)\n" +
            "      && FiltersUtils.isApproxNominalVoltage(equipment.converterStation2.terminal, 380.0, 3.0)\n" +
            "      && equipment.converterStation1.terminal.voltageLevel.substation.name.equals('s1')\n" +
            "      && equipment.converterStation2.terminal.voltageLevel.substation.name.equals('s2')\n" +
            "     ) {\n" +
            "           filter(equipment.id) { equipments equipment.id }\n" +
            "     }\n" +
            "}\n", filtersToScript.generateGroovyScriptFromFilters(HvdcLineFilter.builder()
            .id(FILTER1_UUID)
            .name("filter9")
            .equipmentID("hvdcId1")
            .equipmentName("hvdcName1")
            .substationName1("s1")
            .substationName2("s2")
            .countries1(countries1)
            .countries2(countries2)
            .nominalVoltage1(NumericalFilter.builder().type(RangeType.RANGE).value1(200.).value2(400.).build())
            .nominalVoltage2(NumericalFilter.builder().type(RangeType.APPROX).value1(380.).value2(3.).build())
            .build()));
    }
}
