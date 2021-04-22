package org.gridsuite.filters.server.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.gridsuite.filters.server.utils.FilterType;

@Getter
@AllArgsConstructor
public class FilterAttributes {
    String name;
    FilterType type;
}
