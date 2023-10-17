/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.server.dto.expertrule;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.powsybl.commons.PowsyblException;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.gridsuite.filter.server.utils.DataType;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@SuperBuilder
public class BooleanExpertRule extends AbstractExpertRule {

    @Schema(description = "Value")
    private boolean value;

    private static boolean getBooleanValue(String value) {
        return Boolean.parseBoolean(value);
    }

    @Override
    public String getStringValue() {
        return String.valueOf(isValue());
    }

    @Override
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public DataType getDataType() {
        return DataType.BOOLEAN;
    }

    @Override
    public boolean evaluateRule(String identifiableValue) {
        boolean equipmentValue = getBooleanValue(identifiableValue);
        boolean filterValue = isValue();
        return switch (this.getOperator()) {
            case EQUALS -> equipmentValue == filterValue;
            case NOT_EQUALS -> equipmentValue != filterValue;
            default -> throw new PowsyblException("Operator not allowed");
        };
    }
}
