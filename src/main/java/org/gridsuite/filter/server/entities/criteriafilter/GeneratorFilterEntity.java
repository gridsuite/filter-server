/**
 *  Copyright (c) 2021, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.filter.server.entities.criteriafilter;

import com.powsybl.iidm.network.EnergySource;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.*;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@Getter
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "generator_filter")
public class GeneratorFilterEntity extends AbstractInjectionFilterEntity {
    @Enumerated(EnumType.STRING)
    @Column(name = "energySource")
    EnergySource energySource;
}
