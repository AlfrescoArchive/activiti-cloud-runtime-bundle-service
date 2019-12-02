package org.activiti.cloud.services.message.connector.processor;

import org.springframework.integration.aggregator.MessageGroupProcessor;
import org.springframework.integration.store.MessageGroup;

public class MessageGroupProcessorHandlerChain implements MessageGroupProcessor {
    
    private final MessageGroupProcessorChain chain;
    
    public MessageGroupProcessorHandlerChain(MessageGroupProcessorChain chain) {
        this.chain = chain;
    }

    @Override
    public Object processMessageGroup(MessageGroup group) {
        return chain.handle(group);
    }
    
}
