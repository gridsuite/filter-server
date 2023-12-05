/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.server.utils;

/**
 * @author Etienne Homer <etienne.homer at rte-france.com>
 */
public enum EquipmentType {
    LINE("lines"),
    GENERATOR("generators"),
    LOAD("loads"),
    SHUNT_COMPENSATOR("shuntCompensators"),
    STATIC_VAR_COMPENSATOR("staticVarCompensators"),
    BATTERY("batteries"),
    BUS("buses"),
    BUSBAR_SECTION("busbarSections"),
    DANGLING_LINE("danglingLines"),
    LCC_CONVERTER_STATION("lccConverterStations"),
    VSC_CONVERTER_STATION("vscConverterStations"),
    TWO_WINDINGS_TRANSFORMER("twoWindingsTransformers"),
    THREE_WINDINGS_TRANSFORMER("threeWindingsTransformers"),
    HVDC_LINE("hvdcLines"),
    SUBSTATION("substations"),
    VOLTAGE_LEVEL("voltageLevels");

    private final String collectionName;

    EquipmentType(String collectionName) {
        this.collectionName = collectionName;
    }

    public String getCollectionName() {
        return collectionName;
    }
}
