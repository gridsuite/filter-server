/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.server.dto.criteriafilter;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.gridsuite.filter.server.utils.EquipmentType;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@Getter
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
@Schema(description = "Lcc converter station Filters", allOf = AbstractInjectionFilter.class)
public class LccConverterStationFilter extends AbstractInjectionFilter {
    public LccConverterStationFilter(InjectionFilterAttributes injectionFilterAttributes) {
        super(injectionFilterAttributes);
    }

    @Override
    public EquipmentType getEquipmentType() {
        return EquipmentType.LCC_CONVERTER_STATION;
    }
}
