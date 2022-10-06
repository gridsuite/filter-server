package org.gridsuite.filter.server.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.gridsuite.filter.server.utils.EquipmentType;

@AllArgsConstructor
@Getter
public class CsvFileFilterEquipmentAttributes {

    @Schema(description = "Equipment Type")
    private EquipmentType equipmentType;

    @Schema(description = "Equipment ID")
    private String equipmentId;

    @Schema(description = "Distribution Key")
    private Double distributionKey;
}
