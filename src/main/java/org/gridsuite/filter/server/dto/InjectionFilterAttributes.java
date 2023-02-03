/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.filter.server.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

@AllArgsConstructor
@Getter
public class InjectionFilterAttributes {
    @Schema(description = "Equipment ID")
    private String equipmentID;

    @Schema(description = "Equipment name")
    private String equipmentName;

    @Schema(description = "SubstationName")
    String substationName;

    @Schema(description = "Countries")
    private SortedSet<String> countries;

    @Schema(description = "Free properties")
    private Map<String, Set<String>> freeProperties;

    @Schema(description = "Nominal voltage")
    private NumericalFilter nominalVoltage;
}
