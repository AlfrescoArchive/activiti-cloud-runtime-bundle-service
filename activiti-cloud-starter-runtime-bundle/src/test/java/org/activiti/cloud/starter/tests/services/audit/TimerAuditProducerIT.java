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

import static org.activiti.cloud.starter.tests.services.audit.AuditProducerIT.ALL_REQUIRED_HEADERS;
import static org.activiti.cloud.starter.tests.services.audit.AuditProducerIT.RUNTIME_BUNDLE_INFO_HEADERS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.awaitility.Awaitility.await;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.activiti.api.process.model.builders.StartProcessPayloadBuilder;
import org.activiti.api.process.model.events.BPMNTimerEvent;
import org.activiti.cloud.api.model.shared.events.CloudRuntimeEvent;
import org.activiti.cloud.api.process.model.CloudProcessInstance;
import org.activiti.cloud.api.process.model.events.CloudBPMNTimerFiredEvent;
import org.activiti.cloud.api.process.model.events.CloudBPMNTimerScheduledEvent;
import org.activiti.cloud.starter.tests.helper.ProcessInstanceRestTemplate;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.impl.test.JobTestHelper;
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
public class TimerAuditProducerIT {

    private static final String PROCESS_INTERMEDIATE_TIMER_EVENT = "intermediateTimerEventExample";

    @Autowired
    private ProcessInstanceRestTemplate processInstanceRestTemplate;

    @Autowired
    private AuditConsumerStreamHandler streamHandler;
    
    @Autowired
    ProcessEngineConfiguration processEngineConfiguration;
    
    @Autowired
    private ManagementService managementService;

    @Test
    public void shouldProduceEventsForIntermediateTimerEvent() {

        //given
        Date startTime = new Date();
        ResponseEntity<CloudProcessInstance> startProcessEntity = processInstanceRestTemplate.startProcess(new StartProcessPayloadBuilder()
                                                                                                                    .withProcessDefinitionKey(PROCESS_INTERMEDIATE_TIMER_EVENT)
                                                                                                                    .withName("processInstanceName")
                                                                                                                    .withBusinessKey("businessKey")
                                                                                                                    .build());
 
        //when
        await().untilAsserted(() -> {
            assertThat(streamHandler.getReceivedHeaders()).containsKeys(RUNTIME_BUNDLE_INFO_HEADERS);
            assertThat(streamHandler.getReceivedHeaders()).containsKeys(ALL_REQUIRED_HEADERS);
            List<CloudRuntimeEvent<?, ?>> receivedEvents = streamHandler.getLatestReceivedEvents();

            List<CloudBPMNTimerScheduledEvent> timerEvents = receivedEvents
                    .stream()
                    .filter(CloudBPMNTimerScheduledEvent.class::isInstance)
                    .map(CloudBPMNTimerScheduledEvent.class::cast)
                    .collect(Collectors.toList());

            assertThat(timerEvents)
                    .extracting( CloudRuntimeEvent::getEventType,
                                 CloudRuntimeEvent::getBusinessKey,
                                 CloudRuntimeEvent::getProcessDefinitionId,
                                 CloudRuntimeEvent::getProcessInstanceId,
                                 CloudRuntimeEvent::getProcessDefinitionKey,
                                 CloudRuntimeEvent::getProcessDefinitionVersion,
                                 event -> event.getEntity().getProcessDefinitionId(),
                                 event -> event.getEntity().getProcessInstanceId(),
                                 event -> event.getEntity().getTimerPayload().getJobHandlerConfiguration(),
                                 event -> event.getEntity().getTimerPayload().getJobType(),
                                 event -> event.getEntity().getTimerPayload().getJobHandlerType()  
                    )
                    .contains(
                            tuple(BPMNTimerEvent.TimerEvents.TIMER_SCHEDULED,
                                  "businessKey",
                                  startProcessEntity.getBody().getProcessDefinitionId(),
                                  startProcessEntity.getBody().getId(),
                                  startProcessEntity.getBody().getProcessDefinitionKey(),
                                  startProcessEntity.getBody().getProcessDefinitionVersion(),
                                  startProcessEntity.getBody().getProcessDefinitionId(),
                                  startProcessEntity.getBody().getId(),
                                  "{\"activityId\":\"timer\"}",
                                  "timer",
                                  "trigger-timer"
                                  
                            )
                    );

        });
            
         
        long waitTime = 50 * 60 * 1000;
        Date dueDate = new Date(startTime.getTime() + waitTime);
        
        // After setting the clock to time '50minutes and 5 seconds', the second timer should fire
        processEngineConfiguration.getClock().setCurrentTime(new Date(dueDate.getTime() + 5000));
        waitForJobExecutorToProcessAllJobs(5000L, 25L);
        
        //when
        await().untilAsserted(() -> {
            assertThat(streamHandler.getReceivedHeaders()).containsKeys(RUNTIME_BUNDLE_INFO_HEADERS);
            assertThat(streamHandler.getReceivedHeaders()).containsKeys(ALL_REQUIRED_HEADERS);
            List<CloudRuntimeEvent<?, ?>> receivedEvents = streamHandler.getLatestReceivedEvents();

            List<CloudBPMNTimerFiredEvent> timerEvents = receivedEvents
                    .stream()
                    .filter(CloudBPMNTimerFiredEvent.class::isInstance)
                    .map(CloudBPMNTimerFiredEvent.class::cast)
                    .collect(Collectors.toList());

            assertThat(timerEvents)
                    .extracting( CloudRuntimeEvent::getEventType,
                                 CloudRuntimeEvent::getBusinessKey,
                                 CloudRuntimeEvent::getProcessDefinitionId,
                                 CloudRuntimeEvent::getProcessInstanceId,
                                 CloudRuntimeEvent::getProcessDefinitionKey,
                                 CloudRuntimeEvent::getProcessDefinitionVersion,
                                 event -> event.getEntity().getProcessDefinitionId(),
                                 event -> event.getEntity().getProcessInstanceId(),
                                 event -> event.getEntity().getTimerPayload().getJobHandlerConfiguration(),
                                 event -> event.getEntity().getTimerPayload().getJobType(),
                                 event -> event.getEntity().getTimerPayload().getJobHandlerType()  
                    )
                    .contains(
                            tuple(BPMNTimerEvent.TimerEvents.TIMER_FIRED,
                                  "businessKey",
                                  startProcessEntity.getBody().getProcessDefinitionId(),
                                  startProcessEntity.getBody().getId(),
                                  startProcessEntity.getBody().getProcessDefinitionKey(),
                                  startProcessEntity.getBody().getProcessDefinitionVersion(),
                                  startProcessEntity.getBody().getProcessDefinitionId(),
                                  startProcessEntity.getBody().getId(),
                                  "{\"activityId\":\"timer\"}",
                                  "timer",
                                  "trigger-timer"
                                  
                            )
                    );

        });

    }
    
    public void waitForJobExecutorToProcessAllJobs(long maxMillisToWait, long intervalMillis) {
        JobTestHelper.waitForJobExecutorToProcessAllJobs(processEngineConfiguration, managementService, maxMillisToWait, intervalMillis);
    }

}
