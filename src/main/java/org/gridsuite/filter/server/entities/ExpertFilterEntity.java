/*
 *  Copyright (c) 2023, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.filter.server.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.gridsuite.filter.server.utils.EquipmentType;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "expert_filter")
public class ExpertFilterEntity extends AbstractFilterEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "equipmentType")
    private EquipmentType equipmentType;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "expertFilterEntity_rules_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "expertFilterEntity_rules_fk"
            ))
    private ExpertRuleEntity rules;
}
