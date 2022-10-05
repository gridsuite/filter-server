package org.gridsuite.filter.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.gridsuite.filter.server.utils.FilterType;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Getter
@Schema(description = "CSV File Filters", allOf = AbstractFilter.class)
@SuperBuilder
@NoArgsConstructor
public class CsvFileFilter extends AbstractFilter {

    private List<CsvFileFilterEquipmentAttributes> csvFileFilterEquipmentAttributes;

    public CsvFileFilter(UUID id,
                         Date creationDate,
                         Date modificationDate,
                         List<CsvFileFilterEquipmentAttributes> csvFileFilterEquipmentAttributes) {
        super(id, creationDate, modificationDate);
        this.csvFileFilterEquipmentAttributes = csvFileFilterEquipmentAttributes;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Override
    public FilterType getType() {
        return FilterType.IMPORT_CSV;
    }
}
