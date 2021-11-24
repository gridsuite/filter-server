/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.server.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.gridsuite.filter.server.utils.EquipmentType;
import org.gridsuite.filter.server.utils.FilterType;

import java.util.Date;
import java.util.UUID;

/**
 * @author Borsenberger jacques <borsenberger.jacques at rte-france.com>
 */

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class FormFilter extends AbstractFilter {

    private AbstractEquipmentFilterForm equipmentFilterForm;

    public FormFilter(UUID id, Date creationDate, Date modificationDate, AbstractEquipmentFilterForm equipmentFilterForm) {
        super(id, creationDate, modificationDate);
        this.equipmentFilterForm = equipmentFilterForm;
    }

    @Override
    public EquipmentType getEquipmentType() {
        return equipmentFilterForm.getEquipmentType();
    }

    @Override
    public FilterType getType() {
        return FilterType.FORM;
    }
}
