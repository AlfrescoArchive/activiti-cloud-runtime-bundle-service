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

package org.activiti.cloud.services.message.connector.processor;

import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

import org.activiti.api.process.model.builders.MessagePayloadBuilder;
import org.activiti.api.process.model.payloads.MessageEventPayload;
import org.activiti.api.process.model.payloads.StartMessagePayload;
import org.activiti.cloud.services.message.connector.integration.MessageEventHeaders;
import org.activiti.cloud.services.message.connector.support.MessageTimestampComparator;
import org.activiti.cloud.services.message.connector.support.SpELEvaluatingMessageListProcessor;
import org.activiti.cloud.services.message.connector.support.SpELEvaluatingReleaseStrategy;
import org.springframework.integration.aggregator.MessageListProcessor;
import org.springframework.integration.aggregator.ReleaseStrategy;
import org.springframework.integration.store.MessageGroup;
import org.springframework.integration.store.MessageGroupStore;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;

public class StartMessagePayloadGroupProcessor implements MessageGroupProcessorHandler {
    
    private final static String condition = "!messages.?[headers['eventType'] == 'START_MESSAGE_DEPLOYED'].empty " 
                                             + "&& !messages.?[headers['eventType'] == 'MESSAGE_SENT'].empty";

    private final static ReleaseStrategy strategy = new SpELEvaluatingReleaseStrategy(condition);
    
    private final Comparator<Message<?>> comparator = new MessageTimestampComparator();
    
    private final static MessageListProcessor processor = new SpELEvaluatingMessageListProcessor("#this.?[headers['eventType'] == 'MESSAGE_SENT']");

    private final MessageGroupStore messageGroupStore;

    public StartMessagePayloadGroupProcessor(MessageGroupStore messageGroupStore) {
        this.messageGroupStore = messageGroupStore;
    }    
    @Override
    public Collection<Message<?>> handle(MessageGroup group) {
        
        if (canProcess(group)) {
            Collection<Message<?>> result = process(group.getMessages());

            messageGroupStore.removeMessagesFromGroup(group.getGroupId(),
                                                      result);
            return result.stream()
                         .sorted(comparator)
                         .map(this::messageEventPayload)
                         .collect(Collectors.toList());
        }
        
        return null;
    }
    
    private Message<?> messageEventPayload(Message<?> message) {
        MessageEventPayload eventPayload = MessageEventPayload.class.cast(message.getPayload());

        StartMessagePayload startPayload = MessagePayloadBuilder.start(eventPayload.getName())
                                                                .withBusinessKey(eventPayload.getBusinessKey())
                                                                .withVariables(eventPayload.getVariables())
                                                                .build();

        return MessageBuilder.withPayload(startPayload)
                             .setHeader(MessageEventHeaders.MESSAGE_PAYLOAD_TYPE, 
                                        StartMessagePayload.class.getSimpleName())
                             .build();       
    }
    
    public boolean canProcess(MessageGroup group) {
        return strategy.canRelease(group);
    }
    
    public <R> R process(Collection<Message<?>> messages) {
        return (R) processor.process(messages);
    }

}
