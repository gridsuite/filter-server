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

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
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
