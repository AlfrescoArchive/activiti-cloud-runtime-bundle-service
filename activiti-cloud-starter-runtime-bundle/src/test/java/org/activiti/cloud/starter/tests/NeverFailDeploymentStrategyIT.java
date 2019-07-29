/*
 * Copyright 2019 Alfresco, Inc. and/or its affiliates.
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

package org.activiti.cloud.starter.tests;

import org.activiti.cloud.api.process.model.CloudProcessDefinition;
import org.activiti.cloud.starter.tests.helper.ProcessDefinitionRestTemplate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.PagedResources;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource({"classpath:application-invalid-process-test.properties", "classpath:access-control.properties"})

public class NeverFailDeploymentStrategyIT {

    @Autowired
    private ProcessDefinitionRestTemplate processDefinitionRestTemplate;

    @Test
    public void rbShouldStartEvenIfItFailToParseSomeProcessDefinition() {
        //when
        ResponseEntity<PagedResources<CloudProcessDefinition>> processDefinitions = processDefinitionRestTemplate.getProcessDefinitions();

        //then
        //application-invalid-process-test.properties is pointing to a folder containing only an invalid process definition
        //so no process definitions are expected
        assertThat(processDefinitions.getBody()).isNotNull();
        assertThat(processDefinitions.getBody().getContent()).isEmpty();
    }
}
