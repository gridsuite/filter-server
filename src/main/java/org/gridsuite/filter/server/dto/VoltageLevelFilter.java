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
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.gridsuite.filter.server.utils.EquipmentType;
import org.springframework.util.CollectionUtils;

import java.util.SortedSet;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
@Schema(description = "Voltage level Filters", allOf = AutomaticFilter.class)
public class VoltageLevelFilter extends AbstractEquipmentFilterForm {
    @Override
    public EquipmentType getEquipmentType() {
        return EquipmentType.VOLTAGE_LEVEL;
    }

    @Schema(description = "Countries")
    private SortedSet<String> countries;

    @Schema(description = "Nominal voltage")
    private NumericalFilter nominalVoltage;

    public VoltageLevelFilter(String equipmentID, String equipmentName, SortedSet<String> countries, NumericalFilter nominalVoltage) {
        super(equipmentID, equipmentName);
        this.countries = countries;
        this.nominalVoltage =  nominalVoltage;
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty()
            && CollectionUtils.isEmpty(countries)
            && nominalVoltage == null;
    }
}
