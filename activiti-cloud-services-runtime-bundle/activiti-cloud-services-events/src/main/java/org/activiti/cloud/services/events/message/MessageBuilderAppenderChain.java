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

public class MessageBuilderAppenderChain {

    private final List<MessageBuilderAppender> appenders = new ArrayList<>();

    public <P> MessageBuilder<P> withPayload(P payload) {
        Assert.notNull(payload, "payload must not be null");
        
        MessageBuilder<P> messageBuilder = MessageBuilder.withPayload(payload);

        // Let's resolve payload class name 
        messageBuilder.setHeader("messagePayloadType", payload.getClass().getName());
        
        for (MessageBuilderAppender appender : appenders) {
            appender.apply(messageBuilder);
        }

        return messageBuilder;
    }

    public MessageBuilderAppenderChain chain(MessageBuilderAppender filter) {
        Assert.notNull(filter, "filter must not be null");

        appenders.add(filter);

        return this;
    }

}
