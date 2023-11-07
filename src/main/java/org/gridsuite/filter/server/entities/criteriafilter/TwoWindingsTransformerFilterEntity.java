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

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.Set;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "two_windings_transformer_filter")
public class TwoWindingsTransformerFilterEntity extends AbstractGenericFilterEntity {
    @Column(name = "countries")
    @ElementCollection
    @CollectionTable(foreignKey = @ForeignKey(name = "twoWindingsTransformerFilterEntity_countries_fk"), indexes = {@Index(name = "twoWindingsTransformerFilterEntity_countries_idx", columnList = "two_windings_transformer_filter_entity_id")})
    Set<String> countries;

    @Column(name = "substationName")
    String substationName;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "numericFilterId1_id",
        referencedColumnName = "id",
        foreignKey = @ForeignKey(
            name = "twoWindingsTransformer_numericFilterId_id_fk1"
        ), nullable = true)
    NumericFilterEntity nominalVoltage1;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "twoWindingsTransformernumericFilterId2_id",
        referencedColumnName = "id",
        foreignKey = @ForeignKey(
            name = "twoWindingsTransformer_numericFilterId_id_fk2"
        ), nullable = true)
    NumericFilterEntity nominalVoltage2;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "substationFreeProperties_id",
        referencedColumnName = "id",
        foreignKey = @ForeignKey)
    FreePropertiesFilterEntity substationFreeProperties;
}
