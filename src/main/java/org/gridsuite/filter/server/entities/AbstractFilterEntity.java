/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.server.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

import static javax.persistence.TemporalType.TIMESTAMP;

/**
 * @author Jacques Borsenberger <jacques.borsenberger at rte-france.com>
 */
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AbstractFilterEntity {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "name")
    private String name;

    @CreatedDate
    @Temporal(TIMESTAMP)
    @Column(name = "creationDate", updatable = false)
    private Date creationDate;

    @LastModifiedDate
    @Temporal(TIMESTAMP)
    @Column(name = "modificationDate")
    private Date modificationDate;

    @Column()
    private String description;
}
