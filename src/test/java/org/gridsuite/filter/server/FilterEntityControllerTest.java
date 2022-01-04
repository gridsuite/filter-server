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
import com.powsybl.commons.PowsyblException;
import org.gridsuite.filter.server.dto.*;
import org.gridsuite.filter.server.utils.EquipmentType;
import org.gridsuite.filter.server.utils.FilterType;
import org.gridsuite.filter.server.utils.MatcherJson;
import org.gridsuite.filter.server.utils.RangeType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.*;

import static org.apache.commons.lang3.StringUtils.join;
import static org.gridsuite.filter.server.AbstractFilterRepositoryProxy.WRONG_FILTER_TYPE;
import static org.junit.Assert.*;
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

    @Autowired
    ObjectMapper objectMapper = new ObjectMapper();

    public static final SortedSet COUNTRIES1 = new TreeSet(Collections.singleton("France"));
    public static final SortedSet COUNTRIES2 = new TreeSet(Collections.singleton("Germany"));

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

    public String joinWithComma(Object... array) {
        return join(array, ",");
    }

    @Test
    public void testLineFilter() throws Exception {
        UUID filterId1 = UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e");
        UUID filterId2 = UUID.fromString("42b70a4d-e0c4-413a-8e3e-78e9027d300f");

        Date creationDate = new Date();
        Date modificationDate = new Date();

        LineFilter lineFilter = new LineFilter("equipmentID", "equipmentName", "substationName1", "substationName2", COUNTRIES1, COUNTRIES2, new NumericalFilter(RangeType.RANGE, 5., 8.), new NumericalFilter(RangeType.EQUALITY, 6., null));

        FormFilter lineFormFilter = new FormFilter(
                filterId1,
                creationDate,
                modificationDate,
                lineFilter
        );

        insertFilter(filterId1, lineFormFilter);
        checkFormFilter(filterId1, lineFormFilter);

        List<FilterAttributes> filterAttributes = objectMapper.readValue(
            mvc.perform(get("/" + FilterApi.API_VERSION + "/filters/metadata?ids={id}", filterId1)
                    .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(),
            new TypeReference<>() {
            });

        Date dateCreation = filterAttributes.get(0).getCreationDate();
        Date dateModification = filterAttributes.get(0).getModificationDate();

        FormFilter hvdcLineFormFilter = new FormFilter(
                filterId1,
                dateCreation,
                dateModification,
                new HvdcLineFilter(
                        "equipmentID",
                        "equipmentName",
                        "substationName1",
                        "substationName2",
                        COUNTRIES1,
                        COUNTRIES2,
                        new NumericalFilter(RangeType.RANGE, 50., null)
                )
        );

        modifyFormFilter(filterId1, hvdcLineFormFilter);

        checkFormFilter(filterId1, hvdcLineFormFilter);

        ScriptFilter scriptFilter = new ScriptFilter(filterId2, creationDate, modificationDate, "test");

        insertFilter(filterId2, scriptFilter);
        checkScriptFilter(filterId2, scriptFilter);

        var res = mvc.perform(get(URL_TEMPLATE))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        filterAttributes = objectMapper.readValue(res, new TypeReference<>() {
        });
        assertEquals(2, filterAttributes.size());
        if (!filterAttributes.get(0).getId().equals(filterId1)) {
            Collections.reverse(filterAttributes);
        }

        matchFilterInfos(filterAttributes.get(0), filterId1, FilterType.FORM, creationDate, modificationDate);
        matchFilterInfos(filterAttributes.get(1), filterId2, FilterType.SCRIPT, creationDate, modificationDate);

        filterAttributes = objectMapper.readValue(
            mvc.perform(get("/" + FilterApi.API_VERSION + "/filters/metadata?ids={id}", filterId1)
                    .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(),
            new TypeReference<>() {
            });
        assertEquals(1, filterAttributes.size());
        matchFilterInfos(filterAttributes.get(0), filterId1, FilterType.FORM, creationDate, modificationDate);

        // test replace line filter with other filter type
        AbstractFilter generatorFormFilter = new FormFilter(
                filterId1,
                creationDate,
                modificationDate,
                new GeneratorFilter(new InjectionFilterAttributes("eqId1", "gen1", "s1", new TreeSet<>(Set.of("FR", "BE")), new NumericalFilter(RangeType.RANGE, 50., null))
                )
        );

        modifyFormFilter(filterId1, generatorFormFilter);

        filterAttributes = objectMapper.readValue(
            mvc.perform(get("/" + FilterApi.API_VERSION + "/filters/metadata?ids={id}", filterId1)
                    .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(),
            new TypeReference<>() {
            });
        assertEquals(1, filterAttributes.size());
        matchFilterInfos(filterAttributes.get(0), filterId1, FilterType.FORM, creationDate, modificationDate);

        // delete
        mvc.perform(delete(URL_TEMPLATE + filterId2)).andExpect(status().isOk());

        mvc.perform(delete(URL_TEMPLATE + filterId2)).andExpect(status().isNotFound());

        mvc.perform(get(URL_TEMPLATE + filterId2)).andExpect(status().isNotFound());

        mvc.perform(put(URL_TEMPLATE + filterId2).contentType(APPLICATION_JSON).content(objectMapper.writeValueAsString(scriptFilter))).andExpect(status().isNotFound());

        filterService.deleteAll();
    }

    @Test
    public void testGeneratorFilter() throws Exception {
        insertInjectionFilter(EquipmentType.GENERATOR, UUID.fromString("42b70a4d-e0c4-413a-8e3e-78e9027d300f"),
                        "genId1", "genName", "s1", Set.of("FR", "IT"), RangeType.RANGE, 210., 240.);
    }

    @Test
    public void testLoadFilter() throws Exception {
        insertInjectionFilter(EquipmentType.LOAD, UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e"),
            "loadId1", "loadName", "s2", Set.of("BE", "NL"), RangeType.APPROX, 225., 5.);
    }

    @Test
    public void testShuntCompensatorFilter() throws Exception {
        insertInjectionFilter(EquipmentType.SHUNT_COMPENSATOR, UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e"),
            "shuntId1", "shuntName", "s3", Set.of("ES"), RangeType.EQUALITY, 150., null);
    }

    @Test
    public void testStaticVarCompensatorFilter() throws Exception {
        insertInjectionFilter(EquipmentType.STATIC_VAR_COMPENSATOR, UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e"),
            "staticVarCompensatorId1", "staticVarCompensatorName", "s1", null, null, null, null);
    }

    @Test
    public void testBatteryFilter() throws Exception {
        insertInjectionFilter(EquipmentType.BATTERY, UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e"),
            "batteryId1", "batteryName", null, Set.of("FR"), RangeType.RANGE, 45., 65.);
    }

    @Test
    public void testBusBarSectionFilter() throws Exception {
        insertInjectionFilter(EquipmentType.BUSBAR_SECTION, UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e"),
            null, "batteryName", null, Set.of("DE"), RangeType.EQUALITY, 380., null);
    }

    @Test
    public void testDanglingLineFilter() throws Exception {
        insertInjectionFilter(EquipmentType.DANGLING_LINE, UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e"),
            "danglingLineId1", null, "s2", Set.of("FR"), RangeType.APPROX, 150., 8.);
    }

    @Test
    public void testLccConverterStationFilter() throws Exception {
        insertInjectionFilter(EquipmentType.LCC_CONVERTER_STATION, UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e"),
            "lccId1", "lccName1", "s3", Set.of("FR", "BE", "NL", "DE", "IT"), RangeType.RANGE, 20., 400.);
    }

    @Test
    public void testVscConverterStationFilter() throws Exception {
        insertInjectionFilter(EquipmentType.VSC_CONVERTER_STATION, UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e"),
            "vscId1", "vscName1", "s2", null, RangeType.EQUALITY, 225., null);
    }

    @Test
    public void testHvdcLineFilter() throws Exception {
        insertHvdcLineFilter(UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e"),
            "hvdcId1", "hvdcName1", "s1", "s2", COUNTRIES1, COUNTRIES2, RangeType.EQUALITY, 380., null);
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

        insertTransformerFilter(EquipmentType.TWO_WINDINGS_TRANSFORMER, UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e"),
            "2wtId1", "2wtName1", "s1", null, rangeTypes, values1, values2);
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

        insertTransformerFilter(EquipmentType.THREE_WINDINGS_TRANSFORMER, UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e"),
            "3wtId1", "3wtName1", "s2", Set.of("IT", "CH"), rangeTypes, values1, values2);
    }

    @Test
    public void testFilterToScript() throws Exception {
        UUID filterId1 = UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e");
        UUID filterId2 = UUID.fromString("42b70a4d-e0c4-413a-8e3e-78e9027d300f");
        UUID filterId3 = UUID.fromString("99999999-e0c4-413a-8e3e-78e9027d300f");
        Date creationDate = new Date();
        Date modificationDate = new Date();

        LineFilter lineFilter = new LineFilter("equipmentID", "equipmentName", "substationName1", "substationName2", COUNTRIES1, COUNTRIES2, new NumericalFilter(RangeType.RANGE, 5., 8.), new NumericalFilter(RangeType.EQUALITY, 6., null));

        FormFilter lineFormFilter = new FormFilter(
                filterId1,
                creationDate,
                modificationDate,
                lineFilter
        );

        insertFilter(filterId1, lineFormFilter);
        checkFormFilter(filterId1, lineFormFilter);

        // new script from filter
        mvc.perform(post(URL_TEMPLATE + filterId1 + "/new-script?newId=" + UUID.randomUUID())).andExpect(status().isOk());

        mvc.perform(get(URL_TEMPLATE))
            .andExpect(status().isOk())
            .andExpect(content().json("[{\"type\":\"FORM\"}, {\"type\":\"SCRIPT\"}]"));

        // replace filter with script
        mvc.perform(put(URL_TEMPLATE + filterId1 + "/replace-with-script")).andExpect(status().isOk());

        mvc.perform(get(URL_TEMPLATE))
            .andExpect(status().isOk())
            .andExpect(content().json("[{\"type\":\"SCRIPT\"}, {\"type\":\"SCRIPT\"}]"));

        checkScriptFilter(filterId1, new ScriptFilter(filterId1, creationDate, modificationDate, "&& equipment.terminal1.voltageLevel.substation.name.equals('substationName1')"));

        ScriptFilter scriptFilter = new ScriptFilter(filterId2, creationDate, modificationDate, "test");

        insertFilter(filterId2, scriptFilter);
        checkScriptFilter(filterId2, new ScriptFilter(filterId2, creationDate, modificationDate, "test"));

        assertThrows("Wrong filter type, should never happen", Exception.class, () -> mvc.perform(post(URL_TEMPLATE + filterId2 + "/new-script?newId=" + UUID.randomUUID())));
        assertThrows("Wrong filter type, should never happen", Exception.class, () -> mvc.perform(put(URL_TEMPLATE + filterId2 + "/replace-with-script")));
        mvc.perform(post(URL_TEMPLATE + filterId3 + "/new-script?newId=" + filterId2)).andExpect(status().isNotFound());
        mvc.perform(put(URL_TEMPLATE + filterId3 + "/replace-with-script")).andExpect(status().isNotFound());
    }

    private AbstractFilter insertFilter(UUID filterId, AbstractFilter filter) throws Exception {
        String response = mvc.perform(post(URL_TEMPLATE).param("id", filterId.toString())
                        .content(objectMapper.writeValueAsString(filter))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return objectMapper.readValue(response, AbstractFilter.class);
    }

    private void modifyFormFilter(UUID filterId, AbstractFilter newFilter) throws Exception {
        mvc.perform(put(URL_TEMPLATE + filterId)
            .content(objectMapper.writeValueAsString(newFilter))
            .contentType(APPLICATION_JSON))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        String modifiedFilterAsString = mvc.perform(get(URL_TEMPLATE + filterId)).andReturn().getResponse().getContentAsString();
        FormFilter modifiedFilter = objectMapper.readValue(modifiedFilterAsString, FormFilter.class);
        checkFormFilter(filterId, modifiedFilter);

        mvc.perform(get(URL_TEMPLATE))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        MvcResult mockResponse = mvc.perform(get(URL_TEMPLATE + filterId)).andExpect(status().isOk()).andReturn();
        modifiedFilter = objectMapper.readValue(mockResponse.getResponse().getContentAsString(), FormFilter.class);
        checkFormFilter(filterId, modifiedFilter);
    }

    private void insertInjectionFilter(EquipmentType equipmentType, UUID id, String equipmentID, String equipmentName,
                                       String substationName, Set<String> countries,
                                       RangeType rangeType, Double value1, Double value2)  throws Exception {
        NumericalFilter numericalFilter = new NumericalFilter(rangeType, value1, value2);
        AbstractInjectionFilter abstractInjectionFilter;
        Date creationDate = new Date();
        Date modificationDate = new Date();
        SortedSet sortedCountries = AbstractFilterRepositoryProxy.setToSorterSet(countries);
        InjectionFilterAttributes injectionFilterAttributes =  new InjectionFilterAttributes(equipmentID, equipmentName, substationName, sortedCountries, numericalFilter);
        switch (equipmentType) {
            case BATTERY:
                abstractInjectionFilter = new BatteryFilter(injectionFilterAttributes);
                break;
            case BUSBAR_SECTION:
                abstractInjectionFilter = new BusBarSectionFilter(injectionFilterAttributes);
                break;
            case DANGLING_LINE:
                abstractInjectionFilter = new DanglingLineFilter(injectionFilterAttributes);
                break;
            case GENERATOR:
                abstractInjectionFilter = new GeneratorFilter(injectionFilterAttributes);
                break;
            case LCC_CONVERTER_STATION:
                abstractInjectionFilter = new LccConverterStationFilter(injectionFilterAttributes);
                break;
            case LOAD:
                abstractInjectionFilter = new LoadFilter(injectionFilterAttributes);
                break;
            case SHUNT_COMPENSATOR:
                abstractInjectionFilter = new ShuntCompensatorFilter(injectionFilterAttributes);
                break;
            case STATIC_VAR_COMPENSATOR:
                abstractInjectionFilter = new StaticVarCompensatorFilter(injectionFilterAttributes);
                break;
            case VSC_CONVERTER_STATION:
                abstractInjectionFilter = new VscConverterStationFilter(injectionFilterAttributes);
                break;
            default:
                throw new PowsyblException("Equipment type not allowed");
        }
        FormFilter injectionFilter = new FormFilter(
                id,
                creationDate,
                modificationDate,
                abstractInjectionFilter
        );

        insertFilter(id, injectionFilter);
        AbstractInjectionFilter injectionEquipment = (AbstractInjectionFilter) injectionFilter.getEquipmentFilterForm();
        injectionEquipment.setCountries(AbstractFilterRepositoryProxy.setToSorterSet(countries));
        checkFormFilter(id, injectionFilter);

        List<FilterAttributes> filterAttributes = objectMapper.readValue(
            mvc.perform(get("/" + FilterApi.API_VERSION + "/filters/metadata?ids={id}", id)
                    .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(),
            new TypeReference<>() {
            });

        assertEquals(1, filterAttributes.size());
        assertEquals(id, filterAttributes.get(0).getId());
        assertEquals(FilterType.FORM, filterAttributes.get(0).getType());

        mvc.perform(delete(URL_TEMPLATE + id)).andExpect(status().isOk());
    }

    private void insertTransformerFilter(EquipmentType equipmentType, UUID id, String equipmentID, String equipmentName,
                                         String substationName, Set<String> countries,
                                         List<RangeType> rangeTypes, List<Double> values1, List<Double> values2)  throws Exception {

        NumericalFilter numericalFilter1 = new NumericalFilter(rangeTypes.get(0), values1.get(0), values2.get(0));
        NumericalFilter numericalFilter2 = new NumericalFilter(rangeTypes.get(1), values1.get(1), values2.get(1));
        AbstractEquipmentFilterForm equipmentFilterForm;
        if (equipmentType == EquipmentType.TWO_WINDINGS_TRANSFORMER) {
            equipmentFilterForm = new TwoWindingsTransformerFilter(equipmentID, equipmentName, substationName, AbstractFilterRepositoryProxy.setToSorterSet(countries), numericalFilter1, numericalFilter2);
        } else if (equipmentType == EquipmentType.THREE_WINDINGS_TRANSFORMER) {
            NumericalFilter numericalFilter3 = new NumericalFilter(rangeTypes.get(2), values1.get(2), values2.get(2));
            equipmentFilterForm = new ThreeWindingsTransformerFilter(equipmentID, equipmentName, substationName, AbstractFilterRepositoryProxy.setToSorterSet(countries), numericalFilter1, numericalFilter2, numericalFilter3);
        } else {
            throw new PowsyblException(WRONG_FILTER_TYPE);
        }
        Date creationDate = new Date();
        Date modificationDate = new Date();

        FormFilter transformerFilter = new FormFilter(
                id,
                creationDate,
                modificationDate,
                equipmentFilterForm
        );

        insertFilter(id, transformerFilter);
        checkFormFilter(id, transformerFilter);

        List<FilterAttributes> filterAttributes = objectMapper.readValue(
            mvc.perform(get("/" + FilterApi.API_VERSION + "/filters/metadata?ids={id}", id)
                    .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(),
            new TypeReference<>() {
            });

        assertEquals(1, filterAttributes.size());
        assertEquals(id, filterAttributes.get(0).getId());
        assertEquals(FilterType.FORM, filterAttributes.get(0).getType());

        mvc.perform(delete(URL_TEMPLATE + id)).andExpect(status().isOk());
    }

    private void insertHvdcLineFilter(UUID id, String equipmentID, String equipmentName,
                                      String substationName1, String substationName2, SortedSet<String> countries1,
                                      SortedSet<String> countries2, RangeType rangeType, Double value1, Double value2)  throws Exception {
        Date creationDate = new Date();
        Date modificationDate = new Date();
        FormFilter hvdcLineFilter = new FormFilter(
                id,
                creationDate,
                modificationDate,
                new HvdcLineFilter(
                        equipmentID,
                        equipmentName,
                        substationName1,
                        substationName2,
                        countries1,
                        countries2,
                        new NumericalFilter(rangeType, value1, value2)
                )
        );

        insertFilter(id, hvdcLineFilter);
        checkFormFilter(id, hvdcLineFilter);

        String filtersAsString = mvc.perform(get(URL_TEMPLATE))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<AbstractFilter> filters = objectMapper.readValue(filtersAsString,
                new TypeReference<>() {
                });
        assertEquals(1, filters.size());
        matchFilterInfos(filters.get(0), id, FilterType.FORM, creationDate, modificationDate);

        List<FilterAttributes> filterAttributes = objectMapper.readValue(
            mvc.perform(get("/" + FilterApi.API_VERSION + "/filters/metadata?ids={id}", id)
                    .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(),
            new TypeReference<>() {
            });

        assertEquals(1, filterAttributes.size());
        matchFilterInfos(filterAttributes.get(0), id, FilterType.FORM, creationDate, modificationDate);

        mvc.perform(delete(URL_TEMPLATE + id)).andExpect(status().isOk());

        filterAttributes = objectMapper.readValue(
            mvc.perform(get("/" + FilterApi.API_VERSION + "/filters/metadata?ids={id}", id)
                    .contentType(APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsString(),
                new TypeReference<>() {
                });

        assertEquals(0, filterAttributes.size());
    }

    private void checkFormFilter(UUID filterId, FormFilter formFilter) throws Exception {
        String foundFilterAsString = mvc.perform(get(URL_TEMPLATE + filterId)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        FormFilter foundFilter = objectMapper.readValue(foundFilterAsString, FormFilter.class);
        matchFormFilterInfos(foundFilter, formFilter);
    }

    private void checkScriptFilter(UUID filterId, ScriptFilter scriptFilter) throws Exception {
        String foundFilterAsString = mvc.perform(get(URL_TEMPLATE + filterId)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        ScriptFilter foundFilter = objectMapper.readValue(foundFilterAsString, ScriptFilter.class);
        matchScriptFilterInfos(foundFilter, scriptFilter);
    }

    private void matchFilterInfos(IFilterAttributes filter1, IFilterAttributes filter2) {
        assertEquals(filter1.getId(), filter2.getId());
        assertEquals(filter1.getType(), filter2.getType());
        assertTrue((filter2.getCreationDate().getTime() - filter1.getCreationDate().getTime()) < 2000);
        assertTrue((filter2.getModificationDate().getTime() - filter1.getModificationDate().getTime()) < 2000);
    }

    private void matchFilterInfos(IFilterAttributes filterAttribute, UUID id, FilterType type, Date creationDate, Date modificationDate) {
        assertEquals(filterAttribute.getId(), id);
        assertEquals(filterAttribute.getType(), type);
        assertTrue((creationDate.getTime() - filterAttribute.getCreationDate().getTime()) < 2000);
        assertTrue((modificationDate.getTime() - filterAttribute.getModificationDate().getTime()) < 2000);
    }

    private void matchFormFilterInfos(FormFilter formFilter1, FormFilter formFilter2) {
        matchFilterInfos(formFilter1, formFilter2);
        matchEquipmentFormFilter(formFilter1.getEquipmentFilterForm(), formFilter2.getEquipmentFilterForm());
    }

    private void matchEquipmentFormFilter(AbstractEquipmentFilterForm equipmentFilterForm1, AbstractEquipmentFilterForm equipmentFilterForm2) {
        org.hamcrest.MatcherAssert.assertThat(equipmentFilterForm1, new MatcherJson<>(objectMapper, equipmentFilterForm2));
    }

    private void matchScriptFilterInfos(ScriptFilter scriptFilter1, ScriptFilter scriptFilter2) {
        matchFilterInfos(scriptFilter1, scriptFilter2);
        assertTrue(scriptFilter1.getScript().contains(scriptFilter2.getScript()));
    }
}
