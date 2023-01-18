/**
 *  Copyright (c) 2022, RTE (http://www.rte-france.com)
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
@Table(name = "voltage_level_filter")
public class VoltageLevelFilterEntity extends AbstractGenericFilterEntity {
    @Column(name = "countries")
    @ElementCollection
    @CollectionTable(foreignKey = @ForeignKey(name = "voltageLevelFilterEntity_countries_fk"), indexes = {@Index(name = "voltageLevelFilterEntity_countries_idx", columnList = "voltage_level_filter_entity_id")})
    Set<String> countries;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "substationFreeProperties",
        referencedColumnName = "id",
        foreignKey = @ForeignKey)
    FreePropertiesFilterEntity substationFreeProperties;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name  =  "numericFilterId_id",
        referencedColumnName  =  "id",
        foreignKey = @ForeignKey(
            name = "voltageLevel_numericFilterId_id_fk"
        ), nullable = true)
    NumericFilterEntity nominalVoltage;
}
