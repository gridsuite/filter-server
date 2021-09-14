/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.server;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import org.gridsuite.filter.server.dto.*;
import org.gridsuite.filter.server.utils.FilterType;
import org.gridsuite.filter.server.utils.RangeType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.join;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 * @author Jacques Borsenberger <jacques.borsenberger at rte-france.com>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(classes = {FilterApplication.class})
public class FilterEntityControllerTest {

    public static final String URL_TEMPLATE = "/" + FilterApi.API_VERSION + "/filters/";
    @Autowired
    private MockMvc mvc;

    @Autowired
    private FilterService filterService;

    @Before
    public void setUp() {
        Configuration.defaultConfiguration();
        MockitoAnnotations.initMocks(this);
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(DeserializationFeature.USE_LONG_FOR_INTS);
        objectMapper.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
        objectMapper.disable(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE);

        Configuration.setDefaults(new Configuration.Defaults() {

            private final JsonProvider jsonProvider = new JacksonJsonProvider(objectMapper);
            private final MappingProvider mappingProvider = new JacksonMappingProvider(objectMapper);

            @Override
            public JsonProvider jsonProvider() {
                return jsonProvider;
            }

            @Override
            public MappingProvider mappingProvider() {
                return mappingProvider;
            }

            @Override
            public Set<Option> options() {
                return EnumSet.noneOf(Option.class);
            }
        });
    }

    @After
    public void cleanUp() {
        filterService.deleteAll();
    }

    ObjectMapper objectMapper = new ObjectMapper();

    public String joinWithComma(Object... array) {
        return join(array, ",");
    }

