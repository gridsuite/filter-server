package org.gridsuite.filters.server.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.Set;

@Getter
@NoArgsConstructor
@SuperBuilder
@Table("line_filters")
public class LineFilterEntity extends AbstractGenericFilter {
    Set<String> countries1;
    Set<String> countries2;

    String substationName1;
    String substationName2;

    NumericFilterEntity nominalVoltage1;
    NumericFilterEntity nominalVoltage2;

}
