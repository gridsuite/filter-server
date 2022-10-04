package org.gridsuite.filter.server.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.gridsuite.filter.server.utils.EquipmentType;
import org.gridsuite.filter.server.utils.FilterType;

import java.util.Date;
import java.util.UUID;

@Getter
@Schema(description = "CSV File Filters", allOf = AbstractFilter.class)
@SuperBuilder
@NoArgsConstructor
public class CsvFileFilter extends AbstractFilter{

    private EquipmentFilterAttributes equipmentFilterAttributes;

    @Schema(description = "Equipment Type")
    private EquipmentType equipmentType;

    public CsvFileFilter(UUID id,
                         Date creationDate,
                         Date modificationDate,
                         EquipmentFilterAttributes equipmentFilterAttributes,
                         EquipmentType equipmentType) {
        super(id, creationDate, modificationDate);
        this.equipmentFilterAttributes = equipmentFilterAttributes;
        this.equipmentType = equipmentType;
    }

    @Override
    public FilterType getType() {
        return FilterType.CSV_FILE;
    }
}
