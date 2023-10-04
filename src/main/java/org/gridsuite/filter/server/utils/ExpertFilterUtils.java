/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.filter.server.utils;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import org.gridsuite.filter.server.dto.expertrule.AbstractExpertRule;

import java.util.Optional;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
public final class ExpertFilterUtils {
    private ExpertFilterUtils() { }

    public static <I extends Injection<I>> boolean evaluateExpertFilter(AbstractExpertRule filter, Injection<I> injection) {
        // As long as there are rules, we go down the tree
        if (CombinatorType.AND == (filter.getCombinator())) {
            return evaluateAndCombination(filter, injection);
        } else if (CombinatorType.OR == filter.getCombinator()) {
            return evaluateOrCombination(filter, injection);
        } else {
            // Evaluate individual filters
            return filter.evaluateRule(getFieldValue(filter.getField(), injection));
        }
    }

    private static <I extends Injection<I>> boolean evaluateOrCombination(AbstractExpertRule filter, Injection<I> injection) {
        for (AbstractExpertRule rule : filter.getRules()) {
            // Recursively evaluate the rule
            if (evaluateExpertFilter(rule, injection)) {
                // If any rule is true, the whole combination is true
                return true;
            }
        }
        return false;
    }

    private static <I extends Injection<I>> boolean evaluateAndCombination(AbstractExpertRule filter, Injection<I> injection) {
        for (AbstractExpertRule rule : filter.getRules()) {
            // Recursively evaluate the rule
            if (!evaluateExpertFilter(rule, injection)) {
                // If any rule is false, the whole combination is false
                return false;
            }
        }
        return true;
    }

    private static <I extends Injection<I>> String getFieldValue(FieldType field, Injection<I> injection) {
        return switch (injection.getType()) {
            case GENERATOR -> getGeneratorFieldValue(field, (Generator) injection);
            case LOAD -> getLoadFieldValue(field, (Load) injection);
            default -> throw new PowsyblException("Not implemented with expert filter");
        };
    }

    private static String getLoadFieldValue(FieldType field, Load load) {
        return switch (field) {
            case ID -> load.getId();
            default -> throw new PowsyblException("Not implemented with expert filter");
        };
    }

    private static String getGeneratorFieldValue(FieldType field, Generator generator) {
        return switch (field) {
            case ID -> generator.getId();
            case NAME -> generator.getNameOrId();
            case NOMINAL_VOLTAGE -> String.valueOf(generator.getTerminal().getVoltageLevel().getNominalV());
            case COUNTRY -> {
                Optional<Country> country = generator.getTerminal().getVoltageLevel().getSubstation().flatMap(Substation::getCountry);
                yield country.isPresent() ? String.valueOf(country.get()) : "";
            }
            case ENERGY_SOURCE -> String.valueOf(generator.getEnergySource());
            case MIN_P -> String.valueOf(generator.getMinP());
            case MAX_P -> String.valueOf(generator.getMaxP());
            case TARGET_V -> String.valueOf(generator.getTargetV());
            case TARGET_P -> String.valueOf(generator.getTargetP());
            case TARGET_Q -> String.valueOf(generator.getTargetQ());
            case VOLTAGE_REGULATOR_ON -> String.valueOf(generator.isVoltageRegulatorOn());
        };
    }
}
