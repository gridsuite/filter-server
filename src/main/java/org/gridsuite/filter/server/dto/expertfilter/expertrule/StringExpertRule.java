/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.server.dto.expertfilter.expertrule;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Identifiable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.StringUtils;
import org.gridsuite.filter.server.utils.expertfilter.DataType;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.gridsuite.filter.server.utils.expertfilter.ExpertFilterUtils.getFieldValue;

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

    // a derived set of values in case multiple
    private Set<String> values;

    private Set<String> getValues() {
        // lazy initialization
        if (values == null) {
            values = Stream.of(this.value.trim().split(","))
                    .collect(Collectors.toSet());
        }
        return values;
    }

    @Override
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public DataType getDataType() {
        return DataType.STRING;
    }

    @Override
    public boolean evaluateRule(Identifiable<?> identifiable) {
        String identifiableValue = getFieldValue(this.getField(), identifiable);
        return switch (this.getOperator()) {
            case IS -> identifiableValue.equalsIgnoreCase(this.getValue());
            case CONTAINS -> StringUtils.containsIgnoreCase(identifiableValue, this.getValue());
            case BEGINS_WITH -> StringUtils.startsWithIgnoreCase(identifiableValue, this.getValue());
            case ENDS_WITH -> StringUtils.endsWithIgnoreCase(identifiableValue, this.getValue());
            case IN -> this.getValues().contains(identifiableValue);
            case NOT_IN -> !this.getValues().contains(identifiableValue);
            default -> throw new PowsyblException(this.getOperator() + " operator not supported with " + this.getDataType() + " rule data type");
        };
    }

    @Override
    public String getStringValue() {
        return getValue();
    }
}
