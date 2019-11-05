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

package org.activiti.cloud.services.messages;

import org.activiti.api.process.model.payloads.ReceiveMessagePayload;
import org.activiti.cloud.services.messages.channels.MessageEventsChannels;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;

public class ReceiveMessagePayloadMessageStreamListener {
    
    private static final Logger logger = LoggerFactory.getLogger(ReceiveMessagePayloadMessageStreamListener.class);
    
    private final ApplicationEventPublisher eventPublisher;
    
    public ReceiveMessagePayloadMessageStreamListener(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }
    
    @StreamListener(MessageEventsChannels.RECEIVE_MESSAGE_PAYLOAD_CONSUMER_CHANNEL)
    public void receiveMessage(Message<ReceiveMessagePayload> message) throws MessagingException {
        logger.debug("receiveMessage: {}", message);
        
        eventPublisher.publishEvent(message.getPayload());
    }
}