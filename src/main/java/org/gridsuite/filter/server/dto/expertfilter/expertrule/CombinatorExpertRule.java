/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.server.dto.expertfilter.expertrule;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Identifiable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.gridsuite.filter.server.FilterLoader;
import org.gridsuite.filter.server.dto.identifierlistfilter.FilterEquipments;
import org.gridsuite.filter.server.utils.expertfilter.CombinatorType;
import org.gridsuite.filter.server.utils.expertfilter.DataType;

import java.util.Map;
import java.util.UUID;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@AllArgsConstructor
@Getter
@SuperBuilder
public class CombinatorExpertRule extends AbstractExpertRule {

    @Override
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public DataType getDataType() {
        return DataType.COMBINATOR;
    }

    @Override
    public boolean evaluateRule(Identifiable<?> identifiable, FilterLoader filterLoader, Map<UUID, FilterEquipments> cachedUuidFilters) {
        // As long as there are rules, we go down the tree
        if (CombinatorType.AND == this.getCombinator()) {
            for (AbstractExpertRule rule : this.getRules()) {
                // Recursively evaluate the rule
                if (!rule.evaluateRule(identifiable, filterLoader, cachedUuidFilters)) {
                    // If any rule is false, the whole combination is false
                    return false;
                }
            }
            return true;
        } else if (CombinatorType.OR == this.getCombinator()) {
            for (AbstractExpertRule rule : this.getRules()) {
                // Recursively evaluate the rule
                if (rule.evaluateRule(identifiable, filterLoader, cachedUuidFilters)) {
                    // If any rule is true, the whole combination is true
                    return true;
                }
            }
            return false;
        } else {
            throw new PowsyblException(this.getCombinator() + " combinator type is not implemented with expert filter");
        }
    }

    @Override
    public String getStringValue() {
        return null;
    }
}
