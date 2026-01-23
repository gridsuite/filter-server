/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.server.utils.assertions;

import org.assertj.core.util.CheckReturnValue;
import org.gridsuite.filter.expertfilter.ExpertFilterDto;

/**
 *  @author Thang PHAM <quyet-thang.pham at rte-france.com>
 * {@link org.assertj.core.api.Assertions Assertions} completed with our custom assertions classes.
 */
public class Assertions extends org.assertj.core.api.Assertions {
    @CheckReturnValue
    public static <T extends ExpertFilterDto> DTOAssert<ExpertFilterDto> assertThat(T actual) {
        return new DTOAssert<>(actual);
    }
}
