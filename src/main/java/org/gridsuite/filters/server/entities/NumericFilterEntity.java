package org.gridsuite.filters.server.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.gridsuite.filters.server.utils.RangeType;
import org.springframework.data.cassandra.core.mapping.UserDefinedType;
import javax.persistence.*;

@Getter
@AllArgsConstructor
@UserDefinedType(value = "numericFilter")
public class NumericFilterEntity {

    RangeType filterType;
    Double value1;
    Double value2;

}
