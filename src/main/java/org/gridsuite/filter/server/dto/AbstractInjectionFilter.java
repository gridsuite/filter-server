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
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.springframework.util.CollectionUtils;

import java.util.SortedSet;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
@Schema(description = "Injection Filters", allOf = FormFilter.class)
public abstract class AbstractInjectionFilter extends AbstractEquipmentFilterForm {
    @Schema(description = "SubstationName")
    String substationName;

    @Schema(description = "Countries")
    private SortedSet<String> countries;

    @Schema(description = "Nominal voltage")
    private NumericalFilter nominalVoltage;

    AbstractInjectionFilter(String equipmentID, String equipmentName, String substationName, SortedSet<String> countries, NumericalFilter nominalVoltage) {
        super(equipmentID, equipmentName);
        this.substationName = substationName;
        this.countries = countries;
        this.nominalVoltage = nominalVoltage;
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty()
            && substationName == null
            && CollectionUtils.isEmpty(countries)
            && nominalVoltage == null;
    }
}
