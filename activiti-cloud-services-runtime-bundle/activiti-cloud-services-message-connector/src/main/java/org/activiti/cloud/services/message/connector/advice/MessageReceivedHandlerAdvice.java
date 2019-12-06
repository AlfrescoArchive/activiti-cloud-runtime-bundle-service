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

import static org.activiti.api.process.model.events.BPMNMessageEvent.MessageEvents.MESSAGE_RECEIVED;
import static org.activiti.api.process.model.events.BPMNMessageEvent.MessageEvents.MESSAGE_WAITING;
import static org.activiti.cloud.services.message.connector.integration.MessageEventHeaders.MESSAGE_EVENT_TYPE;

import java.util.Optional;

import org.activiti.cloud.services.message.connector.support.LockTemplate;
import org.activiti.cloud.services.message.connector.support.MessageTimestampComparator;
import org.springframework.integration.aggregator.CorrelationStrategy;
import org.springframework.integration.store.MessageGroup;
import org.springframework.integration.store.MessageGroupStore;
import org.springframework.integration.util.UUIDConverter;
import org.springframework.messaging.Message;

public class MessageReceivedHandlerAdvice extends AbstractMessageConnectorHandlerAdvice {

    private final MessageGroupStore messageStore;
    private final LockTemplate lockTemplate;
    private final CorrelationStrategy correlationStrategy;
    
    public MessageReceivedHandlerAdvice(MessageGroupStore messageStore,
                                        CorrelationStrategy correlationStrategy,
                                        LockTemplate lockTemplate) {
        this.messageStore = messageStore;
        this.lockTemplate = lockTemplate;
        this.correlationStrategy = correlationStrategy;
    }
    
    @Override
    public <T> T doHandle(Message<?> message) {
        Object groupId = correlationStrategy.getCorrelationKey(message);
        Object key = UUIDConverter.getUUID(groupId).toString();

        lockTemplate.lockInterruptibly(key, () -> {
            MessageGroup group = messageStore.getMessageGroup(groupId);
            
            group.getMessages()
                 .stream()
                 .filter(this::canRemove)
                 .min(MessageTimestampComparator.INSTANCE)
                 .ifPresent(result -> {
                     messageStore.removeMessagesFromGroup(groupId, result);                            
                 });
        });
        
        return null;
        
    }

    @Override
    public boolean canHandle(Message<?> message) {
        return Optional.ofNullable(message.getHeaders()
                                          .get(MESSAGE_EVENT_TYPE))
                       .filter(MESSAGE_RECEIVED.name()::equals)
                       .isPresent();
    }
    
    public boolean canRemove(Message<?> message) {
        return Optional.ofNullable(message.getHeaders()
                                          .get(MESSAGE_EVENT_TYPE))
                       .filter(MESSAGE_WAITING.name()::equals)
                       .isPresent();
    }
}