    @Test
    public void testLineFilter() throws Exception {
        UUID filterId1 = UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e");
        UUID filterId2 = UUID.fromString("42b70a4d-e0c4-413a-8e3e-78e9027d300f");

        Date creationDate = new Date();
        Date modificationDate = new Date();

        // test all fields
        String lineFilter = "{" + joinWithComma(
            jsonVal("name", "testLine"),
            jsonVal("id", filterId1.toString()),
            jsonVal("type", FilterType.LINE.name()),
            jsonVal("substationName1", "ragala"),
            jsonVal("substationName2", "miamMiam"),
            jsonVal("equipmentID", "vazy"),
            jsonVal("equipmentName", "tata"),
            numericalRange("nominalVoltage1", RangeType.RANGE, 5., 8.),
            numericalRange("nominalVoltage2", RangeType.EQUALITY, 6., null),
            jsonSet("countries1", Set.of("yoyo")),
            jsonSet("countries2", Set.of("smurf", "schtroumph"))) + "}";

        insertFilter(filterId1, lineFilter);
        List<FilterAttributes> filterAttributes = objectMapper.readValue(
            mvc.perform(get("/" + FilterApi.API_VERSION + "/filters/metadata").contentType(APPLICATION_JSON).header("ids", filterId1))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(),
            new TypeReference<>() {
            });

        Date dateCreation = filterAttributes.get(0).getCreationDate();
        Date dateModification = filterAttributes.get(0).getModificationDate();

        // test replace with same filter type and null value (country set & numerical range)
        String minimalLineFilter = "{" + joinWithComma(
            jsonVal("name", "testLineBis"),
            jsonVal("id", filterId1.toString()),
            jsonVal("type", FilterType.LINE.name()))
            + "}";

        modifyFilter(filterId1, minimalLineFilter);

        // script filter
        String scriptFilter = "{" + joinWithComma(
            jsonVal("name", "testScript"),
            jsonVal("id", filterId2.toString()),
            jsonVal("type", FilterType.SCRIPT.name()),
            jsonVal("script", "test"),
            jsonVal("description", "oups")) +
            "}";

        insertFilter(filterId2, scriptFilter);

        var res = mvc.perform(get(URL_TEMPLATE))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        filterAttributes = objectMapper.readValue(res, new TypeReference<>() {
        });
        assertEquals(2, filterAttributes.size());
        if (!filterAttributes.get(0).getId().equals(filterId1)) {
            Collections.reverse(filterAttributes);
        }

        matchFilterDescription(filterAttributes.get(0), filterId1, "testLineBis", FilterType.LINE, creationDate, modificationDate, null);
        matchFilterDescription(filterAttributes.get(1), filterId2, "testScript", FilterType.SCRIPT, creationDate, modificationDate, "oups");

        filterAttributes = objectMapper.readValue(
            mvc.perform(get("/" + FilterApi.API_VERSION + "/filters/metadata").contentType(APPLICATION_JSON).header("ids", filterId1))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(),
            new TypeReference<>() {
            });
        assertEquals(1, filterAttributes.size());
        assertEquals(dateCreation, filterAttributes.get(0).getCreationDate());
        assertEquals("testLineBis", filterAttributes.get(0).getName());
        assertTrue(dateModification.getTime() < filterAttributes.get(0).getModificationDate().getTime());

        // test replace line filter with other filter type
        String generatorFilter = "{" + joinWithComma(
            jsonVal("name", "testGenerator"),
            jsonVal("type", FilterType.GENERATOR.name()),
            jsonVal("description", "descr generator"),
            jsonVal("substationName", "s1"),
            jsonVal("equipmentID", "eqId1"),
            jsonVal("equipmentName", "gen1"),
            numericalRange("nominalVoltage", RangeType.APPROX, 225., 3.),
            jsonSet("countries", Set.of("FR", "BE"))) + "}";

        modifyFilter(filterId1, generatorFilter);

        filterAttributes = objectMapper.readValue(
            mvc.perform(get("/" + FilterApi.API_VERSION + "/filters/metadata").contentType(APPLICATION_JSON).header("ids", filterId1))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(),
            new TypeReference<>() {
            });
        assertEquals(1, filterAttributes.size());
        assertEquals(dateCreation, filterAttributes.get(0).getCreationDate());
        assertEquals("testGenerator", filterAttributes.get(0).getName());
        assertTrue(dateModification.getTime() < filterAttributes.get(0).getModificationDate().getTime());
        assertEquals(filterId1, filterAttributes.get(0).getId());
        assertEquals("descr generator", filterAttributes.get(0).getDescription());
        assertEquals(FilterType.GENERATOR, filterAttributes.get(0).getType());

        // delete
        mvc.perform(delete(URL_TEMPLATE + filterId2)).andExpect(status().isOk());

        mvc.perform(delete(URL_TEMPLATE + filterId2)).andExpect(status().isNotFound());

        mvc.perform(get(URL_TEMPLATE + filterId2)).andExpect(status().isNotFound());

        mvc.perform(put(URL_TEMPLATE + filterId2).contentType(APPLICATION_JSON).content(scriptFilter)).andExpect(status().isNotFound());

        filterService.deleteAll();
    }

    @Test
    public void testGeneratorFilter() throws Exception {
        insertInjectionFilter(FilterType.GENERATOR, "testGenerator", UUID.fromString("42b70a4d-e0c4-413a-8e3e-78e9027d300f"),
                        "genId1", "genName", "s1", "descr gen", Set.of("FR", "IT"), RangeType.RANGE, 210., 240.);
    }

    @Test
    public void testLoadFilter() throws Exception {
        insertInjectionFilter(FilterType.LOAD, "testLoad", UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e"),
            "loadId1", "loadName", "s2", "descr load", Set.of("BE", "NL"), RangeType.APPROX, 225., 5.);
    }

    @Test
    public void testShuntCompensatorFilter() throws Exception {
        insertInjectionFilter(FilterType.SHUNT_COMPENSATOR, "testShuntCompensator", UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e"),
            "shuntId1", "shuntName", "s3", "descr shunt", Set.of("ES"), RangeType.EQUALITY, 150., null);
    }

    @Test
    public void testStaticVarCompensatorFilter() throws Exception {
        insertInjectionFilter(FilterType.STATIC_VAR_COMPENSATOR, "testStaticVarCompensator", UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e"),
            "staticVarCompensatorId1", "staticVarCompensatorName", "s1", "descr static var compensator", null, null, null, null);
    }

