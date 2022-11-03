/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.server.utils;

import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VoltageLevel;
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
        Optional<Country> country = terminal.getVoltageLevel().getSubstation().flatMap(Substation::getCountry);
        return filterCountries.isEmpty() || country.map(c -> filterCountries.contains(c.name())).orElse(false);
    }

    public static boolean isLocatedIn(List<String> filterCountries, VoltageLevel voltageLevel) {
        Optional<Country> country = voltageLevel.getSubstation().flatMap(Substation::getCountry);
        return filterCountries.isEmpty() || country.map(c -> filterCountries.contains(c.name())).orElse(false);
    }

    public static boolean isLocatedIn(List<String> filterCountries, Substation substation) {
        Optional<Country> country = substation.getCountry();
        return filterCountries.isEmpty() || country.map(c -> filterCountries.contains(c.name())).orElse(false);
    }

    public static boolean isEqualityNominalVoltage(Terminal terminal, Double value) {
        return isEqualityNominalVoltage(terminal.getVoltageLevel(), value);
    }

    public static boolean isEqualityNominalVoltage(VoltageLevel voltageLevel, Double value) {
        return isEqualityNominalVoltage(voltageLevel.getNominalV(), value);
    }

    public static boolean isEqualityNominalVoltage(Double nominalV, Double value) {
        return value == null || Math.abs(nominalV - value) <= 0.000001;
    }

    public static boolean isRangeNominalVoltage(Terminal terminal, Double minValue, Double maxValue) {
        return isRangeNominalVoltage(terminal.getVoltageLevel(), minValue, maxValue);
    }

    public static boolean isRangeNominalVoltage(VoltageLevel voltageLevel, Double minValue, Double maxValue) {
        return isRangeNominalVoltage(voltageLevel.getNominalV(), minValue, maxValue);
    }

    public static boolean isRangeNominalVoltage(Double nominalV, Double minValue, Double maxValue) {
        return (minValue == null || nominalV >= minValue) && (maxValue == null || nominalV <= maxValue);
    }
}
