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

package org.activiti.cloud.services.messages.config;

import org.activiti.cloud.services.events.configuration.RuntimeBundleProperties;
import org.activiti.cloud.services.events.converter.ToCloudProcessRuntimeEventConverter;
import org.activiti.cloud.services.messages.BpmnMessageEventMessageBuilderFactory;
import org.activiti.cloud.services.messages.BpmnMessageReceivedEventMessageProducer;
import org.activiti.cloud.services.messages.BpmnMessageSentEventMessageProducer;
import org.activiti.cloud.services.messages.BpmnMessageWaitingEventMessageProducer;
import org.activiti.cloud.services.messages.MessageEventsProcessEngineConfigurator;
import org.activiti.cloud.services.messages.ReceiveMessagePayloadMessageStreamListener;
import org.activiti.cloud.services.messages.channels.MessageEventsChannels;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:config/message-events-channels.properties")
@EnableBinding({
    MessageEventsChannels.Producer.class,
    MessageEventsChannels.Consumer.class
})
public class MessageEventsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public BpmnMessageEventMessageBuilderFactory messageEventPayloadMessageBuilderFactory(RuntimeBundleProperties properties) {
        return new BpmnMessageEventMessageBuilderFactory(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public BpmnMessageReceivedEventMessageProducer throwMessageReceivedEventListener(MessageEventsChannels.Producer producerChannels,
                                                                                     BpmnMessageEventMessageBuilderFactory messageBuilderFactory,
                                                                                     ToCloudProcessRuntimeEventConverter runtimeEventConverter) {
        return new BpmnMessageReceivedEventMessageProducer(producerChannels.bpmnMessageReceivedEventProducerChannel(),
                                                           messageBuilderFactory,
                                                           runtimeEventConverter);
    }

    @Bean
    @ConditionalOnMissingBean
    public BpmnMessageWaitingEventMessageProducer throwMessageWaitingEventMessageProducer(MessageEventsChannels.Producer producerChannels,
                                                                                          BpmnMessageEventMessageBuilderFactory messageBuilderFactory,
                                                                                          ToCloudProcessRuntimeEventConverter runtimeEventConverter) {
        return new BpmnMessageWaitingEventMessageProducer(producerChannels.bpmnMessageWaitingEventProducerChannel(),
                                                          messageBuilderFactory,
                                                          runtimeEventConverter);
    }

    @Bean
    @ConditionalOnMissingBean
    public BpmnMessageSentEventMessageProducer bpmnMessageSentEventProducer(MessageEventsChannels.Producer producerChannels,
                                                                            BpmnMessageEventMessageBuilderFactory messageBuilderFactory,
                                                                            ToCloudProcessRuntimeEventConverter runtimeEventConverter) {
        return new BpmnMessageSentEventMessageProducer(producerChannels.bpmnMessageSentEventProducerChannel(),
                                                       messageBuilderFactory,
                                                       runtimeEventConverter);
    }

    @Bean
    @ConditionalOnMissingBean
    public ReceiveMessagePayloadMessageStreamListener MessageEventPayloadStreamListener(ApplicationEventPublisher eventPublisher) {
        return new ReceiveMessagePayloadMessageStreamListener(eventPublisher);
    }
    
    @Bean
    @ConditionalOnMissingBean
    public MessageEventsProcessEngineConfigurator bpmnMessagesEngineConfigurator() {
        return new MessageEventsProcessEngineConfigurator();
    }

}
