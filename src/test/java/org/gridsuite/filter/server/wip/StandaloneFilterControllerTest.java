/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.server.wip;

import org.gridsuite.filter.server.FilterApi;
import org.gridsuite.filter.server.FilterApplication;
import org.gridsuite.filter.utils.EquipmentType;
import org.gridsuite.filter.utils.expertfilter.CombinatorType;
import org.gridsuite.filter.wip.ExpertFilter;
import org.gridsuite.filter.wip.Filter;
import org.gridsuite.filter.wip.IdentifierListFilter;
import org.gridsuite.filter.wip.rule.CombinatorExpertRule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Mohamed Ben Rejeb {@literal <mohamed.ben-rejeb at rte-france.com>}
 */
@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(classes = {FilterApplication.class, TestChannelBinderConfiguration.class})
class StandaloneFilterControllerTest {

    static final String URL = "/" + FilterApi.API_VERSION + "/standalone-filters";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StandaloneFilterService standaloneFilterService;

    @Test
    void getFilterWhenIdentifierListFilterExistsReturnsOk() throws Exception {
        // Arrange
        UUID id = UUID.randomUUID();
        when(standaloneFilterService.getFilter(id)).thenReturn(Optional.of(
                IdentifierListFilter.builder().equipmentType(EquipmentType.GENERATOR).equipmentIds(Set.of("GEN1")).build()));

        // Act & Assert
        mockMvc.perform(get(URL + "/" + id).contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filterType").value("IDENTIFIER_LIST"))
                .andExpect(jsonPath("$.equipmentType").value("GENERATOR"));

        verify(standaloneFilterService).getFilter(id);
    }

    @Test
    void getFilterWhenExpertFilterExistsReturnsOk() throws Exception {
        // Arrange
        UUID id = UUID.randomUUID();
        when(standaloneFilterService.getFilter(id)).thenReturn(Optional.of(
                ExpertFilter.builder().equipmentType(EquipmentType.LINE)
                        .rule(CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(List.of()).build())
                        .build()));

        // Act & Assert
        mockMvc.perform(get(URL + "/" + id).contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filterType").value("EXPERT"))
                .andExpect(jsonPath("$.equipmentType").value("LINE"));

        verify(standaloneFilterService).getFilter(id);
    }

    @Test
    void getFilterWhenNotFoundReturns404() throws Exception {
        // Arrange
        UUID id = UUID.randomUUID();
        when(standaloneFilterService.getFilter(id)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get(URL + "/" + id).contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(standaloneFilterService).getFilter(id);
    }

    @Test
    void getFiltersWhenFiltersExistReturnsOkWithAllFilters() throws Exception {
        // Arrange
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        List<Filter> filters = List.of(
                IdentifierListFilter.builder().equipmentType(EquipmentType.LINE).equipmentIds(Set.of("L1")).build(),
                ExpertFilter.builder().equipmentType(EquipmentType.GENERATOR)
                        .rule(CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(List.of()).build()).build()
        );
        when(standaloneFilterService.getFilters(List.of(id1, id2))).thenReturn(filters);

        // Act & Assert
        mockMvc.perform(get(URL)
                        .param("ids", id1.toString())
                        .param("ids", id2.toString())
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(standaloneFilterService).getFilters(List.of(id1, id2));
    }

    @Test
    void getFiltersWhenNoFiltersFoundReturnsOkWithEmptyList() throws Exception {
        // Arrange
        UUID id = UUID.randomUUID();
        when(standaloneFilterService.getFilters(List.of(id))).thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get(URL)
                        .param("ids", id.toString())
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(standaloneFilterService).getFilters(List.of(id));
    }
}
