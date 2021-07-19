/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.server;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import org.gridsuite.filter.server.dto.*;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 * @author Jacques Borsenberger <jacques.borsenberger at rte-france.com>
 */
@RestController
@RequestMapping(value = "/" + FilterApi.API_VERSION)
@Api(value = "Filter server")
@ComponentScan(basePackageClasses = FilterService.class)
public class FilterController {

    private final FilterService service;

    public FilterController(FilterService service) {
        this.service = service;
    }

    @GetMapping(value = "filters", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get all filters", response = List.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "All filters")})
    public ResponseEntity<List<FilterAttributes>> getFilters() {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(service.getFilters());
    }

    @GetMapping(value = "filters/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get filter by id", response = AbstractFilter.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "The filter"),
        @ApiResponse(code = 404, message = "The filter does not exists")})
    public ResponseEntity<AbstractFilter> getFilter(@PathVariable("id") UUID id) {
        return service.getFilter(id).map(filter -> ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(filter))
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping(value = "filters/", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Create a filter", response = AbstractFilter.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "The filter has been successfully created")})
    public ResponseEntity<AbstractFilter> createFilter(@RequestBody(required = true) AbstractFilter filter) {
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(service.createFilter(filter));
    }

    @DeleteMapping(value = "filters/{id}")
    @ApiOperation(value = "delete the filter")
    @ApiResponse(code = 200, message = "The filter has been deleted")
    public ResponseEntity<Void> deleteFilter(@PathVariable("id") UUID id) {
        service.deleteFilter(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "filters/{id}/rename")
    @ApiOperation(value = "Rename filter by name")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "The filter has been renamed"),
        @ApiResponse(code = 404, message = "The filter does not exists")})
    public void renameFilter(@PathVariable("id") UUID id,
                             @RequestBody String newName) {
        service.renameFilter(id, newName);
    }

    @PutMapping(value = "filters/{id}/replace-with-script")
    @ApiOperation(value = "Replace a filter with a script filter", response = AbstractFilter.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "The filter have been replaced successfully")})
    public ResponseEntity<AbstractFilter> replaceFilterWithScript(@PathVariable("id") UUID id) {
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(service.replaceFilterWithScript(id));
    }

    @PutMapping(value = "filters/{id}/new-script/{scriptName}")
    @ApiOperation(value = "Create a new script filter from a filter", response = AbstractFilter.class)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "The script filter have been created successfully")})
    public ResponseEntity<AbstractFilter> newScriptFromFilter(@PathVariable("id") UUID id,
                                                              @PathVariable("scriptName") String scriptName) {
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(service.newScriptFromFilter(id, scriptName));
    }
}
