/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.server;

import com.powsybl.network.store.client.NetworkStoreService;
import org.gridsuite.filter.api.FilterEvaluator;
import org.gridsuite.filter.exception.FilterCycleException;
import org.gridsuite.filter.server.error.FilterException;
import org.gridsuite.filter.server.service.DirectoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * @author Mohamed Ben-rejeb {@literal <mohamed.ben-rejeb at rte-france.com>}
 */
@SpringBootTest
class FilterServiceTest {

    @Mock
    private RepositoryService repositoryService;
    @Mock
    private NetworkStoreService networkStoreService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private DirectoryService directoryService;
    @Mock
    private FilterEvaluator filterEvaluator;

    @Autowired
    private FilterService filterService;

    @BeforeEach
    void setUp() {
        filterService = new FilterService(repositoryService, networkStoreService, notificationService, directoryService, filterEvaluator);
    }

    @Test
    void getCyclicFilterNamesUsesResolvedNamesAndIds() throws Exception {
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        UUID third = UUID.randomUUID();
        List<UUID> cycle = List.of(first, second, third);
        when(directoryService.getElementsName(cycle, "user-id")).thenReturn(Map.of(
            first, "A",
            second, "B",
            third, "A"
        ));

        Map<String, Object> properties = invokeGetCyclicFilterNames(cycle);

        assertThat(properties)
            .containsEntry("filters", "A -> B -> A");
    }

    @Test
    void getCyclicFilterNamesReturnsEmptyMapWhenCycleMissing() throws Exception {
        Map<String, Object> properties = invokeGetCyclicFilterNames(List.of());

        assertThat(properties).isEmpty();
        verify(directoryService, never()).getElementsName(anyList(), anyString());
    }

    @Test
    void getCyclicFilterNamesFallsBackWhenDirectoryLookupFails() {
        UUID first = UUID.randomUUID();
        List<UUID> cycle = List.of(first);
        when(directoryService.getElementsName(cycle, "user-id")).thenThrow(new IllegalStateException("exp"));

        assertThatThrownBy(() -> invokeGetCyclicFilterNames(cycle))
            .isInstanceOf(FilterException.class)
            .hasMessage("cycle");
    }

    private Map<String, Object> invokeGetCyclicFilterNames(List<UUID> cycle) throws Exception {
        FilterCycleException exception = new FilterCycleException("cycle", cycle);
        Method helper = FilterService.class.getDeclaredMethod("getCyclicFilterNames", String.class, FilterCycleException.class);
        helper.setAccessible(true);
        try {
            return (Map<String, Object>) helper.invoke(filterService, "user-id", exception);
        } catch (InvocationTargetException targetException) {
            Throwable cause = targetException.getCause();
            throw (Exception) cause;
        }
    }
}
