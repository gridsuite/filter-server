/*
 *  Copyright (c) 2023, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.filter.server.entities.expertfilter;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.gridsuite.filter.utils.expertfilter.CombinatorType;
import org.gridsuite.filter.utils.expertfilter.DataType;
import org.gridsuite.filter.utils.expertfilter.FieldType;
import org.gridsuite.filter.utils.expertfilter.OperatorType;

import java.util.List;
import java.util.UUID;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@Getter
@NoArgsConstructor
@SuperBuilder
@AllArgsConstructor
@Entity
@Table(name = "expert_rule")
public class ExpertRuleEntity {
    @Id
    @Column(name = "id")
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "combinator")
    private CombinatorType combinator;

    @Enumerated(EnumType.STRING)
    @Column(name = "field")
    private FieldType field;

    // TODO FM this string can store an array too (',' separated), we should store an array because right now it's limited to 255 char
    @Column(name = "value_") // "value" is not supported in UT with H2
    private String value;

    @Enumerated(EnumType.STRING)
    @Column(name = "operator")
    private OperatorType operator;

    @Enumerated(EnumType.STRING)
    @Column(name = "dataType")
    private DataType dataType;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "parentRule")
    private List<ExpertRuleEntity> rules;

    @ManyToOne
    @JoinColumn(name = "parentRule_id", referencedColumnName = "id",
            foreignKey = @ForeignKey(
                    name = "expertRule_parentRule_fk"
            ), nullable = true)
    private ExpertRuleEntity parentRule;
}
