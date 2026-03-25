/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.server;

import com.powsybl.network.store.client.NetworkStoreService;
import org.gridsuite.filter.exception.FilterCycleException;
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

    @Autowired
    private FilterService filterService;

    @BeforeEach
    void setUp() {
        filterService = new FilterService(repositoryService, networkStoreService, notificationService);
    }

    @Test
    void getCyclicFilterNamesUsesResolvedIdsAndIds() throws Exception {
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        UUID third = UUID.randomUUID();
        List<UUID> cycle = List.of(first, second, third);

        Map<String, Object> properties = invokeGetCyclicFilterIds(cycle);

        assertThat(properties)
            .containsEntry("filters", first + " -> " + second + " -> " + third);
    }

    @Test
    void getCyclicFilterIdsReturnsEmptyMapWhenCycleMissing() throws Exception {
        Map<String, Object> properties = invokeGetCyclicFilterIds(List.of());

        assertThat(properties).isEmpty();
    }

    private Map<String, Object> invokeGetCyclicFilterIds(List<UUID> cycle) throws Exception {
        FilterCycleException exception = new FilterCycleException("cycle", cycle);
        Method helper = FilterService.class.getDeclaredMethod("getCyclicFilterIds", FilterCycleException.class);
        helper.setAccessible(true);
        try {
            return (Map<String, Object>) helper.invoke(filterService, exception);
        } catch (InvocationTargetException targetException) {
            Throwable cause = targetException.getCause();
            throw (Exception) cause;
        }
    }
}
