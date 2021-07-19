/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.gridsuite.filter.server.utils.FilterType;
import org.springframework.util.CollectionUtils;

import java.util.Set;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@Getter
@NoArgsConstructor
@SuperBuilder
@ApiModel(description = "Three windings transformer Filters", parent = AbstractGenericFilter.class)
public class ThreeWindingsTransformerFilter extends AbstractGenericFilter {
    @Override
    public FilterType getType() {
        return FilterType.THREE_WINDINGS_TRANSFORMER;
    }

    @ApiModelProperty("SubstationName")
    String substationName;

    @ApiModelProperty("Countries")
    private Set<String> countries;

    @ApiModelProperty("Nominal voltage 1")
    private NumericalFilter nominalVoltage1;

    @ApiModelProperty("Nominal voltage 2")
    private NumericalFilter nominalVoltage2;

    @ApiModelProperty("Nominal voltage 3")
    private NumericalFilter nominalVoltage3;

    @Override
    public boolean isEmpty() {
        return super.isEmpty()
            && substationName == null
            && CollectionUtils.isEmpty(countries)
            && nominalVoltage1 == null
            && nominalVoltage2 == null
            && nominalVoltage3 == null;
    }
}
