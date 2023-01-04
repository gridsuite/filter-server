/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.server;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.gridsuite.filter.server.dto.AbstractFilter;
import org.gridsuite.filter.server.dto.FilterEquipments;
import org.gridsuite.filter.server.dto.IFilterAttributes;
import org.gridsuite.filter.server.dto.IdentifiableAttributes;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 * @author Jacques Borsenberger <jacques.borsenberger at rte-france.com>
 */
@RestController
@RequestMapping(value = "/" + FilterApi.API_VERSION)
@Tag(name = "Filter server")
@ComponentScan(basePackageClasses = FilterService.class)
public class FilterController {

    private final FilterService service;

    public FilterController(FilterService service) {
        this.service = service;
    }

    @GetMapping(value = "/filters", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get all filters")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "All filters")})
    public ResponseEntity<List<IFilterAttributes>> getFilters() {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(service.getFilters());
    }

    @GetMapping(value = "/filters/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get filter by id")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "The filter"),
        @ApiResponse(responseCode = "404", description = "The filter does not exists")})
    public ResponseEntity<AbstractFilter> getFilter(@PathVariable("id") UUID id) {
        return service.getFilter(id).map(filter -> ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(filter))
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping(value = "/filters", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create a filter")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "The filter has been successfully created")})
    public ResponseEntity<AbstractFilter> createFilter(@RequestParam("id") UUID filterId,
                                                       @RequestBody AbstractFilter filter) {
        filter.setId(filterId);
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(service.createFilter(filter));
    }

    @PostMapping(value = "/filters")
    @Operation(summary = "Create a filter from an existing filter")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "The filter has been successfully created"),
                           @ApiResponse(responseCode = "404", description = "Source filter not found")})
    public ResponseEntity<AbstractFilter> createFilter(@RequestParam("duplicateFrom") UUID sourceFilterId, @RequestParam("id") UUID filterId) {
        return service.createFilter(sourceFilterId, filterId).map(filter -> ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(filter))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping(value = "/filters/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Modify a filter")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "The filter has been successfully modified")})
    public ResponseEntity<Void> changeFilter(@PathVariable UUID id, @RequestBody AbstractFilter filter, @RequestHeader("userId") String userId) {
        try {
            service.changeFilter(id, filter, userId);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException ignored) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping(value = "/filters/{id}")
    @Operation(summary = "delete the filter")
    @ApiResponse(responseCode = "200", description = "The filter has been deleted")
    public ResponseEntity<Void> deleteFilter(@PathVariable("id") UUID id) {
        service.deleteFilter(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/filters/metadata")
    @Operation(summary = "get filters metadata")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "filters metadata"),
        @ApiResponse(responseCode = "404", description = "The filters don't exist")})
    public ResponseEntity<List<AbstractFilter>> getFiltersMetadata(@RequestParam("ids") List<UUID> ids) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(service.getFilters(ids));
    }

    @PutMapping(value = "/filters/{id}/replace-with-script")
    @Operation(summary = "Replace a filter with a script filter")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "The filter have been replaced successfully")})
    public ResponseEntity<AbstractFilter> replaceFilterWithScript(@PathVariable("id") UUID id, @RequestHeader("userId") String userId) {
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(service.replaceFilterWithScript(id, userId));
    }

    @PostMapping(value = "/filters/{id}/new-script")
    @Operation(summary = "Create a new script filter from a filter")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "The script filter have been created successfully")})
    public ResponseEntity<AbstractFilter> newScriptFromFilter(@PathVariable("id") UUID filterId,
                                                              @RequestParam(required = false, value = "newId") UUID newId) {
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(service.newScriptFromFilter(filterId, newId));
    }

    @GetMapping(value = "/filters/{id}/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Export a filter to JSON format")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "The filter on JSON format")})
    public ResponseEntity<List<IdentifiableAttributes>> exportFilter(@PathVariable("id") UUID id,
                                                                     @RequestParam(value = "networkUuid") UUID networkUuid,
                                                                     @RequestParam(value = "variantId", required = false) String variantId) {
        return service.exportFilter(id, networkUuid, variantId).map(identifiables -> ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(identifiables))
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping(value = "/filters/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Export list of filters to JSON format")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "The filters on JSON format")})
    public ResponseEntity<List<FilterEquipments>> exportFilters(@RequestParam("ids") List<UUID> ids,
                                                                @RequestParam(value = "networkUuid") UUID networkUuid,
                                                                @RequestParam(value = "variantId", required = false) String variantId) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(service.exportFilters(ids, networkUuid, variantId));
    }
}
