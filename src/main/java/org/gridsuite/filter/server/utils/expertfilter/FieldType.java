/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.server.utils.expertfilter;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
public enum FieldType {
    ID,
    NAME,
    NOMINAL_VOLTAGE,
    MIN_P,
    MAX_P,
    TARGET_P,
    TARGET_V,
    TARGET_Q,
    ENERGY_SOURCE,
    COUNTRY,
    VOLTAGE_REGULATOR_ON,
    PLANNED_ACTIVE_POWER_SET_POINT,
    VOLTAGE_LEVEL_ID,
    CONNECTED,
    RATED_S,
    MARGINAL_COST,
    PLANNED_OUTAGE_RATE,
    FORCED_OUTAGE_RATE,
    P0,
    Q0,
    LOW_VOLTAGE_LIMIT,
    HIGH_VOLTAGE_LIMIT,
    NOMINAL_V_1,
    NOMINAL_V_2,
    R,
    X,
    G,
    B,
    CONNECTED_1,
    CONNECTED_2,
    VOLTAGE_LEVEL_ID_1,
    VOLTAGE_LEVEL_ID_2,
    LOAD_TAP_CHANGING_CAPABILITIES,
    RATIO_REGULATING,
    RATIO_TARGET_V,
}
