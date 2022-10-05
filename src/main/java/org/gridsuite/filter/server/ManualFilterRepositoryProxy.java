package org.gridsuite.filter.server;

import com.powsybl.commons.PowsyblException;
import org.gridsuite.filter.server.dto.AbstractEquipmentFilterForm;
import org.gridsuite.filter.server.dto.AbstractFilter;
import org.gridsuite.filter.server.dto.ManualFilterEquipmentAttributes;
import org.gridsuite.filter.server.dto.ManualFilter;
import org.gridsuite.filter.server.entities.AbstractFilterEntity;
import org.gridsuite.filter.server.entities.ManualFilterEntity;
import org.gridsuite.filter.server.entities.ManualFilterEquipmentEntity;
import org.gridsuite.filter.server.repositories.ManualFilterRepository;
import org.gridsuite.filter.server.utils.FilterType;

import java.util.UUID;
import java.util.stream.Collectors;

public class ManualFilterRepositoryProxy extends AbstractFilterRepositoryProxy<ManualFilterEntity, ManualFilterRepository> {
    private final ManualFilterRepository manualFilterRepository;

    public ManualFilterRepositoryProxy(ManualFilterRepository manualFilterRepository) {
        this.manualFilterRepository = manualFilterRepository;
    }

    @Override
    ManualFilterRepository getRepository() {
        return manualFilterRepository;
    }

    @Override
    AbstractFilter toDto(ManualFilterEntity filterEntity) {
        return new ManualFilter(filterEntity.getId(),
                filterEntity.getCreationDate(),
                filterEntity.getModificationDate(),
                filterEntity.getEquipmentType(),
                filterEntity.getFilterEquipmentEntityList()
                        .stream()
                        .map(entity -> new ManualFilterEquipmentAttributes(entity.getEquipmentId(),
                                                                     entity.getDistributionKey()))
                        .collect(Collectors.toList()));
    }

    @Override
    ManualFilterEntity fromDto(AbstractFilter dto) {
        if (dto instanceof ManualFilter) {
            var filter = (ManualFilter) dto;
            var manualFilterEntityBuilder = ManualFilterEntity.builder()
                    .equipmentType(filter.getEquipmentType())
                    .filterEquipmentEntityList(filter.getEquipmentFilterAttributes()
                            .stream()
                            .map(attributes -> ManualFilterEquipmentEntity.builder()
                                    .id(UUID.randomUUID())
                                    .equipmentId(attributes.getEquipmentID())
                                    .distributionKey(attributes.getDistributionKey())
                                    .build())
                            .collect(Collectors.toList()));

            buildAbstractFilter(manualFilterEntityBuilder, filter);
            return manualFilterEntityBuilder.build();
        }
        throw new PowsyblException(WRONG_FILTER_TYPE);
    }

    @Override
    FilterType getFilterType() {
        return null;
    }

    @Override
    public AbstractEquipmentFilterForm buildEquipmentFormFilter(AbstractFilterEntity entity) {
        return null;
    }
}
