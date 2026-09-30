/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.server.wip;

import com.google.common.annotations.Beta;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.gridsuite.filter.server.FilterApi;
import org.gridsuite.filter.server.wip.dto.FilterWithDistributionKeys;
import org.gridsuite.filter.wip.Filter;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author Mohamed Ben Rejeb {@literal <mohamed.ben-rejeb at rte-france.com>}
 */
@RestController
@RequestMapping(value = "/" + FilterApi.API_VERSION + "/standalone-filters")
@Tag(name = "Standalone filter server")
@Beta
public class StandaloneFilterController {

    private final StandaloneFilterService service;

    public StandaloneFilterController(StandaloneFilterService service) {
        this.service = service;
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get filter by id")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "The filter"),
        @ApiResponse(responseCode = "404", description = "The filter does not exist")
    })
    public ResponseEntity<Filter> getFilter(@PathVariable("id") UUID id) {
        return service.getFilter(id)
                .map(filter -> ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(filter))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get multiple filters by ids, indexed by id")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "The filters found, indexed by their id; ids with no matching filter are omitted")
    })
    public ResponseEntity<Map<UUID, Filter>> getFilters(@RequestParam List<UUID> ids) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(service.getFilters(ids));
    }

    @GetMapping(value = "/with-distribution-keys", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get multiple filters by ids, along with their distribution keys, indexed by id",
            description = "Only identifier list filters have distribution keys, expert filters are returned with an empty one. Ids with no matching filter are omitted.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "The filters found, indexed by their id, each with its distribution keys indexed by equipment id")
    })
    public ResponseEntity<Map<UUID, FilterWithDistributionKeys>> getFiltersWithDistributionKeys(@RequestParam List<UUID> ids) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(service.getFiltersWithDistributionKeys(ids));
    }
}
