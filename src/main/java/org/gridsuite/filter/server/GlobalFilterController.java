package org.gridsuite.filter.server;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.gridsuite.filter.globalfilter.GlobalFilter;
import org.gridsuite.filter.utils.EquipmentType;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequestMapping(value = "/" + FilterApi.API_VERSION + "/global-filter",
    produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "GlobalFilter component controller")
@AllArgsConstructor
public class GlobalFilterController {
    private final GlobalFilterService service;

    @PostMapping(value = "")
    @Operation(summary = "Get network equipments IDs that match the filter(s)")
    @ApiResponse(responseCode = "200", description = "The filter")
    @ApiResponse(responseCode = "400", description = "Invalid parameters")
    @ApiResponse(responseCode = "404", description = "The filter does not exists")
    public ResponseEntity<List<String>> getResults(
            @Parameter(description = "The network UUID") @RequestParam(name = "networkUuid") @NonNull final UUID networkUuid,
            @Parameter(description = "The variant ID of the network") @RequestParam(name = "variantId") @NonNull final String variantId,
            @Parameter(description = "The equipments types to filter and return") @RequestParam(name = "equipmentTypes") @NonNull final List<EquipmentType> equipmentTypes,
            @Parameter(description = "The filter(s) to apply") @RequestBody @NonNull final GlobalFilter filterParams) {
        if (filterParams.isEmpty()) {
            throw HttpClientErrorException.create(HttpStatus.BAD_REQUEST, "At least one filter must be specified.", null, null, null);
        }
        return ResponseEntity.ok(service.getFilteredIds(networkUuid, variantId, filterParams, equipmentTypes));
    }
}
