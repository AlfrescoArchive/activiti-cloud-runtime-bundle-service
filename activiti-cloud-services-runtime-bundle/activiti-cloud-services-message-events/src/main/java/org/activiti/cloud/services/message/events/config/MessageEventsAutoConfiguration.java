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

package org.activiti.cloud.services.message.events.config;

import org.activiti.cloud.services.events.configuration.RuntimeBundleProperties;
import org.activiti.cloud.services.message.events.BpmnMessageEventMessageBuilderFactory;
import org.activiti.cloud.services.message.events.BpmnMessageReceivedEventMessageProducer;
import org.activiti.cloud.services.message.events.BpmnMessageSentEventMessageProducer;
import org.activiti.cloud.services.message.events.BpmnMessageWaitingEventMessageProducer;
import org.activiti.cloud.services.message.events.MessageSubscriptionCancelledEventMessageProducer;
import org.activiti.cloud.services.message.events.MessageSubscriptionEventMessageBuilderFactory;
import org.activiti.cloud.services.message.events.StartMessageDeployedEventMessageBuilderFactory;
import org.activiti.cloud.services.message.events.StartMessageDeployedEventMessageProducer;
import org.activiti.cloud.services.message.events.channels.MessageEventsSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:config/message-events-channels.properties")
@EnableBinding({
    MessageEventsSource.class
})
public class MessageEventsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public BpmnMessageEventMessageBuilderFactory messageEventPayloadMessageBuilderFactory(RuntimeBundleProperties properties) {
        return new BpmnMessageEventMessageBuilderFactory(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public StartMessageDeployedEventMessageBuilderFactory messageDeployedEventMessageBuilderFactory(RuntimeBundleProperties properties) {
        return new StartMessageDeployedEventMessageBuilderFactory(properties);
    }
    
    @Bean
    @ConditionalOnMissingBean
    public MessageSubscriptionEventMessageBuilderFactory messageSubscriptionEventMessageBuilderFactory(RuntimeBundleProperties properties) {
        return new MessageSubscriptionEventMessageBuilderFactory(properties);
    }
    
    @Bean
    @ConditionalOnMissingBean
    public BpmnMessageReceivedEventMessageProducer throwMessageReceivedEventListener(MessageEventsSource producerChannels,
                                                                                     BpmnMessageEventMessageBuilderFactory messageBuilderFactory) {
        return new BpmnMessageReceivedEventMessageProducer(producerChannels.messageConnector(),
                                                           messageBuilderFactory);
    }

    @Bean
    @ConditionalOnMissingBean
    public BpmnMessageWaitingEventMessageProducer throwMessageWaitingEventMessageProducer(MessageEventsSource producerChannels,
                                                                                          BpmnMessageEventMessageBuilderFactory messageBuilderFactory) {
        return new BpmnMessageWaitingEventMessageProducer(producerChannels.messageConnector(),
                                                          messageBuilderFactory);
    }

    @Bean
    @ConditionalOnMissingBean
    public BpmnMessageSentEventMessageProducer bpmnMessageSentEventProducer(MessageEventsSource producerChannels,
                                                                            BpmnMessageEventMessageBuilderFactory messageBuilderFactory) {
        return new BpmnMessageSentEventMessageProducer(producerChannels.messageConnector(),
                                                       messageBuilderFactory);
    }
    
    @Bean
    @ConditionalOnMissingBean
    public StartMessageDeployedEventMessageProducer MessageDeployedEventMessageProducer(MessageEventsSource producerChannels,
                                                                                        StartMessageDeployedEventMessageBuilderFactory messageBuilderFactory) {
        return new StartMessageDeployedEventMessageProducer(producerChannels.messageConnector(),
                                                            messageBuilderFactory);
    }
    
    @Bean
    @ConditionalOnMissingBean
    public MessageSubscriptionCancelledEventMessageProducer messageSubscriptionCancelledEventMessageProducer(MessageEventsSource producerChannels,
                                                                                                             MessageSubscriptionEventMessageBuilderFactory messageBuilderFactory) {
        return new MessageSubscriptionCancelledEventMessageProducer(producerChannels.messageConnector(),
                                                                    messageBuilderFactory);
    }
}
