/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.server.dto.expertfilter.expertrule;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Identifiable;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.gridsuite.filter.server.dto.identifierlistfilter.FilterEquipments;
import org.gridsuite.filter.server.utils.expertfilter.DataType;
import org.gridsuite.filter.server.utils.expertfilter.ExpertFilterUtils;
import org.gridsuite.filter.server.FilterLoader;

import java.util.Map;
import java.util.UUID;

import static org.gridsuite.filter.server.utils.expertfilter.ExpertFilterUtils.getFieldValue;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@AllArgsConstructor
@SuperBuilder
public class FilterUuidExpertRule extends StringExpertRule {

    @Override
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public DataType getDataType() {
        return DataType.FILTER_UUID;
    }

    @Override
    public boolean evaluateRule(Identifiable<?> identifiable, FilterLoader filterLoader, Map<UUID, FilterEquipments> cachedUuidFilters) {
        String identifiableValue = getFieldValue(this.getField(), identifiable);
        return switch (this.getOperator()) {
            case IS_PART_OF -> ExpertFilterUtils.isPartOf(identifiable.getNetwork(), identifiableValue, this.getValues(), filterLoader, cachedUuidFilters);
            case IS_NOT_PART_OF -> !ExpertFilterUtils.isPartOf(identifiable.getNetwork(), identifiableValue, this.getValues(), filterLoader, cachedUuidFilters);
            default -> throw new PowsyblException(this.getOperator() + " operator not supported with " + this.getDataType() + " rule data type");
        };
    }
}
