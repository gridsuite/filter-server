/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.filter.server.service;

import lombok.Getter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.gridsuite.filter.server.dto.ElementAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author Bassel El Cheikh <bassel.el-cheikh at rte-france.com>
 */

@Service
public class DirectoryService {
    public static final String DELIMITER = "/";
    public static final String DIRECTORY_API_VERSION = "v1";
    public static final String ELEMENT_END_POINT_INFOS = "/elements";

    @Getter
    private final String baseUri;
    private final RestTemplate restTemplate;

    @Autowired
    public DirectoryService(@Value("${gridsuite.services.directory-server.base-uri:http://directory-server/}") String baseUri,
                            RestTemplateBuilder restTemplateBuilder) {
        this.baseUri = baseUri;
        this.restTemplate = restTemplateBuilder.build();
    }

    public Map<UUID, String> getElementsName(List<UUID> ids, String userId) {
        Map<UUID, String> result = new HashMap<>();
        String endPointUrl = getBaseUri() + DELIMITER + DIRECTORY_API_VERSION + ELEMENT_END_POINT_INFOS;
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(endPointUrl);
        uriComponentsBuilder.queryParam("ids", ids);
        var uriComponent = uriComponentsBuilder.buildAndExpand();

        HttpHeaders headers = new HttpHeaders();
        headers.set("userId", userId);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        List<ElementAttributes> elementAttributes = restTemplate.exchange(uriComponent.toUriString(),
                HttpMethod.GET, entity, new ParameterizedTypeReference<List<ElementAttributes>>() { }).getBody();
        if (elementAttributes != null) {
            for (ElementAttributes elementAttribute : elementAttributes) {
                result.put(elementAttribute.elementUuid(), elementAttribute.elementName());
            }
        }

        return result;
    }
}
