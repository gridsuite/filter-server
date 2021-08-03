/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.server.configs;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.gridsuite.filter.server.FilterApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@Configuration
public class FilterSwaggerConfig {
    @Bean
    public OpenAPI createOpenApi() {
        return new OpenAPI()
            .info(new Info()
                .title("Filters API")
                .description("This is the documentation of the filters REST API")
                .version(FilterApi.API_VERSION));
    }
}
