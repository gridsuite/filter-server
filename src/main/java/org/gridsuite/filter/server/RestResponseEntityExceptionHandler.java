/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.server;

import com.powsybl.ws.commons.error.AbstractBaseRestExceptionHandler;
import com.powsybl.ws.commons.error.ServerNameProvider;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;

/**
 * @author Mohamed Ben-rejeb {@literal <mohamed.ben-rejeb at rte-france.com>}
 */
@ControllerAdvice
public class RestResponseEntityExceptionHandler
    extends AbstractBaseRestExceptionHandler<FilterException, FilterBusinessErrorCode> {

    public RestResponseEntityExceptionHandler(ServerNameProvider serverNameProvider) {
        super(serverNameProvider);
    }

    @NotNull
    @Override
    protected FilterBusinessErrorCode getBusinessCode(FilterException ex) {
        return ex.getBusinessErrorCode();
    }

    @Override
    protected HttpStatus mapStatus(FilterBusinessErrorCode code) {
        return switch (code) {
            case FILTER_CYCLE_DETECTED -> HttpStatus.BAD_REQUEST;
            case FILTER_REMOTE_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
