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

import org.activiti.engine.runtime.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.binding.BinderAwareChannelResolver;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

public class DefaultJobMessageProducer implements JobMessageProducer {
    private static final Logger logger = LoggerFactory.getLogger(DefaultJobMessageProducer.class);

    private final BinderAwareChannelResolver resolver;
    private final ApplicationEventPublisher eventPublisher;

    public DefaultJobMessageProducer(BinderAwareChannelResolver resolver,
                                     ApplicationEventPublisher eventPublisher) {
        this.resolver = resolver;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void sendMessage(@NonNull String destination, @NonNull Job job) {
        Assert.isTrue(TransactionSynchronizationManager.isSynchronizationActive(), "requires active transaction synchronization");
        Assert.hasLength(job.getId(), "job id must not be empty");
        Assert.hasLength(destination, "destination must not be empty");
        
        // Let's try to resolve message channel while inside main Activiti transaction to minimize infrastructure errors 
        MessageChannel messageChannel = resolver.resolveDestination(destination);

        // Let's send message right after the main transaction has successfully committed. 
        TransactionSynchronizationManager.registerSynchronization(new JobMessageTransactionSynchronization(destination, 
                                                                                                           messageChannel, 
                                                                                                           job));
    }
    
    protected Message<String> buildMessage(Job job) {
        return MessageBuilder.withPayload(job.getId())
                // TODO set headers?  
                .build();
    }
    
    class JobMessageTransactionSynchronization implements TransactionSynchronization {

        private final MessageChannel messageChannel;
        private final Job job;
        private final String destination;

        public JobMessageTransactionSynchronization(String destination, MessageChannel messageChannel, Job job) {
            this.destination = destination;
            this.messageChannel = messageChannel;
            this.job = job;
        }

        @Override
        public void afterCommit() {
            Message<String> message = buildMessage(job);
            
            logger.debug("Sending job message '{}' to destination '{}' via message channel: {}", message, destination, messageChannel);
            
            try { 
                boolean sent = messageChannel.send(message);
                
                if(!sent)
                    throw new RuntimeException("Job message cannot be sent due to non-fatal reason from message channel.");

                eventPublisher.publishEvent(new JobMessageSentEvent(job.getId(), destination, job));
                
            } catch(Exception cause) {
                logger.error("Sending job message {} failed due to error: {}", message, cause.getMessage());

                eventPublisher.publishEvent(new JobMessageFailedEvent(job.getId(), destination, cause, job));
            }
        }
    }
}
