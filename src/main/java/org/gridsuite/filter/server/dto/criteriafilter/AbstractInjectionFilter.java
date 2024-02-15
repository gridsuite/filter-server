/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.server.dto.criteriafilter;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

import org.springframework.util.CollectionUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
@Schema(description = "Injection Filters", allOf = CriteriaFilter.class)
public abstract class AbstractInjectionFilter extends AbstractEquipmentFilterForm {
    @Schema(description = "SubstationName")
    String substationName;

    @Schema(description = "Countries")
    private SortedSet<String> countries;

    @Schema(description = "Substation free properties")
    // LinkedHashMap to keep order too
    @JsonDeserialize(as = LinkedHashMap.class)
    private Map<String, List<String>> substationFreeProperties;

    @Schema(description = "Free properties")
    @JsonDeserialize(as = LinkedHashMap.class)
    private Map<String, List<String>> freeProperties;

    @Schema(description = "Nominal voltage")
    private NumericalFilter nominalVoltage;

    AbstractInjectionFilter(InjectionFilterAttributes injectionFilterAttributes) {
        super(injectionFilterAttributes.getEquipmentID(), injectionFilterAttributes.getEquipmentName());
        this.substationName = injectionFilterAttributes.getSubstationName();
        this.countries = injectionFilterAttributes.getCountries();
        this.substationFreeProperties = injectionFilterAttributes.getSubstationFreeProperties();
        this.freeProperties = injectionFilterAttributes.getFreeProperties();
        this.nominalVoltage = injectionFilterAttributes.getNominalVoltage();
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty()
            && substationName == null
            && CollectionUtils.isEmpty(countries)
            && nominalVoltage == null
            && CollectionUtils.isEmpty(substationFreeProperties);
    }
}
