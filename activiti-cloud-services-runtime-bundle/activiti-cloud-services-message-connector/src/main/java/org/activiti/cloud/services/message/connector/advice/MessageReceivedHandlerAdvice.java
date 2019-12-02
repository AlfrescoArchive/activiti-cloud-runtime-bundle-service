package org.activiti.cloud.services.message.connector.advice;

import org.activiti.cloud.services.message.connector.support.LockTemplate;
import org.activiti.cloud.services.message.connector.support.MessageTimestampComparator;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.integration.aggregator.CorrelationStrategy;
import org.springframework.integration.core.MessageSelector;
import org.springframework.integration.handler.advice.AbstractHandleMessageAdvice;
import org.springframework.integration.store.MessageGroup;
import org.springframework.integration.store.MessageGroupStore;
import org.springframework.integration.util.UUIDConverter;
import org.springframework.messaging.Message;

public class MessageReceivedHandlerAdvice extends AbstractHandleMessageAdvice {

    private final MessageGroupStore messageStore;
    private final LockTemplate lockTemplate;
    private final CorrelationStrategy correlationStrategy;
    
    private final MessageSelector messageSelector = (message) -> "MESSAGE_RECEIVED".equals(message.getHeaders()
                                                                                                  .get("eventType"));

    private final MessageSelector waitingSelector = (message) -> "MESSAGE_WAITING".equals(message.getHeaders()
                                                                                                 .get("eventType"));
    
    public MessageReceivedHandlerAdvice(MessageGroupStore messageStore,
                                        CorrelationStrategy correlationStrategy,
                                        LockTemplate lockTemplate) {
        this.messageStore = messageStore;
        this.lockTemplate = lockTemplate;
        this.correlationStrategy = correlationStrategy;
    }
    
    @Override
    public String getComponentType() {
        return MessageReceivedHandlerAdvice.class.getSimpleName();
    }

    @Override
    protected Object doInvoke(MethodInvocation invocation, 
                              Message<?> message) throws Throwable {
        if (!messageSelector.accept(message)) {
            return invocation.proceed();
        }
            
        Object groupId = correlationStrategy.getCorrelationKey(message);
        Object key = UUIDConverter.getUUID(groupId).toString();

        lockTemplate.lockInterruptibly(key, () -> {
            MessageGroup group = messageStore.getMessageGroup(groupId);
            
            group.getMessages()
                 .stream()
                 .filter(it -> waitingSelector.accept(it))
                 .min(MessageTimestampComparator.INSTANCE)
                 .ifPresent(result -> {
                     messageStore.removeMessagesFromGroup(groupId, result);                            
                 });
        });
        
        return null;
        
    }
}
