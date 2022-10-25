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
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VariantManagerConstants;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.HvdcTestNetwork;
import com.powsybl.iidm.network.test.ShuntTestCaseFactory;
import com.powsybl.iidm.network.test.SvcTestCaseFactory;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;
import com.powsybl.network.store.client.NetworkStoreService;
import com.powsybl.network.store.client.PreloadingStrategy;
import com.powsybl.network.store.iidm.impl.NetworkFactoryImpl;
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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.util.NestedServletException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.join;
import static org.gridsuite.filter.server.AbstractFilterRepositoryProxy.WRONG_FILTER_TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

    private Network network;
    private Network network2;
    private Network network6;

    @Autowired
    private FilterService filterService;

    @MockBean
    private NetworkStoreService networkStoreService;

    @Autowired
    ObjectMapper objectMapper = new ObjectMapper();

    public static final SortedSet COUNTRIES1 = new TreeSet(Collections.singleton("France"));
    public static final SortedSet COUNTRIES2 = new TreeSet(Collections.singleton("Germany"));

    private static final UUID NETWORK_UUID = UUID.fromString("7928181c-7977-4592-ba19-88027e4254e4");
    private static final UUID NETWORK_UUID_2 = UUID.fromString("7928181c-7977-4592-ba19-88027e4254e5");
    private static final UUID NETWORK_UUID_3 = UUID.fromString("7928181c-7977-4592-ba19-88027e4254e6");
    private static final UUID NETWORK_UUID_4 = UUID.fromString("7928181c-7977-4592-ba19-88027e4254e7");
    private static final UUID NETWORK_UUID_5 = UUID.fromString("11111111-7977-4592-2222-88027e4254e7");
    private static final UUID NETWORK_UUID_6 = UUID.fromString("11111111-7977-4592-2222-88027e4254e8");
    private static final UUID NETWORK_NOT_FOUND_UUID = UUID.fromString("88888888-7977-3333-9999-88027e4254e7");
    private static final String VARIANT_ID_1 = "variant_1";

    @Before
    public void setUp() {
        Configuration.defaultConfiguration();
        MockitoAnnotations.initMocks(this);
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(DeserializationFeature.USE_LONG_FOR_INTS);
        objectMapper.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
        objectMapper.disable(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE);

        network = EurostagTutorialExample1Factory.createWithMoreGenerators(new NetworkFactoryImpl());
        network.getVariantManager().cloneVariant(VariantManagerConstants.INITIAL_VARIANT_ID, VARIANT_ID_1);
        network.getVariantManager().setWorkingVariant(VARIANT_ID_1);
        // remove generator 'GEN2' from network in variant VARIANT_ID_1
        network.getGenerator("GEN2").remove();
        network.getVariantManager().setWorkingVariant(VariantManagerConstants.INITIAL_VARIANT_ID);

        network2 = HvdcTestNetwork.createVsc(new NetworkFactoryImpl());
        Network network3 = SvcTestCaseFactory.createWithMoreSVCs(new NetworkFactoryImpl());
        Network network4 = ShuntTestCaseFactory.create(new NetworkFactoryImpl());
        Network network5 = ThreeWindingsTransformerNetworkFactory.create(new NetworkFactoryImpl());
        network6 = EurostagTutorialExample1Factory.createWithFixedCurrentLimits(new NetworkFactoryImpl());
        given(networkStoreService.getNetwork(NETWORK_UUID, PreloadingStrategy.COLLECTION)).willReturn(network);
        given(networkStoreService.getNetwork(NETWORK_UUID_2, PreloadingStrategy.COLLECTION)).willReturn(network2);
        given(networkStoreService.getNetwork(NETWORK_UUID_3, PreloadingStrategy.COLLECTION)).willReturn(network3);
        given(networkStoreService.getNetwork(NETWORK_UUID_4, PreloadingStrategy.COLLECTION)).willReturn(network4);
        given(networkStoreService.getNetwork(NETWORK_UUID_5, PreloadingStrategy.COLLECTION)).willReturn(network5);
        given(networkStoreService.getNetwork(NETWORK_UUID_6, PreloadingStrategy.COLLECTION)).willReturn(network6);
        given(networkStoreService.getNetwork(NETWORK_NOT_FOUND_UUID, PreloadingStrategy.COLLECTION)).willReturn(null);

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

        LineFilter lineFilter = new LineFilter("NHV1_NHV2_1", null, "P1", "P2", new TreeSet<>(Set.of("FR")), new TreeSet<>(Set.of("FR")), new NumericalFilter(RangeType.RANGE, 360., 400.), new NumericalFilter(RangeType.RANGE, 356.25, 393.75));
        AutomaticFilter lineAutomaticFilter = new AutomaticFilter(
                filterId1,
                creationDate,
                modificationDate,
                lineFilter
        );
        insertFilter(filterId1, lineAutomaticFilter);
        checkFormFilter(filterId1, lineAutomaticFilter);

        // export
        assertThrows("Network '" + NETWORK_NOT_FOUND_UUID + "' not found", NestedServletException.class, () -> mvc.perform(get(URL_TEMPLATE + filterId1 + "/export?networkUuid=" + NETWORK_NOT_FOUND_UUID)
            .contentType(APPLICATION_JSON)));

        mvc.perform(get(URL_TEMPLATE + filterId1 + "/export?networkUuid=" + NETWORK_UUID)
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

        Date dateCreation = filterAttributes.get(0).getCreationDate();
        Date dateModification = filterAttributes.get(0).getModificationDate();

        AutomaticFilter hvdcLineAutomaticFilter = new AutomaticFilter(
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
        modifyFormFilter(filterId1, hvdcLineAutomaticFilter);
        checkFormFilter(filterId1, hvdcLineAutomaticFilter);

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

        matchFilterInfos(filterAttributes.get(0), filterId1, FilterType.AUTOMATIC, creationDate, modificationDate);
        matchFilterInfos(filterAttributes.get(1), filterId2, FilterType.SCRIPT, creationDate, modificationDate);

        filterAttributes = objectMapper.readValue(
            mvc.perform(get("/" + FilterApi.API_VERSION + "/filters/metadata?ids={id}", filterId1)
                    .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(),
            new TypeReference<>() {
            });
        assertEquals(1, filterAttributes.size());
        matchFilterInfos(filterAttributes.get(0), filterId1, FilterType.AUTOMATIC, creationDate, modificationDate);

        // test replace line filter with other filter type
        AbstractFilter generatorFormFilter = new AutomaticFilter(
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
        matchFilterInfos(filterAttributes.get(0), filterId1, FilterType.AUTOMATIC, creationDate, modificationDate);

        // update with same type filter
        AbstractFilter generatorFormFilter2 = new AutomaticFilter(
                filterId1,
                creationDate,
                modificationDate,
                new GeneratorFilter(new InjectionFilterAttributes("eqId2", "gen2", "s2", new TreeSet<>(Set.of("FR", "BE")), new NumericalFilter(RangeType.RANGE, 50., null))
                )
        );
        modifyFormFilter(filterId1, generatorFormFilter2);

        // delete
        mvc.perform(delete(URL_TEMPLATE + filterId2)).andExpect(status().isOk());

        mvc.perform(delete(URL_TEMPLATE + filterId2)).andExpect(status().isNotFound());

        mvc.perform(get(URL_TEMPLATE + filterId2)).andExpect(status().isNotFound());

        mvc.perform(put(URL_TEMPLATE + filterId2).contentType(APPLICATION_JSON).content(objectMapper.writeValueAsString(scriptFilter))).andExpect(status().isNotFound());

        filterService.deleteAll();
    }

    @Test
    public void testLineFilter2() throws Exception {
        UUID filterId3 = UUID.fromString("42b70a4d-e0c4-413a-8e3e-78e9027d300c");
        UUID filterId4 = UUID.fromString("42b70a4d-e0c4-413a-8e3e-78e9027d300d");
        Date creationDate = new Date();
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

        AutomaticFilter lineAutomaticFilterBEFR = insertLineFilter(filterId3, null, null, null, new TreeSet<>(Set.of("BE")), new TreeSet<>(Set.of("FR")),
                rangeTypes, values1, values2, NETWORK_UUID_6, null, bothMatch, false);

        // update form filter <-> script filter (rejected)
        ScriptFilter scriptFilter = new ScriptFilter(filterId4, creationDate, modificationDate, "test");
        insertFilter(filterId4, scriptFilter);
        checkScriptFilter(filterId4, scriptFilter);
        assertThrows(NestedServletException.class, () -> mvc.perform(put(URL_TEMPLATE + filterId3)
                .content(objectMapper.writeValueAsString(scriptFilter))
                .contentType(APPLICATION_JSON)));
        assertThrows(NestedServletException.class, () -> mvc.perform(put(URL_TEMPLATE + filterId4)
                .content(objectMapper.writeValueAsString(lineAutomaticFilterBEFR))
                .contentType(APPLICATION_JSON)));

        mvc.perform(delete(URL_TEMPLATE + filterId3)).andExpect(status().isOk());
        mvc.perform(delete(URL_TEMPLATE + filterId4)).andExpect(status().isOk());

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
        insertInjectionFilter(EquipmentType.GENERATOR, UUID.fromString("42b70a4d-e0c4-413a-8e3e-78e9027d300f"),
            "GEN", "GEN", "P1", Set.of("FR", "IT"), RangeType.RANGE, 15., 30., NETWORK_UUID, null, "[{\"id\":\"GEN\",\"type\":\"GENERATOR\"}]");
        insertInjectionFilter(EquipmentType.GENERATOR, UUID.fromString("42b70a4d-e0c4-413a-8e3e-78e9027d300f"),
            "GEN", "GEN", "P1", null, RangeType.RANGE, 15., 30., NETWORK_UUID, null, "[{\"id\":\"GEN\",\"type\":\"GENERATOR\"}]");
        insertInjectionFilter(EquipmentType.GENERATOR, UUID.fromString("42b70a4d-e0c4-413a-8e3e-78e9027d300f"),
            "GEN", "nameNotFound", "P1", null, RangeType.RANGE, 15., 30., NETWORK_UUID, null, "[]");
        insertInjectionFilter(EquipmentType.GENERATOR, UUID.fromString("42b70a4d-e0c4-413a-8e3e-78e9027d300f"),
            "GEN", "GEN", null, null, RangeType.RANGE, 15., 30., NETWORK_UUID, null, "[{\"id\":\"GEN\",\"type\":\"GENERATOR\"}]");
        insertInjectionFilter(EquipmentType.GENERATOR, UUID.fromString("42b70a4d-e0c4-413a-8e3e-78e9027d300f"),
            "GEN", "GEN", "substationNameNotFound", null, RangeType.RANGE, 15., 30., NETWORK_UUID, null, "[]");
        insertInjectionFilter(EquipmentType.GENERATOR, UUID.fromString("42b70a4d-e0c4-413a-8e3e-78e9027d300f"),
            "GEN", "GEN", "P1", Set.of("FR", "IT"), RangeType.EQUALITY, 145., null, NETWORK_UUID, null, "[]");
        insertInjectionFilter(EquipmentType.GENERATOR, UUID.fromString("42b70a4d-e0c4-413a-8e3e-78e9027d300f"),
            "GEN", "GEN", "P1", Set.of("FR", "IT"), RangeType.RANGE, 19., 22., NETWORK_UUID, null, "[]");
        insertInjectionFilter(EquipmentType.GENERATOR, UUID.fromString("42b70a4d-e0c4-413a-8e3e-78e9027d300f"),
            "GEN", "GEN", "P1", Set.of("FR", "IT"), RangeType.RANGE, 27., 30., NETWORK_UUID, null, "[]");
        insertInjectionFilter(EquipmentType.GENERATOR, UUID.fromString("42b70a4d-e0c4-413a-8e3e-78e9027d300f"),
            "GEN", "GEN", "P1", Set.of("FR", "IT"), RangeType.RANGE, 34.30, 35.70, NETWORK_UUID, null, "[]");
        insertInjectionFilter(EquipmentType.GENERATOR, UUID.fromString("42b70a4d-e0c4-413a-8e3e-78e9027d300f"),
            "GEN", "GEN", "P1", Set.of("FR", "IT"), RangeType.RANGE, 14.55, 15.45, NETWORK_UUID, null, "[]");
    }

    @Test
    public void testLoadFilter() throws Exception {
        insertInjectionFilter(EquipmentType.LOAD, UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e"),
            "LOAD", null, "P2", Set.of("FR"), RangeType.RANGE, 144., 176., NETWORK_UUID, VARIANT_ID_1, "[{\"id\":\"LOAD\",\"type\":\"LOAD\"}]");
    }

    @Test
    public void testShuntCompensatorFilter() throws Exception {
        insertInjectionFilter(EquipmentType.SHUNT_COMPENSATOR, UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e"),
            "SHUNT", "SHUNT", "S1", Set.of("FR"), RangeType.EQUALITY, 380., null, NETWORK_UUID_4, null, "[{\"id\":\"SHUNT\",\"type\":\"SHUNT_COMPENSATOR\"}]");
    }

    @Test
    public void testStaticVarCompensatorFilter() throws Exception {
        insertInjectionFilter(EquipmentType.STATIC_VAR_COMPENSATOR, UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e"),
            "SVC3", null, "S2", null, null, null, null, NETWORK_UUID_3, null, "[{\"id\":\"SVC3\",\"type\":\"STATIC_VAR_COMPENSATOR\"}]");
    }

    @Test
    public void testBatteryFilter() throws Exception {
        insertInjectionFilter(EquipmentType.BATTERY, UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e"),
            "batteryId1", "batteryName", null, Set.of("FR"), RangeType.RANGE, 45., 65., NETWORK_UUID, null, "[]");
    }

    @Test
    public void testBusBarSectionFilter() throws Exception {
        insertInjectionFilter(EquipmentType.BUSBAR_SECTION, UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e"),
            null, "batteryName", null, Set.of("DE"), RangeType.EQUALITY, 380., null, NETWORK_UUID, null, "[]");
    }

    @Test
    public void testDanglingLineFilter() throws Exception {
        insertInjectionFilter(EquipmentType.DANGLING_LINE, UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e"),
            "danglingLineId1", null, "s2", Set.of("FR"), RangeType.RANGE, 138., 162., NETWORK_UUID, null, "[]");
    }

    @Test
    public void testLccConverterStationFilter() throws Exception {
        insertInjectionFilter(EquipmentType.LCC_CONVERTER_STATION, UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e"),
            "lccId1", "lccName1", "s3", Set.of("FR", "BE", "NL", "DE", "IT"), RangeType.RANGE, 20., 400., NETWORK_UUID, null, "[]");
    }

    @Test
    public void testVscConverterStationFilter() throws Exception {
        insertInjectionFilter(EquipmentType.VSC_CONVERTER_STATION, UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e"),
            "vscId1", "vscName1", "s2", null, RangeType.EQUALITY, 225., null, NETWORK_UUID, null, "[]");
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
    public void testFilterToScript() throws Exception {
        UUID filterId1 = UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e");
        UUID filterId2 = UUID.fromString("42b70a4d-e0c4-413a-8e3e-78e9027d300f");
        UUID filterId3 = UUID.fromString("99999999-e0c4-413a-8e3e-78e9027d300f");
        Date creationDate = new Date();
        Date modificationDate = new Date();

        LineFilter lineFilter = new LineFilter("equipmentID", "equipmentName", "substationName1", "substationName2", COUNTRIES1, COUNTRIES2, new NumericalFilter(RangeType.RANGE, 5., 8.), new NumericalFilter(RangeType.EQUALITY, 6., null));

        AutomaticFilter lineAutomaticFilter = new AutomaticFilter(
                filterId1,
                creationDate,
                modificationDate,
                lineFilter
        );

        insertFilter(filterId1, lineAutomaticFilter);
        checkFormFilter(filterId1, lineAutomaticFilter);

        // new script from filter
        mvc.perform(post(URL_TEMPLATE + filterId1 + "/new-script?newId=" + UUID.randomUUID())).andExpect(status().isOk());

        mvc.perform(get(URL_TEMPLATE))
            .andExpect(status().isOk())
            .andExpect(content().json("[{\"type\":\"AUTOMATIC\"}, {\"type\":\"SCRIPT\"}]"));

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

        assertThrows("Filter implementation not yet supported: ScriptFilter", NestedServletException.class, () -> mvc.perform(get(URL_TEMPLATE + filterId2 + "/export?networkUuid=" + NETWORK_UUID)
            .contentType(APPLICATION_JSON)));
    }

    @Test
    public void testDuplicateFilter() throws Exception {
        UUID filterId1 = UUID.fromString("99999999-e0c4-413a-8e3e-78e9027d300f");
        Date creationDate = new Date();
        Date modificationDate = new Date();
        LineFilter lineFilter = new LineFilter("equipmentID", "equipmentName", "substationName1", "substationName2", COUNTRIES1, COUNTRIES2, new NumericalFilter(RangeType.RANGE, 5., 8.), new NumericalFilter(RangeType.EQUALITY, 6., null));
        AutomaticFilter lineAutomaticFilter = new AutomaticFilter(
                filterId1,
                creationDate,
                modificationDate,
                lineFilter
        );
        insertFilter(filterId1, lineAutomaticFilter);
        mvc.perform(post("/" + FilterApi.API_VERSION + "/filters?duplicateFrom=" + filterId1 + "&id=" + UUID.randomUUID())).andExpect(status().isOk());
        checkFormFilter(filterId1, lineAutomaticFilter);
    }

    @Test
    public void testManualFilter() throws Exception {
        UUID filterId = UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e");
        Date creationDate = new Date();
        Date modificationDate = new Date();
        ManualFilterEquipmentAttributes attributes1 = new ManualFilterEquipmentAttributes("line1", null);
        ManualFilterEquipmentAttributes attributes2 = new ManualFilterEquipmentAttributes("line2", null);

        ManualFilter manualFilter = new ManualFilter(filterId, creationDate, modificationDate, EquipmentType.LINE, List.of(attributes1, attributes2));
        insertFilter(filterId, manualFilter);
        checkManualFilter(filterId, manualFilter);
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
        AutomaticFilter modifiedFilter = objectMapper.readValue(modifiedFilterAsString, AutomaticFilter.class);
        checkFormFilter(filterId, modifiedFilter);

        mvc.perform(get(URL_TEMPLATE))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        MvcResult mockResponse = mvc.perform(get(URL_TEMPLATE + filterId)).andExpect(status().isOk()).andReturn();
        modifiedFilter = objectMapper.readValue(mockResponse.getResponse().getContentAsString(), AutomaticFilter.class);
        checkFormFilter(filterId, modifiedFilter);
    }

    private void insertInjectionFilter(EquipmentType equipmentType, UUID id, String equipmentID, String equipmentName,
                                       String substationName, Set<String> countries,
                                       RangeType rangeType, Double value1, Double value2,
                                       UUID networkUuid, String variantId, String expectedJsonExport)  throws Exception {
        NumericalFilter numericalFilter = rangeType != null ? new NumericalFilter(rangeType, value1, value2) : null;
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
        AutomaticFilter injectionFilter = new AutomaticFilter(
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
        assertEquals(FilterType.AUTOMATIC, filterAttributes.get(0).getType());

        mvc.perform(get(URL_TEMPLATE + id + "/export?networkUuid=" + networkUuid + (variantId != null ? "&variantId=" + variantId : ""))
            .contentType(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
            .andExpect(content().json(expectedJsonExport));

        mvc.perform(delete(URL_TEMPLATE + id)).andExpect(status().isOk());
    }

    private void insertTransformerFilter(EquipmentType equipmentType, UUID id, String equipmentID, String equipmentName,
                                         String substationName, Set<String> countries,
                                         List<RangeType> rangeTypes, List<Double> values1, List<Double> values2,
                                         UUID networkUuid, String variantId, String expectedJsonExport)  throws Exception {
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

        AutomaticFilter transformerFilter = new AutomaticFilter(
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
        assertEquals(FilterType.AUTOMATIC, filterAttributes.get(0).getType());

        mvc.perform(get(URL_TEMPLATE + id + "/export?networkUuid=" + networkUuid + (variantId != null ? "&variantId=" + variantId : ""))
            .contentType(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
            .andExpect(content().json(expectedJsonExport));

        mvc.perform(delete(URL_TEMPLATE + id)).andExpect(status().isOk());
    }

    private void insertHvdcLineFilter(UUID id, String equipmentID, String equipmentName,
                                      String substationName1, String substationName2, SortedSet<String> countries1,
                                      SortedSet<String> countries2, RangeType rangeType, Double value1, Double value2,
                                      UUID networkUuid, String variantId, String expectedJsonExport)  throws Exception {
        Date creationDate = new Date();
        Date modificationDate = new Date();
        AutomaticFilter hvdcLineFilter = new AutomaticFilter(
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
        matchFilterInfos(filters.get(0), id, FilterType.AUTOMATIC, creationDate, modificationDate);

        List<FilterAttributes> filterAttributes = objectMapper.readValue(
            mvc.perform(get("/" + FilterApi.API_VERSION + "/filters/metadata?ids={id}", id)
                    .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(),
            new TypeReference<>() {
            });

        assertEquals(1, filterAttributes.size());
        matchFilterInfos(filterAttributes.get(0), id, FilterType.AUTOMATIC, creationDate, modificationDate);

        mvc.perform(get(URL_TEMPLATE + id + "/export?networkUuid=" + networkUuid + (variantId != null ? "&variantId=" + variantId : ""))
            .contentType(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
            .andExpect(content().json(expectedJsonExport));

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

    private AutomaticFilter insertLineFilter(UUID id, String equipmentID, String equipmentName,
                                             String substationName, Set<String> countries1, Set<String> countries2,
                                             List<RangeType> rangeTypes, List<Double> values1, List<Double> values2,
                                             UUID networkUuid, String variantId, String expectedJsonExport, boolean delete)  throws Exception {
        NumericalFilter numericalFilter1 = null;
        if (rangeTypes.size() >= 1) {
            numericalFilter1 = new NumericalFilter(rangeTypes.get(0), values1.get(0), values2.get(0));
        }
        NumericalFilter numericalFilter2 = null;
        if (rangeTypes.size() == 2) {
            numericalFilter2 = new NumericalFilter(rangeTypes.get(1), values1.get(1), values2.get(1));
        }
        AbstractEquipmentFilterForm equipmentFilterForm = new LineFilter(equipmentID, equipmentName, null, substationName, AbstractFilterRepositoryProxy.setToSorterSet(countries1), AbstractFilterRepositoryProxy.setToSorterSet(countries2), numericalFilter1, numericalFilter2);
        Date creationDate = new Date();
        Date modificationDate = new Date();
        AutomaticFilter filter = new AutomaticFilter(
                id,
                creationDate,
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
        assertEquals(FilterType.AUTOMATIC, filterAttributes.get(0).getType());

        mvc.perform(get(URL_TEMPLATE + id + "/export?networkUuid=" + networkUuid + (variantId != null ? "&variantId=" + variantId : ""))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(content().json(expectedJsonExport));
        if (delete) {
            mvc.perform(delete(URL_TEMPLATE + id)).andExpect(status().isOk());
        }
        return filter;
    }

    private void checkFormFilter(UUID filterId, AutomaticFilter automaticFilter) throws Exception {
        String foundFilterAsString = mvc.perform(get(URL_TEMPLATE + filterId)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        AutomaticFilter foundFilter = objectMapper.readValue(foundFilterAsString, AutomaticFilter.class);
        matchFormFilterInfos(foundFilter, automaticFilter);
    }

    private void checkScriptFilter(UUID filterId, ScriptFilter scriptFilter) throws Exception {
        String foundFilterAsString = mvc.perform(get(URL_TEMPLATE + filterId)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        ScriptFilter foundFilter = objectMapper.readValue(foundFilterAsString, ScriptFilter.class);
        matchScriptFilterInfos(foundFilter, scriptFilter);
    }

    private void checkManualFilter(UUID filterId, ManualFilter manualFilter) throws Exception {
        String foundFilterAsString = mvc.perform(get(URL_TEMPLATE + filterId)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        ManualFilter foundFilter = objectMapper.readValue(foundFilterAsString, ManualFilter.class);
        matchManualFilterInfos(foundFilter, manualFilter);
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

    private void matchFormFilterInfos(AutomaticFilter automaticFilter1, AutomaticFilter automaticFilter2) {
        matchFilterInfos(automaticFilter1, automaticFilter2);
        matchEquipmentFormFilter(automaticFilter1.getEquipmentFilterForm(), automaticFilter2.getEquipmentFilterForm());
    }

    private void matchEquipmentFormFilter(AbstractEquipmentFilterForm equipmentFilterForm1, AbstractEquipmentFilterForm equipmentFilterForm2) {
        org.hamcrest.MatcherAssert.assertThat(equipmentFilterForm1, new MatcherJson<>(objectMapper, equipmentFilterForm2));
    }

    private void matchScriptFilterInfos(ScriptFilter scriptFilter1, ScriptFilter scriptFilter2) {
        matchFilterInfos(scriptFilter1, scriptFilter2);
        assertTrue(scriptFilter1.getScript().contains(scriptFilter2.getScript()));
    }

    private void matchManualFilterInfos(ManualFilter manualFilter1, ManualFilter manualFilter2) {
        matchFilterInfos(manualFilter1, manualFilter2);
        assertTrue(new MatcherJson<>(objectMapper, manualFilter2.getFilterEquipmentsAttributes()).matchesSafely(manualFilter1.getFilterEquipmentsAttributes()));

    }
}
