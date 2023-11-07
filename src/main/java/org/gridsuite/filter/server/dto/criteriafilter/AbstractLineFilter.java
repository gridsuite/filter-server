/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.server.dto.criteriafilter;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.springframework.util.CollectionUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
@Schema(description = "Line Filters", allOf = CriteriaFilter.class)
public abstract class AbstractLineFilter extends AbstractEquipmentFilterForm {

    @Schema(description = "SubstationName1")
    String substationName1;

    @Schema(description = "SubstationName2")
    String substationName2;

    @Schema(description = "Countries1")
    private SortedSet<String> countries1;

    @Schema(description = "Countries2")
    private SortedSet<String> countries2;

    @Schema(description = "Free properties 1")
    // LinkedHashMap to keep order too
    @JsonDeserialize(as = LinkedHashMap.class)
    private Map<String, List<String>> freeProperties1;

    @Schema(description = "Free properties 2")
    // LinkedHashMap to keep order too
    @JsonDeserialize(as = LinkedHashMap.class)
    private Map<String, List<String>> freeProperties2;

    AbstractLineFilter(String equipmentID, String equipmentName, String substationName1, String substationName2, SortedSet<String> countries1, SortedSet<String> countries2) {
        super(equipmentID, equipmentName);
        this.substationName1 = substationName1;
        this.substationName2 = substationName2;
        this.countries1 = countries1;
        this.countries2 = countries2;
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty()
                && substationName1 == null
                && substationName2 == null
                && CollectionUtils.isEmpty(countries1)
                && CollectionUtils.isEmpty(countries2)
                && CollectionUtils.isEmpty(freeProperties1)
                && CollectionUtils.isEmpty(freeProperties2);
    }
}
