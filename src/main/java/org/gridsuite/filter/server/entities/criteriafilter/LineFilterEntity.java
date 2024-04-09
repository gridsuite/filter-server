/**
 *  Copyright (c) 2021, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.filter.server.entities.criteriafilter;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import jakarta.persistence.*;

import java.util.Set;

/**
 * @author Jacques Borsenberger <jacques.borsenberger at rte-france.com>
 */
@Getter
@NoArgsConstructor
@SuperBuilder
@AllArgsConstructor
@Entity
@Table(name = "line_filter")
public class LineFilterEntity extends AbstractGenericFilterEntity {
    @Column(name = "countries1")
    @ElementCollection
    @CollectionTable(foreignKey = @ForeignKey(name = "lineFilterEntity_countries_fk1"), indexes = {@Index(name = "lineFilterEntity_countries_idx1", columnList = "line_filter_entity_id")})
    Set<String> countries1;

    @Column(name = "countries2")
    @ElementCollection
    @CollectionTable(foreignKey = @ForeignKey(name = "lineFilterEntity_countries_fk2"), indexes = {@Index(name = "lineFilterEntity_countries_idx2", columnList = "line_filter_entity_id")})
    Set<String> countries2;

    @Column(name = "substationName1")
    String substationName1;
    @Column(name = "substationName2")
    String substationName2;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "numericFilterId1_id",
        referencedColumnName = "id",
        foreignKey = @ForeignKey(
            name = "line_numericFilterId_id_fk1"
        ), nullable = true)
    NumericFilterEntity nominalVoltage1;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "numericFilterId2_id",
        referencedColumnName = "id",
        foreignKey = @ForeignKey(
            name = "line_numericFilterId_id_fk2"
        ), nullable = true)
    NumericFilterEntity nominalVoltage2;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "substationFreeProperties1_id",
        referencedColumnName = "id",
        foreignKey = @ForeignKey)
    FreePropertiesFilterEntity substationFreeProperties1;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "substationFreeProperties2_id",
        referencedColumnName = "id",
        foreignKey = @ForeignKey)
    FreePropertiesFilterEntity substationFreeProperties2;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "freeProperties_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey)
    FreePropertiesFilterEntity freeProperties;
}
