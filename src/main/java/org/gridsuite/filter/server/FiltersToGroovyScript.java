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
import org.gridsuite.filter.server.dto.AbstractInjectionFilter;
import org.gridsuite.filter.server.dto.HvdcLineFilter;
import org.gridsuite.filter.server.dto.LineFilter;
import org.gridsuite.filter.server.dto.NumericalFilter;
import org.gridsuite.filter.server.dto.ThreeWindingsTransformerFilter;
import org.gridsuite.filter.server.dto.TwoWindingsTransformerFilter;
import org.gridsuite.filter.server.utils.RangeType;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.stringtemplate.v4.ST;

import java.io.IOException;
import java.nio.charset.Charset;

import static java.util.stream.Collectors.joining;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@Service
public class FiltersToGroovyScript {
    private static final String COLLECTION_NAME = "collectionName";
    private static final String NO_EMPTY_FILTER = "noEmptyFilter";
    private static final String EQUIPMENT_ID = "equipmentId";
    private static final String EQUIPMENT_NAME = "equipmentName";
    private static final String COUNTRIES = "countries";
    private static final String SUBSTATION_NAME = "substationName";

    private final String lineTemplate;
    private final String injectionTemplate;
    private final String twoWindingsTransformerTemplate;
    private final String threeWindingsTransformerTemplate;
    private final String hvdcLineTemplate;

    private static final String NOMINAL_V = "nominalV";

    public FiltersToGroovyScript() {
        try {
            lineTemplate = IOUtils.toString(new ClassPathResource("line.st").getInputStream(), Charset.defaultCharset());
            injectionTemplate = IOUtils.toString(new ClassPathResource("injection.st").getInputStream(), Charset.defaultCharset());
            twoWindingsTransformerTemplate = IOUtils.toString(new ClassPathResource("twoWindingsTransformer.st").getInputStream(), Charset.defaultCharset());
            threeWindingsTransformerTemplate = IOUtils.toString(new ClassPathResource("threeWindingsTransformer.st").getInputStream(), Charset.defaultCharset());
            hvdcLineTemplate = IOUtils.toString(new ClassPathResource("hvdcLine.st").getInputStream(), Charset.defaultCharset());
        } catch (IOException e) {
            throw new PowsyblException("Unable to load templates for groovy script generation !!");
        }
    }

    private void addFilterNominalVoltage(ST template, NumericalFilter filter, String index) {
        String indexStr = index != null ? index : "";
        template.add(NOMINAL_V + indexStr + "Type", filter.getType().name());
        if (filter.getType() == RangeType.EQUALITY) {
            template.add(NOMINAL_V + indexStr + "Equality", "true");
            template.add(NOMINAL_V + indexStr, filter.getValue1() != null ? filter.getValue1() : "null");
        } else if (filter.getType() == RangeType.RANGE) {
            template.add(NOMINAL_V + indexStr + "Range", "true");
            template.add("minNominalV" + indexStr, filter.getValue1() != null ? filter.getValue1() : "null");
            template.add("maxNominalV" + indexStr, filter.getValue2() != null ? filter.getValue2() : "null");
        } else if (filter.getType() == RangeType.APPROX) {
            template.add(NOMINAL_V + indexStr + "Approx", "true");
            template.add(NOMINAL_V + indexStr, filter.getValue1() != null ? filter.getValue1() : "null");
            template.add("percentNominalV" + indexStr, filter.getValue2() != null ? filter.getValue2() : "null");
        }
    }

