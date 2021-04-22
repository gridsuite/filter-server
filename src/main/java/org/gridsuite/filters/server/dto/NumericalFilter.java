package org.gridsuite.filters.server.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.gridsuite.filters.server.utils.RangeType;

@Getter
@AllArgsConstructor
public class NumericalFilter {
    RangeType type;
    Double value1;
    Double value2;

}
