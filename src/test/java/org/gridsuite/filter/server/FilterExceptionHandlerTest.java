/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.server;

import com.powsybl.ws.commons.error.PowsyblWsProblemDetail;
import org.gridsuite.filter.server.error.FilterBusinessErrorCode;
import org.gridsuite.filter.server.error.FilterException;
import org.gridsuite.filter.server.error.FilterExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Mohamed Ben-rejeb {@literal <mohamed.ben-rejeb at rte-france.com>}
 */
class FilterExceptionHandlerTest {

    private TestFilterExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new TestFilterExceptionHandler();
    }

    @Test
    void mapsBusinessErrorToStatus() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/filters");
        FilterException exception = new FilterException(
            FilterBusinessErrorCode.FILTER_CYCLE_DETECTED,
            "cycle",
            Map.of("filters", "A, B")
        );

        ResponseEntity<PowsyblWsProblemDetail> response = handler.invokeHandleDomainException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertEquals("filter.filterCycleDetected", response.getBody().getBusinessErrorCode());
        assertThat(response.getBody().getBusinessErrorValues()).containsEntry("filters", "A, B");
    }

    private static final class TestFilterExceptionHandler extends FilterExceptionHandler {

        private TestFilterExceptionHandler() {
            super(() -> "filter-server");
        }

        ResponseEntity<PowsyblWsProblemDetail> invokeHandleDomainException(FilterException exception, MockHttpServletRequest request) {
            return super.handleDomainException(exception, request);
        }
    }
}
