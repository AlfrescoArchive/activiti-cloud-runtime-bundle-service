package org.activiti.cloud.services.message.connector.release;

import java.util.Optional;

import org.springframework.integration.aggregator.ReleaseStrategy;
import org.springframework.integration.store.MessageGroup;

public class MessageGroupReleaseStrategyChain implements ReleaseStrategy {
    
    private final MessageGroupReleaseChain chain;
    
    public MessageGroupReleaseStrategyChain(MessageGroupReleaseChain chain) {
        this.chain = chain;
    }
 
    @Override
    public boolean canRelease(MessageGroup group) {
        return Optional.ofNullable(chain.handle(group))
                       .map(Boolean::booleanValue)
                       .orElse(false);
    }

}
