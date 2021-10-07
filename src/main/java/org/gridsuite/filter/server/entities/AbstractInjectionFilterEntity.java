/**
 *  Copyright (c) 2021, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.filter.server.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;
import java.util.Set;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@Getter
@NoArgsConstructor
@SuperBuilder
@MappedSuperclass
public abstract class AbstractInjectionFilterEntity extends AbstractGenericFilterEntity {
    @Column(name = "countries")
    @ElementCollection
    @CollectionTable(foreignKey = @ForeignKey(name = "abstractInjectionFilterEntity_countries_fk"))
    Set<String> countries;

    @Column(name = "substationName")
    String substationName;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name  =  "numericFilterId_id",
        referencedColumnName  =  "id",
        foreignKey = @ForeignKey(
            name = "numericFilterId_id_fk"
        ), nullable = true)
    NumericFilterEntity nominalVoltage;
}