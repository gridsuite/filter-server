/*
 *  Copyright (c) 2021, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.filter.server.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.gridsuite.filter.server.dto.NumericalFilter;
import org.gridsuite.filter.server.utils.RangeType;

import javax.persistence.*;
import java.util.UUID;

/**
 * @author Jacques Borsenberger <jacques.borsenberger at rte-france.com>
 */
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
