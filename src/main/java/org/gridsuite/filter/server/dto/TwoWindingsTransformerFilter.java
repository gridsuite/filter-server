/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.server.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.gridsuite.filter.server.entities.NumericFilterEntity;
import org.gridsuite.filter.server.utils.EquipmentType;
import org.springframework.util.CollectionUtils;

import java.util.Set;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@Getter
@NoArgsConstructor
@SuperBuilder
@Schema(description = "Two windings transformer Filters", allOf = FormFilter.class)
public class TwoWindingsTransformerFilter extends AbstractEquipmentFilterForm {
    @Override
    public EquipmentType getEquipmentType() {
        return EquipmentType.TWO_WINDINGS_TRANSFORMER;
    }

    @Schema(description = "SubstationName")
    String substationName;

    @Schema(description = "Countries")
    private Set<String> countries;

    @Schema(description = "Nominal voltage 1")
    private NumericalFilter nominalVoltage1;

    @Schema(description = "Nominal voltage 2")
    private NumericalFilter nominalVoltage2;

    public TwoWindingsTransformerFilter(String equipmentID, String equipmentName, String substationName, Set<String> countries, NumericFilterEntity nominalVoltage1, NumericFilterEntity nominalVoltage2) {
        super(equipmentID, equipmentName);
        this.substationName =  substationName;
        this.countries =  countries;
        this.nominalVoltage1 =  NumericalFilter.builder().type(nominalVoltage1.getFilterType()).value1(nominalVoltage1.getValue1()).value2(nominalVoltage1.getValue2()).build();
        this.nominalVoltage2 =  NumericalFilter.builder().type(nominalVoltage2.getFilterType()).value1(nominalVoltage2.getValue1()).value2(nominalVoltage2.getValue2()).build();
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty()
            && substationName == null
            && CollectionUtils.isEmpty(countries)
            && nominalVoltage1 == null
            && nominalVoltage2 == null;
    }
}
