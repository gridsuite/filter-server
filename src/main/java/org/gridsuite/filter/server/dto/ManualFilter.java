package org.gridsuite.filter.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.gridsuite.filter.server.utils.EquipmentType;
import org.gridsuite.filter.server.utils.FilterType;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Getter
@Schema(description = "Manual Filters", allOf = AbstractFilter.class)
@SuperBuilder
@NoArgsConstructor
public class ManualFilter extends AbstractFilter {

    EquipmentType equipmentType;
    List<ManualFilterEquipmentAttributes> equipmentFilterAttributes;

    public ManualFilter(UUID id,
                        Date creationDate,
                        Date modificationDate,
                        EquipmentType equipmentType,
                        List<ManualFilterEquipmentAttributes> equipmentFilterAttributes) {
        super(id, creationDate, modificationDate);
        this.equipmentType = equipmentType;
        this.equipmentFilterAttributes = equipmentFilterAttributes;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Override
    public FilterType getType() {
        return FilterType.MANUAL;
    }

    public EquipmentType getEquipmentType() {
        return equipmentType;
    }
}
