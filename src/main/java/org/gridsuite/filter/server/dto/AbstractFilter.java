/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.server.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.gridsuite.filter.server.utils.FilterType;

import java.util.Date;
import java.util.UUID;

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
public abstract class AbstractFilter implements IFilterAttributes {

    UUID id;

    String name;

    Date creationDate;

    Date modificationDate;

    String description;

    public abstract FilterType getType();
}