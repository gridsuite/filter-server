/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.server.dto.expertrule;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Identifiable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.gridsuite.filter.server.utils.DataType;

import static org.gridsuite.filter.server.utils.ExpertFilterUtils.getFieldValue;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@SuperBuilder
public class NumberExpertRule extends AbstractExpertRule {

    @Schema(description = "Value")
    private Double value;

    public static Double getNumberValue(String value) {
        return Double.parseDouble(value);
    }

    @Override
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public DataType getDataType() {
        return DataType.NUMBER;
    }

    @Override
    public boolean evaluateRule(Identifiable<?> identifiable) {
        Double identifiableValue = getNumberValue(getFieldValue(this.getField(), identifiable));
        Double filterValue = this.getValue();
        return switch (this.getOperator()) {
            case EQUALS -> identifiableValue.equals(filterValue);
            case GREATER_OR_EQUALS -> identifiableValue.compareTo(filterValue) >= 0;
            case GREATER -> identifiableValue.compareTo(filterValue) > 0;
            case LOWER_OR_EQUALS -> identifiableValue.compareTo(filterValue) <= 0;
            case LOWER -> identifiableValue.compareTo(filterValue) < 0;
            default -> throw new PowsyblException(this.getOperator() + " operator not supported with " + this.getDataType() + " rule data type");
        };
    }

    @Override
    public String getStringValue() {
        return String.valueOf(this.getValue());
    }
}
