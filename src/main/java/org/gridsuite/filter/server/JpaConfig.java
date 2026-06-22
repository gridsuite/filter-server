/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.server;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Enables JPA auditing (e.g. {@code @LastModifiedDate} on filter entities).
 * Declared here rather than on {@link FilterApplication} so it does not leak into
 * web-layer slice tests ({@code @WebMvcTest}), which do not load the JPA context.
 *
 * @author Achour BERRAHMA <achour.berrahma at rte-france.com>
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
