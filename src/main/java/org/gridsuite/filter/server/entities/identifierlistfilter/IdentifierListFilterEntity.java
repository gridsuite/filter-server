/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.filter.server.entities.identifierlistfilter;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.gridsuite.filter.server.entities.AbstractFilterEntity;
import org.gridsuite.filter.utils.EquipmentType;

import java.util.List;

/**
 * @author Seddik Yengui <seddik.yengui at rte-france.com>
 */

@Getter
@NoArgsConstructor
@SuperBuilder
@AllArgsConstructor
@Entity
@Table(name = "identifier_list_filter")
public class IdentifierListFilterEntity extends AbstractFilterEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "equipmentType")
    private EquipmentType equipmentType;

    @OneToMany(cascade = CascadeType.ALL)
    @OrderColumn(name = "equipmentId_order")
    private List<IdentifierListFilterEquipmentEntity> filterEquipmentEntityList;
}
