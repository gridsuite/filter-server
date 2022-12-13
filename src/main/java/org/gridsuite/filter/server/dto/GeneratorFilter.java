/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.server.dto;

import com.powsybl.iidm.network.EnergySource;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.gridsuite.filter.server.utils.EquipmentType;
import java.util.SortedSet;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@Getter
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
@Schema(description = "Generator Filters", allOf = AbstractInjectionFilter.class)
public class GeneratorFilter extends AbstractInjectionFilter {

    @Schema(description = "Energy source")
    EnergySource energySource;

    public GeneratorFilter(String equipmentID, String equipmentName, String substationName, SortedSet<String> countries, NumericalFilter nominalVoltage, EnergySource energySource) {
        super(new InjectionFilterAttributes(equipmentID, equipmentName, substationName, countries, nominalVoltage));
        this.energySource = energySource;
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty() && energySource == null;
    }

    @Override
    public EquipmentType getEquipmentType() {
        return EquipmentType.GENERATOR;
    }
}
