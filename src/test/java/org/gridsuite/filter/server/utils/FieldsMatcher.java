/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.server.utils;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import java.util.Objects;

import static java.lang.String.format;

/**
 * @author Laurent Garnier <laurent.garnier at rte-france.com>
 */

class EqualDiagnosingMatcher<U> extends TypeSafeDiagnosingMatcher<U> {

    private final U expected;

    EqualDiagnosingMatcher(U expected) {
        this.expected = expected;
    }

    protected boolean matchesSafely(U actual, Description mismatchDescription) {
        boolean equals = Objects.equals(expected, actual);
        if (!equals) {
            mismatchDescription.appendText("was not " + expected);
        }
        return equals;
    }

    public void describeTo(Description description) {
        description.appendText("Objects.equals");
    }
}

