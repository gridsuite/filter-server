/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.server.utils;

import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Terminal;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import java.util.List;
import java.util.Optional;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public final class FiltersUtils {
    private static final PathMatcher ANT_MATCHER = new AntPathMatcher("\0");

    private FiltersUtils() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    public static boolean matchID(String filterID, Identifiable<?> equipment) {
        return ANT_MATCHER.match(filterID, equipment.getId());
    }

    public static boolean matchName(String filterName, Identifiable<?> equipment) {
        Optional<String> name = equipment.getOptionalName();
        return name.filter(s -> ANT_MATCHER.match(filterName, s)).isPresent();
    }

    public static boolean isLocatedIn(List<String> filterCountries, Terminal terminal) {
        return filterCountries.isEmpty() || terminal.getVoltageLevel().getSubstation().getCountry().isPresent()
                        && filterCountries.contains(terminal.getVoltageLevel().getSubstation().getCountry().get().name());
    }

    public static boolean isEqualityNominalVoltage(Terminal terminal, Double value) {
        return value == null || terminal.getVoltageLevel().getNominalV() == value;
    }

    public static boolean isRangeNominalVoltage(Terminal terminal, Double minValue, Double maxValue) {
        double nominalV = terminal.getVoltageLevel().getNominalV();
        return (minValue == null || nominalV >= minValue) && (maxValue == null || nominalV <= maxValue);
    }

    public static boolean isApproxNominalVoltage(Terminal terminal, Double value, Double percent) {
        double nominalV = terminal.getVoltageLevel().getNominalV();
        return (value == null || percent == null) || (nominalV >= (value - ((percent * value) / 100)) && nominalV <= (value + ((percent * value) / 100)));
    }
}
