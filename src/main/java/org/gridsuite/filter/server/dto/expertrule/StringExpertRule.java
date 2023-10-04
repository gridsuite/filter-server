/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
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
public class StringExpertRule extends AbstractExpertRule {

    @Schema(description = "Value")
    private String value;

    @Override
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public DataType getDataType() {
        return DataType.STRING;
    }

    @Override
    public boolean evaluateRule(String identifiableValue) {
        return switch (this.getOperator()) {
            case IS -> identifiableValue.equals(this.getValue());
            case CONTAINS -> identifiableValue.contains(this.getValue());
            case BEGINS_WITH -> identifiableValue.startsWith(this.getValue());
            case ENDS_WITH -> identifiableValue.endsWith(this.getValue());
            default -> throw new PowsyblException("Operator not allowed");
        };
    }

    @Override
    public String getStringValue() {
        return getValue();
    }
}
