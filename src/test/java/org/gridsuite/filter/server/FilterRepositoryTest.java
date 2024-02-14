/*
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.powsybl.commons.PowsyblException;
import org.gridsuite.filter.server.dto.criteriafilter.CriteriaFilter;
import org.gridsuite.filter.server.dto.criteriafilter.LineFilter;
import org.gridsuite.filter.server.dto.criteriafilter.NumericalFilter;
import org.gridsuite.filter.server.entities.criteriafilter.AbstractInjectionFilterEntity;
import org.gridsuite.filter.server.entities.criteriafilter.GeneratorFilterEntity;
import org.gridsuite.filter.server.repositories.criteriafilter.GeneratorFilterRepository;
import org.gridsuite.filter.server.repositories.proxies.criteriafilter.GeneratorFilterRepositoryProxy;
import org.gridsuite.filter.server.utils.RangeType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

import static org.junit.Assert.assertThrows;

/**
 * @author Hugo Marcellin <hugo.marcelin at rte-france.com>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class FilterRepositoryTest {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    GeneratorFilterRepository generatorFilterRepository;
    GeneratorFilterRepositoryProxy generatorFilterRepositoryProxy;

    @Before
    public void setUp() {
        generatorFilterRepositoryProxy = new GeneratorFilterRepositoryProxy(generatorFilterRepository);
    }

    @Test
    public void buildWrongFilterType() throws Exception {
        LineFilter lineFilter = LineFilter.builder().equipmentID("NHV1_NHV2_1")
            .substationName1("P1")
            .substationName2("P2")
            .countries1(new TreeSet<>(Set.of("FR")))
            .countries2(new TreeSet<>(Set.of("FR")))
            .freeProperties2(Map.of("region", List.of("north")))
            .freeProperties1(Map.of("region", List.of("south")))
            .nominalVoltage1(new NumericalFilter(RangeType.RANGE, 360., 400.))
            .nominalVoltage2(new NumericalFilter(RangeType.RANGE, 356.25, 393.75))
            .build();

        AbstractInjectionFilterEntity.AbstractInjectionFilterEntityBuilder<?, ?> builder = GeneratorFilterEntity.builder();
        CriteriaFilter criteriaFilter = CriteriaFilter.builder().equipmentFilterForm(lineFilter).build();
        assertThrows("Wrong filter type, should never happen", PowsyblException.class, () -> generatorFilterRepositoryProxy.buildInjectionFilter(builder, criteriaFilter));
    }
}
