package org.gridsuite.filter.server.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Getter
@NoArgsConstructor
@SuperBuilder
@AllArgsConstructor
@Entity
@Table(name = "manual_filter_equipment")
public class ManualFilterEquipmentEntity{
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "equipmentId")
    private String equipmentId;

    @Column(name = "distributionKey")
    private Double distributionKey;
}
