/*
 *  Copyright (c) 2021, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.filter.server;

import com.powsybl.commons.PowsyblException;
import org.gridsuite.filter.server.dto.AbstractFilter;
import org.gridsuite.filter.server.dto.ScriptFilter;
import org.gridsuite.filter.server.entities.ScriptFilterEntity;
import org.gridsuite.filter.server.repositories.ScriptFilterRepository;
import org.gridsuite.filter.server.utils.FilterType;

/**
 * @author Jacques Borsenberger <jacques.borsenberger at rte-france.com>
 */

class ScriptFilterRepositoryProxy extends AbstractFilterRepositoryProxy<ScriptFilterEntity, ScriptFilterRepository> {
    private final ScriptFilterRepository scriptFiltersRepository;

    public ScriptFilterRepositoryProxy(ScriptFilterRepository scriptFiltersRepository) {
        this.scriptFiltersRepository = scriptFiltersRepository;
    }

    @Override
    public FilterType getRepositoryType() {
        return FilterType.SCRIPT;
    }

    @Override
    public ScriptFilterRepository getRepository() {
        return scriptFiltersRepository;
    }

    @Override
    public AbstractFilter toDto(ScriptFilterEntity entity) {
        return buildAbstractFilter(
            ScriptFilter.builder()
                .script(entity.getScript()),
            entity).build();
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
