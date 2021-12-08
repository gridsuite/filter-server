/**
 *  Copyright (c) 2021, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.server.dto;

import lombok.*;
import org.gridsuite.filter.server.utils.RangeType;

/**
 * @author Jacques Borsenberger <jacques.borsenberger at rte-france.com>
 */

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class NumericalFilter {
    RangeType type;
    Double value1;
    Double value2;

}
