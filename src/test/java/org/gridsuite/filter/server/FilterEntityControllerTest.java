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
import com.fasterxml.jackson.databind.ObjectWriter;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.*;
import com.powsybl.network.store.client.NetworkStoreService;
import com.powsybl.network.store.iidm.impl.NetworkFactoryImpl;
import jakarta.servlet.ServletException;
import org.apache.commons.collections4.OrderedMap;
import org.apache.commons.collections4.map.LinkedMap;
import org.gridsuite.filter.server.dto.AbstractFilter;
import org.gridsuite.filter.server.dto.FilterAttributes;
import org.gridsuite.filter.server.dto.IFilterAttributes;
import org.gridsuite.filter.server.dto.criteriafilter.DanglingLineFilter;
import org.gridsuite.filter.server.dto.criteriafilter.*;
import org.gridsuite.filter.server.dto.expertfilter.ExpertFilter;
import org.gridsuite.filter.server.dto.expertfilter.expertrule.*;
import org.gridsuite.filter.server.dto.identifierlistfilter.FilterEquipments;
import org.gridsuite.filter.server.dto.identifierlistfilter.IdentifiableAttributes;
import org.gridsuite.filter.server.dto.identifierlistfilter.IdentifierListFilter;
import org.gridsuite.filter.server.dto.identifierlistfilter.IdentifierListFilterEquipmentAttributes;
import org.gridsuite.filter.server.dto.scriptfilter.ScriptFilter;
import org.gridsuite.filter.server.repositories.proxies.AbstractFilterRepositoryProxy;
import org.gridsuite.filter.server.utils.*;
import org.gridsuite.filter.server.utils.expertfilter.CombinatorType;
import org.gridsuite.filter.server.utils.expertfilter.FieldType;
import org.gridsuite.filter.server.utils.expertfilter.OperatorType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.messaging.Message;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.*;

import static org.apache.commons.lang3.StringUtils.join;
import static org.gridsuite.filter.server.repositories.proxies.AbstractFilterRepositoryProxy.WRONG_FILTER_TYPE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.given;
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
@ContextConfiguration(classes = {FilterApplication.class, TestChannelBinderConfiguration.class})
public class FilterEntityControllerTest {

    public static final String URL_TEMPLATE = "/" + FilterApi.API_VERSION + "/filters";
    private static final long TIMEOUT = 1000;

    @Autowired
    private MockMvc mvc;

    private Network network;
    private Network network2;
    private Network network6;
    private ObjectWriter objectWriter;

    @Autowired
    private FilterService filterService;

    @MockBean
    private NetworkStoreService networkStoreService;

    @Autowired
    private OutputDestination output;

    @Autowired
    ObjectMapper objectMapper = new ObjectMapper();

    public static final SortedSet<String> COUNTRIES1 = new TreeSet<>(Collections.singleton("France"));
    public static final SortedSet<String> COUNTRIES2 = new TreeSet<>(Collections.singleton("Germany"));

    public static final OrderedMap<String, List<String>> FREE_PROPS = new LinkedMap<>(
        Map.of("region", List.of("north", "south")));

    private static final UUID NETWORK_UUID = UUID.fromString("7928181c-7977-4592-ba19-88027e4254e4");
    private static final UUID NETWORK_UUID_2 = UUID.fromString("7928181c-7977-4592-ba19-88027e4254e5");
    private static final UUID NETWORK_UUID_3 = UUID.fromString("7928181c-7977-4592-ba19-88027e4254e6");
    private static final UUID NETWORK_UUID_4 = UUID.fromString("7928181c-7977-4592-ba19-88027e4254e7");
    private static final UUID NETWORK_UUID_5 = UUID.fromString("11111111-7977-4592-2222-88027e4254e7");
    private static final UUID NETWORK_UUID_6 = UUID.fromString("11111111-7977-4592-2222-88027e4254e8");
    private static final UUID NETWORK_NOT_FOUND_UUID = UUID.fromString("88888888-7977-3333-9999-88027e4254e7");
    private static final String VARIANT_ID_1 = "variant_1";
    private static final String USER_ID_HEADER = "userId";

    private String elementUpdateDestination = "element.update";

