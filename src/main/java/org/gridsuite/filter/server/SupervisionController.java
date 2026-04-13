/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.server;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.gridsuite.filter.IFilterAttributes;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Radouane KHOUADRI <radouane.khouadri at rte-france.com>
 */
@RestController
@RequestMapping(value = "/" + FilterApi.API_VERSION + "/supervision")
@Tag(name = "Filter server - Supervision")
public class SupervisionController {

    private final FilterService service;

    public SupervisionController(FilterService service) {
        this.service = service;
    }

    @GetMapping(value = "/filters", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get all filters")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "All filters")})
    public ResponseEntity<List<IFilterAttributes>> getFilters() {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(service.getFilters());
    }
}
