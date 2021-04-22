/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filters.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.gridsuite.filters.server.utils.FilterType;

import java.util.Set;

/**
 * @author Chamseddine benhamed <chamseddine.benhamed at rte-france.com>
 */
@Getter
@NoArgsConstructor
@SuperBuilder
@ApiModel(description = "Line Filters", parent = AbstractGenericFilter.class)
public class LineFilter extends AbstractGenericFilter {
    @Override
    public FilterType getType() {
        return FilterType.LINE;
    }

    @ApiModelProperty("SubstationName1")
    String substationName1;

    @ApiModelProperty("SubstationName2")
    String substationName2;

    @ApiModelProperty("Countries1")
    private Set<String> countries1;

    @ApiModelProperty("Countries2")
    private Set<String> countries2;

    @ApiModelProperty("Nominal voltage 1")
    private NumericalFilter nominalVoltage1;

    @ApiModelProperty("Nominal voltage 2")
    private NumericalFilter nominalVoltage2;

}
