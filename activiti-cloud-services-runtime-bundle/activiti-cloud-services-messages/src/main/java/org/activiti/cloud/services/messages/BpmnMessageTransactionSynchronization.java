package org.activiti.cloud.services.messages;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.MessageDispatchingException;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.transaction.support.TransactionSynchronization;

public class BpmnMessageTransactionSynchronization implements TransactionSynchronization {

    private static final Logger logger = LoggerFactory.getLogger(BpmnMessageTransactionSynchronization.class);

    private final Message<?> message;
    private final MessageChannel messageChannel;

    public BpmnMessageTransactionSynchronization(Message<?> message,
                                                  MessageChannel messageChannel) {
        this.message = message;
        this.messageChannel = messageChannel;
    }

    @Override
    public void afterCommit() {
        logger.debug("Sending bpmn message '{}' via message channel: {}", message, messageChannel);
        
        try { 
            boolean sent = messageChannel.send(message);
            
            if(!sent) {
                throw new MessageDispatchingException(message);
            }
            
        } catch(Exception cause) {
            logger.error("Sending bpmn message {} failed due to error: {}", message, cause.getMessage());
        }
    }
}