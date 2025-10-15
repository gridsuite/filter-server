/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.powsybl.ws.commons.error.PowsyblWsProblemDetail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.client.HttpClientErrorException;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Mohamed Ben-rejeb {@literal <mohamed.ben-rejeb at rte-france.com>}
 */
class RestResponseEntityExceptionHandlerTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

    private TestRestResponseEntityExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new TestRestResponseEntityExceptionHandler();
    }

    @Test
    void mapsBusinessErrorToStatus() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/filters");
        FilterException exception = new FilterException(FilterBusinessErrorCode.FILTER_CYCLE_DETECTED, "cycle");

        ResponseEntity<PowsyblWsProblemDetail> response = handler.invokeHandleDomainException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertEquals("filter.filterCycleDetected", response.getBody().getBusinessErrorCode());
    }

    @Test
    void propagatesRemoteErrorDetails() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/filters/remote");
        PowsyblWsProblemDetail remote = PowsyblWsProblemDetail.builder(HttpStatus.INTERNAL_SERVER_ERROR)
            .server("directory")
            .businessErrorCode("directory.remoteError")
            .detail("Directory failure")
            .timestamp(Instant.parse("2025-04-10T00:00:00Z"))
            .path("/directory")
            .build();

        FilterException exception = new FilterException(FilterBusinessErrorCode.FILTER_REMOTE_ERROR, "wrap", remote);

        ResponseEntity<PowsyblWsProblemDetail> response = handler.invokeHandleDomainException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertEquals("directory.remoteError", response.getBody().getBusinessErrorCode());
        assertThat(response.getBody().getChain()).hasSize(1);
    }

    @Test
    void wrapsInvalidRemotePayloadWithDefaultCode() {
        MockHttpServletRequest request = new MockHttpServletRequest("DELETE", "/filters/remote");
        HttpClientErrorException exception = HttpClientErrorException.create(
            HttpStatus.BAD_GATEWAY,
            "bad gateway",
            null,
            "oops".getBytes(StandardCharsets.UTF_8),
            StandardCharsets.UTF_8
        );

        ResponseEntity<PowsyblWsProblemDetail> response = handler.invokeHandleRemoteException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertEquals("filter.remoteError", response.getBody().getBusinessErrorCode());
    }

    @Test
    void keepsRemoteStatusWhenPayloadValid() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/filters/remote");
        PowsyblWsProblemDetail remote = PowsyblWsProblemDetail.builder(HttpStatus.BAD_GATEWAY)
            .server("directory")
            .businessErrorCode("directory.remoteError")
            .detail("bad gateway")
            .timestamp(Instant.parse("2025-04-11T00:00:00Z"))
            .path("/directory")
            .build();

        byte[] payload = OBJECT_MAPPER.writeValueAsBytes(remote);
        HttpClientErrorException exception = HttpClientErrorException.create(HttpStatus.BAD_GATEWAY, "bad gateway",
            null, payload, StandardCharsets.UTF_8);

        ResponseEntity<PowsyblWsProblemDetail> response = handler.invokeHandleRemoteException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getChain()).hasSize(1);
    }

    private static final class TestRestResponseEntityExceptionHandler extends RestResponseEntityExceptionHandler {

        private TestRestResponseEntityExceptionHandler() {
            super(() -> "filter-server");
        }

        ResponseEntity<PowsyblWsProblemDetail> invokeHandleDomainException(FilterException exception, MockHttpServletRequest request) {
            return super.handleDomainException(exception, request);
        }

        ResponseEntity<PowsyblWsProblemDetail> invokeHandleRemoteException(HttpClientErrorException exception, MockHttpServletRequest request) {
            return super.handleRemoteException(exception, request);
        }
    }
}
