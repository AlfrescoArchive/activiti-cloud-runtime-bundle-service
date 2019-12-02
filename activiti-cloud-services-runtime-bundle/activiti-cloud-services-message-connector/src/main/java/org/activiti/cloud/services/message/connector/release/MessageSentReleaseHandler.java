package org.activiti.cloud.services.message.connector.release;

import org.activiti.cloud.services.message.connector.support.SpELEvaluatingReleaseStrategy;
import org.springframework.integration.aggregator.ReleaseStrategy;
import org.springframework.integration.store.MessageGroup;

public class MessageSentReleaseHandler implements MessageGroupReleaseHandler {

    private final static String condition = "!messages.?[headers['eventType'] == 'MESSAGE_WAITING' || headers['eventType'] == 'START_MESSAGE_DEPLOYED'].empty " 
                                            + "&& !messages.?[headers['eventType'] == 'MESSAGE_SENT'].empty";
    
    private final static ReleaseStrategy strategy = new SpELEvaluatingReleaseStrategy(condition);

    public MessageSentReleaseHandler() {
    }
    
    @Override
    public Boolean handle(MessageGroup group) {
        if (strategy.canRelease(group)) {
            return true;
        }
        
        return null;
    }

}
