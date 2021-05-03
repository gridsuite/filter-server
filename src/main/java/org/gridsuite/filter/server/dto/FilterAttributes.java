package org.gridsuite.filter.server.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.gridsuite.filter.server.utils.FilterType;

@Getter
@AllArgsConstructor
public class FilterAttributes {
    String name;
    FilterType type;
}
