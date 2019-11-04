package org.activiti.cloud.starter.tests.runtime;

import org.activiti.api.process.model.payloads.MessageEventPayload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.support.MessageBuilder;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@TestComponent
@EnableBinding(MessageConnectorChannels.class)
public class MessageConnectorConsumer {

    private static Map<SubscriptionKey, MessageEventPayload> messages = new ConcurrentHashMap<>();
    private static Set<SubscriptionKey> subscriptions = new HashSet<>();

    @Autowired
    private MessageConnectorChannels messageConnectorChannels;
    
    @StreamListener(MessageConnectorChannels.SENT_MESSAGES)
    public void handleSentMessageEventPayload(MessageEventPayload message) {
        SubscriptionKey key = key(message);
        messages.put(key, message);

        if (subscriptions.contains(key)) {
            deliver(message);
        }
    }

    @StreamListener(MessageConnectorChannels.RECEIVED_MESSAGES)
    public void handleReceivedMessageEventPayload(MessageEventPayload message) {
        SubscriptionKey key = key(message);

        messages.remove(key);
        subscriptions.remove(key);
    }

    @StreamListener(MessageConnectorChannels.WAITING_MESSAGES)
    public void handleWaitingMessageEventPayload(MessageEventPayload subscription) {
        SubscriptionKey key = key(subscription);
        subscriptions.add(key);
        
        MessageEventPayload message = messages.get(key);
        
        if (message != null) {
            deliver(message);
        }
    }
    
    private void deliver(MessageEventPayload payload) {
        messageConnectorChannels.deliver()
                                .send(MessageBuilder.withPayload(payload)
                                                    .build());
    }
    
    private SubscriptionKey key(MessageEventPayload message) {
        return new SubscriptionKey(message.getName(),
                                   Optional.ofNullable(message.getCorrelationKey()));
    }
    
    static class SubscriptionKey {
        private final String messageName;
        private final Optional<String> correlationKey;

        public SubscriptionKey(String messageName, 
                               Optional<String> correlationKey) {
            this.messageName = messageName;
            this.correlationKey = correlationKey;
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
