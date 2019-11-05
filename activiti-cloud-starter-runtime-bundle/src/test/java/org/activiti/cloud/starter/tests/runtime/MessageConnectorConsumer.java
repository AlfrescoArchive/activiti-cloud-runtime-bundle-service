package org.activiti.cloud.starter.tests.runtime;

import org.activiti.api.process.model.builders.MessagePayloadBuilder;
import org.activiti.api.process.model.payloads.MessageEventPayload;
import org.activiti.api.process.model.payloads.ReceiveMessagePayload;
import org.activiti.cloud.api.process.model.events.CloudBPMNMessageEvent;
import org.activiti.cloud.api.process.model.events.CloudBPMNMessageReceivedEvent;
import org.activiti.cloud.api.process.model.events.CloudBPMNMessageSentEvent;
import org.activiti.cloud.api.process.model.events.CloudBPMNMessageWaitingEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.context.annotation.PropertySource;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@TestConfiguration
@EnableBinding({
    MessageConnectorChannels.Consumer.class,
    MessageConnectorChannels.Producer.class
})
@PropertySource("classpath:config/message-connector-channels.properties")
public class MessageConnectorConsumer {

    private static Map<SubscriptionKey, Message<CloudBPMNMessageSentEvent>> messages = new HashMap<>();
    private static Set<SubscriptionKey> subscriptions = new HashSet<>();

    @Autowired
    private MessageConnectorChannels.Producer producer;
    
    @StreamListener(MessageConnectorChannels.BPMN_MESSAGE_SENT_EVENT_CONSUMER_CHANNEL)
    public void handleCloudBPMNMessageSentEvent(Message<CloudBPMNMessageSentEvent> message) {
        SubscriptionKey key = key(message.getPayload());
        
        boolean hasSubscription = false;
        
        synchronized (key.intern()) {
            messages.put(key, message);

            if (subscriptions.contains(key)) {
                hasSubscription = true;
            }
        }

        if (hasSubscription) {
            Message<ReceiveMessagePayload> receiveMessage = receiveMessage(message);
            
            deliver(receiveMessage);
        }
    }

    @StreamListener(MessageConnectorChannels.BPMN_MESSAGE_RECEIVED_EVENT_CONSUMER_CHANNEL)
    public void handleCloudBPMNMessageReceivedEvent(Message<CloudBPMNMessageReceivedEvent> message) {
        SubscriptionKey key = key(message.getPayload());

        synchronized (key.intern()) {
            messages.remove(key);
            subscriptions.remove(key);
        }
    }

    @StreamListener(MessageConnectorChannels.BPMN_MESSAGE_WAITING_EVENT_CONSUMER_CHANNEL)
    public void handleCloudBPMNMessageWaitingEvent(Message<CloudBPMNMessageWaitingEvent> message) {
        SubscriptionKey key = key(message.getPayload());
        Message<CloudBPMNMessageSentEvent> existingMessage = null;

        synchronized (key.intern()) {
            subscriptions.add(key);
            
            existingMessage = messages.get(key);
        }

        if (existingMessage != null) {
            Message<ReceiveMessagePayload> receiveMessage = receiveMessage(existingMessage);
            
            deliver(receiveMessage);
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
                             .copyHeaders(message.getHeaders())
                             .build();
    }    
    
    private void deliver(Message<ReceiveMessagePayload> message) {
        
        producer.receiveMessagePayloadProducerChannel()
                .send(message);
    }
    
    private SubscriptionKey key(CloudBPMNMessageEvent event) {
        MessageEventPayload messagePayload = event.getEntity()
                                                  .getMessagePayload();
        
        return new SubscriptionKey(messagePayload.getName(),
                                   Optional.ofNullable(messagePayload.getCorrelationKey()));
    }
    
    static class SubscriptionKey {
        private final String messageName;
        private final Optional<String> correlationKey;

        public SubscriptionKey(String messageName, 
                               Optional<String> correlationKey) {
            this.messageName = messageName;
            this.correlationKey = correlationKey;
        }
        
        public String intern() {
            return new String(this.messageName + this.correlationKey.orElse("")).intern();
        }
        
        public String getMessageName() {
            return messageName;
        }
        
        public Optional<String> getCorrelationKey() {
            return correlationKey;
        }

        @Override
        public int hashCode() {
            return Objects.hash(correlationKey, 
                                messageName);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            SubscriptionKey other = (SubscriptionKey) obj;
            return Objects.equals(correlationKey, other.correlationKey) && Objects.equals(messageName,
                                                                                          other.messageName);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("SubscriptionKey [messageName=");
            builder.append(messageName);
            builder.append(", correlationKey=");
            builder.append(correlationKey);
            builder.append("]");
            return builder.toString();
        }
        
    }
 
    
}
