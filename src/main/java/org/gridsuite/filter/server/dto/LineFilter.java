/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.gridsuite.filter.server.utils.EquipmentType;
import org.springframework.util.CollectionUtils;

import java.util.Set;

/**
 * @author Jacques Borsenberger <jacques.borsenberger at rte-france.com>
 */
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@Schema(description = "Line Filters", allOf = FormFilter.class)
public class LineFilter extends AbstractEquipmentFilterForm {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
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

    @Schema(description = "Nominal voltage 1")
    private NumericalFilter nominalVoltage1;

    @Schema(description = "Nominal voltage 2")
    private NumericalFilter nominalVoltage2;

    public LineFilter(String equipmentID, String equipmentName, String substationName1, String substationName2, Set<String> countries1, Set<String> countries2, NumericalFilter nominalVoltage1, NumericalFilter nominalVoltage2) {
        super(equipmentID, equipmentName);
        this.substationName1 =  substationName1;
        this.substationName2 =  substationName2;
        this.countries1 =  countries1;
        this.countries2 =  countries2;
        this.nominalVoltage1 =  nominalVoltage1;
        this.nominalVoltage2 =  nominalVoltage2;
    }

    public boolean isEmpty() {
        return super.isEmpty()
            && substationName1 == null
            && substationName2 == null
            && CollectionUtils.isEmpty(countries1)
            && CollectionUtils.isEmpty(countries2)
            && nominalVoltage1 == null
            && nominalVoltage2 == null;
    }
}
