package org.gridsuite.filter.server.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import javax.persistence.*;

import java.util.Set;

@Getter
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "line_filter")
public class LineFilterEntity extends AbstractGenericFilterEntity {
    @Column(name = "countries1")
    @ElementCollection
    @CollectionTable(foreignKey = @ForeignKey(name = "lineFilterEntity_countries_fk1"), indexes = {@Index(name = "lineFilterEntity_countries_idx1", columnList = "lineFilterEntity_name")})
    Set<String> countries1;

    @Column(name = "countries2")
    @ElementCollection
    @CollectionTable(foreignKey = @ForeignKey(name = "lineFilterEntity_countries_fk2"), indexes = {@Index(name = "lineFilterEntity_countries_idx2", columnList = "lineFilterEntity_name")})
    Set<String> countries2;

    @Column(name = "substationName1")
    String substationName1;
    @Column(name = "substationName2")
    String substationName2;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name  =  "numericFilterId1_id",
        referencedColumnName  =  "id",
        foreignKey = @ForeignKey(
            name = "numericFilterId_id_fk1"
        ), nullable = true)
    NumericFilterEntity nominalVoltage1;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name  =  "numericFilterId2_id",
        referencedColumnName  =  "id",
        foreignKey = @ForeignKey(
            name = "numericFilterId_id_fk2"
        ), nullable = true)
    NumericFilterEntity nominalVoltage2;

}
