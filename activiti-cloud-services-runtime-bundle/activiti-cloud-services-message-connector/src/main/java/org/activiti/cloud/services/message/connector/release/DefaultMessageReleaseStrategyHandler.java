package org.activiti.cloud.services.message.connector.release;

import org.springframework.integration.aggregator.MessageCountReleaseStrategy;
import org.springframework.integration.aggregator.ReleaseStrategy;
import org.springframework.integration.store.MessageGroup;

public class DefaultMessageReleaseStrategyHandler implements MessageGroupReleaseHandler {
    
    private final static ReleaseStrategy delegate = new MessageCountReleaseStrategy();

    @Override
    public Boolean handle(MessageGroup group) {
        return delegate.canRelease(group);
    }

}
