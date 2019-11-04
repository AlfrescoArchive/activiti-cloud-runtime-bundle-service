package org.activiti.cloud.services.messages.config;

import org.activiti.cloud.services.events.configuration.RuntimeBundleProperties;
import org.activiti.cloud.services.messages.BpmnMessageReceivedEvenMessageProducer;
import org.activiti.cloud.services.messages.BpmnMessageSentEventMessageProducer;
import org.activiti.cloud.services.messages.BpmnMessageWaitingEventMessageProducer;
import org.activiti.cloud.services.messages.BpmnMessagesProcessEngineConfigurator;
import org.activiti.cloud.services.messages.MessageEventPayloadMessageBuilderFactory;
import org.activiti.cloud.services.messages.MessageEventPayloadStreamListener;
import org.activiti.cloud.services.messages.channels.BpmnMessageChannels;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:config/bpmn-messages.properties")
@EnableBinding(BpmnMessageChannels.class)
public class BpmnMessagesAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MessageEventPayloadMessageBuilderFactory messageEventPayloadMessageBuilderFactory(RuntimeBundleProperties properties) {
        return new MessageEventPayloadMessageBuilderFactory(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public BpmnMessageReceivedEvenMessageProducer throwMessageReceivedEventListener(BpmnMessageChannels bpmnMessageChannels,
                                                                                    MessageEventPayloadMessageBuilderFactory messageBuilderFactory) {
        return new BpmnMessageReceivedEvenMessageProducer(bpmnMessageChannels.received(),
                                                          messageBuilderFactory);
    }

    @Bean
    @ConditionalOnMissingBean
    public BpmnMessageWaitingEventMessageProducer throwMessageWaitingEventMessageProducer(BpmnMessageChannels bpmnMessageChannels,
                                                                                          MessageEventPayloadMessageBuilderFactory messageBuilderFactory) {
        return new BpmnMessageWaitingEventMessageProducer(bpmnMessageChannels.waiting(),
                                                          messageBuilderFactory);
    }

    @Bean
    @ConditionalOnMissingBean
    public BpmnMessageSentEventMessageProducer bpmnMessageSentEventProducer(BpmnMessageChannels bpmnMessageChannels,
                                                                            MessageEventPayloadMessageBuilderFactory messageBuilderFactory) {
        return new BpmnMessageSentEventMessageProducer(bpmnMessageChannels.sent(),
                                                       messageBuilderFactory);
    }

    @Bean
    @ConditionalOnMissingBean
    public MessageEventPayloadStreamListener MessageEventPayloadStreamListener(ApplicationEventPublisher eventPublisher) {
        return new MessageEventPayloadStreamListener(eventPublisher);
    }
    
    @Bean
    @ConditionalOnMissingBean
    public BpmnMessagesProcessEngineConfigurator bpmnMessagesEngineConfigurator() {
        return new BpmnMessagesProcessEngineConfigurator();
    }

}
