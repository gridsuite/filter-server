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
@Table(name = "hvdc_line_filter")
public class HvdcLineFilterEntity extends AbstractGenericFilterEntity {
    @Column(name = "countries1")
    @ElementCollection
    @CollectionTable(foreignKey = @ForeignKey(name = "hvdcLineFilterEntity_countries_fk1"), indexes = {@Index(name = "hvdcLineFilterEntity_countries_idx1", columnList = "hvdc_line_filter_entity_id")})
    Set<String> countries1;

    @Column(name = "countries2")
    @ElementCollection
    @CollectionTable(foreignKey = @ForeignKey(name = "hvdcLineFilterEntity_countries_fk2"), indexes = {@Index(name = "hvdcLineFilterEntity_countries_idx2", columnList = "hvdc_line_filter_entity_id")})
    Set<String> countries2;

    @Column(name = "substationName1")
    String substationName1;

    @Column(name = "substationName2")
    String substationName2;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "hvdcLineFilterEntity_numericFilterId_id",
        referencedColumnName = "id",
        foreignKey = @ForeignKey(
            name = "numericFilterId_id_fk"
        ), nullable = true)
    NumericFilterEntity nominalVoltage;

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
}
