/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.server;

import com.powsybl.commons.PowsyblException;
import org.gridsuite.filter.server.dto.IFilterAttributes;
import org.gridsuite.filter.server.dto.AbstractFilter;
import org.gridsuite.filter.server.dto.ScriptFilter;
import org.gridsuite.filter.server.repositories.LineFilterRepository;
import org.gridsuite.filter.server.repositories.ScriptFilterRepository;
import org.gridsuite.filter.server.utils.FilterType;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

import static org.gridsuite.filter.server.AbstractFilterRepositoryProxy.WRONG_FILTER_TYPE;

@Service
public class FilterService {

    private static final String FILTER_LIST = "Filter list ";
    private static final String NOT_FOUND = " not found";

    private final EnumMap<FilterType, AbstractFilterRepositoryProxy<?, ?>> filterRepositories = new EnumMap<>(FilterType.class);

    private FiltersToGroovyScript filtersToScript;

    public FilterService(FiltersToGroovyScript filtersToScript,
                         final ScriptFilterRepository scriptFiltersRepository,
                         final LineFilterRepository lineFilterRepository) {
        this.filtersToScript = filtersToScript;
        filterRepositories.put(FilterType.LINE, new LineFilterRepositoryProxy(lineFilterRepository));
        filterRepositories.put(FilterType.SCRIPT, new ScriptFilterRepositoryProxy(scriptFiltersRepository));
    }

    List<IFilterAttributes> getFilters() {
        return filterRepositories.entrySet().stream()
            .flatMap(entry -> entry.getValue().getFiltersAttributes())
            .collect(Collectors.toList());
    }

    List<IFilterAttributes> getFilters(List<UUID> ids) {
        return filterRepositories.entrySet().stream()
            .flatMap(entry -> entry.getValue().getFiltersAttributes(ids))
            .collect(Collectors.toList());
    }

    Optional<AbstractFilter> getFilter(UUID id) {
        Objects.requireNonNull(id);
        for (AbstractFilterRepositoryProxy<?, ?> repository : filterRepositories.values()) {
            Optional<AbstractFilter> res = repository.getFilter(id);
            if (res.isPresent()) {
                return res;
            }
        }
        return Optional.empty();
    }

    @Transactional
    public <F extends AbstractFilter> AbstractFilter createFilter(F filter) {
        return filterRepositories.get(filter.getType()).insert(filter);
    }

    @Transactional
    public <F extends AbstractFilter> void changeFilter(UUID id, F filter) {
        filterRepositories.get(filter.getType()).modify(id, filter);
    }

    void deleteFilter(UUID id) {
        Objects.requireNonNull(id);
        if (filterRepositories.values().stream().noneMatch(repository -> repository.deleteById(id))) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, FILTER_LIST + id + NOT_FOUND);
        }
    }

    public void deleteAll() {
        filterRepositories.values().forEach(AbstractFilterRepositoryProxy::deleteAll);
    }

    private String generateGroovyScriptFromFilter(AbstractFilter filter) {
        return filtersToScript.generateGroovyScriptFromFilters(filter);
    }

    @Transactional
    public AbstractFilter replaceFilterWithScript(UUID id) {
        Objects.requireNonNull(id);

        Optional<AbstractFilter> filter = getFilter(id);
        if (filter.isPresent()) {
            if (filter.get().getType() == FilterType.SCRIPT) {
                throw new PowsyblException(WRONG_FILTER_TYPE);
            } else {
                String script = generateGroovyScriptFromFilter(filter.get());
                filterRepositories.get(filter.get().getType()).deleteById(filter.get().getId());
                return filterRepositories.get(FilterType.SCRIPT).insert(ScriptFilter.builder().id(filter.get().getId()).name(filter.get().getName()).script(script).build());
            }
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, FILTER_LIST + id + NOT_FOUND);
        }
    }

    @Transactional
    public AbstractFilter newScriptFromFilter(UUID id, String scriptName) {
        Objects.requireNonNull(id);

        Optional<AbstractFilter> filter = getFilter(id);
        if (filter.isPresent()) {
            if (filter.get().getType() == FilterType.SCRIPT) {
                throw new PowsyblException(WRONG_FILTER_TYPE);
            } else {
                String script = generateGroovyScriptFromFilter(filter.get());
                return filterRepositories.get(FilterType.SCRIPT).insert(ScriptFilter.builder().name(scriptName).script(script).build());
            }
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, FILTER_LIST + id + NOT_FOUND);
        }
    }
}
