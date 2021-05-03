package org.gridsuite.filter.server.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import javax.persistence.*;

@Getter
@NoArgsConstructor
@SuperBuilder
@MappedSuperclass
public abstract class AbstractGenericFilterEntity extends AbstractFilterEntity {

    @Column(name = "equipmentName")
    private String equipmentName;
    @Column(name = "equipmentId")
    private String equipmentId;

}
