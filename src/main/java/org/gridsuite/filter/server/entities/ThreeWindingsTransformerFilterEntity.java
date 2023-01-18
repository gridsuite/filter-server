/**
 *  Copyright (c) 2021, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.filter.server.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.Set;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "three_windings_transformer_filter")
public class ThreeWindingsTransformerFilterEntity extends AbstractGenericFilterEntity {
    @Column(name = "countries")
    @ElementCollection
    @CollectionTable(foreignKey = @ForeignKey(name = "threeWindingsTransformerFilterEntity_countries_fk"), indexes = {@Index(name = "threeWindingsTransformerFilterEntity_countries_idx", columnList = "three_windings_transformer_filter_entity_id")})
    Set<String> countries;

    @Column(name = "substationName")
    String substationName;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name  =  "threeWindingsTransformer_numericFilterId1_id",
        referencedColumnName  =  "id",
        foreignKey = @ForeignKey(
            name = "threeWindingsTransformer_numericFilterId_id_fk1"
        ), nullable = true)
    NumericFilterEntity nominalVoltage1;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name  =  "threeWindingsTransformer_numericFilterId2_id",
        referencedColumnName  =  "id",
        foreignKey = @ForeignKey(
            name = "threeWindingsTransformer_numericFilterId_id_fk2"
        ), nullable = true)
    NumericFilterEntity nominalVoltage2;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name  =  "threeWindingsTransformer_numericFilterId3_id",
        referencedColumnName  =  "id",
        foreignKey = @ForeignKey(
            name = "threeWindingsTransformer_numericFilterId_id_fk3"
        ), nullable = true)
    NumericFilterEntity nominalVoltage3;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "substationFreeProperties",
        referencedColumnName = "id",
        foreignKey = @ForeignKey)
    FreePropertiesFilterEntity substationFreeProperties;
}
