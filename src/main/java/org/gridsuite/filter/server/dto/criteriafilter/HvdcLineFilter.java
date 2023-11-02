/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.server.dto.criteriafilter;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.gridsuite.filter.server.utils.EquipmentType;

import java.util.SortedSet;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
@Schema(description = "Hvdc Filters", allOf = AbstractLineFilter.class)
public class HvdcLineFilter extends AbstractLineFilter {
    @Override
    public EquipmentType getEquipmentType() {
        return EquipmentType.HVDC_LINE;
    }

    @Schema(description = "Nominal voltage")
    private NumericalFilter nominalVoltage;

    public HvdcLineFilter(String equipmentID, String equipmentName, String substationName1, String substationName2,
        SortedSet<String> countries1, SortedSet<String> countries2,
        NumericalFilter nominalVoltage) {
        super(equipmentID, equipmentName, substationName1, substationName2, countries1, countries2);
        this.nominalVoltage = nominalVoltage;
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty()
            && nominalVoltage == null;
    }
}
