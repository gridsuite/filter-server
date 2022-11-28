/*
 *  Copyright (c) 2021, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.filter.server.repositories;

/**
 * @author Jacques Borsenberger <jacques.borsenberger at rte-france.com>
 */
public interface FilterMetadata {
    java.util.UUID getId();

    java.util.Date getModificationDate();
}
