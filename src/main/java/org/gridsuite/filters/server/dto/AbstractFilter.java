/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filters.server.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.gridsuite.filters.server.utils.FilterType;

/**
 * @author Jacques Borsenberger <jacques.borsenberger at rte-france.com>
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    property = "type",
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    visible = true
)
@JsonSubTypes({//Below, we define the names and the binding classes.
    @JsonSubTypes.Type(value = ScriptFilter.class, name = "SCRIPT"),
    @JsonSubTypes.Type(value = LineFilter.class, name = "LINE")
})
@ApiModel(description = "Basic class for Filters", subTypes = {ScriptFilter.class, LineFilter.class}, discriminator = "type")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public abstract class AbstractFilter {

    String name;

    public abstract FilterType getType();
}