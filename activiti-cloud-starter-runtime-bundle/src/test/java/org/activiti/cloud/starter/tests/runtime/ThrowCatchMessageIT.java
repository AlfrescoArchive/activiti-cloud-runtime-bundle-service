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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.activiti.api.process.model.StartMessageDeploymentDefinition;
import org.activiti.api.process.model.StartMessageSubscription;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.model.events.StartMessageDeployedEvent;
import org.activiti.api.process.model.payloads.ReceiveMessagePayload;
import org.activiti.api.process.model.payloads.StartMessagePayload;
import org.activiti.api.process.model.payloads.StartProcessPayload;
import org.activiti.api.runtime.event.impl.StartMessageDeployedEvents;
import org.activiti.cloud.api.process.model.CloudProcessInstance;
import org.activiti.cloud.api.process.model.events.CloudBPMNMessageReceivedEvent;
import org.activiti.cloud.api.process.model.events.CloudBPMNMessageSentEvent;
import org.activiti.cloud.api.process.model.events.CloudBPMNMessageWaitingEvent;
import org.activiti.cloud.services.message.connector.MessageConnectorConsumer;
import org.activiti.cloud.services.message.events.ReceiveMessagePayloadMessageStreamListener;
import org.activiti.cloud.services.message.events.StartMessagePayloadMessageStreamListener;
import org.activiti.cloud.starter.tests.helper.ProcessInstanceRestTemplate;
import org.activiti.engine.RuntimeService;
import org.awaitility.Awaitility;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource("classpath:application-test.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ContextConfiguration(classes = RuntimeITConfiguration.class)
@Import(ThrowCatchMessageIT.TestStartMessageDeployedEventListener.class)
public class ThrowCatchMessageIT {

    private static final String BOUNDARY_SUBPROCESS_THROW_CATCH_MESSAGE_IT_PROCESS3 = "BoundarySubprocessThrowCatchMessageIT_Process3";

    private static final String BOUNDARY_SUBPROCESS_THROW_CATCH_MESSAGE_IT_PROCESS2 = "BoundarySubprocessThrowCatchMessageIT_Process2";

    private static final String BOUNDARY_SUBPROCESS_THROW_CATCH_MESSAGE_IT_PROCESS1 = "BoundarySubprocessThrowCatchMessageIT_Process1";

    private static final String EVENT_SUBPROCESS_NON_INTERRUPTING_THROW_CATCH_MESSAGE_IT_PROCESS2 = "EventSubprocessNonInterruptingThrowCatchMessageIT_Process2";

    private static final String EVENT_SUBPROCESS_NON_INTERRUPTING_THROW_CATCH_MESSAGE_IT_PROCESS3 = "EventSubprocessNonInterruptingThrowCatchMessageIT_Process3";

    private static final String EVENT_SUBPROCESS_NON_INTERRUPTING_THROW_CATCH_MESSAGE_IT_PROCESS1 = "EventSubprocessNonInterruptingThrowCatchMessageIT_Process1";

    private static final String EVENT_SUBPROCESS_THROW_CATCH_MESSAGE_IT_PROCESS3 = "EventSubprocessThrowCatchMessageIT_Process3";

    private static final String EVENT_SUBPROCESS_THROW_CATCH_MESSAGE_IT_PROCESS2 = "EventSubprocessThrowCatchMessageIT_Process2";

    private static final String EVENT_SUBPROCESS_THROW_CATCH_MESSAGE_IT_PROCESS1 = "EventSubprocessThrowCatchMessageIT_Process1";

    private static final String BOUNDARY_THROW_CATCH_MESSAGE_IT_PROCESS3 = "BoundaryThrowCatchMessageIT_Process3";

    private static final String BOUNDARY_THROW_CATCH_MESSAGE_IT_PROCESS2 = "BoundaryThrowCatchMessageIT_Process2";

    private static final String BOUNDARY_THROW_CATCH_MESSAGE_IT_PROCESS1 = "BoundaryThrowCatchMessageIT_Process1";

    private static final String THROW_CATCH_MESSAGE_IT_PROCESS3 = "ThrowCatchMessageIT_Process3";

    private static final String THROW_CATCH_MESSAGE_IT_PROCESS2 = "ThrowCatchMessageIT_Process2";

    private static final String THROW_CATCH_MESSAGE_IT_PROCESS1 = "ThrowCatchMessageIT_Process1";

    private static final String CORRELATION_ID = "correlationId";

    private static final String CORRELATION_KEY = "correlationKey";

    private static final String BUSINESS_KEY = "businessKey";

    private static final String INTERMEDIATE_CATCH_MESSAGE_PROCESS = "IntermediateCatchMessageProcess";

    private static final String INTERMEDIATE_THROW_MESSAGE_PROCESS = "IntermediateThrowMessageProcess";

    @Autowired
    private RuntimeService runtimeService;

    @SpyBean 
    private MessageConnectorConsumer messageConnectorConsumer; 

    @SpyBean
    private StartMessagePayloadMessageStreamListener startMessagePayloadMessageStreamListener; 

    @SpyBean
    private ReceiveMessagePayloadMessageStreamListener receiveMessagePayloadMessageStreamListener;
    
    @Autowired
    private ProcessInstanceRestTemplate processInstanceRestTemplate;
    
    @Autowired
    private TestStartMessageDeployedEventListener startMessageDeployedEventListener;

    @TestComponent
    static class TestStartMessageDeployedEventListener {
        
        private List<StartMessageDeployedEvent> events;
        
        @EventListener
        public void handle(StartMessageDeployedEvents event) {
            this.events = new ArrayList<StartMessageDeployedEvent>(event.getStartMessageDeployedEvents());
        }
        
        public List<StartMessageDeployedEvent> getEvents() {
            return events;
        }
    }
    
    @Test
    public void shouldHandleStartMessageDeployedEvents() {
        // given
        List<StartMessageDeployedEvent> events = startMessageDeployedEventListener.getEvents();
        
        // then
        assertThat(events).extracting(StartMessageDeployedEvent::getEntity)
                          .extracting(StartMessageDeploymentDefinition::getMessageSubscription)
                          .extracting(StartMessageSubscription::getEventName)
                          .contains("EventSubprocessThrowEndMessage",
                                    "EventSubprocessStartProcess3",
                                    "ThrowEndMessage",
                                    "ThrowIntermediateMessage",
                                    "EventSubprocessNonInterruptingThrowEndMessage",
                                    "EventSubprocessStartProcessNonInterrupting3",
                                    "BoundaryThrowEndMessage",
                                    "BoundaryThrowIntermediateMessage");
    }
    
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
        
        verify(messageConnectorConsumer, times(1)).handleCloudBPMNMessageSentEvent(ArgumentMatchers.<Message<CloudBPMNMessageSentEvent>>any());
        verify(messageConnectorConsumer, times(1)).handleCloudBPMNMessageWaitingEvent(ArgumentMatchers.<Message<CloudBPMNMessageWaitingEvent>>any());
        verify(messageConnectorConsumer, times(1)).handleCloudBPMNMessageReceivedEvent(ArgumentMatchers.<Message<CloudBPMNMessageReceivedEvent>>any());

        verify(receiveMessagePayloadMessageStreamListener, times(1)).receiveMessage(ArgumentMatchers.<Message<ReceiveMessagePayload>>any());
        verify(startMessagePayloadMessageStreamListener, never()).startMessage(ArgumentMatchers.<Message<StartMessagePayload>>any());
    }
    
    @Test
    public void shouldCompleteComplexBpmnMessageEventProcessWithIntermediateCatchEvent() {
        //given
        StartProcessPayload throwProcessPayload = ProcessPayloadBuilder.start()
                                                                       .withProcessDefinitionKey(THROW_CATCH_MESSAGE_IT_PROCESS1)
                                                                       .withBusinessKey(BUSINESS_KEY)
                                                                       .withVariable(CORRELATION_KEY, CORRELATION_ID)
                                                                       .build();
        //when
        ResponseEntity<CloudProcessInstance> throwProcessResponse = processInstanceRestTemplate.startProcess(throwProcessPayload);

        //then
        assertThat(throwProcessResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(throwProcessResponse.getBody()).isNotNull();
        assertThat(runtimeService.createProcessInstanceQuery()
                                 .processDefinitionKey(THROW_CATCH_MESSAGE_IT_PROCESS1)
                                 .list()).isEmpty();

        // then
        Awaitility.await().untilAsserted(() -> {
            assertThat(runtimeService.createProcessInstanceQuery()
                                     .processDefinitionKey(THROW_CATCH_MESSAGE_IT_PROCESS2)
                                     .list()).isEmpty();
            
            assertThat(runtimeService.createProcessInstanceQuery()
                                     .processDefinitionKey(THROW_CATCH_MESSAGE_IT_PROCESS3)
                                     .list()).isEmpty();
        });
        
        verify(messageConnectorConsumer, times(3)).handleCloudBPMNMessageSentEvent(ArgumentMatchers.<Message<CloudBPMNMessageSentEvent>>any());
        verify(messageConnectorConsumer, times(1)).handleCloudBPMNMessageWaitingEvent(ArgumentMatchers.<Message<CloudBPMNMessageWaitingEvent>>any());
        verify(messageConnectorConsumer, times(3)).handleCloudBPMNMessageReceivedEvent(ArgumentMatchers.<Message<CloudBPMNMessageReceivedEvent>>any());
        
        verify(receiveMessagePayloadMessageStreamListener, times(1)).receiveMessage(ArgumentMatchers.<Message<ReceiveMessagePayload>>any());
        verify(startMessagePayloadMessageStreamListener, times(2)).startMessage(ArgumentMatchers.<Message<StartMessagePayload>>any());
    }
    
    @Test
    public void shouldCompleteComplexBpmnMessageEventProcessWithBoundaryCatchEvent() {
        //given
        StartProcessPayload throwProcessPayload = ProcessPayloadBuilder.start()
                                                                       .withProcessDefinitionKey(BOUNDARY_THROW_CATCH_MESSAGE_IT_PROCESS1)
                                                                       .withBusinessKey(BUSINESS_KEY)
                                                                       .build();
        //when
        ResponseEntity<CloudProcessInstance> throwProcessResponse = processInstanceRestTemplate.startProcess(throwProcessPayload);

        //then
        assertThat(throwProcessResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(throwProcessResponse.getBody()).isNotNull();
        assertThat(runtimeService.createProcessInstanceQuery()
                                 .processDefinitionKey(BOUNDARY_THROW_CATCH_MESSAGE_IT_PROCESS1)
                                 .list()).isEmpty();

        // then
        Awaitility.await().untilAsserted(() -> {
            assertThat(runtimeService.createProcessInstanceQuery()
                                     .processDefinitionKey(BOUNDARY_THROW_CATCH_MESSAGE_IT_PROCESS2)
                                     .list()).isEmpty();
            
            assertThat(runtimeService.createProcessInstanceQuery()
                                     .processDefinitionKey(BOUNDARY_THROW_CATCH_MESSAGE_IT_PROCESS3)
                                     .list()).isEmpty();
        });
        
        verify(messageConnectorConsumer, times(3)).handleCloudBPMNMessageSentEvent(ArgumentMatchers.<Message<CloudBPMNMessageSentEvent>>any());
        verify(messageConnectorConsumer, times(1)).handleCloudBPMNMessageWaitingEvent(ArgumentMatchers.<Message<CloudBPMNMessageWaitingEvent>>any());
        verify(messageConnectorConsumer, times(3)).handleCloudBPMNMessageReceivedEvent(ArgumentMatchers.<Message<CloudBPMNMessageReceivedEvent>>any());

        verify(receiveMessagePayloadMessageStreamListener, times(1)).receiveMessage(ArgumentMatchers.<Message<ReceiveMessagePayload>>any());
        verify(startMessagePayloadMessageStreamListener, times(2)).startMessage(ArgumentMatchers.<Message<StartMessagePayload>>any());
    }
    

    @Test
    public void shouldCompleteComplexBpmnMessageEventMultipleProcessesWithIntermediateCatchEvent() {
        // given
        int processInstances = 1;
        
        //when
        IntStream.rangeClosed(1, processInstances)
                 .mapToObj(i -> ProcessPayloadBuilder.start()
                                                     .withProcessDefinitionKey(THROW_CATCH_MESSAGE_IT_PROCESS1)
                                                     .withBusinessKey(BUSINESS_KEY + i)
                                                     .build())
                 .forEach(processInstanceRestTemplate::startProcess);
        
        // then
        Awaitility.await().untilAsserted(() -> {
            assertThat(runtimeService.createProcessInstanceQuery()
                                     .processDefinitionKey(THROW_CATCH_MESSAGE_IT_PROCESS1)
                                     .list()).isEmpty();
            
            assertThat(runtimeService.createProcessInstanceQuery()
                                     .processDefinitionKey(THROW_CATCH_MESSAGE_IT_PROCESS2)
                                     .list()).isEmpty();
            
            assertThat(runtimeService.createProcessInstanceQuery()
                                     .processDefinitionKey(THROW_CATCH_MESSAGE_IT_PROCESS3)
                                     .list()).isEmpty();
        });

        verify(messageConnectorConsumer, times(3 * processInstances)).handleCloudBPMNMessageSentEvent(ArgumentMatchers.<Message<CloudBPMNMessageSentEvent>>any());
        verify(messageConnectorConsumer, times(processInstances)).handleCloudBPMNMessageWaitingEvent(ArgumentMatchers.<Message<CloudBPMNMessageWaitingEvent>>any());
        verify(messageConnectorConsumer, times(3 * processInstances)).handleCloudBPMNMessageReceivedEvent(ArgumentMatchers.<Message<CloudBPMNMessageReceivedEvent>>any());

        verify(receiveMessagePayloadMessageStreamListener, times(processInstances)).receiveMessage(ArgumentMatchers.<Message<ReceiveMessagePayload>>any());
        verify(startMessagePayloadMessageStreamListener, times(2 * processInstances)).startMessage(ArgumentMatchers.<Message<StartMessagePayload>>any());
    }
    
    @Test
    public void shouldCompleteComplexBpmnMessageEventMultipleProcessesWithBoundaryTaskMessageCatchEvent() {
        // given
        int processInstances = 10;
        
        //when
        IntStream.rangeClosed(1, processInstances)
                 .mapToObj(i -> ProcessPayloadBuilder.start()
                                                     .withProcessDefinitionKey(BOUNDARY_THROW_CATCH_MESSAGE_IT_PROCESS1)
                                                     .withBusinessKey(BUSINESS_KEY + i)
                                                     .build())
                 .forEach(processInstanceRestTemplate::startProcess);
        
        // then
        Awaitility.await().untilAsserted(() -> {
            assertThat(runtimeService.createProcessInstanceQuery()
                                     .processDefinitionKey(BOUNDARY_THROW_CATCH_MESSAGE_IT_PROCESS1)
                                     .list()).isEmpty();
            
            assertThat(runtimeService.createProcessInstanceQuery()
                                     .processDefinitionKey(BOUNDARY_THROW_CATCH_MESSAGE_IT_PROCESS2)
                                     .list()).isEmpty();
            
            assertThat(runtimeService.createProcessInstanceQuery()
                                     .processDefinitionKey(BOUNDARY_THROW_CATCH_MESSAGE_IT_PROCESS3)
                                     .list()).isEmpty();
        });
        
        verify(messageConnectorConsumer, times(3 * processInstances)).handleCloudBPMNMessageSentEvent(ArgumentMatchers.<Message<CloudBPMNMessageSentEvent>>any());
        verify(messageConnectorConsumer, times(processInstances)).handleCloudBPMNMessageWaitingEvent(ArgumentMatchers.<Message<CloudBPMNMessageWaitingEvent>>any());
        verify(messageConnectorConsumer, times(3 * processInstances)).handleCloudBPMNMessageReceivedEvent(ArgumentMatchers.<Message<CloudBPMNMessageReceivedEvent>>any());

        verify(receiveMessagePayloadMessageStreamListener, times(processInstances)).receiveMessage(ArgumentMatchers.<Message<ReceiveMessagePayload>>any());
        verify(startMessagePayloadMessageStreamListener, times(2 * processInstances)).startMessage(ArgumentMatchers.<Message<StartMessagePayload>>any());
    }

    @Test
    public void shouldCompleteComplexBpmnMessageEventMultipleProcessesWithBoundarySubprocessMessageCatchEvent() {
        // given
        int processInstances = 10;
        
        //when
        IntStream.rangeClosed(1, processInstances)
                 .mapToObj(i -> ProcessPayloadBuilder.start()
                                                     .withProcessDefinitionKey(BOUNDARY_SUBPROCESS_THROW_CATCH_MESSAGE_IT_PROCESS1)
                                                     .withBusinessKey(BUSINESS_KEY + i)
                                                     .build())
                 .forEach(processInstanceRestTemplate::startProcess);
        
        // then
        Awaitility.await().untilAsserted(() -> {
            assertThat(runtimeService.createProcessInstanceQuery()
                                     .processDefinitionKey(BOUNDARY_SUBPROCESS_THROW_CATCH_MESSAGE_IT_PROCESS1)
                                     .list()).isEmpty();
            
            assertThat(runtimeService.createProcessInstanceQuery()
                                     .processDefinitionKey(BOUNDARY_SUBPROCESS_THROW_CATCH_MESSAGE_IT_PROCESS2)
                                     .list()).isEmpty();
            
            assertThat(runtimeService.createProcessInstanceQuery()
                                     .processDefinitionKey(BOUNDARY_SUBPROCESS_THROW_CATCH_MESSAGE_IT_PROCESS3)
                                     .list()).isEmpty();
        });
        
        verify(messageConnectorConsumer, times(3 * processInstances)).handleCloudBPMNMessageSentEvent(ArgumentMatchers.<Message<CloudBPMNMessageSentEvent>>any());
        verify(messageConnectorConsumer, times(processInstances)).handleCloudBPMNMessageWaitingEvent(ArgumentMatchers.<Message<CloudBPMNMessageWaitingEvent>>any());
        verify(messageConnectorConsumer, times(3 * processInstances)).handleCloudBPMNMessageReceivedEvent(ArgumentMatchers.<Message<CloudBPMNMessageReceivedEvent>>any());

        verify(receiveMessagePayloadMessageStreamListener, times(processInstances)).receiveMessage(ArgumentMatchers.<Message<ReceiveMessagePayload>>any());
        verify(startMessagePayloadMessageStreamListener, times(2 * processInstances)).startMessage(ArgumentMatchers.<Message<StartMessagePayload>>any());
    }
    
    @Test
    public void shouldCompleteComplexBpmnMessageEventMultipleProcessesWithStartEventSubprocessEvent() {
        // given
        int processInstances = 10;
        
        //when
        IntStream.rangeClosed(1, processInstances)
                 .mapToObj(i -> ProcessPayloadBuilder.start()
                                                     .withProcessDefinitionKey(EVENT_SUBPROCESS_THROW_CATCH_MESSAGE_IT_PROCESS1)
                                                     .withBusinessKey(BUSINESS_KEY + i)
                                                     .build())
                 .forEach(processInstanceRestTemplate::startProcess);
        
        // then
        Awaitility.await().untilAsserted(() -> {
            assertThat(runtimeService.createProcessInstanceQuery()
                                     .processDefinitionKey(EVENT_SUBPROCESS_THROW_CATCH_MESSAGE_IT_PROCESS1)
                                     .list()).isEmpty();
            
            assertThat(runtimeService.createProcessInstanceQuery()
                                     .processDefinitionKey(EVENT_SUBPROCESS_THROW_CATCH_MESSAGE_IT_PROCESS2)
                                     .list()).isEmpty();
            
            assertThat(runtimeService.createProcessInstanceQuery()
                                     .processDefinitionKey(EVENT_SUBPROCESS_THROW_CATCH_MESSAGE_IT_PROCESS3)
                                     .list()).isEmpty();
        });
        
        verify(messageConnectorConsumer, times(4 * processInstances)).handleCloudBPMNMessageSentEvent(ArgumentMatchers.<Message<CloudBPMNMessageSentEvent>>any());
        verify(messageConnectorConsumer, times(2 * processInstances)).handleCloudBPMNMessageWaitingEvent(ArgumentMatchers.<Message<CloudBPMNMessageWaitingEvent>>any());
        verify(messageConnectorConsumer, times(4 * processInstances)).handleCloudBPMNMessageReceivedEvent(ArgumentMatchers.<Message<CloudBPMNMessageReceivedEvent>>any());

        verify(receiveMessagePayloadMessageStreamListener, times(2 * processInstances)).receiveMessage(ArgumentMatchers.<Message<ReceiveMessagePayload>>any());
        verify(startMessagePayloadMessageStreamListener, times(2 * processInstances)).startMessage(ArgumentMatchers.<Message<StartMessagePayload>>any());
    }
    
    
    @Test
    public void shouldCompleteComplexBpmnMessageEventMultipleProcessesWithStartEventSubprocessNonInterruptingEvent() {
        // given
        int processInstances = 10;
        
        //when
        IntStream.rangeClosed(1, processInstances)
                 .mapToObj(i -> ProcessPayloadBuilder.start()
                                                     .withProcessDefinitionKey(EVENT_SUBPROCESS_NON_INTERRUPTING_THROW_CATCH_MESSAGE_IT_PROCESS1)
                                                     .withBusinessKey(BUSINESS_KEY + i)
                                                     .build())
                 .forEach(processInstanceRestTemplate::startProcess);
        
        // then
        Awaitility.await().untilAsserted(() -> {
            assertThat(runtimeService.createProcessInstanceQuery()
                                     .processDefinitionKey(EVENT_SUBPROCESS_NON_INTERRUPTING_THROW_CATCH_MESSAGE_IT_PROCESS1)
                                     .list()).isEmpty();
            
            assertThat(runtimeService.createProcessInstanceQuery()
                                     .processDefinitionKey(EVENT_SUBPROCESS_NON_INTERRUPTING_THROW_CATCH_MESSAGE_IT_PROCESS2)
                                     .list()).isEmpty();
            
            assertThat(runtimeService.createProcessInstanceQuery()
                                     .processDefinitionKey(EVENT_SUBPROCESS_NON_INTERRUPTING_THROW_CATCH_MESSAGE_IT_PROCESS3)
                                     .list()).isEmpty();
        });
        
        verify(messageConnectorConsumer, times(4 * processInstances)).handleCloudBPMNMessageSentEvent(ArgumentMatchers.<Message<CloudBPMNMessageSentEvent>>any());
        verify(messageConnectorConsumer, times(2 * processInstances)).handleCloudBPMNMessageWaitingEvent(ArgumentMatchers.<Message<CloudBPMNMessageWaitingEvent>>any());
        verify(messageConnectorConsumer, times(4 * processInstances)).handleCloudBPMNMessageReceivedEvent(ArgumentMatchers.<Message<CloudBPMNMessageReceivedEvent>>any());

        verify(receiveMessagePayloadMessageStreamListener, times(2 * processInstances)).receiveMessage(ArgumentMatchers.<Message<ReceiveMessagePayload>>any());
        verify(startMessagePayloadMessageStreamListener, times(2 * processInstances)).startMessage(ArgumentMatchers.<Message<StartMessagePayload>>any());
    }    
    
    @Test
    public void shouldThrowCatchBpmnMessages() {
        // given
        int processInstances = 10;
        
        // when
        IntStream.rangeClosed(1, processInstances)
                 .mapToObj(i -> ProcessPayloadBuilder.start()
                           .withProcessDefinitionKey(INTERMEDIATE_THROW_MESSAGE_PROCESS)
                           .withBusinessKey(BUSINESS_KEY+i)
                           .build())
                 .forEach(processInstanceRestTemplate::startProcess);
               
        IntStream.rangeClosed(1, processInstances)
                 .mapToObj(i -> ProcessPayloadBuilder.start()
                           .withProcessDefinitionKey(INTERMEDIATE_CATCH_MESSAGE_PROCESS)
                           .withBusinessKey(BUSINESS_KEY+i)
                           .build())
                 .forEach(processInstanceRestTemplate::startProcess);
        
        // then
        Awaitility.await().untilAsserted(() -> {
            assertThat(runtimeService.createProcessInstanceQuery()
                                     .processDefinitionKey(INTERMEDIATE_THROW_MESSAGE_PROCESS)
                                     .list()).isEmpty();

            assertThat(runtimeService.createProcessInstanceQuery()
                                     .processDefinitionKey(INTERMEDIATE_CATCH_MESSAGE_PROCESS)
                                     .list()).isEmpty();
        });
        
        verify(messageConnectorConsumer, times(processInstances)).handleCloudBPMNMessageSentEvent(ArgumentMatchers.<Message<CloudBPMNMessageSentEvent>>any());
        verify(messageConnectorConsumer, times(processInstances)).handleCloudBPMNMessageWaitingEvent(ArgumentMatchers.<Message<CloudBPMNMessageWaitingEvent>>any());
        verify(messageConnectorConsumer, times(processInstances)).handleCloudBPMNMessageReceivedEvent(ArgumentMatchers.<Message<CloudBPMNMessageReceivedEvent>>any());

        verify(receiveMessagePayloadMessageStreamListener, times(processInstances)).receiveMessage(ArgumentMatchers.<Message<ReceiveMessagePayload>>any());
        verify(startMessagePayloadMessageStreamListener, never()).startMessage(ArgumentMatchers.<Message<StartMessagePayload>>any());
    }

    @Test
    public void shouldCatchThrowBpmnMessages() {
        // given
        int processInstances = 10;

        // when
        IntStream.rangeClosed(1, processInstances)
                 .mapToObj(i -> ProcessPayloadBuilder.start()
                           .withProcessDefinitionKey(INTERMEDIATE_CATCH_MESSAGE_PROCESS)
                           .withBusinessKey(BUSINESS_KEY+i)
                           .build())
                 .forEach(processInstanceRestTemplate::startProcess);

        IntStream.rangeClosed(1, processInstances)
                 .mapToObj(i -> ProcessPayloadBuilder.start()
                           .withProcessDefinitionKey(INTERMEDIATE_THROW_MESSAGE_PROCESS)
                           .withBusinessKey(BUSINESS_KEY+i)
                           .build())
                 .forEach(processInstanceRestTemplate::startProcess);
               
        // then
        Awaitility.await().untilAsserted(() -> {

            assertThat(runtimeService.createProcessInstanceQuery()
                                     .processDefinitionKey(INTERMEDIATE_THROW_MESSAGE_PROCESS)
                                     .list()).isEmpty();
            
            assertThat(runtimeService.createProcessInstanceQuery()
                                     .processDefinitionKey(INTERMEDIATE_CATCH_MESSAGE_PROCESS)
                                     .list()).isEmpty();
        });

        verify(messageConnectorConsumer, times(processInstances)).handleCloudBPMNMessageSentEvent(ArgumentMatchers.<Message<CloudBPMNMessageSentEvent>>any());
        verify(messageConnectorConsumer, times(processInstances)).handleCloudBPMNMessageWaitingEvent(ArgumentMatchers.<Message<CloudBPMNMessageWaitingEvent>>any());
        verify(messageConnectorConsumer, times(processInstances)).handleCloudBPMNMessageReceivedEvent(ArgumentMatchers.<Message<CloudBPMNMessageReceivedEvent>>any());

        verify(receiveMessagePayloadMessageStreamListener, times(processInstances)).receiveMessage(ArgumentMatchers.<Message<ReceiveMessagePayload>>any());
        verify(startMessagePayloadMessageStreamListener, never()).startMessage(ArgumentMatchers.<Message<StartMessagePayload>>any());
    }
    

}