/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.server;

import com.powsybl.commons.PowsyblException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.gridsuite.filter.server.dto.AbstractFilter;
import org.gridsuite.filter.server.dto.LineFilter;
import org.gridsuite.filter.server.dto.NumericalFilter;
import org.gridsuite.filter.server.utils.RangeType;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.CollectionUtils;
import org.stringtemplate.v4.ST;

import java.io.IOException;
import java.nio.charset.Charset;

import static java.util.stream.Collectors.joining;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class FiltersToGroovyScript {
    private final String branchTemplate;
    private static final String NOMINAL_V = "nominalV";

    public FiltersToGroovyScript() {
        try {
            branchTemplate = IOUtils.toString(new ClassPathResource("branch.st").getInputStream(), Charset.defaultCharset());
        } catch (IOException e) {
            throw new PowsyblException("Unable to load templates for groovy script generation !!");
        }
    }

    private void addLineFilterNominalVoltage(ST template, NumericalFilter filter, String index) {
        template.add(NOMINAL_V + index + "Type", filter.getType().name());
        if (filter.getType() == RangeType.EQUALITY) {
            template.add(NOMINAL_V + index + "Equality", "true");
            template.add(NOMINAL_V + index, filter.getValue1() != null ? filter.getValue1() : "null");
        } else if (filter.getType() == RangeType.RANGE) {
            template.add(NOMINAL_V + index + "Range", "true");
            template.add("minNominalV" + index, filter.getValue1() != null ? filter.getValue1() : "null");
            template.add("maxNominalV" + index, filter.getValue2() != null ? filter.getValue2() : "null");
        } else if (filter.getType() == RangeType.APPROX) {
            template.add(NOMINAL_V + index + "Approx", "true");
            template.add(NOMINAL_V + index, filter.getValue1() != null ? filter.getValue1() : "null");
            template.add("percentNominalV" + index, filter.getValue2() != null ? filter.getValue2() : "null");
        }
    }

    public String generateGroovyScriptFromFilters(AbstractFilter filter) {
        String script = "";
        String equipmentsCollection = "";

        switch (filter.getType()) {
            case LINE:
                equipmentsCollection = "lines";
                script += branchTemplate;
                break;
            // other types (generators, loads ...) later
            default:
                throw new PowsyblException("Filter type not allowed");
        }

        ST template = new ST(script);

        switch (filter.getType()) {
            case LINE:
                LineFilter lineFilter = (LineFilter) filter;
                template.add("collectionName", equipmentsCollection);
                if (!lineFilter.isEmpty()) {
                    template.add("noEmptyFilter", "true");
                }
                if (lineFilter.getEquipmentID() != null) {
                    template.add("equipmentId", lineFilter.getEquipmentID());
                }
                if (lineFilter.getEquipmentName() != null) {
                    template.add("equipmentName", lineFilter.getEquipmentName());
                }
                if (!CollectionUtils.isEmpty(lineFilter.getCountries1())) {
                    template.add("countries1", lineFilter.getCountries1().stream().collect(joining("','", "['", "']")));
                }
                if (!CollectionUtils.isEmpty(lineFilter.getCountries2())) {
                    template.add("countries2", lineFilter.getCountries2().stream().collect(joining("','", "['", "']")));
                }
                if (lineFilter.getNominalVoltage1() != null) {
                    addLineFilterNominalVoltage(template, lineFilter.getNominalVoltage1(), "1");
                }
                if (lineFilter.getNominalVoltage2() != null) {
                    addLineFilterNominalVoltage(template, lineFilter.getNominalVoltage2(), "2");
                }
                if (!StringUtils.isEmpty(lineFilter.getSubstationName1())) {
                    template.add("substationName1", lineFilter.getSubstationName1());
                }
                if (!StringUtils.isEmpty(lineFilter.getSubstationName2())) {
                    template.add("substationName2", lineFilter.getSubstationName2());
                }
                break;
            // other types (generators, loads ...) later
            default:
                throw new PowsyblException("Filter type not allowed");
        }

        return template.render();
    }
}
