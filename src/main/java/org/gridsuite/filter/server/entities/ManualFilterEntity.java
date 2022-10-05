package org.gridsuite.filter.server.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.gridsuite.filter.server.utils.EquipmentType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;

@Getter
@NoArgsConstructor
@SuperBuilder
@AllArgsConstructor
@Entity
@Table(name = "manual_filter")
public class ManualFilterEntity extends AbstractFilterEntity {

    @Column(name = "equipmentType")
    private EquipmentType equipmentType;

    @OneToMany
    private List<ManualFilterEquipmentEntity> filterEquipmentEntityList;
}