    @Test
    public void testBatteryFilter() throws Exception {
        insertInjectionFilter(FilterType.BATTERY, "testBattery", UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e"),
            "batteryId1", "batteryName", null, "descr battery", Set.of("FR"), RangeType.RANGE, 45., 65.);
    }

    @Test
    public void testBusBarSectionFilter() throws Exception {
        insertInjectionFilter(FilterType.BUSBAR_SECTION, "testBusBarSection", UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e"),
            null, "batteryName", null, null, Set.of("DE"), RangeType.EQUALITY, 380., null);
    }

    @Test
    public void testDanglingLineFilter() throws Exception {
        insertInjectionFilter(FilterType.DANGLING_LINE, "testDanglingLine", UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e"),
            "danglingLineId1", null, "s2", "descr dangling line", Set.of("FR"), RangeType.APPROX, 150., 8.);
    }

    @Test
    public void testLccConverterStationFilter() throws Exception {
        insertInjectionFilter(FilterType.LCC_CONVERTER_STATION, "testLccConverterStation", UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e"),
            "lccId1", "lccName1", "s3", "descr lcc", Set.of("FR", "BE", "NL", "DE", "IT"), RangeType.RANGE, 20., 400.);
    }

    @Test
    public void testVscConverterStationFilter() throws Exception {
        insertInjectionFilter(FilterType.VSC_CONVERTER_STATION, "testVscConverterStation", UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e"),
            "vscId1", "vscName1", "s2", "descr vsc", null, RangeType.EQUALITY, 225., null);
    }

    @Test
    public void testHvdcLineFilter() throws Exception {
        insertHvdcLineFilter(FilterType.HVDC_LINE, "testHvdcLine", UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e"),
            "hvdcId1", "hvdcName1", "s1", "s2", "descr hvdc", Set.of("FR"), Set.of("UK"), RangeType.EQUALITY, 380., null);
    }

    @Test
    public void testTwoWindingsTransformerFilter() throws Exception {
        List<RangeType> rangeTypes = new ArrayList<>();
        rangeTypes.add(RangeType.EQUALITY);
        rangeTypes.add(RangeType.APPROX);
        List<Double> values1 = new ArrayList<>();
        values1.add(225.);
        values1.add(380.);
        List<Double> values2 = new ArrayList<>();
        values2.add(null);
        values2.add(5.);

        insertTransformerFilter(FilterType.TWO_WINDINGS_TRANSFORMER, "testTwoWindingsTransformer", UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e"),
            "2wtId1", "2wtName1", "s1", "descr 2wt", Set.of("FR", "BE", "NL"), rangeTypes, values1, values2);
    }

    @Test
    public void testThreeWindingsTransformerFilter() throws Exception {
        List<RangeType> rangeTypes = new ArrayList<>();
        rangeTypes.add(RangeType.RANGE);
        rangeTypes.add(RangeType.EQUALITY);
        rangeTypes.add(RangeType.APPROX);
        List<Double> values1 = new ArrayList<>();
        values1.add(210.);
        values1.add(150.);
        values1.add(380.);
        List<Double> values2 = new ArrayList<>();
        values2.add(240.);
        values2.add(null);
        values2.add(5.);

        insertTransformerFilter(FilterType.THREE_WINDINGS_TRANSFORMER, "testThreeWindingsTransformer", UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e"),
            "3wtId1", "3wtName1", "s2", "descr 3wt", Set.of("IT", "CH"), rangeTypes, values1, values2);
    }

