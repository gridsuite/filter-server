/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.server.utils.expertfilter;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.GeneratorStartup;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
public final class ExpertFilterUtils {
    private ExpertFilterUtils() { }

    public static <I extends Identifiable<I>> String getFieldValue(FieldType field, Identifiable<I> identifiable) {
        return switch (identifiable.getType()) {
            case GENERATOR -> getGeneratorFieldValue(field, (Generator) identifiable);
            case LOAD -> getLoadFieldValue(field, (Load) identifiable);
            default -> throw new PowsyblException(identifiable.getType() + " injection type is not implemented with expert filter");
        };
    }

    private static String getInjectionFieldValue(FieldType field, Injection<?> injection) {
        return switch (field) {
            case ID -> injection.getId();
            case NAME -> injection.getNameOrId();
            case COUNTRY -> {
                Optional<Country> country = injection.getTerminal().getVoltageLevel().getSubstation().flatMap(Substation::getCountry);
                yield country.isPresent() ? String.valueOf(country.get()) : "";
            }
            case NOMINAL_VOLTAGE -> String.valueOf(injection.getTerminal().getVoltageLevel().getNominalV());
            case VOLTAGE_LEVEL_ID -> injection.getTerminal().getVoltageLevel().getId();
            case CONNECTED -> String.valueOf(injection.getTerminal().isConnected());
            default -> throw new PowsyblException("Field " + field + " with " + injection.getType() + " injection type is not implemented with expert filter");
        };
    }

    private static String getLoadFieldValue(FieldType field, Load load) {
        return switch (field) {
            default -> getInjectionFieldValue(field, load);
        };
    }

    private static String getGeneratorFieldValue(FieldType field, Generator generator) {
        return switch (field) {
            case ENERGY_SOURCE -> String.valueOf(generator.getEnergySource());
            case MIN_P -> String.valueOf(generator.getMinP());
            case MAX_P -> String.valueOf(generator.getMaxP());
            case TARGET_V -> String.valueOf(generator.getTargetV());
            case TARGET_P -> String.valueOf(generator.getTargetP());
            case TARGET_Q -> String.valueOf(generator.getTargetQ());
            case VOLTAGE_REGULATOR_ON -> String.valueOf(generator.isVoltageRegulatorOn());
            case PLANNED_ACTIVE_POWER_SET_POINT,
                MARGINAL_COST,
                PLANNED_OUTAGE_RATE,
                FORCED_OUTAGE_RATE ->
                getGeneratorStartupField(generator, field);
            case RATED_S -> String.valueOf(generator.getRatedS());
            default -> getInjectionFieldValue(field, generator);
        };
    }

    @NotNull
    private static String getGeneratorStartupField(Generator generator, FieldType fieldType) {
        GeneratorStartup generatorStartup = generator.getExtension(GeneratorStartup.class);
        if (generatorStartup != null) {
            return String.valueOf(switch (fieldType) {
                case PLANNED_ACTIVE_POWER_SET_POINT -> generatorStartup.getPlannedActivePowerSetpoint();
                case MARGINAL_COST -> generatorStartup.getMarginalCost();
                case PLANNED_OUTAGE_RATE -> generatorStartup.getPlannedOutageRate();
                case FORCED_OUTAGE_RATE -> generatorStartup.getForcedOutageRate();
                default -> String.valueOf(Double.NaN);
            });
        } else {
            return String.valueOf(Double.NaN);
        }
    }
}
