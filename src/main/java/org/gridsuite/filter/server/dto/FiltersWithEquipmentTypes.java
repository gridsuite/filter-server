/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.filter.server.dto;

import java.util.List;

/**
 * Store a list of filters and the equipment types that are associated with.
 *
 * @author Florent MILLOT <florent.millot@rte-france.com>
 */
public record FiltersWithEquipmentTypes(List<FilterAttributes> filters,
                                        List<EquipmentTypesByFilterId> selectedEquipmentTypesByFilter) {
}
