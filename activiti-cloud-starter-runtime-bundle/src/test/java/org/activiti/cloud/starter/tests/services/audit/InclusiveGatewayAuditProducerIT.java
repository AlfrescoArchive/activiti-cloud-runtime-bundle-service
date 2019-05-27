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
import static org.activiti.api.task.model.events.TaskCandidateUserEvent.TaskCandidateUserEvents.TASK_CANDIDATE_USER_ADDED;
import static org.activiti.api.task.model.events.TaskRuntimeEvent.TaskEvents.TASK_ASSIGNED;
import static org.activiti.api.task.model.events.TaskRuntimeEvent.TaskEvents.TASK_COMPLETED;
import static org.activiti.api.task.model.events.TaskRuntimeEvent.TaskEvents.TASK_CREATED;
import static org.activiti.api.task.model.events.TaskRuntimeEvent.TaskEvents.TASK_UPDATED;
import static org.activiti.cloud.starter.tests.services.audit.AuditProducerIT.ALL_REQUIRED_HEADERS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.awaitility.Awaitility.await;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.activiti.api.model.shared.model.VariableInstance;
import org.activiti.api.process.model.BPMNActivity;
import org.activiti.api.process.model.builders.StartProcessPayloadBuilder;
import org.activiti.api.task.model.Task.TaskStatus;
import org.activiti.api.task.model.builders.TaskPayloadBuilder;
import org.activiti.api.task.model.payloads.CompleteTaskPayload;
import org.activiti.cloud.api.model.shared.CloudVariableInstance;
import org.activiti.cloud.api.model.shared.events.CloudRuntimeEvent;
import org.activiti.cloud.api.process.model.CloudProcessInstance;
import org.activiti.cloud.api.task.model.CloudTask;
import org.activiti.cloud.starter.tests.helper.ProcessInstanceRestTemplate;
import org.activiti.cloud.starter.tests.helper.TaskRestTemplate;
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
public class InclusiveGatewayAuditProducerIT {

    private static final String INCLUSIVE_GATEWAY_PROCESS = "basicInclusiveGateway";

    @Autowired
    private ProcessInstanceRestTemplate processInstanceRestTemplate;
    
    @Autowired
    private TaskRestTemplate taskRestTemplate;

    @Autowired
    private AuditConsumerStreamHandler streamHandler;