    @Test
    public void testFilterToScript() throws Exception {
        UUID filterId1 = UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e");
        UUID filterId2 = UUID.fromString("42b70a4d-e0c4-413a-8e3e-78e9027d300f");
        UUID filterId3 = UUID.fromString("99999999-e0c4-413a-8e3e-78e9027d300f");

        String lineFilter = "{" + joinWithComma(
            jsonVal("name", "testLine"),
            jsonVal("id", filterId1.toString()),
            jsonVal("type", FilterType.LINE.name()),
            jsonVal("substationName1", "ragala"),
            jsonVal("substationName2", "miamMiam"),
            jsonVal("equipmentID", "vazy"),
            jsonVal("equipmentName", "tata"),
            numericalRange("nominalVoltage1", RangeType.RANGE, 5., 8.),
            numericalRange("nominalVoltage2", RangeType.EQUALITY, 6., null),
            jsonSet("countries1", Set.of("yoyo")),
            jsonSet("countries2", Set.of("smurf", "schtroumph"))) + "}";

        insertFilter(filterId1, lineFilter);

        mvc.perform(get(URL_TEMPLATE))
            .andExpect(status().isOk())
            .andExpect(content().json("[{\"name\":\"testLine\",\"type\":\"LINE\"}]"));

        // new script from filter
        mvc.perform(post(URL_TEMPLATE + filterId1 + "/new-script/" + UUID.randomUUID() + "/testLineScript")).andExpect(status().isOk());

        mvc.perform(get(URL_TEMPLATE))
            .andExpect(status().isOk())
            .andExpect(content().json("[{\"name\":\"testLine\",\"type\":\"LINE\"}, {\"name\":\"testLineScript\",\"type\":\"SCRIPT\"}]"));

        // replace filter with script
        mvc.perform(put(URL_TEMPLATE + filterId1 + "/replace-with-script")).andExpect(status().isOk());

        mvc.perform(get(URL_TEMPLATE))
            .andExpect(status().isOk())
            .andExpect(content().json("[{\"name\":\"testLine\",\"type\":\"SCRIPT\"}, {\"name\":\"testLineScript\",\"type\":\"SCRIPT\"}]"));

        String scriptFilter = "{" + joinWithComma(
            jsonVal("name", "scriptFilter"),
            jsonVal("id", filterId2.toString()),
            jsonVal("type", FilterType.SCRIPT.name()),
            jsonVal("script", "test2"))
            + "}";
        insertFilter(filterId2, scriptFilter);

        assertThrows("Wrong filter type, should never happen", Exception.class, () -> mvc.perform(post(URL_TEMPLATE + filterId2 + "/new-script/" + UUID.randomUUID() + "/testScript2")));
        assertThrows("Wrong filter type, should never happen", Exception.class, () -> mvc.perform(put(URL_TEMPLATE + filterId2 + "/replace-with-script")));
        mvc.perform(put(URL_TEMPLATE + filterId3 + "/new-script/" + "testScript3")).andExpect(status().isNotFound());
        mvc.perform(put(URL_TEMPLATE + filterId3 + "/replace-with-script")).andExpect(status().isNotFound());

    }

    @Test
    public void testRenameFilter() throws Exception {
        UUID filterId1 = UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e");
        UUID filterId2 = UUID.fromString("42b70a4d-e0c4-413a-8e3e-78e9027d300f");

        Date creationDate = new Date();
        Date modificationDate = new Date();

        // test all fields
        String lineFilter = "{" + joinWithComma(
                jsonVal("name", "testLine"),
                jsonVal("id", filterId1.toString()),
                jsonVal("type", FilterType.LINE.name()),
                jsonVal("substationName1", "ragala"),
                jsonVal("substationName2", "miamMiam"),
                jsonVal("equipmentID", "vazy"),
                jsonVal("equipmentName", "tata"),
                numericalRange("nominalVoltage1", RangeType.RANGE, 5., 8.),
                numericalRange("nominalVoltage2", RangeType.EQUALITY, 6., null),
                jsonSet("countries1", Set.of("yoyo")),
                jsonSet("countries2", Set.of("smurf", "schtroumph"))) + "}";

        insertFilter(filterId1, lineFilter);
        renameFilter(filterId1, "testLine", "newName");
    }

    private void matchFilterDescription(IFilterAttributes filterAttribute, UUID id, String name, FilterType type, Date creationDate, Date modificationDate, String description) throws Exception {
        assertEquals(filterAttribute.getName(), name);
        assertEquals(filterAttribute.getId(), id);
        assertEquals(filterAttribute.getType(), type);
        assertTrue((creationDate.getTime() - filterAttribute.getCreationDate().getTime()) < 1000);
        assertTrue((modificationDate.getTime() - filterAttribute.getModificationDate().getTime()) < 1000);
        assertEquals(description, filterAttribute.getDescription());
    }

