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
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.*;
import com.powsybl.network.store.client.NetworkStoreService;
import com.powsybl.network.store.client.PreloadingStrategy;
import com.powsybl.network.store.iidm.impl.NetworkFactoryImpl;
import org.gridsuite.filter.AbstractFilter;
import org.gridsuite.filter.IFilterAttributes;
import org.gridsuite.filter.expertfilter.ExpertFilter;
import org.gridsuite.filter.expertfilter.expertrule.*;
import org.gridsuite.filter.identifierlistfilter.FilterEquipments;
import org.gridsuite.filter.identifierlistfilter.FilteredIdentifiables;
import org.gridsuite.filter.identifierlistfilter.IdentifiableAttributes;
import org.gridsuite.filter.identifierlistfilter.IdentifierListFilter;
import org.gridsuite.filter.identifierlistfilter.IdentifierListFilterEquipmentAttributes;
import org.gridsuite.filter.server.dto.ElementAttributes;
import org.gridsuite.filter.server.dto.FilterAttributes;
import org.gridsuite.filter.server.dto.FiltersWithEquipmentTypes;
import org.gridsuite.filter.server.dto.EquipmentTypesByElement;
import org.gridsuite.filter.server.service.DirectoryService;
import org.gridsuite.filter.server.utils.MatcherJson;
import org.gridsuite.filter.server.utils.assertions.Assertions;
import org.gridsuite.filter.utils.EquipmentType;
import org.gridsuite.filter.utils.FilterType;
import org.gridsuite.filter.utils.expertfilter.CombinatorType;
import org.gridsuite.filter.utils.expertfilter.FieldType;
import org.gridsuite.filter.utils.expertfilter.OperatorType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.messaging.Message;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.*;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.apache.commons.lang3.StringUtils.join;
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

    @Autowired
    private FilterService filterService;

    @Autowired
    private OutputDestination output;

    @Autowired
    ObjectMapper objectMapper = new ObjectMapper();

    @MockBean
    private NetworkStoreService networkStoreService;

    @SpyBean
    private DirectoryService directoryService;

    public static final SortedSet<String> COUNTRIES1 = new TreeSet<>(Collections.singleton("France"));
    public static final SortedSet<String> COUNTRIES2 = new TreeSet<>(Collections.singleton("Germany"));

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

    private WireMockServer wireMockServer;

    @Before
    public void setUp() {
        Configuration.defaultConfiguration();
        final ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.USE_LONG_FOR_INTS);
        mapper.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
        mapper.disable(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE);

        network = EurostagTutorialExample1Factory.createWithMoreGenerators(new NetworkFactoryImpl());
        network.getSubstation("P1").setProperty("region", "north");
        network.getSubstation("P1").setName("P1");
        network.getSubstation("P2").setProperty("region", "south");
        network.getGenerator("GEN").setProperty("region", "north");
        network.getGenerator("GEN").setName("GEN");
        network.getGenerator("GEN2").setProperty("region", "south");
        network.getLoad("LOAD").setProperty("region", "north");
        network.getVoltageLevel("VLGEN").setName("VLGEN");
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
        given(networkStoreService.getNetwork(NETWORK_UUID, PreloadingStrategy.COLLECTION)).willReturn(network);
        given(networkStoreService.getNetwork(NETWORK_UUID_2, PreloadingStrategy.COLLECTION)).willReturn(network2);
        given(networkStoreService.getNetwork(NETWORK_UUID_3, PreloadingStrategy.COLLECTION)).willReturn(network3);
        given(networkStoreService.getNetwork(NETWORK_UUID_4, PreloadingStrategy.COLLECTION)).willReturn(network4);
        given(networkStoreService.getNetwork(NETWORK_UUID_5, PreloadingStrategy.COLLECTION)).willReturn(network5);
        given(networkStoreService.getNetwork(NETWORK_UUID_6, PreloadingStrategy.COLLECTION)).willReturn(network6);
        given(networkStoreService.getNetwork(NETWORK_NOT_FOUND_UUID, PreloadingStrategy.COLLECTION)).willReturn(null);

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

        wireMockServer = new WireMockServer(wireMockConfig().dynamicPort());
        wireMockServer.start();

        // mock base url of filter server as one of wire mock server
        Mockito.doAnswer(invocation -> wireMockServer.baseUrl()).when(directoryService).getBaseUri();
    }

    @After
    public void tearDown() {
        List<String> destinations = List.of(elementUpdateDestination);

        cleanDB();
        assertQueuesEmptyThenClear(destinations, output);
        wireMockServer.stop();
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

    private void checkElementUpdatedMessageSent(UUID elementUuid, String userId) {
        Message<byte[]> message = output.receive(TIMEOUT, elementUpdateDestination);
        assertEquals(elementUuid, message.getHeaders().get(NotificationService.HEADER_ELEMENT_UUID));
        assertEquals(userId, message.getHeaders().get(NotificationService.HEADER_MODIFIED_BY));
    }

    private void updateFilter(UUID filterId, AbstractFilter filter, String userId) throws Exception {
        mvc.perform(put(URL_TEMPLATE + "/" + filterId)
                        .content(objectMapper.writeValueAsString(filter))
                        .contentType(APPLICATION_JSON)
                        .header(USER_ID_HEADER, userId))
                .andExpect(status().isOk());
        checkElementUpdatedMessageSent(filterId, userId);
    }

    private UUID duplicateFilter(UUID filterId) throws Exception {
        String response = mvc.perform(post(URL_TEMPLATE).param("duplicateFrom", filterId.toString()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readValue(response, UUID.class);
    }

    @Test
    public void testLineFilterCrud() throws Exception {
        UUID filterId1 = UUID.fromString("99999999-e0c4-413a-8e3e-78e9027d300f");
        List<AbstractExpertRule> rules = new ArrayList<>();
        createExpertLineRules(rules, COUNTRIES1, COUNTRIES2, new TreeSet<>(Set.of(5., 8.)), new TreeSet<>(Set.of(6.)));
        CombinatorExpertRule combinator = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(rules).build();
        ExpertFilter lineFilter = new ExpertFilter(null, new Date(), EquipmentType.LINE, combinator);

        // --- insert filter --- //
        insertFilter(filterId1, lineFilter);

        // check the inserted filter
        lineFilter.setId(filterId1);
        checkExpertFilter(filterId1, lineFilter);

        // --- duplicate filter -- //
        UUID newFilterId1 = duplicateFilter(filterId1);

        // check the duplicated filter whether it is matched to the original
        lineFilter.setId(newFilterId1);
        checkExpertFilter(newFilterId1, lineFilter);

        // --- modify filter --- //
        List<AbstractExpertRule> rules2 = new ArrayList<>();
        createExpertLineRules(rules2, COUNTRIES2, COUNTRIES1, new TreeSet<>(Set.of(4., 9.)), new TreeSet<>(Set.of(5.)));
        CombinatorExpertRule combinator2 = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(rules2).build();
        ExpertFilter lineFilter2 = new ExpertFilter(null, new Date(), EquipmentType.LINE, combinator2);
        updateFilter(filterId1, lineFilter2, "userId");

        // check the modified filter
        lineFilter2.setId(filterId1);
        checkExpertFilter(filterId1, lineFilter2);

        // --- modify filter with equipment type changed --- //
        List rules3 = new ArrayList<>();
        createExpertRules(rules3, new TreeSet<>(Set.of("FR", "BE")), new TreeSet<>(Set.of(50.)));
        CombinatorExpertRule combinator3 = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(rules3).build();
        ExpertFilter generatorFilter = new ExpertFilter(null, new Date(), EquipmentType.GENERATOR, combinator3);
        updateFilter(filterId1, generatorFilter, "userId");

        // check the modified filter
        generatorFilter.setId(filterId1);
        checkExpertFilter(filterId1, generatorFilter);

        // --- delete filters --- //
        deleteFilter(filterId1);
        deleteFilter(newFilterId1);

        // check empty after delete all
        List<IFilterAttributes> allFilters = getAllFilters();
        Assertions.assertThat(allFilters).isEmpty();
    }

    @Test
    public void testGetFiltersByIds() throws Exception {
        UUID filterId3 = UUID.fromString("42b70a4d-e0c4-413a-8e3e-78e9027d300c");
        UUID filterId4 = UUID.fromString("42b70a4d-e0c4-413a-8e3e-78e9027d300d");

        ArrayList<AbstractExpertRule> rules = new ArrayList<>();
        EnumExpertRule country1Filter = EnumExpertRule.builder().field(FieldType.COUNTRY_1).operator(OperatorType.IN)
                .values(new TreeSet<>(Set.of("FR"))).build();
        rules.add(country1Filter);
        EnumExpertRule country2Filter = EnumExpertRule.builder().field(FieldType.COUNTRY_2).operator(OperatorType.IN)
                .values(new TreeSet<>(Set.of("FR"))).build();
        rules.add(country2Filter);
        NumberExpertRule nominalVoltage1Filter = NumberExpertRule.builder().field(FieldType.NOMINAL_VOLTAGE_1)
                .operator(OperatorType.BETWEEN).values(new TreeSet<>(Set.of(360., 400.))).build();
        rules.add(nominalVoltage1Filter);
        NumberExpertRule nominalVoltage2Filter = NumberExpertRule.builder().field(FieldType.NOMINAL_VOLTAGE_2)
                .operator(OperatorType.BETWEEN).values(new TreeSet<>(Set.of(356.25, 393.75))).build();
        rules.add(nominalVoltage2Filter);
        CombinatorExpertRule parentRule = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(rules).build();
        ExpertFilter lineFilter = new ExpertFilter(filterId3, new Date(), EquipmentType.LINE, parentRule);

        insertFilter(filterId3, lineFilter);
        checkExpertFilter(filterId3, lineFilter);

        ArrayList<AbstractExpertRule> rules2 = new ArrayList<>();
        EnumExpertRule country1Filter2 = EnumExpertRule.builder().field(FieldType.COUNTRY_1).operator(OperatorType.IN)
                .values(new TreeSet<>(Set.of("FR"))).build();
        rules2.add(country1Filter2);
        EnumExpertRule country2Filter2 = EnumExpertRule.builder().field(FieldType.COUNTRY_2).operator(OperatorType.IN)
                .values(new TreeSet<>(Set.of("FR"))).build();
        rules2.add(country2Filter2);
        NumberExpertRule nominalVoltage1Filter2 = NumberExpertRule.builder().field(FieldType.NOMINAL_VOLTAGE_1)
                .operator(OperatorType.BETWEEN).values(new TreeSet<>(Set.of(360., 400.))).build();
        rules2.add(nominalVoltage1Filter2);
        NumberExpertRule nominalVoltage2Filter2 = NumberExpertRule.builder().field(FieldType.NOMINAL_VOLTAGE_2)
                .operator(OperatorType.BETWEEN).values(new TreeSet<>(Set.of(356.25, 393.75))).build();
        rules2.add(nominalVoltage2Filter2);
        CombinatorExpertRule parentRule2 = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(rules2).build();
        ExpertFilter lineFilter2 = new ExpertFilter(filterId4, new Date(), EquipmentType.LINE, parentRule2);

        insertFilter(filterId4, lineFilter2);
        checkExpertFilter(filterId4, lineFilter2);
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
                checkIdentifiableAttributes(new ArrayList<>(filterEquipment1.getIdentifiableAttributes()), new ArrayList<>(filterEquipment2.getIdentifiableAttributes()));
            }
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

    @Test
    public void testEvaluateFilters() throws Exception {
        UUID filterId = UUID.randomUUID();
        FilterAttributes filterAttributes = new FilterAttributes();
        filterAttributes.setId(filterId);
        ArrayList<AbstractExpertRule> rules = new ArrayList<>();
        EnumExpertRule country1Filter = EnumExpertRule.builder().field(FieldType.COUNTRY_1).operator(OperatorType.IN)
            .values(new TreeSet<>(Set.of("FR"))).build();
        rules.add(country1Filter);
        CombinatorExpertRule parentRule = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(rules).build();
        ExpertFilter lineFilter = new ExpertFilter(filterId, new Date(), EquipmentType.LINE, parentRule);
        insertFilter(filterId, lineFilter);
        FiltersWithEquipmentTypes filtersBody = new FiltersWithEquipmentTypes(List.of(filterAttributes), List.of());

        // Apply filter by calling endPoint
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("networkUuid", NETWORK_UUID.toString());
        FilteredIdentifiables result = objectMapper.readValue(mvc.perform(
            post(URL_TEMPLATE + "/evaluate/identifiables")
                .params(params).contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(filtersBody))
            ).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(), new TypeReference<>() { });

        List<IdentifiableAttributes> expected = new ArrayList<>();
        expected.add(new IdentifiableAttributes("NHV1_NHV2_1", IdentifiableType.LINE, null));
        expected.add(new IdentifiableAttributes("NHV1_NHV2_2", IdentifiableType.LINE, null));
        assertTrue(expected.size() == result.equipmentIds().size()
            && result.equipmentIds().containsAll(expected)
            && expected.containsAll(result.equipmentIds()));

    }

    @Test
    public void testEvaluateFilters_SubstationWithEquipmentTypesLineAndGenerator() throws Exception {
        // Create a SUBSTATION expert filter selecting substations NHV1 and NGEN
        UUID filterId = UUID.randomUUID();
        FilterAttributes filterAttributes = new FilterAttributes();
        filterAttributes.setId(filterId);

        ArrayList<AbstractExpertRule> rules = new ArrayList<>();
        EnumExpertRule countryFilter = EnumExpertRule.builder()
            .field(FieldType.COUNTRY)
            .operator(OperatorType.IN)
            .values(new TreeSet<>(Set.of("FR")))
            .build();
        rules.add(countryFilter);
        CombinatorExpertRule parentRule = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(rules).build();

        ExpertFilter substationFilter = new ExpertFilter(filterId, new Date(), EquipmentType.SUBSTATION, parentRule);
        insertFilter(filterId, substationFilter);

        // Ask for sub-equipments: LINE and GENERATOR for this filter
        EquipmentTypesByElement equipmentTypesByElement = new EquipmentTypesByElement(filterId, Set.of(IdentifiableType.LINE, IdentifiableType.GENERATOR));
        FiltersWithEquipmentTypes filtersBody = new FiltersWithEquipmentTypes(List.of(filterAttributes), List.of(equipmentTypesByElement));

        // Apply filter by calling endpoint
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("networkUuid", NETWORK_UUID.toString());
        FilteredIdentifiables result = objectMapper.readValue(mvc.perform(
                post(URL_TEMPLATE + "/evaluate/identifiables")
                    .params(params).contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(filtersBody))
            ).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(), new TypeReference<>() { });

        List<IdentifiableAttributes> expected = new ArrayList<>();
        // Lines connected to NHV1 substation in the sample network
        expected.add(new IdentifiableAttributes("NHV1_NHV2_1", IdentifiableType.LINE, null));
        expected.add(new IdentifiableAttributes("NHV1_NHV2_2", IdentifiableType.LINE, null));
        // Generators in substation NGEN (GEN and GEN2 exist on initial variant)
        expected.add(new IdentifiableAttributes("GEN", IdentifiableType.GENERATOR, null));
        expected.add(new IdentifiableAttributes("GEN2", IdentifiableType.GENERATOR, null));

        assertTrue(result.equipmentIds().containsAll(expected));
    }

    private void stubForFilterInfos(UUID filterId, String excpectedJson) {
        MappingBuilder requestPatternBuilder = WireMock.get(WireMock.urlPathMatching("/v1/elements"))
            .withHeader(USER_ID_HEADER, equalTo(USER_ID_HEADER))
            .withQueryParam("ids", equalTo(filterId.toString()))
            .willReturn(WireMock.ok().withBody(excpectedJson).withHeader("Content-Type", "application/json; charset=utf-8"));

        wireMockServer.stubFor(requestPatternBuilder);
    }

    @Test
    public void testFilterInfos() throws Exception {
        UUID filterId = UUID.randomUUID();
        ArrayList<AbstractExpertRule> rules = new ArrayList<>();
        EnumExpertRule country1Filter = EnumExpertRule.builder().field(FieldType.COUNTRY_1).operator(OperatorType.IN)
            .values(new TreeSet<>(Set.of("FR"))).build();
        rules.add(country1Filter);
        CombinatorExpertRule parentRule = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(rules).build();
        ExpertFilter lineFilter = new ExpertFilter(filterId, new Date(), EquipmentType.LINE, parentRule);
        AbstractFilter filter = insertFilter(filterId, lineFilter);

        FilterAttributes filterAttributes = new FilterAttributes();
        filterAttributes.setId(filter.getId());
        filterAttributes.setType(FilterType.EXPERT);
        filterAttributes.setEquipmentType(EquipmentType.LINE);
        filterAttributes.setModificationDate(filter.getModificationDate());
        filterAttributes.setName("Filter1");

        ElementAttributes elementAttributes = new ElementAttributes(filterId, "Filter1");
        List<ElementAttributes> elementAttributesList = new ArrayList<>();
        elementAttributesList.add(elementAttributes);
        String excpectedElementAttributesJson = objectMapper.writeValueAsString(elementAttributesList);

        stubForFilterInfos(filterId, excpectedElementAttributesJson);
        UUID notFoundFilterId = UUID.randomUUID();
        List<String> filterIds = List.of(filterId.toString(), notFoundFilterId.toString());

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.addAll("filterUuids", filterIds);
        String res = mvc.perform(get(URL_TEMPLATE + "/infos")
                .header(USER_ID_HEADER, USER_ID_HEADER)
                .params(params).contentType(APPLICATION_JSON)).andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        List<FilterAttributes> result = objectMapper.readValue(res, new TypeReference<>() { });

        assertEquals(2, result.size());
        FilterAttributes filterAttribute = result.getFirst();
        assertEquals(filterId, filterAttribute.getId());
        assertEquals("Filter1", filterAttribute.getName());
        assertEquals(FilterType.EXPERT, filterAttribute.getType());
        assertEquals(EquipmentType.LINE, filterAttribute.getEquipmentType());
        assertEquals(filter.getModificationDate(), filterAttribute.getModificationDate());

        FilterAttributes filterAttribute2 = result.getLast();
        assertEquals(notFoundFilterId, filterAttribute2.getId());
        assertNull(filterAttribute2.getName());
        assertNull(filterAttribute2.getType());
        assertNull(filterAttribute2.getEquipmentType());
        assertNull(filterAttribute2.getModificationDate());
    }

    @Test
    public void testExportFilters() throws Exception {
        UUID filterId = UUID.randomUUID();
        UUID filterId2 = UUID.randomUUID();
        UUID filterId3 = UUID.randomUUID();

        ArrayList<AbstractExpertRule> rules = new ArrayList<>();
        EnumExpertRule country1Filter = EnumExpertRule.builder().field(FieldType.COUNTRY_1).operator(OperatorType.IN)
                .values(new TreeSet<>(Set.of("FR"))).build();
        rules.add(country1Filter);
        EnumExpertRule country2Filter = EnumExpertRule.builder().field(FieldType.COUNTRY_2).operator(OperatorType.IN)
                .values(new TreeSet<>(Set.of("FR"))).build();
        rules.add(country2Filter);
        NumberExpertRule nominalVoltage1Filter = NumberExpertRule.builder().field(FieldType.NOMINAL_VOLTAGE_1)
                .operator(OperatorType.BETWEEN).values(new TreeSet<>(Set.of(360., 400.))).build();
        rules.add(nominalVoltage1Filter);
        NumberExpertRule nominalVoltage2Filter = NumberExpertRule.builder().field(FieldType.NOMINAL_VOLTAGE_2)
                .operator(OperatorType.BETWEEN).values(new TreeSet<>(Set.of(356.25, 393.75))).build();
        rules.add(nominalVoltage2Filter);
        CombinatorExpertRule parentRule = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(rules).build();
        ExpertFilter lineFilter = new ExpertFilter(filterId2, new Date(), EquipmentType.LINE, parentRule);

        insertFilter(filterId2, lineFilter);
        checkExpertFilter(filterId2, lineFilter);

        ArrayList<AbstractExpertRule> rules2 = new ArrayList<>();
        EnumExpertRule country1Filter2 = EnumExpertRule.builder().field(FieldType.COUNTRY_1).operator(OperatorType.IN)
                .values(new TreeSet<>(Set.of("FR"))).build();
        rules2.add(country1Filter2);
        EnumExpertRule country2Filter2 = EnumExpertRule.builder().field(FieldType.COUNTRY_2).operator(OperatorType.IN)
                .values(new TreeSet<>(Set.of("FR"))).build();
        rules2.add(country2Filter2);
        NumberExpertRule nominalVoltage1Filter2 = NumberExpertRule.builder().field(FieldType.NOMINAL_VOLTAGE_1)
                .operator(OperatorType.BETWEEN).values(new TreeSet<>(Set.of(360., 400.))).build();
        rules2.add(nominalVoltage1Filter2);
        NumberExpertRule nominalVoltage2Filter2 = NumberExpertRule.builder().field(FieldType.NOMINAL_VOLTAGE_2)
                .operator(OperatorType.BETWEEN).values(new TreeSet<>(Set.of(356.25, 393.75))).build();
        rules2.add(nominalVoltage2Filter2);
        CombinatorExpertRule parentRule2 = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(rules2).build();
        ExpertFilter lineFilter2 = new ExpertFilter(filterId3, new Date(), EquipmentType.LINE, parentRule2);

        insertFilter(filterId3, lineFilter2);
        checkExpertFilter(filterId3, lineFilter2);

        IdentifierListFilterEquipmentAttributes attribute1 = new IdentifierListFilterEquipmentAttributes("GEN", 1.0);
        IdentifierListFilterEquipmentAttributes attribute2 = new IdentifierListFilterEquipmentAttributes("wrongId", 2.0);
        IdentifierListFilterEquipmentAttributes attribute3 = new IdentifierListFilterEquipmentAttributes("wrongId2", 3.0);

        Date date = new Date();
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
            mvc.perform(get(URL_TEMPLATE + "/export").params(params)
                            .contentType(APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString(),
            new TypeReference<>() {
            });

        IdentifiableAttributes identifiableAttributes = new IdentifiableAttributes("GEN", IdentifiableType.GENERATOR, 1.0);
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

    private void createExpertRules(List<AbstractExpertRule> rules, Set<String> countries, Set<Double> nominalVoltages) {
        EnumExpertRule country1Rule = EnumExpertRule.builder().field(FieldType.COUNTRY).operator(OperatorType.IN)
                .values(countries).build();
        rules.add(country1Rule);

        NumberExpertRule nominalVoltage2Rule;
        if (nominalVoltages.size() == 1) {
            nominalVoltage2Rule = NumberExpertRule.builder().field(FieldType.NOMINAL_VOLTAGE).operator(OperatorType.EQUALS)
                    .value(nominalVoltages.stream().findFirst().isPresent() ? nominalVoltages.stream().findFirst().get() : null)
                    .build();
        } else {
            nominalVoltage2Rule = NumberExpertRule.builder().field(FieldType.NOMINAL_VOLTAGE_2).operator(OperatorType.BETWEEN)
                    .values(nominalVoltages).build();
        }
        rules.add(nominalVoltage2Rule);
    }

    private void createExpertLineRules(List<AbstractExpertRule> rules, Set<String> countries1, Set<String> countries2,
                                       Set<Double> nominalVoltage1, Set<Double> nominalVoltage2) {

        EnumExpertRule country1Rule = EnumExpertRule.builder().field(FieldType.COUNTRY_1).operator(OperatorType.IN)
                .values(countries1).build();
        rules.add(country1Rule);
        EnumExpertRule country2Rule = EnumExpertRule.builder().field(FieldType.COUNTRY_2).operator(OperatorType.IN)
                .values(countries2).build();
        rules.add(country2Rule);
        NumberExpertRule nominalVoltage1Rule = NumberExpertRule.builder().field(FieldType.NOMINAL_VOLTAGE_1)
                .operator(OperatorType.BETWEEN).values(nominalVoltage1).build();
        rules.add(nominalVoltage1Rule);

        NumberExpertRule nominalVoltage2Rule;
        if (nominalVoltage2.size() == 1) {
            nominalVoltage2Rule = NumberExpertRule.builder().field(FieldType.NOMINAL_VOLTAGE_2).operator(OperatorType.EQUALS)
                    .value(nominalVoltage2.stream().findFirst().isPresent() ? nominalVoltage2.stream().findFirst().get() : null)
                    .build();
        } else {
            nominalVoltage2Rule = NumberExpertRule.builder().field(FieldType.NOMINAL_VOLTAGE_2).operator(OperatorType.BETWEEN)
                    .values(nominalVoltage2).build();
        }
        rules.add(nominalVoltage2Rule);
    }

    @Test
    public void testGetIdentifiablesCount() throws Exception {
        UUID filterId1 = UUID.randomUUID();
        UUID filterId2 = UUID.randomUUID();
        UUID filterId3 = UUID.randomUUID();

        ArrayList<AbstractExpertRule> rules = new ArrayList<>();
        createExpertLineRules(rules, new TreeSet<>(Set.of("FR")), new TreeSet<>(Set.of("FR")), new TreeSet<>(Set.of(360., 400.)),
                new TreeSet<>(Set.of(356.25, 393.7)));
        CombinatorExpertRule parentRule = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(rules).build();
        ExpertFilter expertFilter = new ExpertFilter(filterId1, new Date(), EquipmentType.LINE, parentRule);

        insertFilter(filterId1, expertFilter);
        checkExpertFilter(filterId1, expertFilter);

        ArrayList<AbstractExpertRule> rules2 = new ArrayList<>();
        createExpertLineRules(rules2, new TreeSet<>(Set.of("FR")), new TreeSet<>(Set.of("FR")), new TreeSet<>(Set.of(360., 400.)),
                new TreeSet<>(Set.of(356.25, 393.7)));
        CombinatorExpertRule parentRule2 = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(rules2).build();
        ExpertFilter expertFilter2 = new ExpertFilter(filterId2, new Date(), EquipmentType.LINE, parentRule2);

        insertFilter(filterId2, expertFilter2);
        checkExpertFilter(filterId2, expertFilter2);

        ArrayList<AbstractExpertRule> rules3 = new ArrayList<>();
        EnumExpertRule country1Rule3 = EnumExpertRule.builder().field(FieldType.COUNTRY_1).operator(OperatorType.IN)
                .values(new TreeSet<>(Set.of("FR", "BE"))).build();
        rules3.add(country1Rule3);
        EnumExpertRule country2Rule3 = EnumExpertRule.builder().field(FieldType.COUNTRY_2).operator(OperatorType.IN)
                .values(new TreeSet<>(Set.of("FR", "IT"))).build();
        rules3.add(country2Rule3);
        CombinatorExpertRule parentRule3 = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(rules3).build();
        ExpertFilter expertFilter3 = new ExpertFilter(filterId3, new Date(), EquipmentType.HVDC_LINE, parentRule3);

        insertFilter(filterId3, expertFilter3);
        checkExpertFilter(filterId3, expertFilter3);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>(Map.of(
                "networkUuid", List.of(NETWORK_UUID.toString()),
                "variantId", List.of(VARIANT_ID_1),
                "ids[g1]", List.of(filterId1.toString()),
                "ids[g2]", List.of(filterId2.toString()),
                "ids[g3]", List.of(filterId3.toString()),
                "ids[g4]", List.of(UUID.randomUUID().toString())
        ));
        Map<String, Long> identifiablesCount = objectMapper.readValue(
                mvc.perform(get(URL_TEMPLATE + "/identifiables-count")
                                .params(params)
                                .contentType(APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsString(),
                new TypeReference<>() {
                });

        assertEquals(2, identifiablesCount.get("g1").longValue());
        assertEquals(2, identifiablesCount.get("g2").longValue());
        assertEquals(0, identifiablesCount.get("g3").longValue());
        assertEquals(0, identifiablesCount.get("g4").longValue());

        assertEquals(4, identifiablesCount.size());
    }

    private void checkIdentifierListFilterExportAndMetadata(UUID filterId, String expectedJson, EquipmentType equipmentType) throws Exception {
        mvc.perform(get(URL_TEMPLATE + "/" + filterId + "/export").param("networkUuid", NETWORK_UUID.toString())
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(content().json(expectedJson));

        List<FilterAttributes> filterAttributes = objectMapper.readValue(
            mvc.perform(get(URL_TEMPLATE + "/metadata").param("ids", filterId.toString())
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
        mvc.perform(get(URL_TEMPLATE + "/" + filterId + "/export").param("networkUuid", NETWORK_UUID.toString())
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(content().json(expectedJson));

        List<FilterAttributes> filterAttributes = objectMapper.readValue(
                mvc.perform(get(URL_TEMPLATE + "/metadata").param("ids", filterId.toString())
                                .contentType(APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsString(),
                new TypeReference<>() {
                });

        assertEquals(1, filterAttributes.size());
        assertEquals(filterId, filterAttributes.get(0).getId());
        assertEquals(FilterType.EXPERT, filterAttributes.get(0).getType());
        assertEquals(equipmentType, filterAttributes.get(0).getEquipmentType());

        deleteFilter(filterId);
    }

    private void checkFilterEvaluating(AbstractFilter filter, String expectedJson) throws Exception {
        mvc.perform(post(URL_TEMPLATE + "/evaluate").param("networkUuid", NETWORK_UUID.toString())
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

    private List<AbstractFilter> insertFilters(Map<UUID, AbstractFilter> filtersToCreateMap) throws Exception {
        String response = mvc.perform(post(URL_TEMPLATE + "/batch")
                        .content(objectMapper.writeValueAsString(filtersToCreateMap))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return objectMapper.readValue(response, new TypeReference<>() { });
    }

    private Map<UUID, UUID> duplicateFilters(List<UUID> sourceFilterUuids) throws Exception {
        String response = mvc.perform(post(URL_TEMPLATE + "/duplicate/batch")
                        .content(objectMapper.writeValueAsString(sourceFilterUuids))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return objectMapper.readValue(response, new TypeReference<>() { });
    }

    private List<AbstractFilter> updateFilters(Map<UUID, AbstractFilter> filtersToUpdateMap) throws Exception {
        String response = mvc.perform(put(URL_TEMPLATE + "/batch")
                        .content(objectMapper.writeValueAsString(filtersToUpdateMap))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return objectMapper.readValue(response, new TypeReference<>() { });
    }

    private void updateFiltersWithNoneExistingId(Map<UUID, AbstractFilter> filtersToUpdateMap) throws Exception {
        mvc.perform(put(URL_TEMPLATE + "/batch")
                        .content(objectMapper.writeValueAsString(filtersToUpdateMap))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    private void deleteFilter(UUID filterId) throws Exception {
        mvc.perform(delete(URL_TEMPLATE + "/" + filterId)).andExpect(status().isOk());
    }

    private void deleteFilters(List<UUID> filterUuids) throws Exception {
        mvc.perform(delete(URL_TEMPLATE)
                        .content(objectMapper.writeValueAsString(filterUuids))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    private List<IFilterAttributes> getAllFilters() throws Exception {
        String response = mvc.perform(get(URL_TEMPLATE)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return objectMapper.readValue(response, new TypeReference<>() { });
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

    private void matchIdentifierListFilterInfos(IdentifierListFilter identifierListFilter1, IdentifierListFilter identifierListFilter2) {
        matchFilterInfos(identifierListFilter1, identifierListFilter2);
        assertTrue(new MatcherJson<>(objectMapper, identifierListFilter2.getFilterEquipmentsAttributes()).matchesSafely(identifierListFilter1.getFilterEquipmentsAttributes()));
    }

    private void matchExpertFilterInfos(ExpertFilter expertFilter1, ExpertFilter expertFilter2) {
        matchFilterInfos(expertFilter1, expertFilter2);
        Assertions.assertThat(expertFilter1).recursivelyEquals(expertFilter2, "topologyKind" /* not persisted field */);
    }

    @Test
    public void testExpertFilterGenerator() throws Exception {
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
    public void testExpertFilterLoad() throws Exception {
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
    public void testExpertFilterVoltageLevel() throws Exception {
        UUID filterId = UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e");
        Date modificationDate = new Date();

        List<AbstractExpertRule> rules = new ArrayList<>();
        StringExpertRule stringRule = StringExpertRule.builder().value("VLLOAD")
            .field(FieldType.ID).operator(OperatorType.IS).build();
        rules.add(stringRule);

        CombinatorExpertRule vlRule = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(rules).build();

        ExpertFilter expertFilter = new ExpertFilter(filterId, modificationDate, EquipmentType.VOLTAGE_LEVEL, vlRule);
        insertFilter(filterId, expertFilter);
        checkExpertFilter(filterId, expertFilter);

        // check result when evaluating a filter on a network
        String expectedResultJson = """
                [{"id":"VLLOAD","type":"VOLTAGE_LEVEL"}]
            """;
        checkExpertFilterExportAndMetadata(filterId, expectedResultJson, EquipmentType.VOLTAGE_LEVEL);
        checkFilterEvaluating(expertFilter, expectedResultJson);
    }

    @Test
    public void testExpertFilterSubstation() throws Exception {
        UUID filterId = UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e");
        Date modificationDate = new Date();

        List<AbstractExpertRule> rules = new ArrayList<>();
        StringExpertRule stringRule = StringExpertRule.builder().value("P1")
            .field(FieldType.ID).operator(OperatorType.IS).build();
        rules.add(stringRule);

        CombinatorExpertRule subRule = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(rules).build();

        ExpertFilter expertFilter = new ExpertFilter(filterId, modificationDate, EquipmentType.SUBSTATION, subRule);
        insertFilter(filterId, expertFilter);
        checkExpertFilter(filterId, expertFilter);

        // check result when evaluating a filter on a network
        String expectedResultJson = """
                [{"id":"P1","type":"SUBSTATION"}]
            """;
        checkExpertFilterExportAndMetadata(filterId, expectedResultJson, EquipmentType.SUBSTATION);
        checkFilterEvaluating(expertFilter, expectedResultJson);
    }

    @Test
    public void testExpertFilterSubstationWithProperties() throws Exception {
        UUID filterId = UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e");
        Date modificationDate = new Date();

        List<AbstractExpertRule> rules = new ArrayList<>();
        StringExpertRule stringRule = StringExpertRule.builder().value("P1")
            .field(FieldType.ID).operator(OperatorType.IS).build();
        rules.add(stringRule);
        PropertiesExpertRule propertiesExpertRule = PropertiesExpertRule.builder().propertyName("region").propertyValues(List.of("north"))
                .field(FieldType.FREE_PROPERTIES).operator(OperatorType.IN).build();
        rules.add(propertiesExpertRule);

        CombinatorExpertRule subRule = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(rules).build();

        ExpertFilter expertFilter = new ExpertFilter(filterId, modificationDate, EquipmentType.SUBSTATION, subRule);
        insertFilter(filterId, expertFilter);
        checkExpertFilter(filterId, expertFilter);

        // check result when evaluating a filter on a network
        String expectedResultJson = """
                [{"id":"P1","type":"SUBSTATION"}]
            """;
        checkExpertFilterExportAndMetadata(filterId, expectedResultJson, EquipmentType.SUBSTATION);
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
        NumberExpertRule numberInRule = NumberExpertRule.builder().values(Set.of(24.0))
                .field(FieldType.NOMINAL_VOLTAGE).operator(OperatorType.IN).build();
        inFilter = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(Arrays.asList(numberInRule)).build();

        expertFilter = new ExpertFilter(filterId, new Date(), EquipmentType.GENERATOR, inFilter);
        insertFilter(filterId, expertFilter);
        checkExpertFilter(filterId, expertFilter);

        // check result when evaluating a filter on a network
        checkExpertFilterExportAndMetadata(filterId, expectedResultJson, EquipmentType.GENERATOR);

        // Build a filter AND with only a NOT_IN operator for NOMINAL_VOLTAGE
        numberInRule = NumberExpertRule.builder().values(Set.of(12.0))
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
    public void testExpertFilterLoadWithInAndNotInOperator() throws Exception {
        UUID filterId = UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e");

        // Build a filter AND with only an IN operator for VOLTAGE_LEVEL_ID
        StringExpertRule stringInRule = StringExpertRule.builder().values(new HashSet<>(Arrays.asList("VLLOAD", "VLLOAD2")))
                .field(FieldType.VOLTAGE_LEVEL_ID).operator(OperatorType.IN).build();
        CombinatorExpertRule inFilter = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(Arrays.asList(stringInRule)).build();

        ExpertFilter expertFilter = new ExpertFilter(filterId, new Date(), EquipmentType.LOAD, inFilter);
        insertFilter(filterId, expertFilter);
        checkExpertFilter(filterId, expertFilter);

        // check result when evaluating a filter on a network
        String expectedResultJson = """
                [
                    {"id":"LOAD","type":"LOAD"}
                ]
            """;
        checkExpertFilterExportAndMetadata(filterId, expectedResultJson, EquipmentType.LOAD);

        // Build a filter AND with only a NOT_IN operator for VOLTAGE_LEVEL_ID
        stringInRule = StringExpertRule.builder().values(new HashSet<>(Arrays.asList("VLLOAD2")))
                .field(FieldType.VOLTAGE_LEVEL_ID).operator(OperatorType.NOT_IN).build();
        inFilter = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(Arrays.asList(stringInRule)).build();

        expertFilter = new ExpertFilter(filterId, new Date(), EquipmentType.LOAD, inFilter);
        insertFilter(filterId, expertFilter);
        checkExpertFilter(filterId, expertFilter);

        // check result when evaluating a filter on a network
        checkExpertFilterExportAndMetadata(filterId, expectedResultJson, EquipmentType.LOAD);

        // Build a filter AND with only an IN operator for NOMINAL_VOLTAGE
        NumberExpertRule numberInRule = NumberExpertRule.builder().values(new HashSet<>(Arrays.asList(150.0)))
                .field(FieldType.NOMINAL_VOLTAGE).operator(OperatorType.IN).build();
        inFilter = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(Arrays.asList(numberInRule)).build();

        expertFilter = new ExpertFilter(filterId, new Date(), EquipmentType.LOAD, inFilter);
        insertFilter(filterId, expertFilter);
        checkExpertFilter(filterId, expertFilter);

        // check result when evaluating a filter on a network
        checkExpertFilterExportAndMetadata(filterId, expectedResultJson, EquipmentType.LOAD);

        // Build a filter AND with only a NOT_IN operator for NOMINAL_VOLTAGE
        numberInRule = NumberExpertRule.builder().values(new HashSet<>(Arrays.asList(12.0)))
                .field(FieldType.NOMINAL_VOLTAGE).operator(OperatorType.NOT_IN).build();
        inFilter = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(Arrays.asList(numberInRule)).build();

        expertFilter = new ExpertFilter(filterId, new Date(), EquipmentType.LOAD, inFilter);
        insertFilter(filterId, expertFilter);
        checkExpertFilter(filterId, expertFilter);

        // check result when evaluating a filter on a network
        checkExpertFilterExportAndMetadata(filterId, expectedResultJson, EquipmentType.LOAD);

        // Build a filter AND with only an IN operator for COUNTRY
        EnumExpertRule enumInRule = EnumExpertRule.builder().values(new HashSet<>(Arrays.asList(Country.FR.name(), Country.GB.name())))
                .field(FieldType.COUNTRY).operator(OperatorType.IN).build();
        inFilter = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(Arrays.asList(enumInRule)).build();

        expertFilter = new ExpertFilter(filterId, new Date(), EquipmentType.LOAD, inFilter);
        insertFilter(filterId, expertFilter);
        checkExpertFilter(filterId, expertFilter);

        // check result when evaluating a filter on a network
        checkExpertFilterExportAndMetadata(filterId, expectedResultJson, EquipmentType.LOAD);

        // Build a filter AND with only a NOT_IN operator for COUNTRY
        enumInRule = EnumExpertRule.builder().values(new HashSet<>(Arrays.asList(Country.GB.name())))
                .field(FieldType.COUNTRY).operator(OperatorType.NOT_IN).build();
        inFilter = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(Arrays.asList(enumInRule)).build();

        expertFilter = new ExpertFilter(filterId, new Date(), EquipmentType.LOAD, inFilter);
        insertFilter(filterId, expertFilter);
        checkExpertFilter(filterId, expertFilter);

        // check result when evaluating a filter on a network
        checkExpertFilterExportAndMetadata(filterId, expectedResultJson, EquipmentType.LOAD);
    }

    @Test
    public void testExpertFilterBusWithInAndNotInOperator() throws Exception {
        UUID filterId = UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e");

        // Build a filter AND with only an IN operator for VOLTAGE_LEVEL_ID
        StringExpertRule stringInRule = StringExpertRule.builder().values(new HashSet<>(Arrays.asList("VLGEN", "VLGEN2", "VLLOAD", "VLLOAD2")))
                .field(FieldType.VOLTAGE_LEVEL_ID).operator(OperatorType.IN).build();
        CombinatorExpertRule inFilter = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(Arrays.asList(stringInRule)).build();

        ExpertFilter expertFilter = new ExpertFilter(filterId, new Date(), EquipmentType.BUS, inFilter);
        expertFilter.setTopologyKind(TopologyKind.BUS_BREAKER); // set optional info

        insertFilter(filterId, expertFilter);
        checkExpertFilter(filterId, expertFilter);

        // check result when evaluating a filter on a network
        String expectedResultJson = """
                [
                    {"id":"NGEN","type":"BUS"},
                    {"id":"NLOAD","type":"BUS"}
                ]
            """;
        checkExpertFilterExportAndMetadata(filterId, expectedResultJson, EquipmentType.BUS);

        // Build a filter AND with only a NOT_IN operator for VOLTAGE_LEVEL_ID
        stringInRule = StringExpertRule.builder().values(new HashSet<>(Arrays.asList("VLHV1", "VLHV2")))
                .field(FieldType.VOLTAGE_LEVEL_ID).operator(OperatorType.NOT_IN).build();
        inFilter = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(Arrays.asList(stringInRule)).build();

        expertFilter = new ExpertFilter(filterId, new Date(), EquipmentType.BUS, inFilter);
        insertFilter(filterId, expertFilter);
        checkExpertFilter(filterId, expertFilter);

        // check result when evaluating a filter on a network
        checkExpertFilterExportAndMetadata(filterId, expectedResultJson, EquipmentType.BUS);

        // Build a filter AND with only an IN operator for NOMINAL_VOLTAGE
        NumberExpertRule numberInRule = NumberExpertRule.builder().values(new HashSet<>(Arrays.asList(24.0, 150.0)))
                .field(FieldType.NOMINAL_VOLTAGE).operator(OperatorType.IN).build();
        inFilter = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(Arrays.asList(numberInRule)).build();

        expertFilter = new ExpertFilter(filterId, new Date(), EquipmentType.BUS, inFilter);
        insertFilter(filterId, expertFilter);
        checkExpertFilter(filterId, expertFilter);

        // check result when evaluating a filter on a network
        checkExpertFilterExportAndMetadata(filterId, expectedResultJson, EquipmentType.BUS);

        // Build a filter AND with only a NOT_IN operator for NOMINAL_VOLTAGE
        numberInRule = NumberExpertRule.builder().values(new HashSet<>(Arrays.asList(380.0)))
                .field(FieldType.NOMINAL_VOLTAGE).operator(OperatorType.NOT_IN).build();
        inFilter = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(Arrays.asList(numberInRule)).build();

        expertFilter = new ExpertFilter(filterId, new Date(), EquipmentType.BUS, inFilter);
        insertFilter(filterId, expertFilter);
        checkExpertFilter(filterId, expertFilter);

        // check result when evaluating a filter on a network
        checkExpertFilterExportAndMetadata(filterId, expectedResultJson, EquipmentType.BUS);

        // Build a filter AND with only an IN operator for COUNTRY
        EnumExpertRule enumInRule = EnumExpertRule.builder().values(new HashSet<>(Arrays.asList(Country.FR.name(), Country.GB.name())))
                .field(FieldType.COUNTRY).operator(OperatorType.IN).build();
        inFilter = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(Arrays.asList(enumInRule)).build();

        expertFilter = new ExpertFilter(filterId, new Date(), EquipmentType.BUS, inFilter);
        insertFilter(filterId, expertFilter);
        checkExpertFilter(filterId, expertFilter);

        expectedResultJson = """
                [
                    {"id":"NGEN","type":"BUS"},
                    {"id":"NHV1","type":"BUS"},
                    {"id":"NHV2","type":"BUS"},
                    {"id":"NLOAD","type":"BUS"}
                ]
            """;
        // check result when evaluating a filter on a network
        checkExpertFilterExportAndMetadata(filterId, expectedResultJson, EquipmentType.BUS);

        // Build a filter AND with only a NOT_IN operator for COUNTRY
        enumInRule = EnumExpertRule.builder().values(new HashSet<>(Arrays.asList(Country.GB.name())))
                .field(FieldType.COUNTRY).operator(OperatorType.NOT_IN).build();
        inFilter = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(Arrays.asList(enumInRule)).build();

        expertFilter = new ExpertFilter(filterId, new Date(), EquipmentType.BUS, inFilter);
        insertFilter(filterId, expertFilter);
        checkExpertFilter(filterId, expertFilter);

        // check result when evaluating a filter on a network
        checkExpertFilterExportAndMetadata(filterId, expectedResultJson, EquipmentType.BUS);
    }

    @Test
    public void testExpertFilterTwoWindingsTransformerWithInAndNotInOperator() throws Exception {
        UUID filterId = UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e");

        // Build a filter AND with only an IN operator for VOLTAGE_LEVEL_ID
        StringExpertRule stringInRule = StringExpertRule.builder().values(new HashSet<>(Arrays.asList("VLHV2")))
                .field(FieldType.VOLTAGE_LEVEL_ID_1).operator(OperatorType.IN).build();
        CombinatorExpertRule inFilter = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(Arrays.asList(stringInRule)).build();

        ExpertFilter expertFilter = new ExpertFilter(filterId, new Date(), EquipmentType.TWO_WINDINGS_TRANSFORMER, inFilter);
        expertFilter.setTopologyKind(TopologyKind.NODE_BREAKER); // set optional info

        insertFilter(filterId, expertFilter);
        checkExpertFilter(filterId, expertFilter);

        // check result when evaluating a filter on a network
        String expectedResultJson = """
                [
                    {"id":"NHV2_NLOAD","type":"TWO_WINDINGS_TRANSFORMER"}
                ]
            """;
        checkExpertFilterExportAndMetadata(filterId, expectedResultJson, EquipmentType.TWO_WINDINGS_TRANSFORMER);

        // Build a filter AND with only a NOT_IN operator for VOLTAGE_LEVEL_ID
        stringInRule = StringExpertRule.builder().values(new HashSet<>(Arrays.asList("VLHV2")))
                .field(FieldType.VOLTAGE_LEVEL_ID_1).operator(OperatorType.NOT_IN).build();
        inFilter = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(Arrays.asList(stringInRule)).build();

        expertFilter = new ExpertFilter(filterId, new Date(), EquipmentType.TWO_WINDINGS_TRANSFORMER, inFilter);
        insertFilter(filterId, expertFilter);
        checkExpertFilter(filterId, expertFilter);

        expectedResultJson = """
                [
                    {"id":"NGEN_NHV1","type":"TWO_WINDINGS_TRANSFORMER"}
                ]
            """;
        // check result when evaluating a filter on a network
        checkExpertFilterExportAndMetadata(filterId, expectedResultJson, EquipmentType.TWO_WINDINGS_TRANSFORMER);

        // Build a filter AND with only an IN operator
    }

    @Test
    public void testExpertFilterLoadLinkToOtherFilterWithIsPartOfOperator() throws Exception {
        // Create identifier list filter for loads
        UUID identifierListFilterId = UUID.fromString("77614d91-c168-4f89-8fb9-77a23729e88e");
        Date modificationDate = new Date();

        IdentifierListFilterEquipmentAttributes load = new IdentifierListFilterEquipmentAttributes("LOAD", 7d);
        IdentifierListFilter identifierListFilter = new IdentifierListFilter(identifierListFilterId, modificationDate, EquipmentType.LOAD, List.of(load));
        insertFilter(identifierListFilterId, identifierListFilter);

        // create expert filter linked to the identifier list filter
        UUID expertFilterId = UUID.fromString("87614d91-c168-4f89-8fb9-77a23729e88e");
        List<AbstractExpertRule> rules = new ArrayList<>();
        FilterUuidExpertRule filterUuidExpertRule = FilterUuidExpertRule.builder().values(Set.of(identifierListFilterId.toString()))
            .field(FieldType.ID).operator(OperatorType.IS_PART_OF).build();
        rules.add(filterUuidExpertRule);

        CombinatorExpertRule combinatorRule = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(rules).build();

        ExpertFilter expertFilter = new ExpertFilter(expertFilterId, modificationDate, EquipmentType.LOAD, combinatorRule);
        insertFilter(expertFilterId, expertFilter);
        checkExpertFilter(expertFilterId, expertFilter);

        // check result when evaluating a filter on a network
        String expectedResultJson = """
                [{"id":"LOAD","type":"LOAD"}]
            """;
        checkExpertFilterExportAndMetadata(expertFilterId, expectedResultJson, EquipmentType.LOAD);
        checkFilterEvaluating(expertFilter, expectedResultJson);
    }

    @Test
    public void testLineFiltersCrudInBatch() throws Exception {
        UUID filterId1 = UUID.randomUUID();
        Date date = new Date();
        ArrayList<AbstractExpertRule> rules = new ArrayList<>();
        createExpertLineRules(rules, COUNTRIES1, COUNTRIES2, new TreeSet<>(Set.of(5., 8.)), new TreeSet<>(Set.of(6.)));

        CombinatorExpertRule parentRule = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(rules).build();
        ExpertFilter expertFilter1 = new ExpertFilter(filterId1, date, EquipmentType.LINE, parentRule);

        UUID filterId2 = UUID.randomUUID();
        ArrayList<AbstractExpertRule> rules2 = new ArrayList<>();
        createExpertLineRules(rules2, COUNTRIES1, COUNTRIES2, new TreeSet<>(Set.of(4., 9.)), new TreeSet<>(Set.of(5.)));

        CombinatorExpertRule parentRule2 = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(rules2).build();
        ExpertFilter expertFilter2 = new ExpertFilter(filterId2, date, EquipmentType.LINE, parentRule2);

        Map<UUID, AbstractFilter> filtersToCreateMap = Map.of(
                filterId1, expertFilter1,
                filterId2, expertFilter2
        );

        // --- insert in batch --- //
        insertFilters(filtersToCreateMap);

        // check inserted filters
        expertFilter1.setId(filterId1);
        checkExpertFilter(filterId1, expertFilter1);
        expertFilter2.setId(filterId2);
        checkExpertFilter(filterId2, expertFilter2);

        // --- duplicate in batch --- //
        Map<UUID, UUID> sourceAndNewUuidMap = duplicateFilters(List.of(filterId1, filterId2));
        sourceAndNewUuidMap.forEach((sourceUuid, newUuid) -> filtersToCreateMap.get(sourceUuid).setId(newUuid));

        // check each duplicated filter whether it is matched to the original
        for (Map.Entry<UUID, UUID> entry : sourceAndNewUuidMap.entrySet()) {
            UUID sourceUuid = entry.getKey();
            UUID newUuid = entry.getValue();
            checkExpertFilter(newUuid, (ExpertFilter) filtersToCreateMap.get(sourceUuid));
        }

        // --- modify filters in batch --- //
        ArrayList<AbstractExpertRule> rules3 = new ArrayList<>();
        createExpertLineRules(rules3, COUNTRIES1, COUNTRIES2, new TreeSet<>(Set.of(3., 10.)), new TreeSet<>(Set.of(4.)));
        CombinatorExpertRule parentRule3 = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(rules3).build();
        ExpertFilter lineExpertFilter = new ExpertFilter(null, date, EquipmentType.LINE, parentRule3);

        ArrayList<AbstractExpertRule> rules4 = new ArrayList<>();
        EnumExpertRule countryFilter4 = EnumExpertRule.builder().field(FieldType.COUNTRY).operator(OperatorType.IN)
                .values(new TreeSet<>(Set.of("FR", "BE"))).build();
        rules4.add(countryFilter4);
        NumberExpertRule nominalVoltageFilter4 = NumberExpertRule.builder().field(FieldType.NOMINAL_VOLTAGE)
                .operator(OperatorType.EQUALS).value(50.).build();
        rules4.add(nominalVoltageFilter4);
        CombinatorExpertRule parentRule4 = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(rules4).build();
        ExpertFilter generatorExpertFilter = new ExpertFilter(null, date, EquipmentType.GENERATOR, parentRule4);

        Map<UUID, AbstractFilter> filtersToUpdateMap = Map.of(
                filterId1, lineExpertFilter,
                filterId2, generatorExpertFilter
        );
        updateFilters(filtersToUpdateMap);

        // check modified filters
        lineExpertFilter.setId(filterId1);
        checkExpertFilter(filterId1, lineExpertFilter);
        generatorExpertFilter.setId(filterId2);
        checkExpertFilter(filterId2, generatorExpertFilter);

        // --- modify filters in batch with a none existing id --- //
        ArrayList<AbstractExpertRule> rules5 = new ArrayList<>();
        EnumExpertRule countryFilter5 = EnumExpertRule.builder().field(FieldType.COUNTRY).operator(OperatorType.IN)
                .values(new TreeSet<>(Set.of("FR", "BE"))).build();
        rules5.add(countryFilter5);

        NumberExpertRule nominalVoltageFilter5 = NumberExpertRule.builder().field(FieldType.NOMINAL_VOLTAGE)
                .operator(OperatorType.EQUALS).value(60.).build();
        rules5.add(nominalVoltageFilter5);
        CombinatorExpertRule parentRule5 = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(rules5).build();
        ExpertFilter generatorExpertFilter2 = new ExpertFilter(null, new Date(), EquipmentType.GENERATOR, parentRule5);

        Map<UUID, AbstractFilter> filtersToUpdateMap2 = Map.of(
                UUID.randomUUID(), lineExpertFilter,
                filterId2, generatorExpertFilter2
        );
        updateFiltersWithNoneExistingId(filtersToUpdateMap2);
        // check modified filters => filter with filterId2 should not be changed
        checkExpertFilter(filterId2, generatorExpertFilter);

        // --- delete filters in batch -- //
        deleteFilters(Stream.concat(sourceAndNewUuidMap.keySet().stream(), sourceAndNewUuidMap.values().stream()).toList());

        // check empty after delete all
        List<IFilterAttributes> allFilters = getAllFilters();
        Assertions.assertThat(allFilters).isEmpty();
    }

}