    @Test
    public void testProcessExecutionWithInclusiveGateway() {
        //when
        streamHandler.getAllReceivedEvents().clear();
        ResponseEntity<CloudProcessInstance> processInstance = processInstanceRestTemplate.startProcess(
                new StartProcessPayloadBuilder()
                        .withProcessDefinitionKey(INCLUSIVE_GATEWAY_PROCESS)
                        .withVariable("input",0)
                        .build());
        String processInstanceId = processInstance.getBody().getId();
        String processDefinitionKey = processInstance.getBody().getProcessDefinitionKey();

        //then
        checkVariable(processInstanceId, "input", 0);
        
        //then task0 is started
        CloudTask task = processInstanceRestTemplate.getTasks(processInstance).getBody().iterator().next();
        String taskId = task.getId();
        
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
                                           "input"),
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
                                           "task0"),
                                     tuple(VARIABLE_CREATED,
                                           processInstanceId,
                                           "input"),
                                     tuple(TASK_CANDIDATE_USER_ADDED,
                                           null,
                                           "hruser"),
                                     tuple(TASK_CREATED,
                                           processInstanceId,
                                           taskId));        

        });
        
        streamHandler.getAllReceivedEvents().clear();
        
        //when
        ResponseEntity<CloudTask> claimTask = taskRestTemplate.claim(task);
        assertThat(claimTask).isNotNull();
        assertThat(claimTask.getBody().getStatus()).isEqualTo(TaskStatus.ASSIGNED);
        
        //then
        await().untilAsserted(() -> {
            List<CloudRuntimeEvent<?, ?>> receivedEvents = streamHandler.getAllReceivedEvents();
            assertThat(streamHandler.getReceivedHeaders()).containsKeys(ALL_REQUIRED_HEADERS);
         
            assertThat(receivedEvents)
                    .extracting(CloudRuntimeEvent::getEventType,
                                CloudRuntimeEvent::getProcessInstanceId,
                                CloudRuntimeEvent::getEntityId)
                    .contains(tuple(TASK_ASSIGNED,
                                    processInstanceId,
                                    taskId),
                              tuple(TASK_UPDATED,
                                    processInstanceId,
                                    taskId));        

        });
        
        streamHandler.getAllReceivedEvents().clear();
        
        //when
        CompleteTaskPayload completeTaskPayload = TaskPayloadBuilder
                                        .complete()
                                        .withTaskId(task.getId())
                                        .withVariables(Collections.singletonMap("input",1))
                                        .build();
        ResponseEntity<CloudTask> completeTask = taskRestTemplate.complete(task,completeTaskPayload);
        
        //then
        assertThat(completeTask).isNotNull();
        assertThat(completeTask.getBody().getStatus()).isEqualTo(TaskStatus.COMPLETED);
        
        checkVariable(processInstanceId, "input", 1);
        
        //then - two tasks should be available
        Iterator <CloudTask> tasks = processInstanceRestTemplate.getTasks(processInstance).getBody().getContent().iterator();
        
        CloudTask task1 = tasks.hasNext() ? tasks.next() : null;
        CloudTask task2 = tasks.hasNext() ? tasks.next() : null;
        
        assertThat(task1).isNotNull();
        assertThat(task2).isNotNull();
   
        await().untilAsserted(() -> {
            List<CloudRuntimeEvent<?, ?>> receivedEvents = streamHandler.getAllReceivedEvents();
            assertThat(streamHandler.getReceivedHeaders()).containsKeys(ALL_REQUIRED_HEADERS);
         
            assertThat(receivedEvents)
                    .extracting(CloudRuntimeEvent::getEventType,
                                CloudRuntimeEvent::getProcessInstanceId,
                                CloudRuntimeEvent::getEntityId)
                    .contains(tuple(VARIABLE_UPDATED,
                                    processInstanceId,
                                    "input"),
                              tuple(VARIABLE_UPDATED,
                                    processInstanceId,
                                    "input"),
                              tuple(TASK_COMPLETED,
                                    processInstanceId,
                                    taskId),
                              tuple(ACTIVITY_COMPLETED,
                                    processInstanceId,
                                    "task0"),
                              tuple(SEQUENCE_FLOW_TAKEN,
                                    processInstanceId,
                                    "flow2"),
                              tuple(ACTIVITY_STARTED,
                                    processInstanceId,
                                    "inclusiveGateway"),
                              tuple(ACTIVITY_COMPLETED,
                                    processInstanceId,
                                    "inclusiveGateway"),
                              tuple(SEQUENCE_FLOW_TAKEN,
                                    processInstanceId,
                                    "flow3"),
                              tuple(ACTIVITY_STARTED,
                                    processInstanceId,
                                    "theTask1"),
                              tuple(VARIABLE_CREATED,
                                    processInstanceId,
                                    "input"),
                              tuple(TASK_CANDIDATE_USER_ADDED,
                                    null,
                                    "hruser"),
                              tuple(TASK_CREATED,
                                    processInstanceId,
                                    task1.getId()),
                              tuple(SEQUENCE_FLOW_TAKEN,
                                    processInstanceId,
                                    "flow4"),
                              tuple(ACTIVITY_STARTED,
                                    processInstanceId,
                                    "theTask2"),
                              tuple(VARIABLE_CREATED,
                                    processInstanceId,
                                    "input"),
                              tuple(TASK_CANDIDATE_USER_ADDED,
                                    null,
                                    "hruser"),
                              tuple(TASK_CREATED,
                                    processInstanceId,
                                    task2.getId()));   
            
        assertThat(receivedEvents)
            .filteredOn(event -> (event.getEventType().equals(ACTIVITY_STARTED) || event.getEventType().equals(ACTIVITY_COMPLETED)) && 
                                 ((BPMNActivity) event.getEntity()).getActivityType().equals("inclusiveGateway"))
            .extracting(CloudRuntimeEvent::getEventType,
                        CloudRuntimeEvent::getProcessDefinitionKey,
                        event -> ((BPMNActivity) event.getEntity()).getActivityType(),
                        event -> ((BPMNActivity) event.getEntity()).getProcessInstanceId()
                        )
            .contains(tuple(ACTIVITY_STARTED,
                            processDefinitionKey,
                            "inclusiveGateway",
                            processInstanceId),
                      tuple(ACTIVITY_COMPLETED,
                            processDefinitionKey,
                            "inclusiveGateway",
                            processInstanceId));

        });
      
        streamHandler.getAllReceivedEvents().clear();
        
        //when - complete first task
        claimTask = taskRestTemplate.claim(task1);
        assertThat(claimTask).isNotNull();
        assertThat(claimTask.getBody().getStatus()).isEqualTo(TaskStatus.ASSIGNED);  
 
        completeTaskPayload = TaskPayloadBuilder
                                    .complete()
                                    .withTaskId(task.getId())
                                    .withVariables(Collections.singletonMap("input",3))
                                    .build();
        completeTask = taskRestTemplate.complete(task1,completeTaskPayload);
        assertThat(completeTask.getBody().getStatus()).isEqualTo(TaskStatus.COMPLETED);
        
        checkVariable(processInstanceId, "input", 3);
        
        //then - first task should be completed, second should be available
        await().untilAsserted(() -> {
              List<CloudRuntimeEvent<?, ?>> receivedEvents = streamHandler.getAllReceivedEvents();
              assertThat(streamHandler.getReceivedHeaders()).containsKeys(ALL_REQUIRED_HEADERS);
           
              assertThat(receivedEvents)
                      .extracting(CloudRuntimeEvent::getEventType,
                                  CloudRuntimeEvent::getProcessInstanceId,
                                  CloudRuntimeEvent::getEntityId)
                      .contains(tuple(TASK_ASSIGNED,
                                      processInstanceId,
                                      task1.getId()),
                                tuple(TASK_UPDATED,
                                      processInstanceId,
                                      task1.getId()),
                                tuple(VARIABLE_UPDATED,
                                      processInstanceId,
                                      "input"),
                                tuple(VARIABLE_UPDATED,
                                      processInstanceId,
                                      "input"),
                                tuple(TASK_COMPLETED,
                                      processInstanceId,
                                      task1.getId()),
                                tuple(ACTIVITY_COMPLETED,
                                      processInstanceId,
                                      "theTask1"),
                                tuple(SEQUENCE_FLOW_TAKEN,
                                      processInstanceId,
                                      "flow6"),
                                tuple(ACTIVITY_STARTED,
                                      processInstanceId,
                                      "theEnd"),
                                tuple(ACTIVITY_COMPLETED,
                                      processInstanceId,
                                      "theEnd")); 
              
        });
        
        tasks = processInstanceRestTemplate.getTasks(processInstance).getBody().getContent().iterator();
        assertThat(tasks).hasSize(1);
        
        //when - complete second task     
        claimTask = taskRestTemplate.claim(task2);
        assertThat(claimTask).isNotNull();
        assertThat(claimTask.getBody().getStatus()).isEqualTo(TaskStatus.ASSIGNED);  
 
        completeTaskPayload = TaskPayloadBuilder
                                    .complete()
                                    .withTaskId(task2.getId())
                                    .withVariables(Collections.singletonMap("input",4))
                                    .build();
        completeTask = taskRestTemplate.complete(task2,completeTaskPayload);
        assertThat(completeTask.getBody().getStatus()).isEqualTo(TaskStatus.COMPLETED);
        
        //then - second task should be completed, process should be completed
        await().untilAsserted(() -> {
              List<CloudRuntimeEvent<?, ?>> receivedEvents = streamHandler.getAllReceivedEvents();
              assertThat(streamHandler.getReceivedHeaders()).containsKeys(ALL_REQUIRED_HEADERS);
           
              assertThat(receivedEvents)
                      .extracting(CloudRuntimeEvent::getEventType,
                                  CloudRuntimeEvent::getProcessInstanceId,
                                  CloudRuntimeEvent::getEntityId)
                      .contains(tuple(TASK_ASSIGNED,
                                      processInstanceId,
                                      task2.getId()),
                                tuple(TASK_UPDATED,
                                      processInstanceId,
                                      task2.getId()),
                                tuple(VARIABLE_UPDATED,
                                      processInstanceId,
                                      "input"),
                                tuple(VARIABLE_UPDATED,
                                      processInstanceId,
                                      "input"),
                                tuple(TASK_COMPLETED,
                                      processInstanceId,
                                      task2.getId()),
                                tuple(ACTIVITY_COMPLETED,
                                      processInstanceId,
                                      "theTask2"),
                                tuple(SEQUENCE_FLOW_TAKEN,
                                      processInstanceId,
                                      "flow6"),
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
                    .extracting(CloudRuntimeEvent::getProcessDefinitionKey,
                                event -> ((VariableInstance) event.getEntity()).getProcessInstanceId(),
                                event -> ((VariableInstance) event.getEntity()).isTaskVariable(),
                                event -> ((VariableInstance) event.getEntity()).getName(),
                                event -> ((VariableInstance) event.getEntity()).getValue())
                    .contains(tuple(processDefinitionKey,
                                    processInstanceId,
                                    true,
                                    "input",
                                    4),
                              tuple(processDefinitionKey,
                                    processInstanceId,
                                    false,
                                    "input",
                                    4));
        });   

    }
    
    public void checkVariable(String processInstanceId, String varName, Integer varValue) {
        Collection<CloudVariableInstance> variableCollection = processInstanceRestTemplate
                .getVariables(processInstanceId)
                .getBody()
                .getContent();

        assertThat(variableCollection)
            .isNotEmpty()
            .extracting(CloudVariableInstance::getName,
                        CloudVariableInstance::getValue)
            .contains(tuple(varName, varValue));
        
    }


}
