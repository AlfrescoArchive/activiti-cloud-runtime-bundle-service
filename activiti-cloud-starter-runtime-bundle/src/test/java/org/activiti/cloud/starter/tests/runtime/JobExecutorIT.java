/*
 * Copyright 2017 Alfresco, Inc. and/or its affiliates.
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
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.activiti.cloud.services.job.executor.JobMessageHandler;
import org.activiti.cloud.services.job.executor.JobMessageHandlerFactory;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.activiti.spring.boot.ProcessEngineConfigurationConfigurer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.stream.binder.ConsumerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@TestPropertySource("classpath:application-test.properties")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "activiti.cloud.rb.job-executor.enabled=true",
        "activiti.cloud.rb.job-executor.message-job-consumer.max-attempts=4"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class JobExecutorIT {
    private static final Logger logger = LoggerFactory.getLogger(JobExecutorIT.class);

    private static final String TEST_BOUNDARY_TIMER_EVENT = "testBoundaryTimerEvent";
    private static final String START_TIMER_EVENT_EXAMPLE = "startTimerEventExample";
    private static final String INTERMEDIATE_TIMER_EVENT_EXAMPLE = "intermediateTimerEventExample";

    private static final String ASYNC_TASK = "asyncTask";

    @Autowired
    private RuntimeService runtimeService;
    
    @Autowired
    private ManagementService managementService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private ConsumerProperties messageJobConsumerProperties;
    
    private ProcessEngineConfiguration processEngineConfiguration;
    
    @Autowired
    private MessageHandler jobMessageHandler;

    @TestConfiguration
    static class JobExecutorITProcessEngineConfigurer implements ProcessEngineConfigurationConfigurer {
        
        @Override
        public void configure(SpringProcessEngineConfiguration processEngineConfiguration) {
            processEngineConfiguration.setAsyncExecutorDefaultTimerJobAcquireWaitTime(1000);
            processEngineConfiguration.setAsyncExecutorDefaultAsyncJobAcquireWaitTime(1000);
        }
        
        @Bean
        public JobMessageHandlerFactory jobMessageHandlerFactory() {
            return new JobMessageHandlerFactory() {

                @Override
                public MessageHandler create(ProcessEngineConfigurationImpl configuration) {
                    return Mockito.spy(new JobMessageHandler(configuration));
                }
            };
        }
    }
    
    @Before
    public void setUp() {
        Mockito.reset(jobMessageHandler);

        processEngineConfiguration = ProcessEngines.getProcessEngine("default").getProcessEngineConfiguration();
    }
    
    @After
    public void tearDown() {
        processEngineConfiguration.getClock().reset();
    }

    @Test
    public void shouldConfigureConsumerProperties() {
        assertThat(messageJobConsumerProperties.getMaxAttempts()).as("should configure consumer properties")
                                                                 .isEqualTo(4);
    }

    @Test
    public void testJobMessageHandler() {
        assertThat(jobMessageHandler).as("should register JobMessageHandler bean")
                                     .isInstanceOf(JobMessageHandler.class);
    }
    
    @SuppressWarnings("deprecation")
    @Test
    public void testAsyncJobs() throws InterruptedException {
        int jobCount = 100;
        CountDownLatch jobsCompleted = new CountDownLatch(jobCount);
        
        runtimeService.addEventListener(new CountDownLatchActvitiEventListener(jobsCompleted), 
                                        ActivitiEventType.JOB_EXECUTION_SUCCESS );
        
        String processDefinitionId = repositoryService.createProcessDefinitionQuery()
                                                      .processDefinitionKey(ASYNC_TASK)
                                                      .singleResult()
                                                      .getId();
        //when
        for(int i=0; i<jobCount; i++)
            runtimeService.createProcessInstanceBuilder()
                          .processDefinitionId(processDefinitionId)
                          .start();

        // then
        await("the async executions should complete and no more jobs should exist")
                .untilAsserted(() -> {
                    assertThat(runtimeService.createExecutionQuery()
                                             .processDefinitionKey(ASYNC_TASK).count()).isEqualTo(0);
                    
                    assertThat(managementService.createJobQuery()
                               .processDefinitionId(processDefinitionId)
                               .count()).isEqualTo(0); 
                });

        assertThat(jobsCompleted.await(1, TimeUnit.MINUTES)).as("should complete all jobs")
                                                            .isTrue();
        // message handler is invoked
        verify(jobMessageHandler, times(jobCount)).handleMessage(ArgumentMatchers.<Message<?>>any());
    }
    
    @SuppressWarnings("deprecation")
    @Test
    public void testCatchingTimerEvent() throws Exception {
        CountDownLatch jobsCompleted = new CountDownLatch(1);
        CountDownLatch timerScheduled = new CountDownLatch(1);
        CountDownLatch timerFired = new CountDownLatch(1);

        // Set the clock fixed
        Date startTime = new Date();

        runtimeService.addEventListener(new CountDownLatchActvitiEventListener(timerScheduled),
                                        ActivitiEventType.TIMER_SCHEDULED);

        runtimeService.addEventListener(new CountDownLatchActvitiEventListener(timerFired),
                                        ActivitiEventType.TIMER_FIRED);

        runtimeService.addEventListener(new CountDownLatchActvitiEventListener(jobsCompleted),
                                        ActivitiEventType.JOB_EXECUTION_SUCCESS);

        // when
        ProcessInstance pi = runtimeService.startProcessInstanceByKey(INTERMEDIATE_TIMER_EVENT_EXAMPLE);

        // then
        assertThat(pi).isNotNull();
        
        await("the timer job should be created")
            .untilAsserted(() -> {
                assertThat(managementService.createTimerJobQuery()
                                            .processInstanceId(pi.getId())
                                            .count()).isEqualTo(1);
            });

        // After setting the clock to time '5 minutes and 5 seconds', the timer should fire
        processEngineConfiguration.getClock().setCurrentTime(new Date(startTime.getTime() + ((5 * 60 * 1000) + 5000)));

        // timer event has been scheduled
        assertThat(timerScheduled.await(1, TimeUnit.MINUTES)).as("should schedule timer")
                                                             .isTrue();
        
        // then
        await("the process instance should complete and no more jobs should exist")
           .untilAsserted(() -> {
               assertThat(runtimeService.createProcessInstanceQuery()
                                        .processDefinitionKey(pi.getProcessDefinitionKey())
                                        .count()).isEqualTo(0);
               
               assertThat(managementService.createTimerJobQuery()
                                           .processInstanceId(pi.getId())
                                           .count()).isEqualTo(0);
           });

        // timer event has been fired
        assertThat(timerFired.await(1, TimeUnit.MINUTES)).as("should fire timer")
                                                         .isTrue();

        // job event has been completed
        assertThat(jobsCompleted.await(1, TimeUnit.MINUTES)).as("should complete job")
                                                            .isTrue();
        // message handler is invoked
        verify(jobMessageHandler).handleMessage(ArgumentMatchers.<Message<?>>any());
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testStartTimeEvent() throws InterruptedException {
        CountDownLatch jobCompleted = new CountDownLatch(1);
        CountDownLatch timerFired = new CountDownLatch(1);

        // Set the clock fixed
        Date startTime = new Date();


        runtimeService.addEventListener(new CountDownLatchActvitiEventListener(timerFired),
                                        ActivitiEventType.TIMER_FIRED);

        runtimeService.addEventListener(new CountDownLatchActvitiEventListener(jobCompleted),
                                        ActivitiEventType.JOB_EXECUTION_SUCCESS);

        //when
        String processDefinitionId = repositoryService.createProcessDefinitionQuery()
                                                      .processDefinitionKey(START_TIMER_EVENT_EXAMPLE)
                                                      .singleResult()
                                                      .getId();
        // when
        ProcessInstance pi = runtimeService.createProcessInstanceQuery().processDefinitionKey(START_TIMER_EVENT_EXAMPLE).singleResult();

        // then
        assertThat(pi).isNull();
        
        await("the timer job should be created")
            .untilAsserted(() -> {
                assertThat(managementService.createTimerJobQuery()
                                            .processDefinitionId(processDefinitionId)
                                            .count()).isEqualTo(1);
            });

        // After setting the clock to time '1 hour and 5 seconds', the timer should fire
        processEngineConfiguration.getClock()
                                  .setCurrentTime(new Date(startTime.getTime() + ((60 * 60 * 1000) + 5000)));

        // then
        await("the process should start and no more timer jobs should exist")
           .untilAsserted(() -> {
               assertThat(runtimeService.createProcessInstanceQuery()
                                        .processDefinitionId(processDefinitionId)
                                        .count()).isEqualTo(1);
               
               assertThat(managementService.createTimerJobQuery()
                                           .processDefinitionId(processDefinitionId)
                                           .count()).isEqualTo(0);
           });

        // timer event has been fired
        assertThat(timerFired.await(1, TimeUnit.MINUTES)).as("should fire timer")
                                                         .isTrue();

        // job event has been completed
        assertThat(jobCompleted.await(1, TimeUnit.MINUTES)).as("should complete job")
                                                            .isTrue();
        // message handler is invoked
        verify(jobMessageHandler).handleMessage(ArgumentMatchers.<Message<?>>any());
    }
    
    @SuppressWarnings("deprecation")
    @Test
    public void testBoundaryTimerEvent() throws Exception {
        CountDownLatch jobsCompleted = new CountDownLatch(1);
        CountDownLatch timerScheduled = new CountDownLatch(1);
        CountDownLatch timerFired = new CountDownLatch(1);

        // Set the clock fixed
        Date startTime = new Date();

        runtimeService.addEventListener(new CountDownLatchActvitiEventListener(timerScheduled),
                                        ActivitiEventType.TIMER_SCHEDULED);

        runtimeService.addEventListener(new CountDownLatchActvitiEventListener(timerFired),
                                        ActivitiEventType.TIMER_FIRED);

        runtimeService.addEventListener(new CountDownLatchActvitiEventListener(jobsCompleted),
                                        ActivitiEventType.JOB_EXECUTION_SUCCESS);

        // when
        ProcessInstance pi = runtimeService.startProcessInstanceByKey(TEST_BOUNDARY_TIMER_EVENT);

        // then
        assertThat(pi).isNotNull();
        
        await("the timer job should be created")
            .untilAsserted(() -> {
                assertThat(managementService.createTimerJobQuery()
                                            .processInstanceId(pi.getId())
                                            .count()).isEqualTo(1);
            });

        // After setting the clock to time '5 minutes and 5 seconds', the timer should fire
        processEngineConfiguration.getClock().setCurrentTime(new Date(startTime.getTime() + ((5 * 60 * 1000) + 5000)));

        // timer event has been scheduled
        assertThat(timerScheduled.await(1, TimeUnit.MINUTES)).as("should schedule timer")
                                                             .isTrue();
        
        // then
        await("the process instance should complete and no more timer jobs should exist")
           .untilAsserted(() -> {
               assertThat(runtimeService.createProcessInstanceQuery()
                                        .processDefinitionKey(pi.getProcessDefinitionKey())
                                        .count()).isEqualTo(0);
               
               assertThat(managementService.createTimerJobQuery()
                                           .processInstanceId(pi.getId())
                                           .count()).isEqualTo(0);
           });

        // timer event has been fired
        assertThat(timerFired.await(1, TimeUnit.MINUTES)).as("should fire timer")
                                                         .isTrue();

        // job event has been completed
        assertThat(jobsCompleted.await(1, TimeUnit.MINUTES)).as("should complete job")
                                                            .isTrue();
        // message handler is invoked
        verify(jobMessageHandler).handleMessage(ArgumentMatchers.<Message<?>>any());
    }
    
    abstract class AbstractActvitiEventListener implements ActivitiEventListener {
        
        @Override
        public boolean isFailOnException() {
            return false;
        }
    }

    class CountDownLatchActvitiEventListener extends AbstractActvitiEventListener {
        
        private final CountDownLatch countDownLatch;
           
        public CountDownLatchActvitiEventListener(CountDownLatch countDownLatch) {
            this.countDownLatch = countDownLatch;
        }
        
        @Override
        public void onEvent(ActivitiEvent arg0) {
            logger.info("Received Activiti Event: {}", arg0);
            
            countDownLatch.countDown();
        }
    }
    

    
}
