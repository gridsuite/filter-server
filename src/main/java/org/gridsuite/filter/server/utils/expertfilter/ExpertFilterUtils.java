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

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
public final class ExpertFilterUtils {

    public static final String FIELD_AND_TYPE_NOT_IMPLEMENTED = "This field and equipment type combination is not implemented with expert filter";

    public static final String TYPE_NOT_IMPLEMENTED = "This equipment type is not implemented with expert filter";

    private ExpertFilterUtils() { }

    public static <I extends Identifiable<I>> String getFieldValue(FieldType field, Identifiable<I> identifiable) {
        return switch (field) {
            case ID -> identifiable.getId();
            case NAME -> identifiable.getNameOrId();
            default -> switch (identifiable.getType()) {
                case VOLTAGE_LEVEL -> getVoltageLevelFieldValue(field, (VoltageLevel) identifiable);
                case GENERATOR -> getGeneratorFieldValue(field, (Generator) identifiable);
                case LOAD -> getLoadFieldValue(field, (Load) identifiable);
                case SHUNT_COMPENSATOR -> getShuntCompensatorFieldValue(field, (ShuntCompensator) identifiable);
                case BUS -> getBusFieldValue(field, (Bus) identifiable);
                case BUSBAR_SECTION -> getBusBarSectionFieldValue(field, (BusbarSection) identifiable);
                case BATTERY -> getBatteryFieldValue(field, (Battery) identifiable);
                case SUBSTATION -> getSubstationFieldValue(field, (Substation) identifiable);
                default -> throw new PowsyblException(TYPE_NOT_IMPLEMENTED + " [" + identifiable.getType() + "]");
            };
        };
    }

    private static String getVoltageLevelFieldValue(FieldType field, VoltageLevel voltageLevel) {
        return switch (field) {
            case COUNTRY ->
                voltageLevel.getSubstation().flatMap(Substation::getCountry).map(String::valueOf).orElse(null);
            case NOMINAL_VOLTAGE -> String.valueOf(voltageLevel.getNominalV());
            case VOLTAGE_LEVEL_ID -> voltageLevel.getId();
            case LOW_VOLTAGE_LIMIT -> String.valueOf(voltageLevel.getLowVoltageLimit());
            case HIGH_VOLTAGE_LIMIT -> String.valueOf(voltageLevel.getHighVoltageLimit());
            default -> throw new PowsyblException(FIELD_AND_TYPE_NOT_IMPLEMENTED + " [" + field + "," + voltageLevel.getType() + "]");
        };
    }

    private static String getLoadFieldValue(FieldType field, Load load) {
        return switch (field) {
            case COUNTRY,
                NOMINAL_VOLTAGE,
                VOLTAGE_LEVEL_ID -> getVoltageLevelFieldValue(field, load.getTerminal().getVoltageLevel());
            case P0 -> String.valueOf(load.getP0());
            case Q0 -> String.valueOf(load.getQ0());
            default -> throw new PowsyblException(FIELD_AND_TYPE_NOT_IMPLEMENTED + " [" + field + "," + load.getType() + "]");

        };
    }

    private static String getShuntCompensatorFieldValue(FieldType field, ShuntCompensator shuntCompensator) {
        return switch (field) {
            case VOLTAGE_LEVEL_ID,
                COUNTRY,
                NOMINAL_VOLTAGE -> getVoltageLevelFieldValue(field, shuntCompensator.getTerminal().getVoltageLevel());
            case MAXIMUM_SECTION_COUNT -> String.valueOf(shuntCompensator.getMaximumSectionCount());
            case SECTION_COUNT -> String.valueOf(shuntCompensator.getSectionCount());
            case SHUNT_COMPENSATOR_TYPE,
                MAX_Q_AT_NOMINAL_V,
                SWITCHED_ON_Q_AT_NOMINAL_V,
                MAX_SUSCEPTANCE,
                SWITCHED_ON_MAX_SUSCEPTANCE -> getSectionBasedFieldValue(field, shuntCompensator);
            case CONNECTED -> getTerminalFieldValue(field, shuntCompensator.getTerminal());
            default -> throw new PowsyblException(FIELD_AND_TYPE_NOT_IMPLEMENTED + " [" + field + "," + shuntCompensator.getType() + "]");
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
            case COUNTRY,
                NOMINAL_VOLTAGE,
                VOLTAGE_LEVEL_ID -> getVoltageLevelFieldValue(field, generator.getTerminal().getVoltageLevel());
            case CONNECTED -> getTerminalFieldValue(field, generator.getTerminal());
            default -> throw new PowsyblException(FIELD_AND_TYPE_NOT_IMPLEMENTED + " [" + field + "," + generator.getType() + "]");
        };
    }