    private void insertFilter(UUID filterId, String content) throws Exception {
        String strRes = mvc.perform(post(URL_TEMPLATE)
            .content(content)
            .contentType(APPLICATION_JSON))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        JSONAssert.assertEquals(content, strRes, JSONCompareMode.LENIENT);

        mvc.perform(get(URL_TEMPLATE))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        MvcResult mockResponse = mvc.perform(get(URL_TEMPLATE + filterId)).andExpect(status().isOk()).andReturn();
        // Check we didn't miss anything
        JSONAssert.assertEquals(content, strRes, JSONCompareMode.LENIENT);
    }

    private void modifyFilter(UUID filterId, String content) throws Exception {
        mvc.perform(put(URL_TEMPLATE + filterId)
            .content(content)
            .contentType(APPLICATION_JSON))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        String strRes = mvc.perform(get(URL_TEMPLATE + filterId)).andReturn().getResponse().getContentAsString();
        JSONAssert.assertEquals(content, strRes, JSONCompareMode.LENIENT);

        mvc.perform(get(URL_TEMPLATE))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        MvcResult mockResponse = mvc.perform(get(URL_TEMPLATE + filterId)).andExpect(status().isOk()).andReturn();
        // Check we didn't miss anything
        JSONAssert.assertEquals(content, strRes, JSONCompareMode.LENIENT);
    }

