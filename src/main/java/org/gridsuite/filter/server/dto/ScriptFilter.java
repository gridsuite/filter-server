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

/**
 * @author Jacques Borsenberger <jacques.borsenberger at rte-france.com>
 */
@Getter
@ApiModel(description = "Script Filters", parent = AbstractFilter.class)
@SuperBuilder
@NoArgsConstructor
public class ScriptFilter extends AbstractFilter {

    @ApiModelProperty("Script")
    private String script;

    @Override
    public FilterType getType() {
        return FilterType.SCRIPT;
    }
}
