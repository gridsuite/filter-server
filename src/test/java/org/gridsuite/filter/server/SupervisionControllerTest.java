/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
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
import org.gridsuite.filter.AbstractFilter;
import org.gridsuite.filter.identifierlistfilter.IdentifierListFilter;
import org.gridsuite.filter.identifierlistfilter.IdentifierListFilterEquipmentAttributes;
import org.gridsuite.filter.utils.EquipmentType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * @author Radouane KHOUADRI {@literal <redouane.khouadri_externe at rte-france.com>}
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(classes = {FilterApplication.class, TestChannelBinderConfiguration.class})
public class SupervisionControllerTest {

    public static final String SUPERVISION_URL_TEMPLATE = "/" + FilterApi.API_VERSION + "/supervision/filters";

    @Autowired
    private FilterService filterService;

    @Autowired
    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mvc;

    @Before
    public void setUp() {
        Configuration.defaultConfiguration();
        final ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.USE_LONG_FOR_INTS);
        mapper.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
        mapper.disable(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE);

        Configuration.setDefaults(new Configuration.Defaults() {

            private final JsonProvider jsonProvider = new JacksonJsonProvider(mapper);
            private final MappingProvider mappingProvider = new JacksonMappingProvider(mapper);

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
    public void tearDown() {
        filterService.deleteAll();
    }

    @Test
    public void testFiltersList() throws Exception {
        int count = getAllFilters().size();
        assertEquals(0, count);

        UUID filterId = UUID.randomUUID();
        Date modificationDate = new Date();

        IdentifierListFilterEquipmentAttributes gen1 = new IdentifierListFilterEquipmentAttributes("GEN", 7d);
        IdentifierListFilterEquipmentAttributes gen2 = new IdentifierListFilterEquipmentAttributes("GEN2", 9d);
        IdentifierListFilter identifierListFilter = new IdentifierListFilter(filterId, modificationDate, EquipmentType.GENERATOR, List.of(gen1, gen2));
        filterService.createFilter(identifierListFilter);

        var list = getAllFilters();
        assertEquals(1, list.size());
        assertEquals(filterId, list.get(0).getId());
    }

    private List<AbstractFilter> getAllFilters() throws Exception {
        String response = mvc.perform(get(SUPERVISION_URL_TEMPLATE)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return objectMapper.readValue(response, new TypeReference<>() { });
    }
}
