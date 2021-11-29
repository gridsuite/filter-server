/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.gridsuite.filter.server.utils.FilterType;

import java.util.Date;
import java.util.UUID;

/**
 * @author Jacques Borsenberger <jacques.borsenberger at rte-france.com>
 */
@Getter
@Schema(description = "Script Filters", allOf = AbstractFilter.class)
@SuperBuilder
@NoArgsConstructor
public class ScriptFilter extends AbstractFilter {

    @Schema(description = "Script")
    private String script;

    public ScriptFilter(UUID id, Date creationDate, Date modificationDate, String script) {
        super(id, creationDate, modificationDate);
        this.script = script;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Override
    public FilterType getType() {
        return FilterType.SCRIPT;
    }

}
