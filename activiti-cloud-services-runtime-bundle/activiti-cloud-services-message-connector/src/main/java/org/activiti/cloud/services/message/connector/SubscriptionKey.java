package org.activiti.cloud.services.message.connector;

import java.util.Objects;
import java.util.Optional;

import org.springframework.data.annotation.Id;
import org.springframework.data.keyvalue.annotation.KeySpace;

@KeySpace("subscriptions")
public class SubscriptionKey {
    
    @Id
    private final String id;
    
    private final String messageName;
    private final Optional<String> correlationKey;

    public SubscriptionKey(String messageName, 
                           Optional<String> correlationKey) {
        this.id = new String(messageName + correlationKey.orElse("")).intern();
        this.messageName = messageName;
        this.correlationKey = correlationKey;
    }
    
    public String getId() {
        return this.id;
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
