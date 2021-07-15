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
import org.gridsuite.filter.server.dto.FilterAttributes;
import org.gridsuite.filter.server.dto.IFilterAttributes;
import org.gridsuite.filter.server.utils.FilterType;
import org.gridsuite.filter.server.utils.RangeType;
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

import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.join;
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

    ObjectMapper objectMapper = new ObjectMapper();

    public String joinWithComma(Object... array) {
        return join(array, ",");
    }

    @Test
    public void test() throws Exception {
        UUID filterId1 = UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e");
        UUID filterId2 = UUID.fromString("42b70a4d-e0c4-413a-8e3e-78e9027d300f");
        UUID notFound = UUID.fromString("44444444-4444-4444-8e3e-78e9027d300f");

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
            mvc.perform(get("/" + FilterApi.API_VERSION + "/metadata").contentType(APPLICATION_JSON).content("[\"" + filterId1 + "\"]"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(),
            new TypeReference<>() {
            });

        Date dateCreation = filterAttributes.get(0).getCreationDate();
        Date dateModification = filterAttributes.get(0).getModificationDate();

        String minimalLineFilter = "{" + joinWithComma(
            jsonVal("name", "testLineBis"),
            jsonVal("id", filterId1.toString()),
            jsonVal("type", FilterType.LINE.name()))
            + "}";
        // test replace and null value (country set & numerical range)
        insertFilter(filterId1, minimalLineFilter);

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
            mvc.perform(get("/" + FilterApi.API_VERSION + "/metadata").contentType(APPLICATION_JSON).content("[\"" + filterId1 + "\"]"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(),
            new TypeReference<>() {
            });
        assertEquals(1, filterAttributes.size());
        assertEquals(dateCreation, filterAttributes.get(0).getCreationDate());
        assertEquals("testLineBis", filterAttributes.get(0).getName());
        assertTrue(dateModification.getTime() < filterAttributes.get(0).getModificationDate().getTime());

        mvc.perform(post(URL_TEMPLATE + filterId1 + "/rename").content("grandLine")).andExpect(status().isOk());

        mvc.perform(get(URL_TEMPLATE))
            .andExpect(status().isOk())
            .andExpect(content().json("[{\"name\":\"grandLine\",\"type\":\"LINE\"}, {\"name\":\"testScript\",\"type\":\"SCRIPT\"}]"));

        mvc.perform(post(URL_TEMPLATE + notFound + "/rename").content("grandLine")).andExpect(status().isNotFound());

        mvc.perform(delete(URL_TEMPLATE + filterId2)).andExpect(status().isOk());

        mvc.perform(get(URL_TEMPLATE))
            .andExpect(status().isOk())
            .andExpect(content().json("[{\"name\":\"grandLine\",\"type\":\"LINE\"}]"));

        mvc.perform(delete(URL_TEMPLATE + filterId2)).andExpect(status().isNotFound());

        mvc.perform(get(URL_TEMPLATE + filterId2)).andExpect(status().isNotFound());

        mvc.perform(post(URL_TEMPLATE + filterId2 + "/rename").content("grandLine")).andExpect(status().isNotFound());

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
        mockResponse.getResponse().getContentAsString();
        // Check we didn't miss anything
        JSONAssert.assertEquals(content, strRes, JSONCompareMode.LENIENT);
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

}
