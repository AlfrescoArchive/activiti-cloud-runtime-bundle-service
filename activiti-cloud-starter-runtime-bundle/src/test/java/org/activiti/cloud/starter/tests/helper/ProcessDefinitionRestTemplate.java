/*
 * Copyright 2018 Alfresco, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.cloud.starter.tests.helper;

import org.activiti.cloud.api.process.model.CloudProcessDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.PagedResources;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class ProcessDefinitionRestTemplate {

    private static final String PROCESS_DEFINITIONS_URL = "/v1/process-definitions/";
    private static final ParameterizedTypeReference<PagedResources<CloudProcessDefinition>> PAGED_DEFINITIONS_RESPONSE_TYPE = new ParameterizedTypeReference<>() {
    };

    @Autowired
    private TestRestTemplate testRestTemplate;

 
    public ResponseEntity<PagedResources<CloudProcessDefinition>> getProcessDefinitions() {
        return testRestTemplate.exchange(PROCESS_DEFINITIONS_URL,
                                         HttpMethod.GET,
                                         null,
                                         PAGED_DEFINITIONS_RESPONSE_TYPE);
    }
 
}