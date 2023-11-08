/**
 *  Copyright (c) 2022, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.filter.server.entities.criteriafilter;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.UUID;

/**
 * @author Laurent Garnier <laurent.garnier at rte-france.com>
 */
@Getter
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "free_property")
public class FreePropertyFilterEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    UUID id;

    @Column(name = "propName", nullable = false)
    String propName;

    @Column(name = "prop_values")
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "prop_value")
    @OrderColumn(name = "pos")
    List<String> propValues;
}
