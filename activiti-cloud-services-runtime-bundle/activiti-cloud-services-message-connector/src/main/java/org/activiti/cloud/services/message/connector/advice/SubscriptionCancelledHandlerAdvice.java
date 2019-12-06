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

package org.activiti.cloud.services.message.connector.advice;

import java.util.Collection;

import org.activiti.cloud.services.message.connector.support.LockTemplate;
import org.activiti.cloud.services.message.connector.support.SpELEvaluatingMessageListProcessor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.integration.aggregator.CorrelationStrategy;
import org.springframework.integration.aggregator.MessageListProcessor;
import org.springframework.integration.core.MessageSelector;
import org.springframework.integration.handler.advice.AbstractHandleMessageAdvice;
import org.springframework.integration.store.MessageGroup;
import org.springframework.integration.store.MessageGroupStore;
import org.springframework.integration.util.UUIDConverter;
import org.springframework.messaging.Message;

public class SubscriptionCancelledHandlerAdvice extends AbstractHandleMessageAdvice {

    private final MessageGroupStore messageStore;
    private final LockTemplate lockTemplate;
    private final CorrelationStrategy correlationStrategy;
    
    private final MessageSelector messageSelector = message -> "MESSAGE_SUBSCRIPTION_CANCELLED".equals(message.getHeaders()
                                                                                                              .get("eventType"));
    
    private final MessageListProcessor pending = new SpELEvaluatingMessageListProcessor("#this.?[headers['eventType'] != 'START_MESSAGE_DEPLOYED']");
    
    public SubscriptionCancelledHandlerAdvice(MessageGroupStore messageStore,
                                              CorrelationStrategy correlationStrategy,
                                              LockTemplate lockTemplate) {
        this.messageStore = messageStore;
        this.lockTemplate = lockTemplate;
        this.correlationStrategy = correlationStrategy;
    }
    
    @Override
    public String getComponentType() {
        return SubscriptionCancelledHandlerAdvice.class.getSimpleName();
    }

    @Override
    protected Object doInvoke(MethodInvocation invocation, 
                              Message<?> message) throws Throwable {
        
        if (!messageSelector.accept(message)) {
            return invocation.proceed();            
        }
        
        Object groupId = correlationStrategy.getCorrelationKey(message);
        Object key = UUIDConverter.getUUID(groupId).toString();

        lockTemplate.lockInterruptibly(key, () -> {
            MessageGroup group = messageStore.getMessageGroup(groupId);
            Collection<Message<?>> messages = process(pending, 
                                                      group.getMessages());;
            if(!messages.isEmpty()) {
                messageStore.removeMessagesFromGroup(groupId, messages);
            }
        });
        
        return null;
    }
    
    @SuppressWarnings("unchecked")
    private Collection<Message<?>> process(MessageListProcessor processor, Collection<Message<?>> messages) {
        return ( Collection<Message<?>>) processor.process(messages);
    }

}
