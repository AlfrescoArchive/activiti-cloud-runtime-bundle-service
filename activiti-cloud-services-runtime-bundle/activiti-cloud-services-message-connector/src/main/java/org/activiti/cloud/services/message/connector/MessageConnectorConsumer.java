package org.activiti.cloud.services.message.connector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.activiti.api.process.model.MessageSubscription;
import org.activiti.api.process.model.StartMessageSubscription;
import org.activiti.api.process.model.builders.MessagePayloadBuilder;
import org.activiti.api.process.model.payloads.MessageEventPayload;
import org.activiti.api.process.model.payloads.ReceiveMessagePayload;
import org.activiti.api.process.model.payloads.StartMessagePayload;
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
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

public class MessageConnectorConsumer {
    private static final Logger logger = LoggerFactory.getLogger(MessageConnectorConsumer.class);

    private static Map<SubscriptionKey, Message<CloudBPMNMessageSentEvent>> messages = new HashMap<>();
    private static Set<SubscriptionKey> catchSubscriptions = new HashSet<>();
    private static Set<SubscriptionKey> startSubscriptions = new HashSet<>();
    
    private final MessageConnectorChannels.Producer producer;
    
    public MessageConnectorConsumer(MessageConnectorChannels.Producer producer) { 
        this.producer = producer;
    }
    
    @StreamListener(MessageConnectorChannels.BPMN_MESSAGE_SENT_EVENT_CONSUMER_CHANNEL)
    public void handleCloudBPMNMessageSentEvent(Message<CloudBPMNMessageSentEvent> message) {
        logger.debug("handleCloudBPMNMessageSentEvent({})", message);

        SubscriptionKey key = key(message.getPayload());
        
        synchronized (key.getId()) {
            messages.put(key, message);

            if (startSubscriptions.contains(key)) {
                Message<StartMessagePayload> startMessage = startMessage(message);
                
                sendStartMessage(startMessage);
            } 
            else if (catchSubscriptions.contains(key)) {
                Message<ReceiveMessagePayload> receiveMessage = receiveMessage(message);
                
                sendReceiveMessage(receiveMessage);
            }
        }
    }

    @StreamListener(MessageConnectorChannels.BPMN_MESSAGE_RECEIVED_EVENT_CONSUMER_CHANNEL)
    public void handleCloudBPMNMessageReceivedEvent(Message<CloudBPMNMessageReceivedEvent> message) {
        logger.debug("handleCloudBPMNMessageReceivedEvent({})", message);
        
        SubscriptionKey key = key(message.getPayload());

        synchronized (key.getId()) {
            messages.remove(key);
            catchSubscriptions.remove(key);
        }
    }

    @StreamListener(MessageConnectorChannels.BPMN_MESSAGE_WAITING_EVENT_CONSUMER_CHANNEL)
    public void handleCloudBPMNMessageWaitingEvent(Message<CloudBPMNMessageWaitingEvent> message) {
        logger.debug("handleCloudBPMNMessageWaitingEvent({})", message);

        SubscriptionKey key = key(message.getPayload());
        Message<CloudBPMNMessageSentEvent> existingMessage = null;

        synchronized (key.getId()) {
            catchSubscriptions.add(key);
            
            existingMessage = messages.get(key);
        }

        if (existingMessage != null) {
            Message<ReceiveMessagePayload> receiveMessage = receiveMessage(existingMessage);
            
            sendReceiveMessage(receiveMessage);
        }
        
    }
    
    @StreamListener(MessageConnectorChannels.MESSAGE_DEPLOYED_EVENT_CONSUMER_CHANNEL)
    public void handleCloudStartMessageDeployedEvent(Message<CloudStartMessageDeployedEvent> message) {
        logger.debug("handleCloudMessageDeployedEvent({})", message);
        
        StartMessageSubscription messageEventSubscription = message.getPayload()
                                                                        .getEntity()
                                                                        .getMessageSubscription();
        
        SubscriptionKey key = new SubscriptionKey(messageEventSubscription.getEventName(),
                                                  Optional.empty());
        
        Message<CloudBPMNMessageSentEvent> existingMessage = null;

        synchronized (key.getId()) {
            startSubscriptions.add(key);
            
            existingMessage = messages.get(key);
        }

        if (existingMessage != null) {
            Message<StartMessagePayload> startMessage = startMessage(existingMessage);
            
            sendStartMessage(startMessage);
        }
    }
    
    @StreamListener(MessageConnectorChannels.MESSAGE_SUBSCRIPTION_CANCELLED_EVENT_CONSUMER_CHANNEL)
    public void handleCloudMessageSubscriptionCancelledEvent(Message<CloudMessageSubscriptionCancelledEvent> message) {
        logger.debug("handleCloudMessageSubscriptionCancelledEvent({})", message);
        
        MessageSubscription messageSubscription = message.getPayload().getEntity();
        
        SubscriptionKey key = new SubscriptionKey(messageSubscription.getEventName(),
                                                  Optional.ofNullable(messageSubscription.getConfiguration()));
        
        synchronized (key.getId()) {
            catchSubscriptions.remove(key);
        }
    }
    
    
    private Message<ReceiveMessagePayload> receiveMessage(Message<CloudBPMNMessageSentEvent> message) {
        
        MessageEventPayload eventPayload = message.getPayload()
                                                  .getEntity()
                                                  .getMessagePayload();

        ReceiveMessagePayload receivePayload = MessagePayloadBuilder.receive(eventPayload.getName())
                                                                    .withCorrelationKey(eventPayload.getCorrelationKey())
                                                                    .withVariables(eventPayload.getVariables())
                                                                    .build();

        return MessageBuilder.withPayload(receivePayload)
                             .setHeader("CloudBPMNMessageSentEventHeaders", message.getHeaders())
                             .build();
    }    

    private Message<StartMessagePayload> startMessage(Message<CloudBPMNMessageSentEvent> message) {
        
        MessageEventPayload eventPayload = message.getPayload()
                                                  .getEntity()
                                                  .getMessagePayload();

        StartMessagePayload startPayload = MessagePayloadBuilder.start(eventPayload.getName())
                                                                  .withBusinessKey(eventPayload.getBusinessKey())
                                                                  .withVariables(eventPayload.getVariables())
                                                                  .build();
        return MessageBuilder.withPayload(startPayload)
                             .setHeader("CloudBPMNMessageSentEventHeaders", message.getHeaders())
                             .build();
    }
    
    private void sendReceiveMessage(Message<ReceiveMessagePayload> message) {
        
        producer.receiveMessagePayloadProducerChannel()
                .send(message);
    }

    private void sendStartMessage(Message<StartMessagePayload> message) {
        
        producer.startMessagePayloadProducerChannel()
                  .send(message);
    }
    
    private SubscriptionKey key(CloudBPMNMessageEvent event) {
        MessageEventPayload messagePayload = event.getEntity()
                                                  .getMessagePayload();
        
        return new SubscriptionKey(messagePayload.getName(),
                                   Optional.ofNullable(messagePayload.getCorrelationKey()));
    }
    
 
    
}
