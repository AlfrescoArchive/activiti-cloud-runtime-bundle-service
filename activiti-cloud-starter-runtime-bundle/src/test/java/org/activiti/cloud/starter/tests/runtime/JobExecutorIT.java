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

import java.util.concurrent.CountDownLatch;

import org.activiti.engine.ManagementService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@TestPropertySource("classpath:application-test.properties")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class JobExecutorIT {

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private ManagementService managementService;
    
    
    @Test(timeout=10000)
    public void shouldCompleteAsyncJobsViaMessageBasedJobExecutor() throws InterruptedException {
        int jobCount = 100;
        CountDownLatch jobsCompleted = new CountDownLatch(jobCount);
        
        runtimeService.addEventListener(new AbstractActvitiEventListener() {
            @Override
            public void onEvent(ActivitiEvent event) {
                jobsCompleted.countDown();
            }
            
        }, ActivitiEventType.JOB_EXECUTION_SUCCESS );
        
        //when
        for(int i=0; i<jobCount; i++)
            runtimeService.createProcessInstanceBuilder()
                          .processDefinitionKey("asyncTask")
                          .start();

        // then
        jobsCompleted.await();
        
        // then
        assertThat(runtimeService.createExecutionQuery().count()).isEqualTo(0); 
        assertThat(managementService.createJobQuery().count()).isEqualTo(0); 
    }
    
    abstract class AbstractActvitiEventListener implements ActivitiEventListener {
        
        @Override
        public boolean isFailOnException() {
            // TODO Auto-generated method stub
            return false;
        }
    }
}
