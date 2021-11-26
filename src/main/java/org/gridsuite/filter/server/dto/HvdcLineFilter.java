/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.server.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.gridsuite.filter.server.utils.EquipmentType;
import org.springframework.util.CollectionUtils;

import java.util.Set;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Schema(description = "Hvdc Filters", allOf = AbstractEquipmentFilterForm.class)
public class HvdcLineFilter extends AbstractEquipmentFilterForm {
    @Override
    public EquipmentType getEquipmentType() {
        return EquipmentType.HVDC_LINE;
    }

    @Schema(description = "SubstationName1")
    String substationName1;

    @Schema(description = "SubstationName2")
    String substationName2;

    @Schema(description = "Countries1")
    private Set<String> countries1;

    @Schema(description = "Countries2")
    private Set<String> countries2;

    @Schema(description = "Nominal voltage")
    private NumericalFilter nominalVoltage;

    public HvdcLineFilter(String equipmentID, String equipmentName, String substationName1, String substationName2, Set<String> countries1, Set<String> countries2, NumericalFilter nominalVoltage) {
        super(equipmentID, equipmentName);
        this.substationName1 =  substationName1;
        this.substationName2 =  substationName2;
        this.countries1 =  countries1;
        this.countries2 =  countries2;
        this.nominalVoltage = nominalVoltage;
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty()
            && substationName1 == null
            && substationName2 == null
            && CollectionUtils.isEmpty(countries1)
            && CollectionUtils.isEmpty(countries2)
            && nominalVoltage == null;
    }
}
