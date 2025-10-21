/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.server;

import com.powsybl.ws.commons.error.BusinessErrorCode;

/**
 * @author Mohamed Ben-rejeb {@literal <mohamed.ben-rejeb at rte-france.com>}
 *
 * Business error codes emitted by the filter service.
 */
public enum FilterBusinessErrorCode implements BusinessErrorCode {
    FILTER_CYCLE_DETECTED("filter.filterCycleDetected");
    private final String code;

    FilterBusinessErrorCode(String code) {
        this.code = code;
    }

    public String value() {
        return code;
    }
}
