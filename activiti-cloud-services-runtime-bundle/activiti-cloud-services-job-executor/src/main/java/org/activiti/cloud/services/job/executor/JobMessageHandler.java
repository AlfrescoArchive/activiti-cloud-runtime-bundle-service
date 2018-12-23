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

package org.activiti.cloud.services.job.executor;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.asyncexecutor.ExecuteAsyncRunnable;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.JobEntity;
import org.activiti.engine.runtime.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;

public class JobMessageHandler  implements MessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(JobMessageHandler.class);

    private final ProcessEngineConfigurationImpl processEngineConfiguration;

    public JobMessageHandler(ProcessEngineConfigurationImpl processEngineConfiguration) {
        this.processEngineConfiguration = processEngineConfiguration;
    }

    @Override
    public void handleMessage(Message<?> message) throws MessagingException {
        try {
            String jobId = new String((byte[]) message.getPayload());

            logger.info("Received job message with id: " + jobId);
            
            // Let's try to find existing job by jobId
            Job job = processEngineConfiguration.getCommandExecutor().execute(new Command<JobEntity>() {
                @Override
                public JobEntity execute(CommandContext commandContext) {
                  return commandContext.getJobEntityManager().findById(jobId);
                }
              });
            
            // Let's execute job  
            if(job != null) {
                ExecuteAsyncRunnable executeAsyncRunnable = new ExecuteAsyncRunnable(job, processEngineConfiguration);

                executeAsyncRunnable.run();
            } else {
                logger.info("Job " + jobId + " does not exist. Job message has been dropped.");
            }

        } catch (Exception cause) {
            throw new ActivitiException("Exception when handling message from job queue", cause);
        }
    }

}