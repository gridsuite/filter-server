package org.gridsuite.filter.server.dto;

import java.util.List;

/**
 * Store a list of filters and the equipment types that are associated with.
 *
 * @author Florent MILLOT <florent.millot@rte-france.com>
 */
public record FiltersWithEquipmentTypes(List<FilterAttributes> filters,
                                        List<EquipmentTypesByElement> selectedEquipmentTypesByFilter) {
}