    @NotNull
    private static String getGeneratorStartupField(Generator generator, FieldType fieldType) {
        GeneratorStartup generatorStartup = generator.getExtension(GeneratorStartup.class);
        if (generatorStartup != null) {
            return String.valueOf(
                switch (fieldType) {
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

    private static String getBusFieldValue(FieldType field, Bus bus) {
        return switch (field) {
            case COUNTRY,
                NOMINAL_VOLTAGE,
                VOLTAGE_LEVEL_ID -> getVoltageLevelFieldValue(field, bus.getVoltageLevel());
            default -> throw new PowsyblException(FIELD_AND_TYPE_NOT_IMPLEMENTED + " [" + field + "," + bus.getType() + "]");
        };
    }

    private static String getBusBarSectionFieldValue(FieldType field, BusbarSection busbarSection) {
        return switch (field) {
            case COUNTRY,
                NOMINAL_VOLTAGE,
                VOLTAGE_LEVEL_ID -> getVoltageLevelFieldValue(field, busbarSection.getTerminal().getVoltageLevel());
            default -> throw new PowsyblException(FIELD_AND_TYPE_NOT_IMPLEMENTED + " [" + field + "," + busbarSection.getType() + "]");
        };
    }

    private static String getTerminalFieldValue(FieldType field, Terminal terminal) {
        return switch (field) {
            case CONNECTED -> String.valueOf(terminal.isConnected());
            default -> throw new PowsyblException(FIELD_AND_TYPE_NOT_IMPLEMENTED + " [" + field + ",terminal]");
        };
    }

    private static String getSectionBasedFieldValue(FieldType field, ShuntCompensator shuntCompensator) {
        double susceptancePerSection = shuntCompensator.getModel(ShuntCompensatorLinearModel.class).getBPerSection();
        double voltageLevelPower = Math.pow(shuntCompensator.getTerminal().getVoltageLevel().getNominalV(), 2);

        return switch (field) {
            case SHUNT_COMPENSATOR_TYPE -> susceptancePerSection > 0 ? "CAPACITOR" : "REACTOR";
            case MAX_Q_AT_NOMINAL_V -> String.valueOf(voltageLevelPower * susceptancePerSection * shuntCompensator.getMaximumSectionCount());
            case SWITCHED_ON_Q_AT_NOMINAL_V -> String.valueOf(voltageLevelPower * susceptancePerSection * shuntCompensator.getSectionCount());
            case MAX_SUSCEPTANCE -> String.valueOf(susceptancePerSection * shuntCompensator.getMaximumSectionCount());
            case SWITCHED_ON_MAX_SUSCEPTANCE -> String.valueOf(susceptancePerSection * shuntCompensator.getSectionCount());
            default -> throw new PowsyblException(FIELD_AND_TYPE_NOT_IMPLEMENTED + " [" + field + "," + shuntCompensator.getType() + "]");
        };
    }

    private static String getBatteryFieldValue(FieldType field, Battery battery) {
        return switch (field) {
            case COUNTRY,
                    NOMINAL_VOLTAGE,
                    VOLTAGE_LEVEL_ID -> getVoltageLevelFieldValue(field, battery.getTerminal().getVoltageLevel());
            case CONNECTED -> getTerminalFieldValue(field, battery.getTerminal());
            case MIN_P -> String.valueOf(battery.getMinP());
            case MAX_P -> String.valueOf(battery.getMaxP());
            case TARGET_P -> String.valueOf(battery.getTargetP());
            case TARGET_Q -> String.valueOf(battery.getTargetQ());

            default -> throw new PowsyblException(FIELD_AND_TYPE_NOT_IMPLEMENTED + " [" + field + "," + battery.getType() + "]");

        };
    }

    private static String getSubstationFieldValue(FieldType field, Substation substation) {
        return switch (field) {
            case COUNTRY -> String.valueOf(substation.getCountry().orElse(null));
            default ->
                throw new PowsyblException(FIELD_AND_TYPE_NOT_IMPLEMENTED + " [" + field + "," + substation.getType() + "]");
        };
    }
}
