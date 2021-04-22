package org.gridsuite.filters.server.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@NoArgsConstructor
@SuperBuilder
public abstract class AbstractGenericFilter extends AbstractFilterEntity {
    private String equipmentName;
    private String equipmentId;

}
