/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.server.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.gridsuite.filter.server.utils.EquipmentType;
import org.springframework.util.CollectionUtils;

import java.util.Map;
import java.util.Set;

/**
 * @author Jacques Borsenberger <jacques.borsenberger at rte-france.com>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
@Schema(description = "Line Filters", allOf = CriteriaFilter.class)
public class LineFilter extends AbstractEquipmentFilterForm {
    public EquipmentType getEquipmentType() {
        return EquipmentType.LINE;
    }

    @Schema(description = "SubstationName1")
    String substationName1;

    @Schema(description = "SubstationName2")
    String substationName2;

    @Schema(description = "Countries1")
    private Set<String> countries1;

    @Schema(description = "Countries2")
    private Set<String> countries2;

    @Schema(description = "Free properties 1")
    private Map<String, Set<String>> freeProperties1;

    @Schema(description = "Free properties 2")
    private Map<String, Set<String>> freeProperties2;

    @Schema(description = "Nominal voltage 1")
    private NumericalFilter nominalVoltage1;

    @Schema(description = "Nominal voltage 2")
    private NumericalFilter nominalVoltage2;

    @Override
    public boolean isEmpty() {
        return super.isEmpty()
            && substationName1 == null
            && substationName2 == null
            && CollectionUtils.isEmpty(countries1)
            && CollectionUtils.isEmpty(countries2)
            && CollectionUtils.isEmpty(freeProperties1)
            && CollectionUtils.isEmpty(freeProperties2)
            && nominalVoltage1 == null
            && nominalVoltage2 == null;
    }
}
