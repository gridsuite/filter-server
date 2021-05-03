/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.server;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import org.gridsuite.filter.server.utils.RangeType;
import org.gridsuite.filter.server.utils.FilterType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.EnumSet;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.join;
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
@WebMvcTest(FilterController.class)
@ContextConfiguration(classes = {FilterApplication.class})
public class FilterEntityControllerTest  {

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

    private void cleanDB() {
        filterService.deleteAll();
    }

    public String joinWithComma(Object... array) {
        return join(array, ",");
    }

    @Test
    public void test() throws Exception {

        // test all fields
        String lineFilter = "{" + joinWithComma(
            jsonVal("name", "testLine"),
            jsonVal("type", FilterType.LINE.name()),
            jsonVal("substationName1", "ragala"),
            jsonVal("substationName2", "miamMiam"),
            jsonVal("equipmentID", "vazy"),
            jsonVal("equipmentName", "tata"),
            numericalRange("nominalVoltage1", RangeType.RANGE, 5., 8.),
            numericalRange("nominalVoltage2", RangeType.EQUALITY, 6., null),
            jsonSet("countries1", Set.of("yoyo")),
            jsonSet("countries2", Set.of("smurf", "schtroumph"))) + "}";

        insertFilter("testLine", lineFilter);

        String minimalLineFilter = "{" + joinWithComma(
            jsonVal("name", "testLine"),
            jsonVal("type", FilterType.LINE.name()))
            + "}";
        // test replace and null value (country set & numerical range)
        insertFilter("testLine", minimalLineFilter);

        String scriptFilter = "{" + joinWithComma(
            jsonVal("name", "testScript"),
            jsonVal("type", FilterType.SCRIPT.name()),
            jsonVal("script", "test")) +
            "}";

        insertFilter("testScript", scriptFilter);

        mvc.perform(get(URL_TEMPLATE))
            .andExpect(status().isOk())
            .andExpect(content().json("[{\"name\":\"testLine\",\"type\":\"LINE\"}, {\"name\":\"testScript\",\"type\":\"SCRIPT\"}]"));

        mvc.perform(post(URL_TEMPLATE + "testLine/rename").content("grandLine")).andExpect(status().isOk());

        mvc.perform(get(URL_TEMPLATE))
            .andExpect(status().isOk())
            .andExpect(content().json("[{\"name\":\"grandLine\",\"type\":\"LINE\"}, {\"name\":\"testScript\",\"type\":\"SCRIPT\"}]"));

        mvc.perform(delete(URL_TEMPLATE + "testScript")).andExpect(status().isOk());

        mvc.perform(get(URL_TEMPLATE))
            .andExpect(status().isOk())
            .andExpect(content().json("[{\"name\":\"grandLine\",\"type\":\"LINE\"}]"));

        mvc.perform(delete(URL_TEMPLATE + "testScript")).andExpect(status().isNotFound());

        mvc.perform(get(URL_TEMPLATE + "testScript")).andExpect(status().isNotFound());

        mvc.perform(post(URL_TEMPLATE + "testLine/rename").content("grandLine")).andExpect(status().isNotFound());

    }

    private void insertFilter(String filtersName, String content) throws Exception {
        mvc.perform(put(URL_TEMPLATE)
            .content(content)
            .contentType(APPLICATION_JSON))
            .andExpect(status().isOk());

        MvcResult mockResponse = mvc.perform(get(URL_TEMPLATE + filtersName)).andExpect(status().isOk()).andReturn();
        String strRes = mockResponse.getResponse().getContentAsString();
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
