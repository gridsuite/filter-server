package org.gridsuite.filter.server;

import com.powsybl.commons.PowsyblException;
import org.gridsuite.filter.server.dto.AbstractEquipmentFilterForm;
import org.gridsuite.filter.server.dto.AbstractFilter;
import org.gridsuite.filter.server.dto.CsvFileFilter;
import org.gridsuite.filter.server.dto.CsvFileFilterEquipmentAttributes;
import org.gridsuite.filter.server.entities.AbstractFilterEntity;
import org.gridsuite.filter.server.entities.CsvFileFilterEntity;
import org.gridsuite.filter.server.entities.CsvFileFilterEquipmentEntity;
import org.gridsuite.filter.server.repositories.CsvFileFilterRepository;
import org.gridsuite.filter.server.utils.FilterType;

import java.util.stream.Collectors;

public class CsvFilterRepositoryProxy extends AbstractFilterRepositoryProxy<CsvFileFilterEntity, CsvFileFilterRepository> {
    private CsvFileFilterRepository csvFileFilterRepository;

    public CsvFilterRepositoryProxy(CsvFileFilterRepository csvFileFilterRepository) {
        this.csvFileFilterRepository = csvFileFilterRepository;
    }

    @Override
    CsvFileFilterRepository getRepository() {
        return csvFileFilterRepository;
    }

    @Override
    AbstractFilter toDto(CsvFileFilterEntity filterEntity) {
        return new CsvFileFilter(filterEntity.getId(),
                filterEntity.getCreationDate(),
                filterEntity.getModificationDate(),
                filterEntity.getCsvFileFilterEquipmentEntityList()
                        .stream()
                        .map(equipment -> new CsvFileFilterEquipmentAttributes(equipment.getEquipmentType(),
                                equipment.getEquipmentId(),
                                equipment.getDistributionKey()))
                        .collect(Collectors.toList()));
    }

    @Override
    CsvFileFilterEntity fromDto(AbstractFilter dto) {
        if (dto instanceof CsvFileFilter) {
            var filter = (CsvFileFilter) dto;
            var csvManualFilterEntityBuilder = CsvFileFilterEntity.builder()
                    .csvFileFilterEquipmentEntityList(filter.getCsvFileFilterEquipmentAttributes()
                            .stream()
                            .map(attribute -> CsvFileFilterEquipmentEntity.builder()
                                                    .equipmentType(attribute.getEquipmentType())
                                                            .equipmentId(attribute.getEquipmentID())
                                                                    .distributionKey(attribute.getDistributionKey())
                                                                            .build())
                            .collect(Collectors.toList()));

            buildAbstractFilter(csvManualFilterEntityBuilder, filter);
            return csvManualFilterEntityBuilder.build();
        }
        throw new PowsyblException(WRONG_FILTER_TYPE);
    }

    @Override
    FilterType getFilterType() {
        return FilterType.IMPORT_CSV;
    }

    @Override
    public AbstractEquipmentFilterForm buildEquipmentFormFilter(AbstractFilterEntity entity) {
        return null;
    }
}