    private void renameFilter(UUID filterId, String oldName, String newName) throws Exception {
        mvc.perform(put(URL_TEMPLATE + "rename/" + filterId + "/" + newName)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        String strRes = mvc.perform(get(URL_TEMPLATE + filterId)).andReturn().getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        LineFilter filter = objectMapper.readValue(strRes, new TypeReference<>() {
        });
        assertEquals(newName, filter.getName());
    }

    public StringBuilder jsonVal(String id, String val) {
        return new StringBuilder("\"").append(id).append("\": \"").append(val).append("\"");
    }

    public StringBuilder jsonDouble(String id, Double val) {
        return new StringBuilder("\"").append(id).append("\": ").append(val);
    }

    public StringBuilder jsonSet(String id, Set<String> set) {
        return new StringBuilder("\"").append(id).append("\": ").append("[")
            .append(!set.isEmpty() ? "\"" + join(set, "\",\"") + "\"" : "").append("]");
    }

    private StringBuilder numericalRange(String id, RangeType range, Double value1, Double value2) {
        return new StringBuilder("\"").append(id).append("\": ")
            .append("{").append(joinWithComma(
                jsonDouble("value1", value1),
                jsonDouble("value2", value2),
                jsonVal("type", range.name()))
            ).append("}");
    }

    private void insertInjectionFilter(FilterType type, String name, UUID id, String equipmentID, String equipmentName,
                                       String substationName, String description, Set<String> countries,
                                       RangeType rangeType, Double value1, Double value2)  throws Exception {
        String filter = "{" + joinWithComma(
            jsonVal("name", name),
            jsonVal("id", id.toString()),
            jsonVal("type", type.name()));

        if (equipmentID != null) {
            filter += ", " + jsonVal("equipmentID", equipmentID);
        }
        if (equipmentName != null) {
            filter += ", " + jsonVal("equipmentName", equipmentName);
        }
        if (substationName != null) {
            filter += ", " + jsonVal("substationName", substationName);
        }
        if (description != null) {
            filter += ", " + jsonVal("description", description);
        }
        if (rangeType != null) {
            filter += ", " + numericalRange("nominalVoltage", rangeType, value1, value2);
        }
        if (countries != null) {
            filter += ", " + jsonSet("countries", countries);
        }
        filter += "}";

        insertFilter(id, filter);

        List<FilterAttributes> filterAttributes = objectMapper.readValue(
            mvc.perform(get("/" + FilterApi.API_VERSION + "/filters/metadata").contentType(APPLICATION_JSON).header("ids", id))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(),
            new TypeReference<>() {
            });

        assertEquals(1, filterAttributes.size());
        assertEquals(name, filterAttributes.get(0).getName());
        assertEquals(id, filterAttributes.get(0).getId());
        assertEquals(type, filterAttributes.get(0).getType());
        assertEquals(description, filterAttributes.get(0).getDescription());

        mvc.perform(delete(URL_TEMPLATE + id)).andExpect(status().isOk());
    }

    private void insertTransformerFilter(FilterType type, String name, UUID id, String equipmentID, String equipmentName,
                                       String substationName, String description, Set<String> countries,
                                       List<RangeType> rangeTypes, List<Double> values1, List<Double> values2)  throws Exception {
        String filter = "{" + joinWithComma(
            jsonVal("name", name),
            jsonVal("id", id.toString()),
            jsonVal("type", type.name()));

        if (equipmentID != null) {
            filter += ", " + jsonVal("equipmentID", equipmentID);
        }
        if (equipmentName != null) {
            filter += ", " + jsonVal("equipmentName", equipmentName);
        }
        if (substationName != null) {
            filter += ", " + jsonVal("substationName", substationName);
        }
        if (description != null) {
            filter += ", " + jsonVal("description", description);
        }
        if (rangeTypes != null) {
            for (int i = 0; i < rangeTypes.size(); ++i) {
                filter += ", " + numericalRange("nominalVoltage" + (i + 1), rangeTypes.get(i), values1.get(i), values2.get(i));
            }
        }
        if (countries != null) {
            filter += ", " + jsonSet("countries", countries);
        }
        filter += "}";

        insertFilter(id, filter);

        List<FilterAttributes> filterAttributes = objectMapper.readValue(
            mvc.perform(get("/" + FilterApi.API_VERSION + "/filters/metadata").contentType(APPLICATION_JSON).header("ids", id))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(),
            new TypeReference<>() {
            });

        assertEquals(1, filterAttributes.size());
        assertEquals(name, filterAttributes.get(0).getName());
        assertEquals(id, filterAttributes.get(0).getId());
        assertEquals(type, filterAttributes.get(0).getType());
        assertEquals(description, filterAttributes.get(0).getDescription());

        mvc.perform(delete(URL_TEMPLATE + id)).andExpect(status().isOk());
    }

    private void insertHvdcLineFilter(FilterType type, String name, UUID id, String equipmentID, String equipmentName,
                                       String substationName1, String substationName2, String description, Set<String> countries1,
                                       Set<String> countries2, RangeType rangeType, Double value1, Double value2)  throws Exception {
        String filter = "{" + joinWithComma(
            jsonVal("name", name),
            jsonVal("id", id.toString()),
            jsonVal("type", type.name()));

        if (equipmentID != null) {
            filter += ", " + jsonVal("equipmentID", equipmentID);
        }
        if (equipmentName != null) {
            filter += ", " + jsonVal("equipmentName", equipmentName);
        }
        if (substationName1 != null) {
            filter += ", " + jsonVal("substationName1", substationName1);
        }
        if (substationName2 != null) {
            filter += ", " + jsonVal("substationName2", substationName2);
        }
        if (description != null) {
            filter += ", " + jsonVal("description", description);
        }
        if (rangeType != null) {
            filter += ", " + numericalRange("nominalVoltage", rangeType, value1, value2);
        }
        if (countries1 != null) {
            filter += ", " + jsonSet("countries1", countries1);
        }
        if (countries2 != null) {
            filter += ", " + jsonSet("countries2", countries2);
        }
        filter += "}";

        insertFilter(id, filter);

        List<FilterAttributes> filterAttributes = objectMapper.readValue(
            mvc.perform(get("/" + FilterApi.API_VERSION + "/filters/metadata").contentType(APPLICATION_JSON).header("ids", id))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(),
            new TypeReference<>() {
            });

        assertEquals(1, filterAttributes.size());
        assertEquals(name, filterAttributes.get(0).getName());
        assertEquals(id, filterAttributes.get(0).getId());
        assertEquals(type, filterAttributes.get(0).getType());
        assertEquals(description, filterAttributes.get(0).getDescription());

        mvc.perform(delete(URL_TEMPLATE + id)).andExpect(status().isOk());
    }
}
