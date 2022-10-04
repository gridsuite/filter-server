package org.gridsuite.filter.server;

import com.powsybl.commons.PowsyblException;
import org.gridsuite.filter.server.dto.AbstractEquipmentFilterForm;
import org.gridsuite.filter.server.dto.AbstractFilter;
import org.gridsuite.filter.server.dto.CsvFileFilter;
import org.gridsuite.filter.server.dto.EquipmentFilterAttributes;
import org.gridsuite.filter.server.dto.ManualFilter;
import org.gridsuite.filter.server.entities.AbstractFilterEntity;
import org.gridsuite.filter.server.entities.CsvFileFilterEntity;
import org.gridsuite.filter.server.entities.ManualFilterEntity;
import org.gridsuite.filter.server.entities.ManualFilterEquipmentEntity;
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
                new EquipmentFilterAttributes(filterEntity.getEquipmentId(), filterEntity.getDistributionKey()),
                filterEntity.getEquipmentType());
    }

    @Override
    CsvFileFilterEntity fromDto(AbstractFilter dto) {
        if (dto instanceof CsvFileFilter) {
            var filter = (CsvFileFilter) dto;
            var csvManualFilterEntityBuilder = CsvFileFilterEntity.builder()
                    .equipmentType(filter.getEquipmentType())
                    .distributionKey(filter.getEquipmentFilterAttributes().getDistributionKey())
                    .equipmentId(filter.getEquipmentFilterAttributes().getEquipmentID());

            buildAbstractFilter(csvManualFilterEntityBuilder, filter);
            return csvManualFilterEntityBuilder.build();
        }
        throw new PowsyblException(WRONG_FILTER_TYPE);
    }

    @Override
    FilterType getFilterType() {
        return FilterType.CSV_FILE;
    }

    @Override
    public AbstractEquipmentFilterForm buildEquipmentFormFilter(AbstractFilterEntity entity) {
        return null;
    }
}
