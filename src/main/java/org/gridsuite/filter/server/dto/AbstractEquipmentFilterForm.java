/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.server.dto;

import com.fasterxml.jackson.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.gridsuite.filter.server.utils.EquipmentType;

/**
 * @author Etienne Homer <homer.etienne at rte-france.com>
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "equipmentType",
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        visible = true
)
@JsonSubTypes({//Below, we define the names and the binding classes.
        @JsonSubTypes.Type(value = LineFilter.class, name = "LINE"),
        @JsonSubTypes.Type(value = GeneratorFilter.class, name = "GENERATOR"),
        @JsonSubTypes.Type(value = LoadFilter.class, name = "LOAD"),
        @JsonSubTypes.Type(value = ShuntCompensatorFilter.class, name = "SHUNT_COMPENSATOR"),
        @JsonSubTypes.Type(value = StaticVarCompensatorFilter.class, name = "STATIC_VAR_COMPENSATOR"),
        @JsonSubTypes.Type(value = BatteryFilter.class, name = "BATTERY"),
        @JsonSubTypes.Type(value = BusBarSectionFilter.class, name = "BUSBAR_SECTION"),
        @JsonSubTypes.Type(value = DanglingLineFilter.class, name = "DANGLING_LINE"),
        @JsonSubTypes.Type(value = LccConverterStationFilter.class, name = "LCC_CONVERTER_STATION"),
        @JsonSubTypes.Type(value = VscConverterStationFilter.class, name = "VSC_CONVERTER_STATION"),
        @JsonSubTypes.Type(value = TwoWindingsTransformerFilter.class, name = "TWO_WINDINGS_TRANSFORMER"),
        @JsonSubTypes.Type(value = ThreeWindingsTransformerFilter.class, name = "THREE_WINDINGS_TRANSFORMER"),
        @JsonSubTypes.Type(value = HvdcLineFilter.class, name = "HVDC_LINE")
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString
public abstract class AbstractEquipmentFilterForm {

    @Schema(description = "Equipment ID")
    private String equipmentID;

    @Schema(description = "Equipment name")
    private String equipmentName;

    public abstract EquipmentType getEquipmentType();

    @JsonIgnore
    protected boolean isEmpty() {
        return equipmentID == null && equipmentName == null;
    }
}
