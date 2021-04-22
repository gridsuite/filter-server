/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filters.server;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import org.gridsuite.filters.server.utils.FilterType;
import org.gridsuite.filters.server.utils.RangeType;
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

import java.util.Comparator;
import java.util.EnumSet;
import java.util.Set;

import static com.powsybl.network.store.model.NetworkStoreApi.VERSION;
import static org.apache.commons.lang3.StringUtils.join;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@RunWith(SpringRunner.class)
@WebMvcTest(FilterListController.class)
@ContextConfiguration(classes = {FiltersApplication.class})
public class FilterEntityControllerTest extends AbstractEmbeddedCassandraSetup {

    public static final String URL_TEMPLATE = "/" + VERSION + "/filters/";
    @Autowired
    private MockMvc mvc;

    @Autowired
    private FiltersService filtersService;

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

    public static class NumericNodeComparator implements Comparator<JsonNode> {
        @Override
        public int compare(JsonNode o1, JsonNode o2) {
            if (o1.equals(o2)) {
                return 0;
            }
            if ((o1 instanceof NumericNode) && (o2 instanceof NumericNode)) {
                Double d1 = o1.asDouble();
                Double d2 = o2.asDouble();
                if (d1.compareTo(d2) == 0) {
                    return 0;
                }
            }
            System.err.println(o1.asText() + " <> " + o2.asText());
            return 1;
        }
    }

    final NumericNodeComparator jSonNumericComparator = new NumericNodeComparator();

    @Test
    public void test() throws Exception {

        // test all fields
        String lineFilter = "{" +
            jsonVal("name", "testLine") +
            jsonVal("type", FilterType.LINE.name()) +
            jsonVal("substationName1", "ragala") +
            jsonVal("substationName2", "miamMiam") +
            jsonVal("equipmentID", "vazy") +
            jsonVal("equipmentName", "tata") +
            numericalRange("nominalVoltage1", RangeType.RANGE, 5., 8.) +
            numericalRange("nominalVoltage2", RangeType.EQUALITY, 6., null) +
            jsonSet("countries1", Set.of("yoyo")) +
            jsonSet("countries2", Set.of("smurf", "schtroumph"), false) +
            "}";

        insertFilter("testLine", lineFilter);

        String minimalLineFilter = "{" +
            jsonVal("name", "testLine") +
            jsonVal("type", FilterType.LINE.name(), false)
            + "}";
        // test replace and null value (country set & numerical range)
        insertFilter("testLine", minimalLineFilter);

        String scriptFilter = "{" +
            jsonVal("name", "testScript") +
            jsonVal("type", FilterType.SCRIPT.name()) +
            jsonVal("script", "test", false) +
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
        mvc.perform(put(URL_TEMPLATE + filtersName)
            .content(content)
            .contentType(APPLICATION_JSON))
            .andExpect(status().isOk());

        MvcResult mockResponse = mvc.perform(get(URL_TEMPLATE + filtersName)).andExpect(status().isOk()).andReturn();
        String strRes = mockResponse.getResponse().getContentAsString();
        // Check we didn't miss anything
        JSONAssert.assertEquals(content, strRes, JSONCompareMode.LENIENT);
    }

    private StringBuilder jsonVal(String id, String val) {
        return jsonVal(id, val, true);
    }

    public StringBuilder jsonVal(String id, String val, boolean trailingComma) {
        return new StringBuilder("\"").append(id).append("\": \"").append(val).append("\"").append(trailingComma ? ", " : "");
    }

    public StringBuilder jsonDouble(String id, Double val) {
        return new StringBuilder("\"").append(id).append("\": ").append(val).append(",");
    }

    public StringBuilder jsonSet(String id, Set<String> set) {
        return jsonSet(id, set, true);
    }

    public StringBuilder jsonSet(String id, Set<String> set, boolean trailingComma) {
        return new StringBuilder("\"").append(id).append("\": ")
            .append("[" + (!set.isEmpty() ? "\"" + join(set, "\",\"") + "\"" : "") + "]")
            .append(trailingComma ? ", " : "");
    }

    private StringBuilder numericalRange(String id, RangeType range, Double value1, Double value2) {
        return new StringBuilder("\"").append(id).append("\": ")
            .append("{")
            .append(jsonDouble("value1", value1))
            .append(jsonDouble("value2", value2))
            .append(jsonVal("type", range.name(), false))
            .append("},");
    }

}
