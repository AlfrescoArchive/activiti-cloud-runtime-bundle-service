package org.activiti.cloud.services.message.connector;

import org.activiti.api.process.model.MessageSubscription;
import org.activiti.api.process.model.StartMessageSubscription;
import org.activiti.api.process.model.builders.MessageEventPayloadBuilder;
import org.activiti.api.process.model.payloads.MessageEventPayload;
import org.activiti.cloud.api.process.model.events.CloudBPMNMessageEvent;
import org.activiti.cloud.api.process.model.events.CloudBPMNMessageReceivedEvent;
import org.activiti.cloud.api.process.model.events.CloudBPMNMessageSentEvent;
import org.activiti.cloud.api.process.model.events.CloudBPMNMessageWaitingEvent;
import org.activiti.cloud.api.process.model.events.CloudMessageSubscriptionCancelledEvent;
import org.activiti.cloud.api.process.model.events.CloudStartMessageDeployedEvent;
import org.activiti.cloud.services.message.connector.channels.MessageConnectorChannels;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

public class MessageConnectorConsumer {
    private static final Logger logger = LoggerFactory.getLogger(MessageConnectorConsumer.class);

    private final Processor processor;
    
    public MessageConnectorConsumer(Processor processor) { 
        this.processor = processor;
    }
    
    private Message<MessageEventPayload> messageEvent(Message<? extends CloudBPMNMessageEvent> message) {
        MessageEventPayload messageEventPayload = message.getPayload()
                .getEntity()
                .getMessagePayload();

        return MessageBuilder.withPayload(messageEventPayload)
                             .copyHeaders(message.getHeaders())
                             .build();
    }
    
    private Message<MessageEventPayload> startMessageEvent(Message<CloudStartMessageDeployedEvent> message) {
        StartMessageSubscription messageSubscription = message.getPayload()
                                                              .getEntity()
                                                              .getMessageSubscription();
        
        MessageEventPayload messageEventPayload = MessageEventPayloadBuilder.messageEvent(messageSubscription.getEventName())
                                                                            .withCorrelationKey(messageSubscription.getConfiguration())
                                                                            .build();
        return MessageBuilder.withPayload(messageEventPayload)
                             .copyHeaders(message.getHeaders())
                             .build();
    }

    private Message<MessageEventPayload> subscriptionCancelledEvent(Message<CloudMessageSubscriptionCancelledEvent> message) {
        MessageSubscription messageSubscription = message.getPayload()
                                                         .getEntity();
        
        MessageEventPayload messageEventPayload = MessageEventPayloadBuilder.messageEvent(messageSubscription.getEventName())
                                                                            .withCorrelationKey(messageSubscription.getConfiguration())
                                                                            .build();
        return MessageBuilder.withPayload(messageEventPayload)
                             .copyHeaders(message.getHeaders())
                             .build();
    }
    
    
    @StreamListener(MessageConnectorChannels.BPMN_MESSAGE_SENT_EVENT_CONSUMER_CHANNEL)
    public void handleCloudBPMNMessageSentEvent(Message<CloudBPMNMessageSentEvent> message) {
        logger.debug("handleCloudBPMNMessageSentEvent({})", message);
        
        Message<MessageEventPayload> messageEvent = messageEvent(message);
        
        processor.input().send(messageEvent);
    }

    @StreamListener(MessageConnectorChannels.BPMN_MESSAGE_RECEIVED_EVENT_CONSUMER_CHANNEL)
    public void handleCloudBPMNMessageReceivedEvent(Message<CloudBPMNMessageReceivedEvent> message) {
        logger.debug("handleCloudBPMNMessageReceivedEvent({})", message);

        Message<MessageEventPayload> messageEvent = messageEvent(message);
        
        processor.input().send(messageEvent);
    }
    
    @StreamListener(MessageConnectorChannels.BPMN_MESSAGE_WAITING_EVENT_CONSUMER_CHANNEL)
    public void handleCloudBPMNMessageWaitingEvent(Message<CloudBPMNMessageWaitingEvent> message) {
        logger.debug("handleCloudBPMNMessageWaitingEvent({})", message);

        Message<MessageEventPayload> messageEvent = messageEvent(message);

        processor.input().send(messageEvent);
    }
    
    @StreamListener(MessageConnectorChannels.MESSAGE_DEPLOYED_EVENT_CONSUMER_CHANNEL)
    public void handleCloudStartMessageDeployedEvent(Message<CloudStartMessageDeployedEvent> message) {
        logger.debug("handleCloudMessageDeployedEvent({})", message);

        Message<MessageEventPayload> messageEvent = startMessageEvent(message);

        processor.input().send(messageEvent);
    }
    
    @StreamListener(MessageConnectorChannels.MESSAGE_SUBSCRIPTION_CANCELLED_EVENT_CONSUMER_CHANNEL)
    public void handleCloudMessageSubscriptionCancelledEvent(Message<CloudMessageSubscriptionCancelledEvent> message) {
        logger.debug("handleCloudMessageSubscriptionCancelledEvent({})", message);

        Message<MessageEventPayload> messageEvent = subscriptionCancelledEvent(message);

        processor.input().send(messageEvent);
    }
    
}
