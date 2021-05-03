package org.gridsuite.filter.server.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.gridsuite.filter.server.utils.RangeType;

import javax.persistence.*;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "numericFilter")
public class NumericFilterEntity {
    @Id
    @GeneratedValue(strategy  =  GenerationType.AUTO)
    @Column(name = "id")
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "rangeType")
    RangeType filterType;

    @Column(name = "value1")
    Double value1;

    @Column(name = "value2")
    Double value2;

}