    public String generateGroovyScriptFromFilters(AbstractFilter filter) {
        String script = "";
        String equipmentsCollection = filter.getType().getCollectionName();

        switch (filter.getType()) {
            case LINE:
                script += lineTemplate;
                break;
            case TWO_WINDINGS_TRANSFORMER:
                script += twoWindingsTransformerTemplate;
                break;
            case THREE_WINDINGS_TRANSFORMER:
                script += threeWindingsTransformerTemplate;
                break;
            case GENERATOR:
            case LOAD:
            case BATTERY:
            case DANGLING_LINE:
            case BUSBAR_SECTION:
            case SHUNT_COMPENSATOR:
            case STATIC_VAR_COMPENSATOR:
            case LCC_CONVERTER_STATION:
            case VSC_CONVERTER_STATION:
                script += injectionTemplate;
                break;

            case HVDC_LINE:
                script += hvdcLineTemplate;
                break;

            default:
                throw new PowsyblException("Filter type not allowed");
        }

        ST template = new ST(script);

        switch (filter.getType()) {
            case LINE:
                LineFilter lineFilter = (LineFilter) filter;
                template.add(COLLECTION_NAME, equipmentsCollection);
                if (!lineFilter.isEmpty()) {
                    template.add(NO_EMPTY_FILTER, "true");
                }
                if (lineFilter.getEquipmentID() != null) {
                    template.add(EQUIPMENT_ID, lineFilter.getEquipmentID());
                }
                if (lineFilter.getEquipmentName() != null) {
                    template.add(EQUIPMENT_NAME, lineFilter.getEquipmentName());
                }
                if (!CollectionUtils.isEmpty(lineFilter.getCountries1())) {
                    template.add(COUNTRIES + "1", lineFilter.getCountries1().stream().collect(joining("','", "['", "']")));
                }
                if (!CollectionUtils.isEmpty(lineFilter.getCountries2())) {
                    template.add(COUNTRIES + "2", lineFilter.getCountries2().stream().collect(joining("','", "['", "']")));
                }
                if (lineFilter.getNominalVoltage1() != null) {
                    addFilterNominalVoltage(template, lineFilter.getNominalVoltage1(), "1");
                }
                if (lineFilter.getNominalVoltage2() != null) {
                    addFilterNominalVoltage(template, lineFilter.getNominalVoltage2(), "2");
                }
                if (!StringUtils.isEmpty(lineFilter.getSubstationName1())) {
                    template.add(SUBSTATION_NAME + "1", lineFilter.getSubstationName1());
                }
                if (!StringUtils.isEmpty(lineFilter.getSubstationName2())) {
                    template.add(SUBSTATION_NAME + "2", lineFilter.getSubstationName2());
                }
                break;

            case TWO_WINDINGS_TRANSFORMER:
                TwoWindingsTransformerFilter twoWindingsTransformerFilter = (TwoWindingsTransformerFilter) filter;
                template.add(COLLECTION_NAME, equipmentsCollection);
                if (!twoWindingsTransformerFilter.isEmpty()) {
                    template.add(NO_EMPTY_FILTER, "true");
                }
                if (twoWindingsTransformerFilter.getEquipmentID() != null) {
                    template.add(EQUIPMENT_ID, twoWindingsTransformerFilter.getEquipmentID());
                }
                if (twoWindingsTransformerFilter.getEquipmentName() != null) {
                    template.add(EQUIPMENT_NAME, twoWindingsTransformerFilter.getEquipmentName());
                }
                if (!CollectionUtils.isEmpty(twoWindingsTransformerFilter.getCountries())) {
                    template.add(COUNTRIES, twoWindingsTransformerFilter.getCountries().stream().collect(joining("','", "['", "']")));
                }
                if (twoWindingsTransformerFilter.getNominalVoltage1() != null) {
                    addFilterNominalVoltage(template, twoWindingsTransformerFilter.getNominalVoltage1(), "1");
                }
                if (twoWindingsTransformerFilter.getNominalVoltage2() != null) {
                    addFilterNominalVoltage(template, twoWindingsTransformerFilter.getNominalVoltage2(), "2");
                }
                if (!StringUtils.isEmpty(twoWindingsTransformerFilter.getSubstationName())) {
                    template.add(SUBSTATION_NAME, twoWindingsTransformerFilter.getSubstationName());
                }
                break;

            case THREE_WINDINGS_TRANSFORMER:
                ThreeWindingsTransformerFilter threeWindingsTransformerFilter = (ThreeWindingsTransformerFilter) filter;
                template.add(COLLECTION_NAME, equipmentsCollection);
                if (!threeWindingsTransformerFilter.isEmpty()) {
                    template.add(NO_EMPTY_FILTER, "true");
                }
                if (threeWindingsTransformerFilter.getEquipmentID() != null) {
                    template.add(EQUIPMENT_ID, threeWindingsTransformerFilter.getEquipmentID());
                }
                if (threeWindingsTransformerFilter.getEquipmentName() != null) {
                    template.add(EQUIPMENT_NAME, threeWindingsTransformerFilter.getEquipmentName());
                }
                if (!CollectionUtils.isEmpty(threeWindingsTransformerFilter.getCountries())) {
                    template.add(COUNTRIES, threeWindingsTransformerFilter.getCountries().stream().collect(joining("','", "['", "']")));
                }
                if (threeWindingsTransformerFilter.getNominalVoltage1() != null) {
                    addFilterNominalVoltage(template, threeWindingsTransformerFilter.getNominalVoltage1(), "1");
                }
                if (threeWindingsTransformerFilter.getNominalVoltage2() != null) {
                    addFilterNominalVoltage(template, threeWindingsTransformerFilter.getNominalVoltage2(), "2");
                }
                if (threeWindingsTransformerFilter.getNominalVoltage3() != null) {
                    addFilterNominalVoltage(template, threeWindingsTransformerFilter.getNominalVoltage3(), "3");
                }
                if (!StringUtils.isEmpty(threeWindingsTransformerFilter.getSubstationName())) {
                    template.add(SUBSTATION_NAME, threeWindingsTransformerFilter.getSubstationName());
                }
                break;

            case GENERATOR:
            case LOAD:
            case BATTERY:
            case DANGLING_LINE:
            case BUSBAR_SECTION:
            case SHUNT_COMPENSATOR:
            case STATIC_VAR_COMPENSATOR:
            case LCC_CONVERTER_STATION:
            case VSC_CONVERTER_STATION:
                AbstractInjectionFilter injectionFilter = (AbstractInjectionFilter) filter;
                template.add(COLLECTION_NAME, equipmentsCollection);
                if (!injectionFilter.isEmpty()) {
                    template.add(NO_EMPTY_FILTER, "true");
                }
                if (injectionFilter.getEquipmentID() != null) {
                    template.add(EQUIPMENT_ID, injectionFilter.getEquipmentID());
                }
                if (injectionFilter.getEquipmentName() != null) {
                    template.add(EQUIPMENT_NAME, injectionFilter.getEquipmentName());
                }
                if (!CollectionUtils.isEmpty(injectionFilter.getCountries())) {
                    template.add(COUNTRIES, injectionFilter.getCountries().stream().collect(joining("','", "['", "']")));
                }
                if (injectionFilter.getNominalVoltage() != null) {
                    addFilterNominalVoltage(template, injectionFilter.getNominalVoltage(), null);
                }
                if (!StringUtils.isEmpty(injectionFilter.getSubstationName())) {
                    template.add(SUBSTATION_NAME, injectionFilter.getSubstationName());
                }
                break;

            case HVDC_LINE:
                HvdcLineFilter hvdcLineFilter = (HvdcLineFilter) filter;
                template.add(COLLECTION_NAME, equipmentsCollection);
                if (!hvdcLineFilter.isEmpty()) {
                    template.add(NO_EMPTY_FILTER, "true");
                }
                if (hvdcLineFilter.getEquipmentID() != null) {
                    template.add(EQUIPMENT_ID, hvdcLineFilter.getEquipmentID());
                }
                if (hvdcLineFilter.getEquipmentName() != null) {
                    template.add(EQUIPMENT_NAME, hvdcLineFilter.getEquipmentName());
                }
                if (!CollectionUtils.isEmpty(hvdcLineFilter.getCountries1())) {
                    template.add(COUNTRIES + "1", hvdcLineFilter.getCountries1().stream().collect(joining("','", "['", "']")));
                }
                if (!CollectionUtils.isEmpty(hvdcLineFilter.getCountries2())) {
                    template.add(COUNTRIES + "2", hvdcLineFilter.getCountries2().stream().collect(joining("','", "['", "']")));
                }
                if (hvdcLineFilter.getNominalVoltage() != null) {
                    addFilterNominalVoltage(template, hvdcLineFilter.getNominalVoltage(), null);
                }
                if (!StringUtils.isEmpty(hvdcLineFilter.getSubstationName1())) {
                    template.add(SUBSTATION_NAME + "1", hvdcLineFilter.getSubstationName1());
                }
                if (!StringUtils.isEmpty(hvdcLineFilter.getSubstationName2())) {
                    template.add(SUBSTATION_NAME + "2", hvdcLineFilter.getSubstationName2());
                }
                break;

            default:
                throw new PowsyblException("Filter type not allowed");
        }

        return template.render();
    }
}
