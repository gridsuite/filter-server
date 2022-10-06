package org.gridsuite.filter.server.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;

@Getter
@NoArgsConstructor
@SuperBuilder
@AllArgsConstructor
@Entity
@Table(name = "csv_file_filter")
public class CsvFileFilterEntity extends AbstractFilterEntity {

    @OneToMany(cascade = CascadeType.ALL)
    List<CsvFileFilterEquipmentEntity> csvFileFilterEquipmentEntityList;
}