    @Before
    public void setUp() {
        Configuration.defaultConfiguration();
        MockitoAnnotations.initMocks(this);
        final ObjectMapper objectMapper = new ObjectMapper();
        objectWriter = objectMapper.writer().withDefaultPrettyPrinter();
        objectMapper.enable(DeserializationFeature.USE_LONG_FOR_INTS);
        objectMapper.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
        objectMapper.disable(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE);

        network = EurostagTutorialExample1Factory.createWithMoreGenerators(new NetworkFactoryImpl());
        network.getSubstation("P1").setProperty("region", "north");
        network.getSubstation("P2").setProperty("region", "south");
        network.getVariantManager().cloneVariant(VariantManagerConstants.INITIAL_VARIANT_ID, VARIANT_ID_1);
        network.getVariantManager().setWorkingVariant(VARIANT_ID_1);
        // remove generator 'GEN2' from network in variant VARIANT_ID_1
        network.getGenerator("GEN2").remove();
        network.getVariantManager().setWorkingVariant(VariantManagerConstants.INITIAL_VARIANT_ID);

        network2 = HvdcTestNetwork.createVsc(new NetworkFactoryImpl());
        network2.getSubstation("S2").setProperty("region", "north");
        Network network3 = SvcTestCaseFactory.createWithMoreSVCs(new NetworkFactoryImpl());
        Network network4 = ShuntTestCaseFactory.create(new NetworkFactoryImpl());
        Network network5 = ThreeWindingsTransformerNetworkFactory.create(new NetworkFactoryImpl());
        network6 = EurostagTutorialExample1Factory.createWithFixedCurrentLimits(new NetworkFactoryImpl());
        given(networkStoreService.getNetwork(NETWORK_UUID)).willReturn(network);
        given(networkStoreService.getNetwork(NETWORK_UUID_2)).willReturn(network2);
        given(networkStoreService.getNetwork(NETWORK_UUID_3)).willReturn(network3);
        given(networkStoreService.getNetwork(NETWORK_UUID_4)).willReturn(network4);
        given(networkStoreService.getNetwork(NETWORK_UUID_5)).willReturn(network5);
        given(networkStoreService.getNetwork(NETWORK_UUID_6)).willReturn(network6);
        given(networkStoreService.getNetwork(NETWORK_NOT_FOUND_UUID)).willReturn(null);

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
    public void tearDown() {
        List<String> destinations = List.of(elementUpdateDestination);

        cleanDB();
        assertQueuesEmptyThenClear(destinations, output);
    }

    private void cleanDB() {
        filterService.deleteAll();

    }

    private void assertQueuesEmptyThenClear(List<String> destinations, OutputDestination output) {
        try {
            destinations.forEach(destination -> assertNull("Should not be any messages in queue " + destination + " : ", output.receive(TIMEOUT, destination)));
        } catch (NullPointerException e) {
            // Ignoring
        } finally {
            output.clear(); // purge in order to not fail the other tests
        }
    }

    public String joinWithComma(Object... array) {
        return join(array, ",");
    }

    @Test
    public void testLineFilter() throws Exception {
        String userId = "userId";
        UUID filterId1 = UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e");
        UUID filterId2 = UUID.fromString("42b70a4d-e0c4-413a-8e3e-78e9027d300f");
        Date modificationDate = new Date();

        LineFilter lineFilter = LineFilter.builder().equipmentID("NHV1_NHV2_1")
            .substationName1("P1")
            .substationName2("P2")
            .countries1(new TreeSet<>(Set.of("FR")))
            .countries2(new TreeSet<>(Set.of("FR")))
            .freeProperties2(Map.of("region", List.of("north")))
            .freeProperties1(Map.of("region", List.of("south")))
            .nominalVoltage1(new NumericalFilter(RangeType.RANGE, 360., 400.))
            .nominalVoltage2(new NumericalFilter(RangeType.RANGE, 356.25, 393.75))
            .build();
        CriteriaFilter lineCriteriaFilter = new CriteriaFilter(
                filterId1,
                modificationDate,
                lineFilter
        );
        insertFilter(filterId1, lineCriteriaFilter);
        checkFormFilter(filterId1, lineCriteriaFilter);

        // export
        assertThrows("Network '" + NETWORK_NOT_FOUND_UUID + "' not found", ServletException.class, () -> mvc.perform(get(URL_TEMPLATE + "/" + filterId1 + "/export?networkUuid=" + NETWORK_NOT_FOUND_UUID)
            .contentType(APPLICATION_JSON)));

        mvc.perform(get(URL_TEMPLATE + "/" + filterId1 + "/export?networkUuid=" + NETWORK_UUID)
            .contentType(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
            .andExpect(content().json("[{\"id\":\"NHV1_NHV2_1\",\"type\":\"LINE\"}]"));

        List<FilterAttributes> filterAttributes = objectMapper.readValue(
            mvc.perform(get("/" + FilterApi.API_VERSION + "/filters/metadata?ids={id}", filterId1)
                    .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(),
            new TypeReference<>() {
            });

        Date dateModification = filterAttributes.get(0).getModificationDate();

        CriteriaFilter hvdcLineCriteriaFilter = new CriteriaFilter(
                filterId1,
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
        modifyFormFilter(filterId1, hvdcLineCriteriaFilter, userId);
        checkFormFilter(filterId1, hvdcLineCriteriaFilter);

        ScriptFilter scriptFilter = new ScriptFilter(filterId2, modificationDate, "test");
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

        matchFilterInfos(filterAttributes.get(0), filterId1, FilterType.CRITERIA, EquipmentType.HVDC_LINE, modificationDate);
        matchFilterInfos(filterAttributes.get(1), filterId2, FilterType.SCRIPT, null, modificationDate);

        filterAttributes = objectMapper.readValue(
            mvc.perform(get("/" + FilterApi.API_VERSION + "/filters/metadata?ids={id}", filterId1)
                    .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(),
            new TypeReference<>() {
            });
        assertEquals(1, filterAttributes.size());
        matchFilterInfos(filterAttributes.get(0), filterId1, FilterType.CRITERIA, EquipmentType.HVDC_LINE, modificationDate);

        // test replace line filter with other filter type
        AbstractFilter generatorFormFilter = new CriteriaFilter(
                filterId1,
                modificationDate,
                new GeneratorFilter("eqId1", "gen1", "s1", new TreeSet<>(Set.of("FR", "BE")), null, new NumericalFilter(RangeType.RANGE, 50., null), null)
        );

        modifyFormFilter(filterId1, generatorFormFilter, userId);

        filterAttributes = objectMapper.readValue(
            mvc.perform(get("/" + FilterApi.API_VERSION + "/filters/metadata?ids={id}", filterId1)
                    .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(),
            new TypeReference<>() {
            });
        assertEquals(1, filterAttributes.size());
        matchFilterInfos(filterAttributes.get(0), filterId1, FilterType.CRITERIA, EquipmentType.GENERATOR, modificationDate);

        // update with same type filter
        AbstractFilter generatorFormFilter2 = new CriteriaFilter(
                filterId1,
                modificationDate,
                new GeneratorFilter("eqId2", "gen2", "s2", new TreeSet<>(Set.of("FR", "BE")), null, new NumericalFilter(RangeType.RANGE, 50., null), null)
        );
        modifyFormFilter(filterId1, generatorFormFilter2, userId);

        // delete
        mvc.perform(delete(URL_TEMPLATE + "/" + filterId2)).andExpect(status().isOk());

        mvc.perform(delete(URL_TEMPLATE + "/" + filterId2)).andExpect(status().isNotFound());

        mvc.perform(get(URL_TEMPLATE + "/" + filterId2)).andExpect(status().isNotFound());

        mvc.perform(put(URL_TEMPLATE + "/" + filterId2).contentType(APPLICATION_JSON).content(objectMapper.writeValueAsString(scriptFilter)).header(USER_ID_HEADER, userId)).andExpect(status().isNotFound());

        filterService.deleteAll();
    }

    @Test
    public void testLineFilter2() throws Exception {
        String userId = "userId";
        UUID filterId3 = UUID.fromString("42b70a4d-e0c4-413a-8e3e-78e9027d300c");
        UUID filterId4 = UUID.fromString("42b70a4d-e0c4-413a-8e3e-78e9027d300d");
        Date modificationDate = new Date();

        // a 2-country network (one substation FR, one BE)
        final double p2NominalVoltage = 63.;
        network6.getLine("NHV1_NHV2_2").getTerminal2().getVoltageLevel().setNominalV(p2NominalVoltage); // patch just for better coverage
        final String noMatch = "[]";
        final String bothMatch = "[{\"id\":\"NHV1_NHV2_1\",\"type\":\"LINE\"},{\"id\":\"NHV1_NHV2_2\",\"type\":\"LINE\"}]";

        List<RangeType> rangeTypes = new ArrayList<>();
        rangeTypes.add(RangeType.EQUALITY);
        List<Double> values1 = new ArrayList<>();
        values1.add(p2NominalVoltage);
        List<Double> values2 = new ArrayList<>();
        values2.add(null);

        CriteriaFilter lineCriteriaFilterBEFR = insertLineFilter(filterId3, null, null, null, new TreeSet<>(Set.of("BE")), new TreeSet<>(Set.of("FR")),
                rangeTypes, values1, values2, NETWORK_UUID_6, null, bothMatch, false);

        // update form filter <-> script filter (rejected)
        ScriptFilter scriptFilter = new ScriptFilter(filterId4, modificationDate, "test");
        insertFilter(filterId4, scriptFilter);
        checkScriptFilter(filterId4, scriptFilter);
        assertThrows(ServletException.class, () -> mvc.perform(put(URL_TEMPLATE + "/" + filterId3)
                .content(objectMapper.writeValueAsString(scriptFilter))
                .contentType(APPLICATION_JSON)
                .header(USER_ID_HEADER, userId)));
        assertThrows(ServletException.class, () -> mvc.perform(put(URL_TEMPLATE + "/" + filterId4)
                .content(objectMapper.writeValueAsString(lineCriteriaFilterBEFR))
                .contentType(APPLICATION_JSON)
                .header(USER_ID_HEADER, userId)));

        mvc.perform(delete(URL_TEMPLATE + "/" + filterId3)).andExpect(status().isOk());
        mvc.perform(delete(URL_TEMPLATE + "/" + filterId4)).andExpect(status().isOk());

        // more country filters
        rangeTypes.add(RangeType.GREATER_OR_EQUAL);
        rangeTypes.set(0, RangeType.GREATER_OR_EQUAL);
        values1.set(0, 0.);
        values1.add(0.);
        values2.add(null);
        insertLineFilter(filterId3, null, null, null, new TreeSet<>(Set.of("BE")), new TreeSet<>(Set.of("FR")),
                rangeTypes, values1, values2, NETWORK_UUID_6, null, bothMatch, true);

        network6.getSubstation("P2").setCountry(Country.FR);
        insertLineFilter(filterId3, null, null, null, new TreeSet<>(Set.of("IT")), new TreeSet<>(Set.of("FR")),
                rangeTypes, values1, values2, NETWORK_UUID_6, null, noMatch, true);
        insertLineFilter(filterId3, null, null, null, new TreeSet<>(Set.of()), new TreeSet<>(Set.of("IT")),
                rangeTypes, values1, values2, NETWORK_UUID_6, null, noMatch, true);
        network6.getSubstation("P1").setCountry(Country.IT);
        insertLineFilter(filterId3, null, null, null, new TreeSet<>(Set.of("FR")), new TreeSet<>(Set.of("IT")),
                rangeTypes, values1, values2, NETWORK_UUID_6, null, bothMatch, true);

        filterService.deleteAll();
    }

    @Test
    public void testGeneratorFilter() throws Exception {
        final String generatorUuid = "42b70a4d-e0c4-413a-8e3e-78e9027d300f";
        final String noMatch = "[]";
        final String oneMatch = "[{\"id\":\"GEN\",\"type\":\"GENERATOR\"}]";
        final String bothMatch = "[{\"id\":\"GEN\",\"type\":\"GENERATOR\"}, {\"id\":\"GEN2\",\"type\":\"GENERATOR\"}]";
        insertInjectionFilter(EquipmentType.GENERATOR, UUID.fromString(generatorUuid),
            "GEN", "GEN", "P1", Set.of("FR", "IT"), RangeType.RANGE, 15., 30., null, NETWORK_UUID, null, oneMatch);
        insertInjectionFilter(EquipmentType.GENERATOR, UUID.fromString(generatorUuid),
            "GEN", "GEN", "P1", null, RangeType.RANGE, 15., 30., null, NETWORK_UUID, null, oneMatch);
        insertInjectionFilter(EquipmentType.GENERATOR, UUID.fromString(generatorUuid),
            "GEN", "nameNotFound", "P1", null, RangeType.RANGE, 15., 30., null, NETWORK_UUID, null, noMatch);
        insertInjectionFilter(EquipmentType.GENERATOR, UUID.fromString(generatorUuid),
            "GEN", "GEN", null, null, RangeType.RANGE, 15., 30., null, NETWORK_UUID, null, oneMatch);
        insertInjectionFilter(EquipmentType.GENERATOR, UUID.fromString(generatorUuid),
            "GEN", "GEN", "substationNameNotFound", null, RangeType.RANGE, 15., 30., null, NETWORK_UUID, null, noMatch);
        insertInjectionFilter(EquipmentType.GENERATOR, UUID.fromString(generatorUuid),
            "GEN", "GEN", "P1", Set.of("FR", "IT"), RangeType.EQUALITY, 145., null, null, NETWORK_UUID, null, noMatch);
        insertInjectionFilter(EquipmentType.GENERATOR, UUID.fromString(generatorUuid),
            "GEN", "GEN", "P1", Set.of("FR", "IT"), RangeType.RANGE, 19., 22., null, NETWORK_UUID, null, noMatch);
        insertInjectionFilter(EquipmentType.GENERATOR, UUID.fromString(generatorUuid),
            "GEN", "GEN", "P1", Set.of("FR", "IT"), RangeType.RANGE, 27., 30., null, NETWORK_UUID, null, noMatch);
        insertInjectionFilter(EquipmentType.GENERATOR, UUID.fromString(generatorUuid),
            "GEN", "GEN", "P1", Set.of("FR", "IT"), RangeType.RANGE, 34.30, 35.70, null, NETWORK_UUID, null, noMatch);
        insertInjectionFilter(EquipmentType.GENERATOR, UUID.fromString(generatorUuid),
            "GEN", "GEN", "P1", Set.of("FR", "IT"), RangeType.RANGE, 14.55, 15.45, null, NETWORK_UUID, null, noMatch);
        // no filter at all
        insertInjectionFilter(EquipmentType.GENERATOR, UUID.fromString(generatorUuid),
            null, null, null, null, null, 0., 0., null, NETWORK_UUID, null, bothMatch);
        // no SOLAR generator
        insertInjectionFilter(EquipmentType.GENERATOR, UUID.fromString(generatorUuid),
            null, null, null, null, null, 0., 0., EnergySource.SOLAR, NETWORK_UUID, null, noMatch);
        // 2 OTHER generators in our network
        insertInjectionFilter(EquipmentType.GENERATOR, UUID.fromString(generatorUuid),
            null, null, null, null, null, 0., 0., EnergySource.OTHER, NETWORK_UUID, null, bothMatch);
    }

    @Test
    public void testLoadFilter() throws Exception {
        insertInjectionFilter(EquipmentType.LOAD, UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e"),
            "LOAD", null, "P2", Set.of("FR"), RangeType.RANGE, 144., 176., null, NETWORK_UUID, VARIANT_ID_1, "[{\"id\":\"LOAD\",\"type\":\"LOAD\"}]");
    }

    @Test
    public void testShuntCompensatorFilter() throws Exception {
        insertInjectionFilter(EquipmentType.SHUNT_COMPENSATOR, UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e"),
            "SHUNT", "SHUNT", "S1", Set.of("FR"), RangeType.EQUALITY, 380., null, null, NETWORK_UUID_4, null, "[{\"id\":\"SHUNT\",\"type\":\"SHUNT_COMPENSATOR\"}]");
    }

    @Test
    public void testStaticVarCompensatorFilter() throws Exception {
        insertInjectionFilter(EquipmentType.STATIC_VAR_COMPENSATOR, UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e"),
            "SVC3", null, "S2", null, null, null, null, null, NETWORK_UUID_3, null, "[{\"id\":\"SVC3\",\"type\":\"STATIC_VAR_COMPENSATOR\"}]");
    }

    @Test
    public void testBatteryFilter() throws Exception {
        insertInjectionFilter(EquipmentType.BATTERY, UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e"),
            "batteryId1", "batteryName", null, Set.of("FR"), RangeType.RANGE, 45., 65., null, NETWORK_UUID, null, "[]");
    }

    @Test
    public void testBusBarSectionFilter() throws Exception {
        insertInjectionFilter(EquipmentType.BUSBAR_SECTION, UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e"),
            null, "batteryName", null, Set.of("DE"), RangeType.EQUALITY, 380., null, null, NETWORK_UUID, null, "[]");
    }

    @Test
    public void testDanglingLineFilter() throws Exception {
        insertInjectionFilter(EquipmentType.DANGLING_LINE, UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e"),
            "danglingLineId1", null, "s2", Set.of("FR"), RangeType.RANGE, 138., 162., null, NETWORK_UUID, null, "[]");
    }

    @Test
    public void testLccConverterStationFilter() throws Exception {
        insertInjectionFilter(EquipmentType.LCC_CONVERTER_STATION, UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e"),
            "lccId1", "lccName1", "s3", Set.of("FR", "BE", "NL", "DE", "IT"), RangeType.RANGE, 20., 400., null, NETWORK_UUID, null, "[]");
    }

    @Test
    public void testVscConverterStationFilter() throws Exception {
        insertInjectionFilter(EquipmentType.VSC_CONVERTER_STATION, UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e"),
            "vscId1", "vscName1", "s2", null, RangeType.EQUALITY, 225., null, null, NETWORK_UUID, null, "[]");
    }

    @Test
    public void testHvdcLineFilter() throws Exception {
        final String noMatch = "[]";
        final String matchHVDCLine = "[{\"id\":\"L\",\"type\":\"HVDC_LINE\"}]";
        insertHvdcLineFilter(UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e"),
            null, "HVDC", "S1", "S2", new TreeSet<>(Set.of("FR", "BE")), new TreeSet<>(Set.of("FR", "IT")), RangeType.RANGE, 380., 420., NETWORK_UUID_2, null, matchHVDCLine);
        insertHvdcLineFilter(UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e"),
            null, "HVDC", "S1", "substationNameNotFound", new TreeSet<>(Set.of("FR", "BE")), new TreeSet<>(Set.of("FR", "IT")), RangeType.RANGE, 380., 420., NETWORK_UUID_2, null, noMatch);
        insertHvdcLineFilter(UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e"),
            null, "HVDC", "substationNameNotFound", "S1", new TreeSet<>(Set.of("FR", "BE")), new TreeSet<>(Set.of("FR", "IT")), RangeType.RANGE, 380., 420., NETWORK_UUID_2, null, noMatch);
        insertHvdcLineFilter(UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e"),
            null, "HVDC", "S1", "S2", new TreeSet<>(Set.of("IT")), new TreeSet<>(Set.of("FR")), RangeType.RANGE, 380., 420., NETWORK_UUID_2, null, noMatch);
        insertHvdcLineFilter(UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e"),
                null, "HVDC", "S1", "S2", new TreeSet<>(Set.of("FR")), new TreeSet<>(Set.of("IT")), RangeType.RANGE, 380., 420., NETWORK_UUID_2, null, noMatch);
        network2.getSubstation("S1").setCountry(Country.IT);
        insertHvdcLineFilter(UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e"),
            null, "HVDC", "S1", "S2", new TreeSet<>(Set.of("FR")), new TreeSet<>(Set.of("IT")), RangeType.RANGE, 380., 420., NETWORK_UUID_2, null, matchHVDCLine);
    }

    @Test
    public void testTwoWindingsTransformerFilter() throws Exception {
        List<RangeType> rangeTypes = new ArrayList<>();
        rangeTypes.add(RangeType.EQUALITY);
        rangeTypes.add(RangeType.RANGE);
        List<Double> values1 = new ArrayList<>();
        values1.add(380.);
        values1.add(142.5);
        List<Double> values2 = new ArrayList<>();
        values2.add(null);
        values2.add(157.5);

        // with this network (EurostagTutorialExample1Factory::create), we have 2 2WT Transfos:
        // - NGEN_NHV1  term1: 24 kV term2: 380 kV
        // - NHV2_NLOAD term1: 380 kV term2: 150 kV
        final String noMatch = "[]";
        final String matchNHV2NLOAD = "[{\"id\":\"NHV2_NLOAD\",\"type\":\"TWO_WINDINGS_TRANSFORMER\"}]";
        final String matchNGENNHV1 = "[{\"id\":\"NGEN_NHV1\",\"type\":\"TWO_WINDINGS_TRANSFORMER\"}]";
        final String bothMatch = "[{\"id\":\"NHV2_NLOAD\",\"type\":\"TWO_WINDINGS_TRANSFORMER\"},{\"id\":\"NGEN_NHV1\",\"type\":\"TWO_WINDINGS_TRANSFORMER\"}]";

        insertTransformerFilter(EquipmentType.TWO_WINDINGS_TRANSFORMER, UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e"),
            "NHV2_NLOAD", "NHV2_NLOAD", "P2", Set.of("FR"), rangeTypes, values1, values2, NETWORK_UUID, null, matchNHV2NLOAD);
        // no eqpt/substation filter: only NHV2_NLOAD match because of RANGE filter
        insertTransformerFilter(EquipmentType.TWO_WINDINGS_TRANSFORMER, UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e"),
                null, null, null, Set.of("FR"), rangeTypes, values1, values2, NETWORK_UUID, null, matchNHV2NLOAD);
        // bad substationName
        insertTransformerFilter(EquipmentType.TWO_WINDINGS_TRANSFORMER, UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e"),
            "NHV2_NLOAD", "NHV2_NLOAD", "substationNameNotFound", Set.of("FR"), rangeTypes, values1, values2, NETWORK_UUID, null, noMatch);
        // this network has only FR substations: IT does not match:
        insertTransformerFilter(EquipmentType.TWO_WINDINGS_TRANSFORMER, UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e"),
            "NHV2_NLOAD", "NHV2_NLOAD", "P2", Set.of("IT"), rangeTypes, values1, values2, NETWORK_UUID, null, noMatch);

        // change RANGE into "> 24"
        rangeTypes.set(1, RangeType.GREATER_THAN);
        values1.set(1, 24.);
        values2.set(1, null);
        // NGEN_NHV1 still does not match
        insertTransformerFilter(EquipmentType.TWO_WINDINGS_TRANSFORMER, UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e"),
                null, null, null, Set.of("FR"), rangeTypes, values1, values2, NETWORK_UUID, null, matchNHV2NLOAD);

        // change "> 24" into ">= 24"
        rangeTypes.set(1, RangeType.GREATER_OR_EQUAL);
        // both transfos now match both filters
        insertTransformerFilter(EquipmentType.TWO_WINDINGS_TRANSFORMER, UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e"),
                null, null, null, Set.of("FR"), rangeTypes, values1, values2, NETWORK_UUID, null, bothMatch);

        // change "== 380" into ">= 0"
        // change ">= 24" into "< 380"
        rangeTypes.set(0, RangeType.GREATER_OR_EQUAL);
        values1.set(0, 0.);
        rangeTypes.set(1, RangeType.LESS_THAN);
        values1.set(1, 380.);
        // both match
        insertTransformerFilter(EquipmentType.TWO_WINDINGS_TRANSFORMER, UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e"),
                null, null, null, Set.of("FR"), rangeTypes, values1, values2, NETWORK_UUID, null, bothMatch);
        // add substation filter on P1 => NGENNHV1
        insertTransformerFilter(EquipmentType.TWO_WINDINGS_TRANSFORMER, UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e"),
                null, null, "P1", Set.of("FR"), rangeTypes, values1, values2, NETWORK_UUID, null, matchNGENNHV1);
        // add substation filter on P2 => NHV2NLOAD
        insertTransformerFilter(EquipmentType.TWO_WINDINGS_TRANSFORMER, UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e"),
                null, null, "P2", Set.of("FR"), rangeTypes, values1, values2, NETWORK_UUID, null, matchNHV2NLOAD);

        // change "< 380" into "< 150"
        values1.set(1, 150.);
        // only NGEN_NHV1 match
        insertTransformerFilter(EquipmentType.TWO_WINDINGS_TRANSFORMER, UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e"),
                null, null, null, Set.of("FR"), rangeTypes, values1, values2, NETWORK_UUID, null, matchNGENNHV1);

        // change "< 150" into "<= 150"
        rangeTypes.set(1, RangeType.LESS_OR_EQUAL);
        // both match
        insertTransformerFilter(EquipmentType.TWO_WINDINGS_TRANSFORMER, UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e"),
                null, null, null, Set.of("FR"), rangeTypes, values1, values2, NETWORK_UUID, null, bothMatch);

        // change ">=0" into "> 400"
        rangeTypes.set(0, RangeType.GREATER_OR_EQUAL);
        values1.set(0, 400.);
        // [400..150] not possible
        insertTransformerFilter(EquipmentType.TWO_WINDINGS_TRANSFORMER, UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e"),
                null, null, null, Set.of("FR"), rangeTypes, values1, values2, NETWORK_UUID, null, noMatch);
    }

    @Test
    public void testThreeWindingsTransformerFilter() throws Exception {
        List<RangeType> rangeTypes = new ArrayList<>();
        rangeTypes.add(RangeType.RANGE);
        rangeTypes.add(RangeType.EQUALITY);
        rangeTypes.add(RangeType.EQUALITY);
        List<Double> values1 = new ArrayList<>();
        values1.add(127.);
        values1.add(33.);
        values1.add(11.);
        List<Double> values2 = new ArrayList<>();
        values2.add(134.);
        values2.add(null);
        values2.add(null);

        // with this network (ThreeWindingsTransformerNetworkFactory.create), we have a single 3WT:
        // - 3WT  term1: 132 kV term2: 33 kV  term3: 11 kV
        final String noMatch = "[]";
        final String match3WT = "[{\"id\":\"3WT\",\"type\":\"THREE_WINDINGS_TRANSFORMER\"}]";

        insertTransformerFilter(EquipmentType.THREE_WINDINGS_TRANSFORMER, UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e"),
            "3WT", null, "SUBSTATION", Set.of("FR", "CH"), rangeTypes, values1, values2, NETWORK_UUID_5, null, match3WT);
        // same without eqpt / sybstation
        insertTransformerFilter(EquipmentType.THREE_WINDINGS_TRANSFORMER, UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e"),
                null, null, null, Set.of("FR", "CH"), rangeTypes, values1, values2, NETWORK_UUID_5, null, match3WT);
        // bad substationName
        insertTransformerFilter(EquipmentType.THREE_WINDINGS_TRANSFORMER, UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e"),
            "3WT", null, "substationNameNotFound", Set.of("FR", "CH"), rangeTypes, values1, values2, NETWORK_UUID_5, null, noMatch);
        // IT does not match
        insertTransformerFilter(EquipmentType.THREE_WINDINGS_TRANSFORMER, UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e"),
            "3WT", null, "SUBSTATION", Set.of("IT"), rangeTypes, values1, values2, NETWORK_UUID_5, null, noMatch);

        // Current filters have covered OR #1/6 in get3WTransformerList

        // variant to increase coverage
        values1.set(2, 500.);
        insertTransformerFilter(EquipmentType.THREE_WINDINGS_TRANSFORMER, UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e"),
                null, null, null, Set.of("FR", "CH"), rangeTypes, values1, values2, NETWORK_UUID_5, null, noMatch);

        // Update filters to cover OR #2/6
        values1.set(1, 11.);
        values1.set(2, 33.);
        insertTransformerFilter(EquipmentType.THREE_WINDINGS_TRANSFORMER, UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e"),
                null, null, null, Set.of("FR", "CH"), rangeTypes, values1, values2, NETWORK_UUID_5, null, match3WT);
        // variant to increase coverage
        values1.set(2, 500.);
        insertTransformerFilter(EquipmentType.THREE_WINDINGS_TRANSFORMER, UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e"),
                null, null, null, Set.of("FR", "CH"), rangeTypes, values1, values2, NETWORK_UUID_5, null, noMatch);

        // Update filters to cover OR #3/6
        values1.set(0, 33.);
        values2.set(0, 33.);
        values1.set(1, 132.);
        values1.set(2, 11.);
        insertTransformerFilter(EquipmentType.THREE_WINDINGS_TRANSFORMER, UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e"),
                null, null, null, Set.of("FR", "CH"), rangeTypes, values1, values2, NETWORK_UUID_5, null, match3WT);
        // variant to increase coverage
        values1.set(2, 500.);
        insertTransformerFilter(EquipmentType.THREE_WINDINGS_TRANSFORMER, UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e"),
                null, null, null, Set.of("FR", "CH"), rangeTypes, values1, values2, NETWORK_UUID_5, null, noMatch);

        // Update filters to cover OR #4/6
        values1.set(1, 11.);
        values1.set(2, 132.);
        insertTransformerFilter(EquipmentType.THREE_WINDINGS_TRANSFORMER, UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e"),
                null, null, null, Set.of("FR", "CH"), rangeTypes, values1, values2, NETWORK_UUID_5, null, match3WT);
        // variant to increase coverage
        values1.set(2, 500.);
        insertTransformerFilter(EquipmentType.THREE_WINDINGS_TRANSFORMER, UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e"),
                null, null, null, Set.of("FR", "CH"), rangeTypes, values1, values2, NETWORK_UUID_5, null, noMatch);

        // Update filters to cover OR #5/6
        values1.set(0, 10.);
        values2.set(0, 12.);
        values1.set(1, 132.);
        values1.set(2, 33.);
        insertTransformerFilter(EquipmentType.THREE_WINDINGS_TRANSFORMER, UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e"),
                null, null, null, Set.of("FR", "CH"), rangeTypes, values1, values2, NETWORK_UUID_5, null, match3WT);
        // variant to increase coverage
        values1.set(2, 500.);
        insertTransformerFilter(EquipmentType.THREE_WINDINGS_TRANSFORMER, UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e"),
                null, null, null, Set.of("FR", "CH"), rangeTypes, values1, values2, NETWORK_UUID_5, null, noMatch);

        // Update filters to cover OR #6/6
        values1.set(1, 33.);
        values1.set(2, 132.);
        insertTransformerFilter(EquipmentType.THREE_WINDINGS_TRANSFORMER, UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e"),
                null, null, null, Set.of("FR", "CH"), rangeTypes, values1, values2, NETWORK_UUID_5, null, match3WT);
        // variant to increase coverage
        values1.set(2, 500.);
        insertTransformerFilter(EquipmentType.THREE_WINDINGS_TRANSFORMER, UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e"),
                null, null, null, Set.of("FR", "CH"), rangeTypes, values1, values2, NETWORK_UUID_5, null, noMatch);
    }

    @Test
    public void testVoltageLevelFilter() throws Exception {
        insertVoltageLevelFilter(UUID.fromString("42b70a4d-e0c4-413a-8e3e-78e9027d300f"),
            "VLGEN", "VLGEN", Set.of("FR", "IT"), RangeType.RANGE, 15., 30., NETWORK_UUID, null, "[{\"id\":\"VLGEN\",\"type\":\"VOLTAGE_LEVEL\"}]");
        insertVoltageLevelFilter(UUID.fromString("42b70a4d-e0c4-413a-8e3e-78e9027d300f"),
            "VLGEN", "VLGEN", null, RangeType.RANGE, 15., 30., NETWORK_UUID, null, "[{\"id\":\"VLGEN\",\"type\":\"VOLTAGE_LEVEL\"}]");
        insertVoltageLevelFilter(UUID.fromString("42b70a4d-e0c4-413a-8e3e-78e9027d300f"),
            "VLGEN", "nameNotFound", null, RangeType.RANGE, 15., 30., NETWORK_UUID, null, "[]");
        insertVoltageLevelFilter(UUID.fromString("42b70a4d-e0c4-413a-8e3e-78e9027d300f"),
            "VLGEN", "VLGEN", null, RangeType.RANGE, 20., 27., NETWORK_UUID, null, "[{\"id\":\"VLGEN\",\"type\":\"VOLTAGE_LEVEL\"}]");
        insertVoltageLevelFilter(UUID.fromString("42b70a4d-e0c4-413a-8e3e-78e9027d300f"),
            "VLGEN", "VLGEN", null, RangeType.RANGE, 29., 36., NETWORK_UUID, null, "[]");
        insertVoltageLevelFilter(UUID.fromString("42b70a4d-e0c4-413a-8e3e-78e9027d300f"),
            "VLGEN", "VLGEN", Set.of("FR", "IT"), RangeType.EQUALITY, 150., null, NETWORK_UUID, null, "[]");
        insertVoltageLevelFilter(UUID.fromString("42b70a4d-e0c4-413a-8e3e-78e9027d300f"),
            "VLGEN", "VLGEN", Set.of("FR", "IT"), RangeType.EQUALITY, 24., null, NETWORK_UUID, null, "[{\"id\":\"VLGEN\",\"type\":\"VOLTAGE_LEVEL\"}]");
        insertVoltageLevelFilter(UUID.fromString("42b70a4d-e0c4-413a-8e3e-78e9027d300f"),
            "VLGEN", "VLGEN", Set.of("ES", "PT"), null, null, null, NETWORK_UUID, null, "[]");
    }

    @Test
    public void testSubstationFilter() throws Exception {
        insertSubstationFilter(UUID.fromString("42b70a4d-e0c4-413a-8e3e-78e9027d300f"),
            "P1", "P1", Set.of("FR", "IT"), NETWORK_UUID, null, "[{\"id\":\"P1\",\"type\":\"SUBSTATION\"}]");
        insertSubstationFilter(UUID.fromString("42b70a4d-e0c4-413a-8e3e-78e9027d300f"),
            "P1", "P1", null, NETWORK_UUID, null, "[{\"id\":\"P1\",\"type\":\"SUBSTATION\"}]");
        insertSubstationFilter(UUID.fromString("42b70a4d-e0c4-413a-8e3e-78e9027d300f"),
            "P1", "nameNotFound", null, NETWORK_UUID, null, "[]");
        insertSubstationFilter(UUID.fromString("42b70a4d-e0c4-413a-8e3e-78e9027d300f"),
            "P1", "P1", Set.of("ES", "PT"), NETWORK_UUID, null, "[]");
        insertSubstationFilter(UUID.fromString("42b70a4d-e0c4-413a-8e3e-78e9027d300f"),
            "P2", "P2", Set.of("FR", "IT"), NETWORK_UUID, null, "[{\"id\":\"P2\",\"type\":\"SUBSTATION\"}]");
        insertSubstationFilter(UUID.fromString("42b70a4d-e0c4-413a-8e3e-78e9027d300f"),
            "P2", "P2", Set.of("ES", "PT"), NETWORK_UUID, null, "[]");
    }

    @Test
    public void testFilterToScript() throws Exception {
        String userId = "userId";
        UUID filterId1 = UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e");
        UUID filterId2 = UUID.fromString("42b70a4d-e0c4-413a-8e3e-78e9027d300f");
        UUID filterId3 = UUID.fromString("99999999-e0c4-413a-8e3e-78e9027d300f");
        Date modificationDate = new Date();

        LineFilter lineFilter = LineFilter.builder()
            .equipmentID("equipmentID").equipmentName("equipmentName")
            .substationName1("substationName1").substationName2("substationName2")
            .countries1(COUNTRIES1).countries2(COUNTRIES2)
            .nominalVoltage1(new NumericalFilter(RangeType.RANGE, 5., 8.))
            .nominalVoltage2(new NumericalFilter(RangeType.EQUALITY, 6., null))
            .build();

        CriteriaFilter lineCriteriaFilter = new CriteriaFilter(
                filterId1,
                modificationDate,
                lineFilter
        );

        insertFilter(filterId1, lineCriteriaFilter);
        checkFormFilter(filterId1, lineCriteriaFilter);

        // new script from filter
        mvc.perform(post(URL_TEMPLATE + "/" + filterId1 + "/new-script?newId=" + UUID.randomUUID())).andExpect(status().isOk());

        mvc.perform(get(URL_TEMPLATE))
            .andExpect(status().isOk())
            .andExpect(content().json("[{\"type\":\"CRITERIA\"}, {\"type\":\"SCRIPT\"}]"));

        // replace filter with script
        mvc.perform(put(URL_TEMPLATE + "/" + filterId1 + "/replace-with-script").header(USER_ID_HEADER, userId)).andExpect(status().isOk());

        checkElementUpdatedMessageSent(filterId1, userId);

        mvc.perform(get(URL_TEMPLATE))
            .andExpect(status().isOk())
            .andExpect(content().json("[{\"type\":\"SCRIPT\"}, {\"type\":\"SCRIPT\"}]"));

        checkScriptFilter(filterId1, new ScriptFilter(filterId1, modificationDate, "&& equipment.terminal1.voltageLevel.substation.name.equals('substationName1')"));

        ScriptFilter scriptFilter = new ScriptFilter(filterId2, modificationDate, "test");

        insertFilter(filterId2, scriptFilter);
        checkScriptFilter(filterId2, new ScriptFilter(filterId2, modificationDate, "test"));

        assertThrows("Wrong filter type, should never happen", Exception.class, () -> mvc.perform(post(URL_TEMPLATE + "/" + filterId2 + "/new-script?newId=" + UUID.randomUUID()).header(USER_ID_HEADER, userId)));
        assertThrows("Wrong filter type, should never happen", Exception.class, () -> mvc.perform(put(URL_TEMPLATE + "/" + filterId2 + "/replace-with-script").header(USER_ID_HEADER, userId)));
        mvc.perform(post(URL_TEMPLATE + "/" + filterId3 + "/new-script?newId=" + filterId2)).andExpect(status().isNotFound());
        mvc.perform(put(URL_TEMPLATE + "/" + filterId3 + "/replace-with-script").header(USER_ID_HEADER, userId)).andExpect(status().isNotFound());

        assertThrows("Filter implementation not yet supported: ScriptFilter", ServletException.class, () -> mvc.perform(get(URL_TEMPLATE + "/" + filterId2 + "/export?networkUuid=" + NETWORK_UUID)
            .contentType(APPLICATION_JSON)));
    }

    @Test
    public void testDuplicateFilter() throws Exception {
        UUID filterId1 = UUID.fromString("99999999-e0c4-413a-8e3e-78e9027d300f");
        Date modificationDate = new Date();
        LineFilter lineFilter = LineFilter.builder().equipmentID("equipmentID").equipmentName("equipmentName")
            .substationName1("substationName1")
            .substationName2("substationName2").countries1(COUNTRIES1).countries2(COUNTRIES2)
            .nominalVoltage1(new NumericalFilter(RangeType.RANGE, 5., 8.))
            .nominalVoltage2(new NumericalFilter(RangeType.EQUALITY, 6., null))
            .build();
        CriteriaFilter lineCriteriaFilter = new CriteriaFilter(
                filterId1,
                modificationDate,
                lineFilter
        );
        insertFilter(filterId1, lineCriteriaFilter);
        mvc.perform(post("/" + FilterApi.API_VERSION + "/filters?duplicateFrom=" + filterId1 + "&id=" + UUID.randomUUID())).andExpect(status().isOk());
        checkFormFilter(filterId1, lineCriteriaFilter);
    }

    @Test
    public void testIdentifierListFilter() throws Exception {
        UUID filterId = UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e");
        Date modificationDate = new Date();

        // Create identifier list filter for generators
        IdentifierListFilterEquipmentAttributes gen1 = new IdentifierListFilterEquipmentAttributes("GEN", 7d);
        IdentifierListFilterEquipmentAttributes gen2 = new IdentifierListFilterEquipmentAttributes("GEN2", 9d);
        IdentifierListFilter identifierListFilter = new IdentifierListFilter(filterId, modificationDate, EquipmentType.GENERATOR, List.of(gen1, gen2));
        insertFilter(filterId, identifierListFilter);
        checkIdentifierListFilter(filterId, identifierListFilter);
        checkIdentifierListFilterExportAndMetadata(filterId, "[{\"id\":\"GEN\",\"type\":\"GENERATOR\",\"distributionKey\":7.0},{\"id\":\"GEN2\",\"type\":\"GENERATOR\",\"distributionKey\":9.0}]\n", EquipmentType.GENERATOR);
        // Create identifier list filter for lines
        UUID lineFilterId = UUID.randomUUID();

        IdentifierListFilterEquipmentAttributes line1 = new IdentifierListFilterEquipmentAttributes("NHV1_NHV2_1", null);
        IdentifierListFilterEquipmentAttributes line2 = new IdentifierListFilterEquipmentAttributes("NHV1_NHV2_2", null);
        IdentifierListFilter lineIdentifierListFilter = new IdentifierListFilter(lineFilterId, modificationDate, EquipmentType.LINE, List.of(line1, line2));
        insertFilter(lineFilterId, lineIdentifierListFilter);
        checkIdentifierListFilter(lineFilterId, lineIdentifierListFilter);
        checkIdentifierListFilterExportAndMetadata(lineFilterId, "[{\"id\":\"NHV1_NHV2_1\",\"type\":\"LINE\",\"distributionKey\":null},{\"id\":\"NHV1_NHV2_2\",\"type\":\"LINE\",\"distributionKey\":null}]", EquipmentType.LINE);

        // Create identifier list filter for Two Windings Transformer
        UUID twoWinTransformerIdentifierListFilterId = UUID.randomUUID();
        IdentifierListFilterEquipmentAttributes twoWT1 = new IdentifierListFilterEquipmentAttributes("NGEN_NHV1", null);
        IdentifierListFilterEquipmentAttributes twoWT2 = new IdentifierListFilterEquipmentAttributes("NHV2_NLOAD", null);
        IdentifierListFilterEquipmentAttributes twoWT3 = new IdentifierListFilterEquipmentAttributes("twoWT3", null);
        IdentifierListFilter twoWinTransformerIdentifierListFilter = new IdentifierListFilter(twoWinTransformerIdentifierListFilterId, modificationDate, EquipmentType.TWO_WINDINGS_TRANSFORMER, List.of(twoWT1, twoWT2, twoWT3));
        insertFilter(twoWinTransformerIdentifierListFilterId, twoWinTransformerIdentifierListFilter);
        checkIdentifierListFilter(twoWinTransformerIdentifierListFilterId, twoWinTransformerIdentifierListFilter);
        checkIdentifierListFilterExportAndMetadata(twoWinTransformerIdentifierListFilterId, "[{\"id\":\"NGEN_NHV1\",\"type\":\"TWO_WINDINGS_TRANSFORMER\",\"distributionKey\":null},{\"id\":\"NHV2_NLOAD\",\"type\":\"TWO_WINDINGS_TRANSFORMER\",\"distributionKey\":null}]", EquipmentType.TWO_WINDINGS_TRANSFORMER);

        // Create identifier list filter for Three Windings Transformer
        UUID threeWTransformerIdentifierListFilterId = UUID.randomUUID();
        IdentifierListFilterEquipmentAttributes threeWT1 = new IdentifierListFilterEquipmentAttributes("threeWT1", null);
        IdentifierListFilterEquipmentAttributes threeWT2 = new IdentifierListFilterEquipmentAttributes("threeWT2", null);
        IdentifierListFilter threeWinTransformerIdentifierListFilter = new IdentifierListFilter(threeWTransformerIdentifierListFilterId, modificationDate, EquipmentType.THREE_WINDINGS_TRANSFORMER, List.of(threeWT1, threeWT2));
        insertFilter(threeWTransformerIdentifierListFilterId, threeWinTransformerIdentifierListFilter);
        checkIdentifierListFilter(threeWTransformerIdentifierListFilterId, threeWinTransformerIdentifierListFilter);
        checkIdentifierListFilterExportAndMetadata(threeWTransformerIdentifierListFilterId, "[]", EquipmentType.THREE_WINDINGS_TRANSFORMER);

        // Create identifier list filter for hvdc
        UUID hvdcFilterId = UUID.randomUUID();
        IdentifierListFilterEquipmentAttributes hvdc1 = new IdentifierListFilterEquipmentAttributes("threeWT1", null);
        IdentifierListFilterEquipmentAttributes hvdc2 = new IdentifierListFilterEquipmentAttributes("threeWT2", null);
        IdentifierListFilter hvdcIdentifierListFilter = new IdentifierListFilter(hvdcFilterId, modificationDate, EquipmentType.HVDC_LINE, List.of(line1, line2));
        insertFilter(hvdcFilterId, hvdcIdentifierListFilter);
        checkIdentifierListFilter(hvdcFilterId, hvdcIdentifierListFilter);
        checkIdentifierListFilterExportAndMetadata(hvdcFilterId, "[]", EquipmentType.HVDC_LINE);

        // Create identifier list filter for voltage levels
        UUID voltageLevelFilterId = UUID.randomUUID();
        IdentifierListFilterEquipmentAttributes vl1 = new IdentifierListFilterEquipmentAttributes("VLHV1", null);
        IdentifierListFilterEquipmentAttributes vl2 = new IdentifierListFilterEquipmentAttributes("VLHV2", null);
        IdentifierListFilter vlIdentifierListFilter = new IdentifierListFilter(voltageLevelFilterId, modificationDate, EquipmentType.VOLTAGE_LEVEL, List.of(vl1, vl2));
        insertFilter(voltageLevelFilterId, vlIdentifierListFilter);
        checkIdentifierListFilter(voltageLevelFilterId, vlIdentifierListFilter);
        checkIdentifierListFilterExportAndMetadata(voltageLevelFilterId, "[{\"id\":\"VLHV1\",\"type\":\"VOLTAGE_LEVEL\"},{\"id\":\"VLHV2\",\"type\":\"VOLTAGE_LEVEL\"}]\n", EquipmentType.VOLTAGE_LEVEL);

        // Create identifier list filter for substations
        UUID substationFilterId = UUID.randomUUID();
        IdentifierListFilterEquipmentAttributes s1 = new IdentifierListFilterEquipmentAttributes("P1", null);
        IdentifierListFilterEquipmentAttributes s2 = new IdentifierListFilterEquipmentAttributes("P2", null);
        IdentifierListFilterEquipmentAttributes s3 = new IdentifierListFilterEquipmentAttributes("P3", null);
        IdentifierListFilter sIdentifierListFilter = new IdentifierListFilter(substationFilterId, modificationDate, EquipmentType.SUBSTATION, List.of(s1, s2, s3));
        insertFilter(substationFilterId, sIdentifierListFilter);
        checkIdentifierListFilter(substationFilterId, sIdentifierListFilter);
        checkIdentifierListFilterExportAndMetadata(substationFilterId, "[{\"id\":\"P1\",\"type\":\"SUBSTATION\"},{\"id\":\"P2\",\"type\":\"SUBSTATION\"}]\n", EquipmentType.SUBSTATION);
    }

    @Test
    public void testGetFiltersByIds() throws Exception {
        UUID filterId3 = UUID.fromString("42b70a4d-e0c4-413a-8e3e-78e9027d300c");
        UUID filterId4 = UUID.fromString("42b70a4d-e0c4-413a-8e3e-78e9027d300d");

        LineFilter lineFilter = LineFilter.builder().equipmentID("NHV1_NHV2_1").substationName1("P1").substationName2("P2")
            .countries1(new TreeSet<>(Set.of("FR"))).countries2(new TreeSet<>(Set.of("FR")))
            .nominalVoltage1(new NumericalFilter(RangeType.RANGE, 360., 400.)).nominalVoltage2(new NumericalFilter(RangeType.RANGE, 356.25, 393.75)).build();
        CriteriaFilter lineCriteriaFilter = new CriteriaFilter(
                filterId3,
                new Date(),
                lineFilter
        );
        insertFilter(filterId3, lineCriteriaFilter);
        checkFormFilter(filterId3, lineCriteriaFilter);

        LineFilter lineFilter2 = LineFilter.builder().equipmentID("NHV1_NHV2_1").substationName1("P1").substationName2("P2")
            .countries1(new TreeSet<>(Set.of("FR"))).countries2(new TreeSet<>(Set.of("FR")))
            .nominalVoltage1(new NumericalFilter(RangeType.RANGE, 360., 400.)).nominalVoltage2(new NumericalFilter(RangeType.RANGE, 356.25, 393.75)).build();

        CriteriaFilter lineCriteriaFilter2 = new CriteriaFilter(
                filterId4,
                new Date(),
                lineFilter2
        );

        insertFilter(filterId4, lineCriteriaFilter2);
        checkFormFilter(filterId4, lineCriteriaFilter2);
    }

    @Test
    public void testExportFilters() throws Exception {
        UUID filterId = UUID.randomUUID();
        UUID filterId2 = UUID.randomUUID();
        UUID filterId3 = UUID.randomUUID();

        LineFilter lineFilter = LineFilter.builder().equipmentID("NHV1_NHV2_1").substationName1("P1").substationName2("P2")
            .countries1(new TreeSet<>(Set.of("FR"))).countries2(new TreeSet<>(Set.of("FR")))
            .nominalVoltage1(new NumericalFilter(RangeType.RANGE, 360., 400.)).nominalVoltage2(new NumericalFilter(RangeType.RANGE, 356.25, 393.75)).build();
        Date date = new Date();
        CriteriaFilter lineCriteriaFilter = new CriteriaFilter(
                filterId2,
                date,
                lineFilter
        );
        insertFilter(filterId2, lineCriteriaFilter);
        checkFormFilter(filterId2, lineCriteriaFilter);

        LineFilter lineFilter2 = LineFilter.builder().equipmentID("NHV1_NHV2_1").substationName1("P1").substationName2("P2")
            .countries1(new TreeSet<>(Set.of("FR"))).countries2(new TreeSet<>(Set.of("FR")))
            .nominalVoltage1(new NumericalFilter(RangeType.RANGE, 360., 400.)).nominalVoltage2(new NumericalFilter(RangeType.RANGE, 356.25, 393.75)).build();

        CriteriaFilter lineCriteriaFilter2 = new CriteriaFilter(
                filterId3,
                date,
                lineFilter2
        );

        insertFilter(filterId3, lineCriteriaFilter2);
        checkFormFilter(filterId3, lineCriteriaFilter2);

        IdentifierListFilterEquipmentAttributes attribute1 = new IdentifierListFilterEquipmentAttributes("GEN", 1.0);
        IdentifierListFilterEquipmentAttributes attribute2 = new IdentifierListFilterEquipmentAttributes("wrongId", 2.0);
        IdentifierListFilterEquipmentAttributes attribute3 = new IdentifierListFilterEquipmentAttributes("wrongId2", 3.0);

        IdentifierListFilter identifierListFilter = new IdentifierListFilter(filterId,
                date,
                EquipmentType.GENERATOR,
                List.of(attribute1, attribute2, attribute3));
        insertFilter(filterId, identifierListFilter);
        checkIdentifierListFilter(filterId, identifierListFilter);

        List<String> values = Arrays.asList(filterId.toString(), filterId2.toString(), filterId3.toString());
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.addAll("ids", values);
        params.add("networkUuid", NETWORK_UUID.toString());
        params.add("variantId", VARIANT_ID_1);

        List<FilterEquipments> filterEquipments = objectMapper.readValue(
                mvc.perform(get("/" + FilterApi.API_VERSION + "/filters/export").params(params)
                                .contentType(APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsString(),
                new TypeReference<>() {
                });
        IdentifiableAttributes identifiableAttributes = new IdentifiableAttributes("GEN", IdentifiableType.GENERATOR, 1.0);
        IdentifiableAttributes identifiableAttributes2 = new IdentifiableAttributes("wrongId", IdentifiableType.GENERATOR, 2.0);
        IdentifiableAttributes identifiableAttributes3 = new IdentifiableAttributes("wrongId2", IdentifiableType.GENERATOR, 3.0);
        IdentifiableAttributes identifiableAttributes4 = new IdentifiableAttributes("NHV1_NHV2_1", IdentifiableType.LINE, null);

        FilterEquipments filterEquipment1 = FilterEquipments.builder()
                .filterId(filterId)
                .identifiableAttributes(List.of(identifiableAttributes))
                .notFoundEquipments(List.of("wrongId", "wrongId2"))
                .build();

        FilterEquipments filterEquipment2 = FilterEquipments.builder()
                .filterId(filterId2)
                .identifiableAttributes(List.of(identifiableAttributes4))
                .build();

        FilterEquipments filterEquipment3 = FilterEquipments.builder()
                .filterId(filterId3)
                .identifiableAttributes(List.of(identifiableAttributes4))
                .build();

        assertEquals(3, filterEquipments.size());
        List<FilterEquipments> expected = new ArrayList<>(List.of(filterEquipment1, filterEquipment2, filterEquipment3));
        checkFilterEquipments(expected, filterEquipments);

    }

    @Test
    public void testGetIdentifiablesCount() throws Exception {
        UUID filterId1 = UUID.fromString("c88c9510-15b4-468d-89c3-c1c6277966c3");
        UUID filterId2 = UUID.fromString("1110b06b-9b81-4d31-ac21-450628cd34ff");
        UUID filterId3 = UUID.fromString("f631034b-ba7c-4bb8-9d61-258a871e9265");
        UUID filterId4 = UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e");

        LineFilter lineFilter1 = LineFilter.builder().equipmentID("NHV1_NHV2_1").substationName1("P1").substationName2("P2")
                .countries1(new TreeSet<>(Set.of("FR"))).countries2(new TreeSet<>(Set.of("FR")))
                .nominalVoltage1(new NumericalFilter(RangeType.RANGE, 360., 400.)).nominalVoltage2(new NumericalFilter(RangeType.RANGE, 356.25, 393.75)).build();
        CriteriaFilter lineCriteriaFilter1 = new CriteriaFilter(
                filterId1,
                new Date(),
                lineFilter1
        );
        insertFilter(filterId1, lineCriteriaFilter1);
        checkFormFilter(filterId1, lineCriteriaFilter1);

        LineFilter lineFilter2 = LineFilter.builder().equipmentID("NHV1_NHV2_2").substationName1("P1").substationName2("P2")
                .countries1(new TreeSet<>(Set.of("FR"))).countries2(new TreeSet<>(Set.of("FR")))
                .nominalVoltage1(new NumericalFilter(RangeType.RANGE, 360., 400.)).nominalVoltage2(new NumericalFilter(RangeType.RANGE, 356.25, 393.75)).build();
        CriteriaFilter lineCriteriaFilter2 = new CriteriaFilter(
                filterId2,
                new Date(),
                lineFilter2
        );
        insertFilter(filterId2, lineCriteriaFilter2);
        checkFormFilter(filterId2, lineCriteriaFilter2);

        Date modificationDate = new Date();
        CriteriaFilter hvdcLineFilter = new CriteriaFilter(
                filterId3,
                modificationDate,
                HvdcLineFilter.builder().equipmentID("NHV1_NHV2_3").equipmentName("equipmentName_3")
                        .substationName1("substationName1").substationName2("substationName2")
                        .countries1(new TreeSet<>(Set.of("FR", "BE"))).countries2(new TreeSet<>(Set.of("FR", "IT")))
                        .freeProperties2(Map.of("region", List.of("north")))
                        .nominalVoltage(new NumericalFilter(RangeType.RANGE, 380., 420.))
                        .build()
        );
        insertFilter(filterId3, hvdcLineFilter);
        checkFormFilter(filterId3, hvdcLineFilter);

        CriteriaFilter substationFilter = new CriteriaFilter(
                filterId4,
                new Date(),
                new SubstationFilter("NHV1_NHV2_4", "equipmentName_4", new TreeSet<>(Set.of("FR", "BE")), null)
        );

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("networkUuid", NETWORK_UUID.toString());
        params.add("variantId", VARIANT_ID_1);
        final var json = TestUtils.resourceToString("/json/identifiables.json");
        Map<String, List<Integer>> filtersComplexityCount = objectMapper.readValue(
                mvc.perform(post("/" + FilterApi.API_VERSION + "/filters/identifiables-count")
                                .params(params)
                                .content(json)
                                .contentType(APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsString(),
                new TypeReference<>() {
                });
        assertEquals(1, filtersComplexityCount.get("0").size());
        assertEquals(1, filtersComplexityCount.get("1").size());
        assertEquals(1, filtersComplexityCount.get("2").size());
        assertEquals(0, filtersComplexityCount.get("3").size());

        assertEquals(4, filtersComplexityCount.size());
    }

    private void checkFilterEquipments(List<FilterEquipments> filterEquipments1, List<FilterEquipments> filterEquipments2) {
        assertEquals(CollectionUtils.isEmpty(filterEquipments1), CollectionUtils.isEmpty(filterEquipments2));
        assertEquals(filterEquipments1.size(), filterEquipments2.size());

        filterEquipments1.sort(Comparator.comparing(filterEquipments -> filterEquipments.getFilterId().toString()));
        filterEquipments2.sort(Comparator.comparing(filterEquipments -> filterEquipments.getFilterId().toString()));

        for (int index = 0; index < filterEquipments1.size(); index++) {
            FilterEquipments filterEquipment1 = filterEquipments1.get(index);
            FilterEquipments filterEquipment2 = filterEquipments2.get(index);
            assertEquals(filterEquipment1.getFilterId(), filterEquipment2.getFilterId());
            assertEquals(CollectionUtils.isEmpty(filterEquipment1.getNotFoundEquipments()), CollectionUtils.isEmpty(filterEquipment2.getNotFoundEquipments()));
            if (filterEquipment1.getNotFoundEquipments() != null) {
                assertTrue(filterEquipment1.getNotFoundEquipments().containsAll(filterEquipment2.getNotFoundEquipments()));
            }
            checkIdentifiableAttributes(new ArrayList<>(filterEquipment1.getIdentifiableAttributes()), new ArrayList<>(filterEquipment2.getIdentifiableAttributes()));
        }
    }

    private void checkIdentifiableAttributes(List<IdentifiableAttributes> identifiableAttributes1, List<IdentifiableAttributes> identifiableAttributes2) {
        assertEquals(CollectionUtils.isEmpty(identifiableAttributes1), CollectionUtils.isEmpty(identifiableAttributes2));
        assertEquals(identifiableAttributes1.size(), identifiableAttributes2.size());

        identifiableAttributes1.sort(Comparator.comparing(IdentifiableAttributes::getId));
        identifiableAttributes2.sort(Comparator.comparing(IdentifiableAttributes::getId));

        for (int index = 0; index < identifiableAttributes1.size(); index++) {
            IdentifiableAttributes identifiableAttribute1 = identifiableAttributes1.get(index);
            IdentifiableAttributes identifiableAttribute2 = identifiableAttributes2.get(index);
            assertEquals(identifiableAttribute1.getId(), identifiableAttribute2.getId());
            assertEquals(identifiableAttribute1.getType(), identifiableAttribute2.getType());
            assertEquals(identifiableAttribute1.getDistributionKey(), identifiableAttribute2.getDistributionKey());
        }
    }

    private void checkIdentifierListFilterExportAndMetadata(UUID filterId, String expectedJson, EquipmentType equipmentType) throws Exception {
        mvc.perform(get(URL_TEMPLATE + "/" + filterId + "/export?networkUuid=" + NETWORK_UUID)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(content().json(expectedJson));

        List<FilterAttributes> filterAttributes = objectMapper.readValue(
            mvc.perform(get("/" + FilterApi.API_VERSION + "/filters/metadata?ids={id}", filterId)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(),
            new TypeReference<>() {
            });

        assertEquals(1, filterAttributes.size());
        assertEquals(filterId, filterAttributes.get(0).getId());
        assertEquals(FilterType.IDENTIFIER_LIST, filterAttributes.get(0).getType());
        assertEquals(equipmentType, filterAttributes.get(0).getEquipmentType());
    }

    private void checkExpertFilterExportAndMetadata(UUID filterId, String expectedJson, EquipmentType equipmentType) throws Exception {
        mvc.perform(get(URL_TEMPLATE + "/" + filterId + "/export?networkUuid=" + NETWORK_UUID)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(content().json(expectedJson));

        List<FilterAttributes> filterAttributes = objectMapper.readValue(
                mvc.perform(get("/" + FilterApi.API_VERSION + "/filters/metadata?ids={id}", filterId)
                                .contentType(APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsString(),
                new TypeReference<>() {
                });

        assertEquals(1, filterAttributes.size());
        assertEquals(filterId, filterAttributes.get(0).getId());
        assertEquals(FilterType.EXPERT, filterAttributes.get(0).getType());
        assertEquals(equipmentType, filterAttributes.get(0).getEquipmentType());

        mvc.perform(delete(URL_TEMPLATE + "/" + filterId)).andExpect(status().isOk());
    }

    private void checkFilterEvaluating(AbstractFilter filter, String expectedJson) throws Exception {
        mvc.perform(post(URL_TEMPLATE + "/evaluate?networkUuid=" + NETWORK_UUID)
                        .content(objectMapper.writeValueAsString(filter))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(content().json(expectedJson));
    }

    private AbstractFilter insertFilter(UUID filterId, AbstractFilter filter) throws Exception {
        String response = mvc.perform(post(URL_TEMPLATE).param("id", filterId.toString())
                        .content(objectMapper.writeValueAsString(filter))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return objectMapper.readValue(response, AbstractFilter.class);
    }

    private void modifyFormFilter(UUID filterId, AbstractFilter newFilter, String userId) throws Exception {
        mvc.perform(put(URL_TEMPLATE + "/" + filterId)
            .content(objectMapper.writeValueAsString(newFilter))
            .contentType(APPLICATION_JSON)
            .header(USER_ID_HEADER, userId))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        checkElementUpdatedMessageSent(filterId, userId);

        String modifiedFilterAsString = mvc.perform(get(URL_TEMPLATE + "/" + filterId)).andReturn().getResponse().getContentAsString();
        CriteriaFilter modifiedFilter = objectMapper.readValue(modifiedFilterAsString, CriteriaFilter.class);
        checkFormFilter(filterId, modifiedFilter);

        mvc.perform(get(URL_TEMPLATE))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        MvcResult mockResponse = mvc.perform(get(URL_TEMPLATE + "/" + filterId)).andExpect(status().isOk()).andReturn();
        modifiedFilter = objectMapper.readValue(mockResponse.getResponse().getContentAsString(), CriteriaFilter.class);
        checkFormFilter(filterId, modifiedFilter);
    }

    private void insertInjectionFilter(EquipmentType equipmentType, UUID id, String equipmentID, String equipmentName,
                                       String substationName, Set<String> countries,
                                       RangeType rangeType, Double value1, Double value2, EnergySource energySource,
                                       UUID networkUuid, String variantId, String expectedJsonExport) throws Exception {
        NumericalFilter numericalFilter = rangeType != null ? new NumericalFilter(rangeType, value1, value2) : null;
        AbstractInjectionFilter abstractInjectionFilter;
        Date modificationDate = new Date();
        SortedSet<String> sortedCountries = AbstractFilterRepositoryProxy.setToSorterSet(countries);
        // compensators are on powsybl networks without substation, so filtering on substation free props would prevent match.
        OrderedMap<String, List<String>> workAroundProps =
            Set.of(EquipmentType.SHUNT_COMPENSATOR, EquipmentType.STATIC_VAR_COMPENSATOR).contains(equipmentType) ? null : FREE_PROPS;
        InjectionFilterAttributes injectionFilterAttributes = new InjectionFilterAttributes(equipmentID, equipmentName, substationName,
            sortedCountries, workAroundProps, numericalFilter);
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
                abstractInjectionFilter = new GeneratorFilter(injectionFilterAttributes.getEquipmentID(),
                        injectionFilterAttributes.getEquipmentName(),
                        injectionFilterAttributes.getSubstationName(),
                        injectionFilterAttributes.getCountries(),
                        injectionFilterAttributes.getFreeProperties(),
                        injectionFilterAttributes.getNominalVoltage(),
                        energySource);
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
        CriteriaFilter injectionFilter = new CriteriaFilter(
                id,

                modificationDate,
                abstractInjectionFilter
        );

        insertFilter(id, injectionFilter);
        AbstractInjectionFilter injectionEquipment = (AbstractInjectionFilter) injectionFilter.getEquipmentFilterForm();
        injectionEquipment.setCountries(AbstractFilterRepositoryProxy.setToSorterSet(countries));
        injectionEquipment.setFreeProperties(workAroundProps);
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
        assertEquals(FilterType.CRITERIA, filterAttributes.get(0).getType());
        assertEquals(equipmentType, filterAttributes.get(0).getEquipmentType());

        mvc.perform(get(URL_TEMPLATE + "/" + id + "/export?networkUuid=" + networkUuid + (variantId != null ? "&variantId=" + variantId : ""))
            .contentType(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
            .andExpect(content().json(expectedJsonExport));

        mvc.perform(delete(URL_TEMPLATE + "/" + id)).andExpect(status().isOk());
    }

    private void insertTransformerFilter(EquipmentType equipmentType, UUID id, String equipmentID, String equipmentName,
                                         String substationName, Set<String> countries,
                                         List<RangeType> rangeTypes, List<Double> values1, List<Double> values2,
                                         UUID networkUuid, String variantId, String expectedJsonExport) throws Exception {
        NumericalFilter numericalFilter1 = new NumericalFilter(rangeTypes.get(0), values1.get(0), values2.get(0));
        NumericalFilter numericalFilter2 = new NumericalFilter(rangeTypes.get(1), values1.get(1), values2.get(1));
        AbstractEquipmentFilterForm equipmentFilterForm;
        if (equipmentType == EquipmentType.TWO_WINDINGS_TRANSFORMER) {
            equipmentFilterForm = TwoWindingsTransformerFilter.builder().equipmentID(equipmentID).equipmentName(equipmentName).substationName(substationName)
                .countries(AbstractFilterRepositoryProxy.setToSorterSet(countries))
                .nominalVoltage1(numericalFilter1)
                .nominalVoltage2(numericalFilter2)
                .build();
        } else if (equipmentType == EquipmentType.THREE_WINDINGS_TRANSFORMER) {
            NumericalFilter numericalFilter3 = new NumericalFilter(rangeTypes.get(2), values1.get(2), values2.get(2));
            equipmentFilterForm = ThreeWindingsTransformerFilter.builder().equipmentID(equipmentID).equipmentName(equipmentName).substationName(substationName)
                .countries(AbstractFilterRepositoryProxy.setToSorterSet(countries))
                .nominalVoltage1(numericalFilter1)
                .nominalVoltage2(numericalFilter2)
                .nominalVoltage3(numericalFilter3)
                .build();
        } else {
            throw new PowsyblException(WRONG_FILTER_TYPE);
        }
        Date modificationDate = new Date();

        CriteriaFilter transformerFilter = new CriteriaFilter(
                id,

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
        assertEquals(FilterType.CRITERIA, filterAttributes.get(0).getType());
        assertEquals(equipmentType, filterAttributes.get(0).getEquipmentType());

        mvc.perform(get(URL_TEMPLATE + "/" + id + "/export?networkUuid=" + networkUuid + (variantId != null ? "&variantId=" + variantId : ""))
            .contentType(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
            .andExpect(content().json(expectedJsonExport));

        mvc.perform(delete(URL_TEMPLATE + "/" + id)).andExpect(status().isOk());
    }

    private void insertHvdcLineFilter(UUID id, String equipmentID, String equipmentName,
                                      String substationName1, String substationName2, SortedSet<String> countries1,
                                      SortedSet<String> countries2, RangeType rangeType, Double value1, Double value2,
                                      UUID networkUuid, String variantId, String expectedJsonExport) throws Exception {
        Date modificationDate = new Date();
        CriteriaFilter hvdcLineFilter = new CriteriaFilter(
                id,
                modificationDate,
                HvdcLineFilter.builder().equipmentID(equipmentID).equipmentName(equipmentName)
                    .substationName1(substationName1).substationName2(substationName2)
                    .countries1(countries1).countries2(countries2)
                    .freeProperties2(Map.of("region", List.of("north")))
                    .nominalVoltage(new NumericalFilter(rangeType, value1, value2))
                    .build()
        );

        insertFilter(id, hvdcLineFilter);
        checkFormFilter(id, hvdcLineFilter);

        String filtersAsString = mvc.perform(get(URL_TEMPLATE))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<FilterAttributes> filterAttributes = objectMapper.readValue(filtersAsString,
                new TypeReference<>() {
                });
        assertEquals(1, filterAttributes.size());
        matchFilterInfos(filterAttributes.get(0), id, FilterType.CRITERIA, EquipmentType.HVDC_LINE, modificationDate);

        filterAttributes = objectMapper.readValue(
            mvc.perform(get("/" + FilterApi.API_VERSION + "/filters/metadata?ids={id}", id)
                    .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(),
            new TypeReference<>() {
            });

        assertEquals(1, filterAttributes.size());
        matchFilterInfos(filterAttributes.get(0), id, FilterType.CRITERIA, EquipmentType.HVDC_LINE, modificationDate);

        mvc.perform(get(URL_TEMPLATE + "/" + id + "/export?networkUuid=" + networkUuid + (variantId != null ? "&variantId=" + variantId : ""))
            .contentType(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
            .andExpect(content().json(expectedJsonExport));

        mvc.perform(delete(URL_TEMPLATE + "/" + id)).andExpect(status().isOk());

        filterAttributes = objectMapper.readValue(
            mvc.perform(get("/" + FilterApi.API_VERSION + "/filters/metadata?ids={id}", id)
                    .contentType(APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsString(),
                new TypeReference<>() {
                });

        assertEquals(0, filterAttributes.size());
    }

    private CriteriaFilter insertLineFilter(UUID id, String equipmentID, String equipmentName,
                                            String substationName, Set<String> countries1, Set<String> countries2,
                                            List<RangeType> rangeTypes, List<Double> values1, List<Double> values2,
                                            UUID networkUuid, String variantId, String expectedJsonExport, boolean delete) throws Exception {
        NumericalFilter numericalFilter1 = null;
        if (rangeTypes.size() >= 1) {
            numericalFilter1 = new NumericalFilter(rangeTypes.get(0), values1.get(0), values2.get(0));
        }
        NumericalFilter numericalFilter2 = null;
        if (rangeTypes.size() == 2) {
            numericalFilter2 = new NumericalFilter(rangeTypes.get(1), values1.get(1), values2.get(1));
        }
        AbstractEquipmentFilterForm equipmentFilterForm = LineFilter.builder().equipmentID(equipmentID).equipmentName(equipmentName)
            .substationName1(substationName)
            .countries1(AbstractFilterRepositoryProxy.setToSorterSet(countries1))
            .countries2(AbstractFilterRepositoryProxy.setToSorterSet(countries2))
            .nominalVoltage1(numericalFilter1)
            .nominalVoltage2(numericalFilter2)
            .build();
        Date modificationDate = new Date();
        CriteriaFilter filter = new CriteriaFilter(
                id,

                modificationDate,
                equipmentFilterForm
        );
        insertFilter(id, filter);
        checkFormFilter(id, filter);
        List<FilterAttributes> filterAttributes = objectMapper.readValue(
                mvc.perform(get("/" + FilterApi.API_VERSION + "/filters/metadata?ids={id}", id)
                                .contentType(APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsString(),
                new TypeReference<>() {
                });

        assertEquals(1, filterAttributes.size());
        assertEquals(id, filterAttributes.get(0).getId());
        assertEquals(FilterType.CRITERIA, filterAttributes.get(0).getType());
        assertEquals(EquipmentType.LINE, filterAttributes.get(0).getEquipmentType());

        mvc.perform(get(URL_TEMPLATE + "/" + id + "/export?networkUuid=" + networkUuid + (variantId != null ? "&variantId=" + variantId : ""))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(content().json(expectedJsonExport));
        if (delete) {
            mvc.perform(delete(URL_TEMPLATE + "/" + id)).andExpect(status().isOk());
        }
        return filter;
    }

    private void insertVoltageLevelFilter(UUID id, String equipmentID, String equipmentName, Set<String> countries,
                                          RangeType rangeType, Double value1, Double value2,
                                          UUID networkUuid, String variantId, String expectedJsonExport) throws Exception {
        NumericalFilter numericalFilter = rangeType != null ? new NumericalFilter(rangeType, value1, value2) : null;
        SortedSet<String> sortedCountries = AbstractFilterRepositoryProxy.setToSorterSet(countries);
        VoltageLevelFilter voltageLevelFilter = new VoltageLevelFilter(equipmentID, equipmentName,
            sortedCountries, null, numericalFilter);
        Date modificationDate = new Date();

        CriteriaFilter filter = new CriteriaFilter(
            id,

            modificationDate,
            voltageLevelFilter
        );

        insertFilter(id, filter);
        checkFormFilter(id, filter);

        List<FilterAttributes> filterAttributes = objectMapper.readValue(
            mvc.perform(get("/" + FilterApi.API_VERSION + "/filters/metadata?ids={id}", id)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(),
            new TypeReference<>() {
            });

        assertEquals(1, filterAttributes.size());
        assertEquals(id, filterAttributes.get(0).getId());
        assertEquals(FilterType.CRITERIA, filterAttributes.get(0).getType());

        mvc.perform(get(URL_TEMPLATE + "/" + id + "/export?networkUuid=" + networkUuid + (variantId != null ? "&variantId=" + variantId : ""))
            .contentType(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
            .andExpect(content().json(expectedJsonExport));

        mvc.perform(delete(URL_TEMPLATE + "/" + id)).andExpect(status().isOk());
    }

    private void insertSubstationFilter(UUID id, String equipmentID, String equipmentName, Set<String> countries,
                                        UUID networkUuid, String variantId, String expectedJsonExport) throws Exception {
        SortedSet<String> sortedCountries = AbstractFilterRepositoryProxy.setToSorterSet(countries);
        SubstationFilter substationFilter = new SubstationFilter(equipmentID, equipmentName, sortedCountries, null);
        Date modificationDate = new Date();

        CriteriaFilter filter = new CriteriaFilter(
            id,

            modificationDate,
            substationFilter
        );

        insertFilter(id, filter);
        checkFormFilter(id, filter);

        List<FilterAttributes> filterAttributes = objectMapper.readValue(
            mvc.perform(get("/" + FilterApi.API_VERSION + "/filters/metadata?ids={id}", id)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(),
            new TypeReference<>() {
            });

        assertEquals(1, filterAttributes.size());
        assertEquals(id, filterAttributes.get(0).getId());
        assertEquals(FilterType.CRITERIA, filterAttributes.get(0).getType());

        mvc.perform(get(URL_TEMPLATE + "/" + id + "/export?networkUuid=" + networkUuid + (variantId != null ? "&variantId=" + variantId : ""))
            .contentType(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
            .andExpect(content().json(expectedJsonExport));

        mvc.perform(delete(URL_TEMPLATE + "/" + id)).andExpect(status().isOk());
    }

    private void checkFormFilter(UUID filterId, CriteriaFilter criteriaFilter) throws Exception {
        String foundFilterAsString = mvc.perform(get(URL_TEMPLATE + "/" + filterId)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        CriteriaFilter foundFilter = objectMapper.readValue(foundFilterAsString, CriteriaFilter.class);
        matchFormFilterInfos(foundFilter, criteriaFilter);
    }

    private void checkScriptFilter(UUID filterId, ScriptFilter scriptFilter) throws Exception {
        String foundFilterAsString = mvc.perform(get(URL_TEMPLATE + "/" + filterId)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        ScriptFilter foundFilter = objectMapper.readValue(foundFilterAsString, ScriptFilter.class);
        matchScriptFilterInfos(foundFilter, scriptFilter);
    }

    private void checkIdentifierListFilter(UUID filterId, IdentifierListFilter identifierListFilter) throws Exception {
        String foundFilterAsString = mvc.perform(get(URL_TEMPLATE + "/" + filterId)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        IdentifierListFilter foundFilter = objectMapper.readValue(foundFilterAsString, IdentifierListFilter.class);
        matchIdentifierListFilterInfos(foundFilter, identifierListFilter);
    }

    private void checkExpertFilter(UUID filterId, ExpertFilter expertFilter) throws Exception {
        String foundFilterAsString = mvc.perform(get(URL_TEMPLATE + "/" + filterId)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        ExpertFilter foundFilter = objectMapper.readValue(foundFilterAsString, ExpertFilter.class);
        matchExpertFilterInfos(foundFilter, expertFilter);
    }

    private void matchFilterInfos(IFilterAttributes filter1, IFilterAttributes filter2) {
        assertEquals(filter1.getId(), filter2.getId());
        assertEquals(filter1.getType(), filter2.getType());
        assertTrue((filter2.getModificationDate().getTime() - filter1.getModificationDate().getTime()) < 2000);
        assertEquals(filter1.getEquipmentType(), filter2.getEquipmentType());
    }

    private void matchFilterInfos(IFilterAttributes filterAttribute, UUID id, FilterType type, EquipmentType equipmentType, Date modificationDate) {
        assertEquals(filterAttribute.getId(), id);
        assertEquals(filterAttribute.getType(), type);
        assertTrue((modificationDate.getTime() - filterAttribute.getModificationDate().getTime()) < 2000);
        assertEquals(filterAttribute.getEquipmentType(), equipmentType);
    }

    private void matchFormFilterInfos(CriteriaFilter criteriaFilter1, CriteriaFilter criteriaFilter2) {
        matchFilterInfos(criteriaFilter1, criteriaFilter2);
        matchEquipmentFormFilter(criteriaFilter1.getEquipmentFilterForm(), criteriaFilter2.getEquipmentFilterForm());
    }

    private void matchEquipmentFormFilter(AbstractEquipmentFilterForm equipmentFilterForm1, AbstractEquipmentFilterForm equipmentFilterForm2) {
        assertThat(equipmentFilterForm1, new FieldsMatcher<>(equipmentFilterForm2));
    }

    private void matchScriptFilterInfos(ScriptFilter scriptFilter1, ScriptFilter scriptFilter2) {
        matchFilterInfos(scriptFilter1, scriptFilter2);
        assertTrue(scriptFilter1.getScript().contains(scriptFilter2.getScript()));
    }

    private void matchIdentifierListFilterInfos(IdentifierListFilter identifierListFilter1, IdentifierListFilter identifierListFilter2) {
        matchFilterInfos(identifierListFilter1, identifierListFilter2);
        assertTrue(new MatcherJson<>(objectMapper, identifierListFilter2.getFilterEquipmentsAttributes()).matchesSafely(identifierListFilter1.getFilterEquipmentsAttributes()));
    }

    private void matchExpertFilterInfos(ExpertFilter expertFilter1, ExpertFilter expertFilter2) {
        matchFilterInfos(expertFilter1, expertFilter2);
        assertTrue(new MatcherJson<>(objectMapper, expertFilter2.getRules()).matchesSafely(expertFilter1.getRules()));
    }

    private void checkElementUpdatedMessageSent(UUID elementUuid, String userId) {
        Message<byte[]> message = output.receive(TIMEOUT, elementUpdateDestination);
        assertEquals(elementUuid, message.getHeaders().get(NotificationService.HEADER_ELEMENT_UUID));
        assertEquals(userId, message.getHeaders().get(NotificationService.HEADER_MODIFIED_BY));
    }

    @Test
    public void testExpertGeneratorFilter() throws Exception {
        UUID filterId = UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e");
        Date modificationDate = new Date();

        // Create OR rules for generators
        List<AbstractExpertRule> orRules = new ArrayList<>();
        NumberExpertRule numRule1 = NumberExpertRule.builder().value(20.0)
                .field(FieldType.NOMINAL_VOLTAGE).operator(OperatorType.GREATER).build();
        orRules.add(numRule1);
        NumberExpertRule numRule2 = NumberExpertRule.builder().value(-9000.0)
                .field(FieldType.MIN_P).operator(OperatorType.EQUALS).build(); // false
        orRules.add(numRule2);
        CombinatorExpertRule orCombination = CombinatorExpertRule.builder().combinator(CombinatorType.OR).rules(orRules).build();
        // Create AND rules for generators
        List<AbstractExpertRule> andRules = new ArrayList<>();
        andRules.add(orCombination);
        NumberExpertRule numRule3 = NumberExpertRule.builder().value(9999.99)
                .field(FieldType.MAX_P).operator(OperatorType.GREATER_OR_EQUALS).build();
        andRules.add(numRule3);
        NumberExpertRule numRule4 = NumberExpertRule.builder().value(24.5)
                .field(FieldType.TARGET_V).operator(OperatorType.EQUALS).build();
        andRules.add(numRule4);
        NumberExpertRule numRule5 = NumberExpertRule.builder().value(400.0)
                .field(FieldType.TARGET_Q).operator(OperatorType.LOWER_OR_EQUALS).build();
        andRules.add(numRule5);
        NumberExpertRule numRule6 = NumberExpertRule.builder().value(500.0)
                .field(FieldType.TARGET_P).operator(OperatorType.GREATER).build();
        andRules.add(numRule6);
        EnumExpertRule enumRule1 = EnumExpertRule.builder().value("OTHER")
                .field(FieldType.ENERGY_SOURCE).operator(OperatorType.EQUALS).build();
        andRules.add(enumRule1);
        EnumExpertRule enumRule2 = EnumExpertRule.builder().value("ES")
                .field(FieldType.COUNTRY).operator(OperatorType.NOT_EQUALS).build();
        andRules.add(enumRule2);
        StringExpertRule stringRule1 = StringExpertRule.builder().value("N")
                .field(FieldType.ID).operator(OperatorType.ENDS_WITH).build();
        andRules.add(stringRule1);
        StringExpertRule stringRule2 = StringExpertRule.builder().value("E")
                .field(FieldType.NAME).operator(OperatorType.CONTAINS).build();
        andRules.add(stringRule2);
        BooleanExpertRule booleanRule1 = BooleanExpertRule.builder().value(false)
                .field(FieldType.VOLTAGE_REGULATOR_ON).operator(OperatorType.NOT_EQUALS).build();
        andRules.add(booleanRule1);

        CombinatorExpertRule andCombination = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(andRules).build();

        ExpertFilter expertFilter = new ExpertFilter(filterId, modificationDate, EquipmentType.GENERATOR, andCombination);
        insertFilter(filterId, expertFilter);
        checkExpertFilter(filterId, expertFilter);

        // check result when evaluating a filter on a network
        String expectedResultJson = """
                [{"id":"GEN","type":"GENERATOR"}]
            """;
        checkExpertFilterExportAndMetadata(filterId, expectedResultJson, EquipmentType.GENERATOR);
        checkFilterEvaluating(expertFilter, expectedResultJson);
    }

    @Test
    public void testExpertLoadFilter() throws Exception {
        UUID filterId = UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e");
        Date modificationDate = new Date();

        // Create rules for loads
        List<AbstractExpertRule> rules = new ArrayList<>();
        StringExpertRule stringRule1 = StringExpertRule.builder().value("LOAD")
                .field(FieldType.ID).operator(OperatorType.IS).build();
        rules.add(stringRule1);

        CombinatorExpertRule gen1 = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(rules).build();

        ExpertFilter expertFilter = new ExpertFilter(filterId, modificationDate, EquipmentType.LOAD, gen1);
        insertFilter(filterId, expertFilter);
        checkExpertFilter(filterId, expertFilter);

        // check result when evaluating a filter on a network
        String expectedResultJson = """
                [{"id":"LOAD","type":"LOAD"}]
            """;
        checkExpertFilterExportAndMetadata(filterId, expectedResultJson, EquipmentType.LOAD);
        checkFilterEvaluating(expertFilter, expectedResultJson);
    }

    @Test
    public void testExpertFilterGeneratorWithInAndNotInOperator() throws Exception {
        UUID filterId = UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e");

        // Build a filter AND with only an IN operator for VOLTAGE_LEVEL_ID
        StringExpertRule stringInRule = StringExpertRule.builder().values(new HashSet<>(Arrays.asList("VLGEN", "VLGEN2")))
                .field(FieldType.VOLTAGE_LEVEL_ID).operator(OperatorType.IN).build();
        CombinatorExpertRule inFilter = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(Arrays.asList(stringInRule)).build();

        ExpertFilter expertFilter = new ExpertFilter(filterId, new Date(), EquipmentType.GENERATOR, inFilter);
        insertFilter(filterId, expertFilter);
        checkExpertFilter(filterId, expertFilter);

        // check result when evaluating a filter on a network
        String expectedResultJson = """
                [
                    {"id":"GEN","type":"GENERATOR"},
                    {"id":"GEN2","type":"GENERATOR"}
                ]
            """;
        checkExpertFilterExportAndMetadata(filterId, expectedResultJson, EquipmentType.GENERATOR);

        // Build a filter AND with only a NOT_IN operator for VOLTAGE_LEVEL_ID
        stringInRule = StringExpertRule.builder().values(new HashSet<>(Arrays.asList("VLGEN2")))
                .field(FieldType.VOLTAGE_LEVEL_ID).operator(OperatorType.NOT_IN).build();
        inFilter = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(Arrays.asList(stringInRule)).build();

        expertFilter = new ExpertFilter(filterId, new Date(), EquipmentType.GENERATOR, inFilter);
        insertFilter(filterId, expertFilter);
        checkExpertFilter(filterId, expertFilter);

        // check result when evaluating a filter on a network
        checkExpertFilterExportAndMetadata(filterId, expectedResultJson, EquipmentType.GENERATOR);

        // Build a filter AND with only an IN operator for NOMINAL_VOLTAGE
        NumberExpertRule numberInRule = NumberExpertRule.builder().values(Arrays.asList(24.0))
                .field(FieldType.NOMINAL_VOLTAGE).operator(OperatorType.IN).build();
        inFilter = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(Arrays.asList(numberInRule)).build();

        expertFilter = new ExpertFilter(filterId, new Date(), EquipmentType.GENERATOR, inFilter);
        insertFilter(filterId, expertFilter);
        checkExpertFilter(filterId, expertFilter);

        // check result when evaluating a filter on a network
        checkExpertFilterExportAndMetadata(filterId, expectedResultJson, EquipmentType.GENERATOR);

        // Build a filter AND with only a NOT_IN operator for NOMINAL_VOLTAGE
        numberInRule = NumberExpertRule.builder().values(Arrays.asList(12.0))
                .field(FieldType.NOMINAL_VOLTAGE).operator(OperatorType.NOT_IN).build();
        inFilter = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(Arrays.asList(numberInRule)).build();

        expertFilter = new ExpertFilter(filterId, new Date(), EquipmentType.GENERATOR, inFilter);
        insertFilter(filterId, expertFilter);
        checkExpertFilter(filterId, expertFilter);

        // check result when evaluating a filter on a network
        checkExpertFilterExportAndMetadata(filterId, expectedResultJson, EquipmentType.GENERATOR);

        // Build a filter AND with only an IN operator for COUNTRY
        EnumExpertRule enumInRule = EnumExpertRule.builder().values(new HashSet<>(Arrays.asList(Country.FR.name(), Country.GB.name())))
                .field(FieldType.COUNTRY).operator(OperatorType.IN).build();
        inFilter = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(Arrays.asList(enumInRule)).build();

        expertFilter = new ExpertFilter(filterId, new Date(), EquipmentType.GENERATOR, inFilter);
        insertFilter(filterId, expertFilter);
        checkExpertFilter(filterId, expertFilter);

        // check result when evaluating a filter on a network
        checkExpertFilterExportAndMetadata(filterId, expectedResultJson, EquipmentType.GENERATOR);

        // Build a filter AND with only a NOT_IN operator for COUNTRY
        enumInRule = EnumExpertRule.builder().values(new HashSet<>(Arrays.asList(Country.GB.name())))
                .field(FieldType.COUNTRY).operator(OperatorType.NOT_IN).build();
        inFilter = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(Arrays.asList(enumInRule)).build();

        expertFilter = new ExpertFilter(filterId, new Date(), EquipmentType.GENERATOR, inFilter);
        insertFilter(filterId, expertFilter);
        checkExpertFilter(filterId, expertFilter);

        // check result when evaluating a filter on a network
        checkExpertFilterExportAndMetadata(filterId, expectedResultJson, EquipmentType.GENERATOR);
    }

    @Test
    public void lineFilterIsEmpty() {
        HvdcLineFilter hvdcFilter = new HvdcLineFilter(
                null,
                null,
                null,
                null,
                new TreeSet<>(),
                new TreeSet<>(),
                new NumericalFilter(RangeType.RANGE, 50., null)
        );
        assertFalse(hvdcFilter.isEmpty());
    }

    @Test
    public void transformerFilterIsEmpty() {
        TwoWindingsTransformerFilter transformerFilter =
                TwoWindingsTransformerFilter.builder()
                        .equipmentID(null)
                        .equipmentName(null)
                        .substationName(null)
                        .countries(new TreeSet<>())
                        .freeProperties(Map.of("region", List.of("north")))
                        .nominalVoltage1(NumericalFilter.builder().type(RangeType.RANGE).value1(370.).value2(390.).build())
                        .nominalVoltage2(NumericalFilter.builder().type(RangeType.EQUALITY).value1(225.).build())
                        .build();

        assertFalse(transformerFilter.isEmpty());
    }
}
