/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.server.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.gridsuite.filter.server.utils.EquipmentType;
import org.springframework.util.CollectionUtils;

import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
@Schema(description = "Three windings transformer Filters", allOf = CriteriaFilter.class)
public class ThreeWindingsTransformerFilter extends AbstractEquipmentFilterForm {
    @Override
    public EquipmentType getEquipmentType() {
        return EquipmentType.THREE_WINDINGS_TRANSFORMER;
    }

    @Schema(description = "SubstationName")
    String substationName;

    @Schema(description = "Countries")
    private SortedSet<String> countries;

    @Schema(description = "Free properties")
    private Map<String, Set<String>> freeProperties;

    @Schema(description = "Nominal voltage 1")
    private NumericalFilter nominalVoltage1;

    @Schema(description = "Nominal voltage 2")
    private NumericalFilter nominalVoltage2;

    @Schema(description = "Nominal voltage 3")
    private NumericalFilter nominalVoltage3;

    @Override
    public boolean isEmpty() {
        return super.isEmpty()
            && substationName == null
            && CollectionUtils.isEmpty(countries)
            && nominalVoltage1 == null
            && nominalVoltage2 == null
            && nominalVoltage3 == null
            && CollectionUtils.isEmpty(freeProperties);
    }
}
