/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.server;

import com.powsybl.ws.commons.error.PowsyblWsProblemDetail;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Mohamed Ben-rejeb {@literal <mohamed.ben-rejeb at rte-france.com>}
 */
class FilterExceptionTest {

    @Test
    void exposesErrorCodeAndRemoteError() {
        PowsyblWsProblemDetail remote = PowsyblWsProblemDetail.builder(HttpStatus.BAD_GATEWAY)
            .server("downstream")
            .detail("failure")
            .timestamp(Instant.parse("2025-04-01T00:00:00Z"))
            .path("/remote")
            .build();

        FilterException exception = new FilterException(FilterBusinessErrorCode.FILTER_REMOTE_ERROR,
            "Wrapped", remote);

        assertThat(exception.getErrorCode()).contains(FilterBusinessErrorCode.FILTER_REMOTE_ERROR);
        assertThat(exception.getBusinessErrorCode()).contains(FilterBusinessErrorCode.FILTER_REMOTE_ERROR);
        assertThat(exception.getRemoteError()).contains(remote);
    }

    @Test
    void defaultConstructorStoresMessage() {
        FilterException exception = new FilterException(FilterBusinessErrorCode.FILTER_CYCLE_DETECTED, "cycle");
        assertThat(exception.getMessage()).isEqualTo("cycle");
        assertThat(exception.getRemoteError()).isEmpty();
    }
}
