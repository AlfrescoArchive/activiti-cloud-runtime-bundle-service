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
package org.activiti.cloud.services.events.message;

import java.util.ArrayList;
import java.util.List;

import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.Assert;

public class MessageChannelSenderBuilder {

    private final List<MessageChannelMessageBuilder> builders = new ArrayList<>();
    
    public MessageChannelSenderBuilder() {
        super();
    }

    public <P> MessageChannelSender build(P payload) {
        Assert.notNull(payload, "payload must not be null");
        
        MessageBuilder<P> request = MessageBuilder.withPayload(payload);

        // Let's resolve and add payload class name into message headers 
        request.setHeader(CloudRuntimeEventMessageHeaders.MESSAGE_PAYLOAD_TYPE, payload.getClass().getName());
        
        for (MessageChannelMessageBuilder builder : builders) {
            builder.apply(request);
        }

        return new MessageChannelSender(request.build());
    }

    public MessageChannelSenderBuilder chain(MessageChannelMessageBuilder builder) {
        Assert.notNull(builder, "builder must not be null");

        builders.add(builder);

        return this;
    }

}
