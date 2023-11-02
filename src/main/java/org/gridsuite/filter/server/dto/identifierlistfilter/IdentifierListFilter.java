/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.filter.server.dto.identifierlistfilter;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.gridsuite.filter.server.dto.AbstractFilter;
import org.gridsuite.filter.server.utils.EquipmentType;
import org.gridsuite.filter.server.utils.FilterType;

import java.util.*;
import java.util.stream.Collectors;


/**
 * @author Seddik Yengui <seddik.yengui at rte-france.com>
 */

@Getter
@Schema(description = "Identifier list Filters", allOf = AbstractFilter.class)
@SuperBuilder
@NoArgsConstructor
public class IdentifierListFilter extends AbstractFilter {

    private List<IdentifierListFilterEquipmentAttributes> filterEquipmentsAttributes;

    public IdentifierListFilter(UUID id,
                                Date modificationDate,
                                EquipmentType equipmentType,
                                List<IdentifierListFilterEquipmentAttributes> filterEquipmentsAttributes) {
        super(id, modificationDate, equipmentType);
        this.filterEquipmentsAttributes = filterEquipmentsAttributes;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Override
    public FilterType getType() {
        return FilterType.IDENTIFIER_LIST;
    }

    public Double getDistributionKey(String equipmentId) {
        return filterEquipmentsAttributes.stream()
                .filter(attribute -> attribute.getEquipmentID().equals(equipmentId))
                .findFirst()
                .map(IdentifierListFilterEquipmentAttributes::getDistributionKey)
                .orElse(null);
    }

    @Override
    public FilterEquipments getFilterEquipments(List<IdentifiableAttributes> identifiableAttributes) {
        // we keep the same order of the equipments in the filter
        List<String> notFound = new ArrayList<>();
        List<IdentifiableAttributes> orderedIdentifiableAttributes = filterEquipmentsAttributes.stream()
                .map(f -> identifiableAttributes.stream()
                        .filter(attribute -> Objects.equals(attribute.getId(), f.getEquipmentID()))
                        .findFirst()
                        .orElseGet(() -> {
                            notFound.add(f.getEquipmentID());
                            return null;
                        }))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return FilterEquipments.builder()
                .filterId(getId())
                .identifiableAttributes(orderedIdentifiableAttributes)
                .notFoundEquipments(notFound)
                .build();
    }
}
