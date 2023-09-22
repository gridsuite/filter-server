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

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToOne;
import java.util.Set;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@MappedSuperclass
public abstract class AbstractInjectionFilterEntity extends AbstractGenericFilterEntity {

    /* as AbstractInjectionFilterEntity is a mapped superclass naming constraints gives to each child class the same name
        for the constraint, liquibase only take one of these the others are discarded, so we let hibernate pick a name
    */
    @Column(name = "countries")
    @ElementCollection
    @CollectionTable(foreignKey = @ForeignKey())
    Set<String> countries;

    @Column(name = "substationName")
    String substationName;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "numericFilterId_id",
        referencedColumnName = "id",
        /* as AbstractInjectionFilterEntity is a mapped superclass naming constraints gives to each child class the same name
           for the constraint, liquibase only take one of these the others are discarded, so we let hibernate pick a name
            */
        foreignKey = @ForeignKey(), nullable = true)
    NumericFilterEntity nominalVoltage;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "substationFreeProperties_id",
        referencedColumnName = "id",
        foreignKey = @ForeignKey)
    FreePropertiesFilterEntity substationFreeProperties;
}
