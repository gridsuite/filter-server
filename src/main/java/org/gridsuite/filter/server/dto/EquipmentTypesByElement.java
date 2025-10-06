package org.gridsuite.filter.server.dto;

import com.powsybl.iidm.network.IdentifiableType;

import java.util.Set;
import java.util.UUID;

/**
 * Store a list of equipment types for a given element ID
 *
 * @author Florent MILLOT <florent.millot@rte-france.com>
 */
public record EquipmentTypesByElement(UUID id, Set<IdentifiableType> equipmentTypes) {
}
