/*
 *  Copyright (c) 2025, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.filter.server.utils;

import org.gridsuite.filter.AbstractFilter;
import org.gridsuite.filter.FilterLoader;
import org.gridsuite.filter.expertfilter.ExpertFilter;
import org.gridsuite.filter.expertfilter.expertrule.AbstractExpertRule;
import org.gridsuite.filter.expertfilter.expertrule.CombinatorExpertRule;
import org.gridsuite.filter.expertfilter.expertrule.FilterUuidExpertRule;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * @author Mohamed BENREJEB <mohamed.ben-rejeb at rte-france.com>
 */
public final class FilterCycleDetector {

    private FilterCycleDetector() {
        // Utility class
    }

    /**
     * Check that there is no cycle starting from the provided filter.
     *
     * @param filter       the starting filter
     * @param filterLoader loader used to retrieve referenced filters
     */
    public static void checkNoCycle(AbstractFilter filter, FilterLoader filterLoader) {
        checkNoCycle(filter, filterLoader, new HashSet<>());
    }

    private static void checkNoCycle(AbstractFilter filter, FilterLoader loader,
                                     Set<UUID> visiting) {
        UUID id = filter.getId();
        if (id != null) {
            if (visiting.contains(id)) {
                throw new RuntimeException("Cycle detected in filters");
            }
            visiting.add(id);
        }

        if (filter instanceof ExpertFilter expertFilter) {
            checkRule(expertFilter.getRules(), loader, visiting);
        }

        if (id != null) {
            visiting.remove(id);
        }
    }

    private static void checkRule(AbstractExpertRule rule, FilterLoader loader,
                                  Set<UUID> visiting) {
        switch (rule) {
            case CombinatorExpertRule combinatorRule -> {
                List<AbstractExpertRule> rules = combinatorRule.getRules();
                if (rules != null) {
                    for (AbstractExpertRule r : rules) {
                        checkRule(r, loader, visiting);
                    }
                }
            }
            case FilterUuidExpertRule uuidRule -> {
                Set<String> values = uuidRule.getValues();
                if (values != null) {
                    for (String value : values) {
                        UUID refId = UUID.fromString(value);
                        List<AbstractFilter> referenced = loader.getFilters(List.of(refId));
                        if (!referenced.isEmpty() && referenced.getFirst() != null) {
                            checkNoCycle(referenced.getFirst(), loader, visiting);
                        }
                    }
                }
            }
            default -> {
            }
        }
    }
}

