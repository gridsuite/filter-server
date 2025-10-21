/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.server.error;

import com.powsybl.ws.commons.error.AbstractBusinessException;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * /**
 * @author Mohamed Ben-rejeb {@literal <mohamed.ben-rejeb at rte-france.com>}
 *
 * Filter server specific runtime exception enriched with a business error code.
 */
public class FilterException extends AbstractBusinessException {

    private final FilterBusinessErrorCode errorCode;

    public FilterException(FilterBusinessErrorCode errorCode, String message) {
        super(Objects.requireNonNull(message, "message must not be null"));
        this.errorCode = Objects.requireNonNull(errorCode, "errorCode must not be null");
    }

    @NotNull
    @Override
    public FilterBusinessErrorCode getBusinessErrorCode() {
        return errorCode;
    }
}
