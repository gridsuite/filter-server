/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
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
import org.gridsuite.filter.server.utils.EquipmentType;
import org.gridsuite.filter.server.utils.FilterType;

import java.util.Date;
import java.util.List;
import java.util.UUID;


/**
 * @author Seddik Yengui <seddik.yengui at rte-france.com>
 */

@Getter
@Schema(description = "Manual Filters", allOf = AbstractFilter.class)
@SuperBuilder
@NoArgsConstructor
public class ManualFilter extends AbstractFilter {

    private List<ManualFilterEquipmentAttributes> filterEquipmentsAttributes;

    public ManualFilter(UUID id,
                        Date modificationDate,
                        EquipmentType equipmentType,
                        List<ManualFilterEquipmentAttributes> filterEquipmentsAttributes) {
        super(id, modificationDate, equipmentType);
        this.filterEquipmentsAttributes = filterEquipmentsAttributes;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Override
    public FilterType getType() {
        return FilterType.MANUAL;
    }

    public Double getDistributionKey(String equipmentId) {
        return filterEquipmentsAttributes.stream()
                .filter(attribute -> attribute.getEquipmentID().equals(equipmentId))
                .findFirst()
                .map(ManualFilterEquipmentAttributes::getDistributionKey)
                .orElse(null);
    }
}
