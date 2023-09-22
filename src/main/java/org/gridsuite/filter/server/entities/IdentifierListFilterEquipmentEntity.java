/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.filter.server.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

/**
 * @author Seddik Yengui <seddik.yengui at rte-france.com>
 */

@Getter
@NoArgsConstructor
@SuperBuilder
@AllArgsConstructor
@Entity
@Table(name = "identifier_list_filter_equipment")
public class IdentifierListFilterEquipmentEntity {
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "equipmentId")
    private String equipmentId;

    @Column(name = "distributionKey")
    private Double distributionKey;
}
