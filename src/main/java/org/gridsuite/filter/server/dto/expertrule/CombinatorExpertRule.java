/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.server.dto.expertrule;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Injection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.gridsuite.filter.server.utils.CombinatorType;
import org.gridsuite.filter.server.utils.DataType;

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
    public boolean evaluateRule(Injection<?> injection) {
        if (CombinatorType.OR == this.getCombinator()) {
            for (AbstractExpertRule rule : this.getRules()) {
                // Recursively evaluate the rule
                if (rule.evaluateRule(injection)) {
                    // If any rule is true, the whole combination is true
                    return true;
                }
            }
            return false;
        } else if (CombinatorType.AND == (this.getCombinator())) {
            for (AbstractExpertRule rule : this.getRules()) {
                // Recursively evaluate the rule
                if (!rule.evaluateRule(injection)) {
                    // If any rule is false, the whole combination is false
                    return false;
                }
            }
            return true;
        } else {
            throw new PowsyblException("Not support combinator: " + this.getCombinator());
        }
    }

    @Override
    public String getStringValue() {
        return null;
    }
}
