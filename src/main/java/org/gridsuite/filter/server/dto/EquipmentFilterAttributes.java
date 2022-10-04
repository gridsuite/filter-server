package org.gridsuite.filter.server.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class EquipmentFilterAttributes {

    @Schema(description = "Equipment ID")
    private String equipmentID;

    @Schema(description = "Distribution Key")
    private Double distributionKey;
}
