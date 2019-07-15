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
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
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
    public void sendMessage(String destination, Job job) {
        eventPublisher.publishEvent(new JobMessageEvent(job.getId(), destination, job));
    }
    
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(@NonNull JobMessageEvent event) {
        logger.debug("On JobMessageEvent: {}", event);

        Assert.isTrue(TransactionSynchronizationManager.isActualTransactionActive(), "requires actual active transaction");

        Assert.notNull(event.getJobId(), "job id must not be null");
        Assert.notNull(event.getDestination(), "destination must not be null");
        
        Message<String> message = buildMessage(event);
        
        // Let's send message 
        MessageChannel messageChannel = resolver.resolveDestination(event.getDestination());
        
        messageChannel.send(message);
    }

    protected Message<String> buildMessage(JobMessageEvent event) {
        return MessageBuilder.withPayload(event.getJobId())
                // TODO set headers?  
                .build();
        
    }
}
