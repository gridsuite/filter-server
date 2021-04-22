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
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * @author Chamseddine benhamed <chamseddine.benhamed at rte-france.com>
 */

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@ApiModel(description = "filter contingency list attributes", parent = AbstractFilter.class)
public abstract class AbstractGenericFilter extends AbstractFilter {

    @ApiModelProperty("Equipment ID")
    private String equipmentID;

    @ApiModelProperty("Equipment name")
    private String equipmentName;

}
