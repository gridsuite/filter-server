/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.server.dto.expertfilter.expertrule;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Identifiable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.gridsuite.filter.server.utils.expertfilter.DataType;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.gridsuite.filter.server.utils.expertfilter.ExpertFilterUtils.getFieldValue;
import static org.gridsuite.filter.server.utils.expertfilter.OperatorType.isMultipleCriteriaOperator;

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

    @Schema(description = "Values")
    @JsonDeserialize(as = HashSet.class)
    private Set<Double> values;

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
            case IN -> this.getValues().contains(identifiableValue);
            case NOT_IN -> !this.getValues().contains(identifiableValue);
            default -> throw new PowsyblException(this.getOperator() + " operator not supported with " + this.getDataType() + " rule data type");
        };
    }

    @Override
    public String getStringValue() {
        if (isMultipleCriteriaOperator(this.getOperator())) { // multiple values
            return this.getValues().stream().map(String::valueOf).collect(Collectors.joining(","));
        } else { // single value or absence
            return this.getValue() != null ? String.valueOf(this.getValue()) : null;
        }
    }
}
