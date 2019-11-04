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

package org.activiti.cloud.starter.tests.runtime;

import static org.assertj.core.api.Assertions.assertThat;

import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.model.payloads.StartProcessPayload;
import org.activiti.cloud.api.process.model.CloudProcessInstance;
import org.activiti.cloud.starter.tests.helper.ProcessInstanceRestTemplate;
import org.activiti.engine.RuntimeService;
import org.awaitility.Awaitility;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.stream.IntStream;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource("classpath:application-test.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ContextConfiguration(classes = RuntimeITConfiguration.class)
public class ThrowCatchMessageIT {

    private static final String CORRELATION_ID = "correlationId";

    private static final String CORRELATION_KEY = "correlationKey";

    private static final String BUSINESS_KEY = "businessKey";

    private static final String INTERMEDIATE_CATCH_MESSAGE_PROCESS = "IntermediateCatchMessageProcess";

    private static final String INTERMEDIATE_THROW_MESSAGE_PROCESS = "IntermediateThrowMessageProcess";

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private ProcessInstanceRestTemplate processInstanceRestTemplate;

    @Test
    public void shouldThrowCatchBpmnMessage() {
        //given
        StartProcessPayload throwProcessPayload = ProcessPayloadBuilder.start()
                                                                       .withProcessDefinitionKey(INTERMEDIATE_THROW_MESSAGE_PROCESS)
                                                                       .withBusinessKey(BUSINESS_KEY)
                                                                       .withVariable(CORRELATION_KEY, CORRELATION_ID)
                                                                       .build();
        //when
        ResponseEntity<CloudProcessInstance> throwProcessResponse = processInstanceRestTemplate.startProcess(throwProcessPayload);

        //then
        assertThat(throwProcessResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(throwProcessResponse.getBody()).isNotNull();
        assertThat(runtimeService.createProcessInstanceQuery()
                                 .processDefinitionKey(INTERMEDIATE_THROW_MESSAGE_PROCESS)
                                 .list()).isEmpty();

        //given
        StartProcessPayload catchProcessPayload = ProcessPayloadBuilder.start()
                                                                       .withProcessDefinitionKey(INTERMEDIATE_CATCH_MESSAGE_PROCESS)
                                                                       .withBusinessKey(BUSINESS_KEY)
                                                                       .withVariable(CORRELATION_KEY, CORRELATION_ID)
                                                                       .build();

        // when
        ResponseEntity<CloudProcessInstance> catchProcessResponse = processInstanceRestTemplate.startProcess(catchProcessPayload);

        // then
        assertThat(catchProcessResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        Awaitility.await().untilAsserted(() -> {
            assertThat(runtimeService.createProcessInstanceQuery()
                                     .processDefinitionKey(INTERMEDIATE_CATCH_MESSAGE_PROCESS)
                                     .list()).isEmpty();
        });
    }
    
    @Test
    public void shouldThrowCatchBpmnMessages() {
        // when
        IntStream.rangeClosed(1, 100)
                 .mapToObj(i -> ProcessPayloadBuilder.start()
                           .withProcessDefinitionKey(INTERMEDIATE_THROW_MESSAGE_PROCESS)
                           .withBusinessKey(BUSINESS_KEY+i)
                           .build())
                 .forEach(processInstanceRestTemplate::startProcess);
               
        // then
        assertThat(runtimeService.createProcessInstanceQuery()
                                 .processDefinitionKey(INTERMEDIATE_THROW_MESSAGE_PROCESS)
                                 .list()).isEmpty();

        // when
        IntStream.rangeClosed(1, 100)
                 .mapToObj(i -> ProcessPayloadBuilder.start()
                           .withProcessDefinitionKey(INTERMEDIATE_CATCH_MESSAGE_PROCESS)
                           .withBusinessKey(BUSINESS_KEY+i)
                           .build())
                 .forEach(processInstanceRestTemplate::startProcess);
        
        // then
        Awaitility.await().untilAsserted(() -> {
            assertThat(runtimeService.createProcessInstanceQuery()
                                     .processDefinitionKey(INTERMEDIATE_CATCH_MESSAGE_PROCESS)
                                     .list()).isEmpty();
        });
    }
    

}