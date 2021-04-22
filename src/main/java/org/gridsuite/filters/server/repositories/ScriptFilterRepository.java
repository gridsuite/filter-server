/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filters.server.repositories;

import org.gridsuite.filters.server.entities.ScriptFilterEntity;
import org.springframework.stereotype.Repository;


/**
 * @author Jacuqes Borsenberger <geoffroy.jamgotchian at rte-france.com>
 */
@Repository
public interface ScriptFilterRepository extends FiltersRepository<ScriptFilterEntity> {
}
