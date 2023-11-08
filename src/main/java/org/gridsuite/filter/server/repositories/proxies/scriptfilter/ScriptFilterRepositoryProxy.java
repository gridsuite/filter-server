/*
 *  Copyright (c) 2021, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.filter.server.repositories.proxies.scriptfilter;

import com.powsybl.commons.PowsyblException;
import org.gridsuite.filter.server.dto.criteriafilter.AbstractEquipmentFilterForm;
import org.gridsuite.filter.server.dto.AbstractFilter;
import org.gridsuite.filter.server.dto.scriptfilter.ScriptFilter;
import org.gridsuite.filter.server.entities.AbstractFilterEntity;
import org.gridsuite.filter.server.entities.scriptfilter.ScriptFilterEntity;
import org.gridsuite.filter.server.repositories.scriptfilter.ScriptFilterRepository;
import org.gridsuite.filter.server.repositories.proxies.AbstractFilterRepositoryProxy;
import org.gridsuite.filter.server.utils.EquipmentType;
import org.gridsuite.filter.server.utils.FilterType;

/**
 * @author Jacques Borsenberger <jacques.borsenberger at rte-france.com>
 */
public class ScriptFilterRepositoryProxy extends AbstractFilterRepositoryProxy<ScriptFilterEntity, ScriptFilterRepository> {
    private final ScriptFilterRepository scriptFiltersRepository;

    public ScriptFilterRepositoryProxy(ScriptFilterRepository scriptFiltersRepository) {
        this.scriptFiltersRepository = scriptFiltersRepository;
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.SCRIPT;
    }

    @Override
    public EquipmentType getEquipmentType() {
        return null;
    }

    @Override
    public AbstractEquipmentFilterForm buildEquipmentFormFilter(AbstractFilterEntity entity) {
        return null;
    }

    @Override
    public ScriptFilterRepository getRepository() {
        return scriptFiltersRepository;
    }

    @Override
    public AbstractFilter toDto(ScriptFilterEntity entity) {
        return new ScriptFilter(entity.getId(), entity.getModificationDate(), entity.getScript());
    }

    @Override
    public ScriptFilterEntity fromDto(AbstractFilter dto) {
        if (dto instanceof ScriptFilter) {
            var filter = (ScriptFilter) dto;
            var scriptBuilderEntity = ScriptFilterEntity.builder()
                .script(filter.getScript());
            buildAbstractFilter(scriptBuilderEntity, filter);
            return scriptBuilderEntity.build();
        }
        throw new PowsyblException(WRONG_FILTER_TYPE);
    }
}
