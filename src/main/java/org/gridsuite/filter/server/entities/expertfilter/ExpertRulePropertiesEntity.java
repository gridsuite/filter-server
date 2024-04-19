/*
 *  Copyright (c) 2024, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.filter.server.entities.expertfilter;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * @author Maissa Souissi <maissa.souissi at rte-france.com>
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "expert_rule_properties")
public class ExpertRulePropertiesEntity extends ExpertRuleEntity {
    @Column(name = "property_name")
    private String propertyName;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "expert_rule_property_value")
    @OrderColumn(name = "pos")
    private List<String> propertyValues;
}

