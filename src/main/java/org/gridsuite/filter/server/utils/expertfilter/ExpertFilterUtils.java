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
import org.apache.commons.collections4.CollectionUtils;
import org.gridsuite.filter.server.FilterLoader;
import org.gridsuite.filter.server.dto.identifierlistfilter.FilterEquipments;
import org.gridsuite.filter.server.dto.identifierlistfilter.IdentifiableAttributes;
import org.gridsuite.filter.server.utils.FilterServiceUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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
                case LINE -> getLineFieldValue(field, (Line) identifiable);
                case GENERATOR -> getGeneratorFieldValue(field, (Generator) identifiable);
                case LOAD -> getLoadFieldValue(field, (Load) identifiable);
                case SHUNT_COMPENSATOR -> getShuntCompensatorFieldValue(field, (ShuntCompensator) identifiable);
                case BUS -> getBusFieldValue(field, (Bus) identifiable);
                case BUSBAR_SECTION -> getBusBarSectionFieldValue(field, (BusbarSection) identifiable);
                case BATTERY -> getBatteryFieldValue(field, (Battery) identifiable);
                case SUBSTATION -> getSubstationFieldValue(field, (Substation) identifiable);
                case TWO_WINDINGS_TRANSFORMER -> getTwoWindingsTransformerFieldValue(field, (TwoWindingsTransformer) identifiable);
                default -> throw new PowsyblException(TYPE_NOT_IMPLEMENTED + " [" + identifiable.getType() + "]");
            };
        };
    }

    private static String getVoltageLevelFieldValue(FieldType field, VoltageLevel voltageLevel) {
        return switch (field) {
            case COUNTRY,
                COUNTRY_1,
                COUNTRY_2 ->
                    voltageLevel.getSubstation().flatMap(Substation::getCountry).map(String::valueOf).orElse(null);
            case NOMINAL_VOLTAGE,
                NOMINAL_VOLTAGE_1,
                NOMINAL_VOLTAGE_2 -> String.valueOf(voltageLevel.getNominalV());
            case VOLTAGE_LEVEL_ID,
                VOLTAGE_LEVEL_ID_1,
                VOLTAGE_LEVEL_ID_2 -> voltageLevel.getId();
            case LOW_VOLTAGE_LIMIT -> String.valueOf(voltageLevel.getLowVoltageLimit());
            case HIGH_VOLTAGE_LIMIT -> String.valueOf(voltageLevel.getHighVoltageLimit());
            default -> throw new PowsyblException(FIELD_AND_TYPE_NOT_IMPLEMENTED + " [" + field + "," + voltageLevel.getType() + "]");
        };
    }

    private static String getLineFieldValue(FieldType field, Line line) {
        return switch (field) {
            case CONNECTED_1 -> getTerminalFieldValue(field, line.getTerminal(TwoSides.ONE));
            case CONNECTED_2 -> getTerminalFieldValue(field, line.getTerminal(TwoSides.TWO));
            case COUNTRY_1,
                VOLTAGE_LEVEL_ID_1,
                NOMINAL_VOLTAGE_1 -> getVoltageLevelFieldValue(field, line.getTerminal(TwoSides.ONE).getVoltageLevel());
            case COUNTRY_2,
                VOLTAGE_LEVEL_ID_2,
                NOMINAL_VOLTAGE_2 -> getVoltageLevelFieldValue(field, line.getTerminal(TwoSides.TWO).getVoltageLevel());
            case SERIE_RESISTANCE -> String.valueOf(line.getR());
            case SERIE_REACTANCE -> String.valueOf(line.getX());
            case SHUNT_CONDUCTANCE_1 -> String.valueOf(line.getG1());
            case SHUNT_CONDUCTANCE_2 -> String.valueOf(line.getG2());
            case SHUNT_SUSCEPTANCE_1 -> String.valueOf(line.getB1());
            case SHUNT_SUSCEPTANCE_2 -> String.valueOf(line.getB2());
            default -> throw new PowsyblException(FIELD_AND_TYPE_NOT_IMPLEMENTED + " [" + field + "," + line.getType() + "]");
        };
    }

    private static String getLoadFieldValue(FieldType field, Load load) {
        return switch (field) {
            case COUNTRY,
                NOMINAL_VOLTAGE,
                VOLTAGE_LEVEL_ID -> getVoltageLevelFieldValue(field, load.getTerminal().getVoltageLevel());
            case P0 -> String.valueOf(load.getP0());
            case Q0 -> String.valueOf(load.getQ0());
            case CONNECTED -> getTerminalFieldValue(field, load.getTerminal());
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
                SWITCHED_ON_SUSCEPTANCE -> getSectionBasedFieldValue(field, shuntCompensator);
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
            case CONNECTED,
                CONNECTED_1,
                CONNECTED_2 -> String.valueOf(terminal.isConnected());
            default -> throw new PowsyblException(FIELD_AND_TYPE_NOT_IMPLEMENTED + " [" + field + ",terminal]");
        };
    }

    private static String getSectionBasedFieldValue(FieldType field, ShuntCompensator shuntCompensator) {
        double susceptancePerSection = shuntCompensator.getModel(ShuntCompensatorLinearModel.class).getBPerSection();
        double qAtNominalV = Math.pow(shuntCompensator.getTerminal().getVoltageLevel().getNominalV(), 2) * Math.abs(susceptancePerSection);

        return switch (field) {
            case SHUNT_COMPENSATOR_TYPE -> susceptancePerSection > 0 ? "CAPACITOR" : "REACTOR";
            case MAX_Q_AT_NOMINAL_V -> String.valueOf(qAtNominalV * shuntCompensator.getMaximumSectionCount());
            case SWITCHED_ON_Q_AT_NOMINAL_V -> String.valueOf(qAtNominalV * shuntCompensator.getSectionCount());
            case MAX_SUSCEPTANCE -> String.valueOf(susceptancePerSection * shuntCompensator.getMaximumSectionCount());
            case SWITCHED_ON_SUSCEPTANCE -> String.valueOf(susceptancePerSection * shuntCompensator.getSectionCount());
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

    private static String getRatioTapChangerFieldValue(FieldType field, RatioTapChanger ratioTapChanger) {
        if (ratioTapChanger == null) {
            return null;
        }
        return switch (field) {
            case RATIO_REGULATING -> String.valueOf(ratioTapChanger.isRegulating());
            case RATIO_TARGET_V -> String.valueOf(ratioTapChanger.getTargetV());
            case LOAD_TAP_CHANGING_CAPABILITIES -> String.valueOf(ratioTapChanger.hasLoadTapChangingCapabilities());
            default -> throw new PowsyblException(FIELD_AND_TYPE_NOT_IMPLEMENTED + " [" + field + ",ratioTapChanger]");
        };
    }

    private static String getTwoWindingsTransformerFieldValue(FieldType field, TwoWindingsTransformer twoWindingsTransformer) {
        return switch (field) {
            case COUNTRY -> twoWindingsTransformer.getSubstation().flatMap(Substation::getCountry).map(String::valueOf).orElse(null);
            case CONNECTED_1 -> String.valueOf(twoWindingsTransformer.getTerminal1().isConnected());
            case CONNECTED_2 -> String.valueOf(twoWindingsTransformer.getTerminal2().isConnected());
            case NOMINAL_VOLTAGE_1 -> String.valueOf(twoWindingsTransformer.getTerminal1().getVoltageLevel().getNominalV());
            case NOMINAL_VOLTAGE_2 -> String.valueOf(twoWindingsTransformer.getTerminal2().getVoltageLevel().getNominalV());
            case VOLTAGE_LEVEL_ID_1 -> twoWindingsTransformer.getTerminal1().getVoltageLevel().getId();
            case VOLTAGE_LEVEL_ID_2 -> twoWindingsTransformer.getTerminal2().getVoltageLevel().getId();
            case RATED_S -> String.valueOf(twoWindingsTransformer.getRatedS());
            case SERIE_RESISTANCE -> String.valueOf(twoWindingsTransformer.getR());
            case SERIE_REACTANCE -> String.valueOf(twoWindingsTransformer.getX());
            case MAGNETIZING_CONDUCTANCE -> String.valueOf(twoWindingsTransformer.getG());
            case MAGNETIZING_SUSCEPTANCE -> String.valueOf(twoWindingsTransformer.getB());
            case RATIO_REGULATING,
                RATIO_TARGET_V,
                LOAD_TAP_CHANGING_CAPABILITIES -> getRatioTapChangerFieldValue(field, twoWindingsTransformer.getRatioTapChanger());
            default -> throw new PowsyblException(FIELD_AND_TYPE_NOT_IMPLEMENTED + " [" + field + "," + twoWindingsTransformer.getType() + "]");
        };
    }

    public static List<FilterEquipments> getFilterEquipments(Network network, Set<String> uuids, FilterLoader filterLoader, Map<UUID, FilterEquipments> cachedUuidFilters) {
        List<FilterEquipments> res = new ArrayList<>();
        uuids.stream().map(UUID::fromString).forEach(uuid -> {
            if (cachedUuidFilters.containsKey(uuid)) {
                if (cachedUuidFilters.get(uuid) != null) {
                    res.add(cachedUuidFilters.get(uuid));
                }
            } else {
                // We do not allow to use expert filters for IS_PART_OF or IS_NOT_PART_OF operators
                List<FilterEquipments> filterEquipments = FilterServiceUtils.getFilterEquipmentsFromUuid(network, uuid, filterLoader);
                cachedUuidFilters.put(uuid, !CollectionUtils.isEmpty(filterEquipments) ? filterEquipments.get(0) : null);
                res.addAll(filterEquipments);
            }
        });
        return res;
    }

    public static boolean isPartOf(Network network, String value, Set<String> uuids, FilterLoader filterLoader, Map<UUID, FilterEquipments> cachedUuidFilters) {
        List<FilterEquipments> equipments = getFilterEquipments(network, uuids, filterLoader, cachedUuidFilters);
        return equipments.stream().flatMap(e -> e.getIdentifiableAttributes().stream()
            .map(IdentifiableAttributes::getId)).collect(Collectors.toSet()).contains(value);
    }
}
