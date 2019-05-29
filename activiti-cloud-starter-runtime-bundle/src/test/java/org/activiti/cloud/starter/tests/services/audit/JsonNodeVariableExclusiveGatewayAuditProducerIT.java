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

package org.activiti.cloud.starter.tests.services.audit;

import static org.activiti.api.model.shared.event.VariableEvent.VariableEvents.VARIABLE_CREATED;
import static org.activiti.api.model.shared.event.VariableEvent.VariableEvents.VARIABLE_UPDATED;
import static org.activiti.api.process.model.events.BPMNActivityEvent.ActivityEvents.ACTIVITY_COMPLETED;
import static org.activiti.api.process.model.events.BPMNActivityEvent.ActivityEvents.ACTIVITY_STARTED;
import static org.activiti.api.process.model.events.ProcessRuntimeEvent.ProcessEvents.PROCESS_COMPLETED;
import static org.activiti.api.process.model.events.ProcessRuntimeEvent.ProcessEvents.PROCESS_CREATED;
import static org.activiti.api.process.model.events.ProcessRuntimeEvent.ProcessEvents.PROCESS_STARTED;
import static org.activiti.api.process.model.events.SequenceFlowEvent.SequenceFlowEvents.SEQUENCE_FLOW_TAKEN;
import static org.activiti.api.task.model.events.TaskRuntimeEvent.TaskEvents.TASK_ASSIGNED;
import static org.activiti.api.task.model.events.TaskRuntimeEvent.TaskEvents.TASK_COMPLETED;
import static org.activiti.api.task.model.events.TaskRuntimeEvent.TaskEvents.TASK_CREATED;
import static org.activiti.cloud.starter.tests.services.audit.AuditProducerIT.ALL_REQUIRED_HEADERS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.awaitility.Awaitility.await;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.activiti.api.model.shared.model.VariableInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.model.payloads.StartProcessPayload;
import org.activiti.api.task.model.Task.TaskStatus;
import org.activiti.api.task.model.builders.TaskPayloadBuilder;
import org.activiti.api.task.model.payloads.CompleteTaskPayload;
import org.activiti.cloud.api.model.shared.CloudVariableInstance;
import org.activiti.cloud.api.model.shared.events.CloudRuntimeEvent;
import org.activiti.cloud.api.process.model.CloudProcessInstance;
import org.activiti.cloud.api.task.model.CloudTask;
import org.activiti.cloud.services.test.identity.keycloak.interceptor.KeycloakTokenProducer;
import org.activiti.cloud.starter.tests.helper.ProcessInstanceRestTemplate;
import org.activiti.cloud.starter.tests.helper.TaskRestTemplate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ActiveProfiles(AuditProducerIT.AUDIT_PRODUCER_IT)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource("classpath:application-test.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class JsonNodeVariableExclusiveGatewayAuditProducerIT {

    private static final String EXCLUSIVE_GATEWAY_PROCESS = "jsonNodeVariableExclusiveGateway";

    @Autowired
    private ProcessInstanceRestTemplate processInstanceRestTemplate;

    @Autowired
    private TaskRestTemplate taskRestTemplate;

    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private AuditConsumerStreamHandler streamHandler;

    @Autowired
    private KeycloakTokenProducer keycloakSecurityContextClientRequestInterceptor;

    @Before
    public void setUp() {
        keycloakSecurityContextClientRequestInterceptor.setKeycloakTestUser("hruser");
    }

    @Test
    public void testProcessExecutionWithExclusiveGateway() throws IOException {
        //when
        streamHandler.getAllReceivedEvents().clear();
        
        String idValue = "30";
        int goValue = 0;
        
        StartProcessPayload payload = ProcessPayloadBuilder
                .start()
                .withProcessDefinitionKey(EXCLUSIVE_GATEWAY_PROCESS)
                .withBusinessKey("businessKey")
                .withVariable("varEntity",createJsonNode(idValue,goValue))
                .build(); 
        
        ResponseEntity<CloudProcessInstance> processInstance = processInstanceRestTemplate.startProcess(payload);
        String processInstanceId = processInstance.getBody().getId();
        
        //then - check process variable
        checkProcessVarEntity(processInstanceId, idValue, goValue);
        
        //then
        CloudTask task = processInstanceRestTemplate.getTasks(processInstance).getBody().iterator().next();
        String taskId = task.getId();
        
        //then - check task variable
        checkTaskVarEntity(taskId, idValue, goValue);    
        
        await().untilAsserted(() -> {

            List<CloudRuntimeEvent<?, ?>> receivedEvents = streamHandler.getAllReceivedEvents();

            assertThat(streamHandler.getReceivedHeaders()).containsKeys(ALL_REQUIRED_HEADERS);

            assertThat(receivedEvents)
                    .extracting(CloudRuntimeEvent::getEventType,
                                CloudRuntimeEvent::getProcessInstanceId,
                                CloudRuntimeEvent::getEntityId)
                    .containsExactly(tuple(PROCESS_CREATED,
                                           processInstanceId,
                                           processInstanceId),
                                     tuple(VARIABLE_CREATED,
                                           processInstanceId,
                                           "varEntity"),
                                     tuple(PROCESS_STARTED,
                                           processInstanceId,
                                           processInstanceId),
                                     tuple(ACTIVITY_STARTED,
                                           processInstanceId,
                                           "theStart"),
                                     tuple(ACTIVITY_COMPLETED,
                                           processInstanceId,
                                           "theStart"),
                                     tuple(SEQUENCE_FLOW_TAKEN,
                                           processInstanceId,
                                           "flow1"),
                                     tuple(ACTIVITY_STARTED,
                                           processInstanceId,
                                           "task1"),
                                     tuple(VARIABLE_CREATED,
                                           processInstanceId,
                                           "varEntity"),
                                     tuple(TASK_CREATED,
                                           processInstanceId,
                                           taskId),
                                     tuple(TASK_ASSIGNED,
                                           processInstanceId,
                                           taskId));        

        });

        streamHandler.getAllReceivedEvents().clear();


        //when
        goValue = -1;
        CompleteTaskPayload completeTaskPayload = TaskPayloadBuilder
                                        .complete()
                                        .withTaskId(taskId)
                                        .withVariable("varEntity",createJsonNode(idValue,goValue))
                                        .build();
        ResponseEntity<CloudTask> completeTask = taskRestTemplate.complete(task,completeTaskPayload);

        //then
        assertThat(completeTask).isNotNull();
        assertThat(completeTask.getBody().getStatus()).isEqualTo(TaskStatus.COMPLETED);

        //then - check process variable
        checkProcessVarEntity(processInstanceId, idValue, goValue);
        
        task = processInstanceRestTemplate.getTasks(processInstance).getBody().iterator().next();
        String newTaskId = task.getId();
        
        //then - check task variable
        checkTaskVarEntity(newTaskId, idValue, goValue);    
        
        await().untilAsserted(() -> {
            List<CloudRuntimeEvent<?, ?>> receivedEvents = streamHandler.getAllReceivedEvents();
            assertThat(streamHandler.getReceivedHeaders()).containsKeys(ALL_REQUIRED_HEADERS);

            assertThat(receivedEvents)
                    .extracting(CloudRuntimeEvent::getEventType,
                                CloudRuntimeEvent::getProcessInstanceId,
                                CloudRuntimeEvent::getEntityId)
                    .contains(tuple(VARIABLE_UPDATED,
                                    processInstanceId,
                                    "varEntity"),
                              tuple(VARIABLE_UPDATED,
                                    processInstanceId,
                                    "varEntity"),
                              tuple(TASK_COMPLETED,
                                    processInstanceId,
                                    taskId),
                              tuple(ACTIVITY_COMPLETED,
                                    processInstanceId,
                                    "task1"),
                              tuple(SEQUENCE_FLOW_TAKEN,
                                    processInstanceId,
                                    "flow2"),
                              tuple(ACTIVITY_STARTED,
                                    processInstanceId,
                                    "exclusiveGateway"),
                              tuple(ACTIVITY_COMPLETED,
                                    processInstanceId,
                                    "exclusiveGateway"),
                              tuple(SEQUENCE_FLOW_TAKEN,
                                    processInstanceId,
                                    "flow21"),
                              tuple(ACTIVITY_STARTED,
                                    processInstanceId,
                                    "task3"),
                              tuple(VARIABLE_CREATED,
                                    processInstanceId,
                                    "varEntity"),
                              tuple(TASK_CREATED,
                                    processInstanceId,
                                    newTaskId),
                              tuple(TASK_ASSIGNED,
                                    processInstanceId,
                                    newTaskId));   

        });

        streamHandler.getAllReceivedEvents().clear();
        
        //when
        goValue = 2;
        completeTaskPayload = TaskPayloadBuilder
                .complete()
                .withTaskId(newTaskId)
                .withVariable("varEntity",createJsonNode(idValue,goValue))
                .build();
        completeTask = taskRestTemplate.complete(task,completeTaskPayload);

        //then
        assertThat(completeTask).isNotNull();
        assertThat(completeTask.getBody().getStatus()).isEqualTo(TaskStatus.COMPLETED);
         
        await().untilAsserted(() -> {
            List<CloudRuntimeEvent<?, ?>> receivedEvents = streamHandler.getAllReceivedEvents();
            assertThat(streamHandler.getReceivedHeaders()).containsKeys(ALL_REQUIRED_HEADERS);

            assertThat(receivedEvents)
                    .extracting(CloudRuntimeEvent::getEventType,
                                CloudRuntimeEvent::getProcessInstanceId,
                                CloudRuntimeEvent::getEntityId)
                    .contains(tuple(VARIABLE_UPDATED,
                                    processInstanceId,
                                    "varEntity"),
                              tuple(VARIABLE_UPDATED,
                                    processInstanceId,
                                    "varEntity"),
                              tuple(TASK_COMPLETED,
                                    processInstanceId,
                                    newTaskId),
                              tuple(ACTIVITY_COMPLETED,
                                    processInstanceId,
                                    "task3"),
                              tuple(SEQUENCE_FLOW_TAKEN,
                                    processInstanceId,
                                    "flow4"),
                              tuple(ACTIVITY_STARTED,
                                    processInstanceId,
                                    "theEnd"),
                              tuple(ACTIVITY_COMPLETED,
                                    processInstanceId,
                                    "theEnd"),
                              tuple(PROCESS_COMPLETED,
                                    processInstanceId,
                                    processInstanceId));   
            
            assertThat(receivedEvents)
            .filteredOn(event -> event.getEventType().equals(VARIABLE_UPDATED))
            .extracting(event -> ((VariableInstance) event.getEntity()).getProcessInstanceId(),
                        event -> ((VariableInstance) event.getEntity()).isTaskVariable(),
                        event -> ((VariableInstance) event.getEntity()).getName())
            .contains(tuple(processInstanceId,
                            true,
                            "varEntity"),
                      tuple(processInstanceId,
                            false,
                            "varEntity"));
            
            checkVarEntity(((VariableInstance) receivedEvents.get(1).getEntity()).getValue(), 
                           idValue,
                           2);
        });

    }
    
    public void checkVarEntity(Map<String, Object> currentVarEntity, String idValue, Integer goValue) { 
        assertThat(currentVarEntity)
        .isNotEmpty();
        
        assertThat(currentVarEntity.get("id")).isEqualTo(idValue);
        assertThat(currentVarEntity.get("go")).isEqualTo(goValue);
        
    }
    
    
    public void checkProcessVarEntity(String processInstanceId, String idValue, Integer goValue) { 
        Collection<CloudVariableInstance> variableCollection = processInstanceRestTemplate
                .getVariables(processInstanceId)
                .getBody()
                .getContent();
        
        assertThat(variableCollection)
            .hasSize(1)
            .extracting(CloudVariableInstance::getName,
                        CloudVariableInstance::getType)
            .contains(tuple("varEntity","json"));
        
        checkVarEntity(variableCollection.iterator().next().getValue(), idValue, goValue);      
    }
    
    public void checkTaskVarEntity(String taskId,String idValue, Integer goValue) { 
        Collection<CloudVariableInstance> variableCollection = taskRestTemplate
                .getVariables(taskId)
                .getBody()
                .getContent();
        
        assertThat(variableCollection)
            .hasSize(1)
            .extracting(CloudVariableInstance::getName,
                        CloudVariableInstance::getType)
            .contains(tuple("varEntity","json"));
            
        checkVarEntity(variableCollection.iterator().next().getValue(), idValue, goValue);   
        
    }
    
    public JsonNode createJsonNode(String idValue, Integer goValue) throws IOException {
        
        return objectMapper.readTree("{\"id\":\""+idValue+"\",\"go\":"+goValue+"}");
  
    }


} 
