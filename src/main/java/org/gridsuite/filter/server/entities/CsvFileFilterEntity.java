package org.gridsuite.filter.server.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.gridsuite.filter.server.utils.EquipmentType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Getter
@NoArgsConstructor
@SuperBuilder
@AllArgsConstructor
@Entity
@Table(name = "csv_file_filter")
public class CsvFileFilterEntity extends AbstractFilterEntity {
    @Column(name = "equipmentType")
    EquipmentType equipmentType;

    @Column(name = "equipmentId")
    private String equipmentId;

    @Column(name = "distributionKey")
    private Double distributionKey;
}
