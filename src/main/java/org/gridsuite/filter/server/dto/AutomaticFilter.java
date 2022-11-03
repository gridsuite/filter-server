/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.gridsuite.filter.server.utils.EquipmentType;
import org.gridsuite.filter.server.utils.FilterType;

import java.util.Date;
import java.util.UUID;

/**
 * @author Homer Etienne <etienne.homer at rte-france.com>
 */

@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@SuperBuilder
public class AutomaticFilter extends AbstractFilter {

    private AbstractEquipmentFilterForm equipmentFilterForm;

    public AutomaticFilter(UUID id, Date creationDate, Date modificationDate, AbstractEquipmentFilterForm equipmentFilterForm) {
        super(id, creationDate, modificationDate, equipmentFilterForm.getEquipmentType());
        this.equipmentFilterForm = equipmentFilterForm;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Override
    public FilterType getType() {
        return FilterType.AUTOMATIC;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Override
    public EquipmentType getEquipmentType() {
        return equipmentFilterForm.getEquipmentType();
    }

    public AbstractEquipmentFilterForm getEquipmentFilterForm() {
        return equipmentFilterForm;
    }
}
