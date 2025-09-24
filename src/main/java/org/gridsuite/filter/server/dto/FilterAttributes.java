/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.filter.server.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.gridsuite.filter.IFilterAttributes;
import org.gridsuite.filter.server.repositories.FilterMetadata;
import org.gridsuite.filter.utils.EquipmentType;
import org.gridsuite.filter.utils.FilterType;

import java.util.Date;
import java.util.UUID;

/**
 * @author Jacques Borsenberger <jacques.borsenberger at rte-france.com>
 */

@Getter
@Setter
@NoArgsConstructor
public class FilterAttributes implements IFilterAttributes {
    UUID id;
    Date modificationDate;
    FilterType type;
    EquipmentType equipmentType;
    String name;

    public FilterAttributes(FilterMetadata filterMetadata, FilterType type, EquipmentType equipmentType) {
        id = filterMetadata.getId();
        modificationDate = filterMetadata.getModificationDate();
        this.type = type;
        this.equipmentType = equipmentType;
    }
}
