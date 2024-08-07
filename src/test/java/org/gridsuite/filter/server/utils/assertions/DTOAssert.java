/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.server.utils.assertions;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/**
 *  @author Thang PHAM <quyet-thang.pham at rte-france.com>
 */
public class DTOAssert<T> extends AbstractAssert<DTOAssert<T>, T> {
    public DTOAssert(T actual) {
        super(actual, DTOAssert.class);
    }

    public DTOAssert<T> recursivelyEquals(T other, String... fieldsToIgnore) {
        isNotNull();
        usingRecursiveComparison(this.getRecursiveConfiguration(fieldsToIgnore)).isEqualTo(other);
        return myself;
    }

    private RecursiveComparisonConfiguration getRecursiveConfiguration(String... fieldsToIgnore) {
        return RecursiveComparisonConfiguration.builder()
            .withIgnoreAllOverriddenEquals(true)                                    // For equals test, need specific tests
            .withIgnoredFieldsOfTypes(UUID.class, Date.class, Instant.class)  // For these types, need specific tests (uuid from db for example)
            .withIgnoreCollectionOrder(true)                                        // For collection order test, need specific tests
            .withIgnoredFields(fieldsToIgnore)
            .build();
    }
}
