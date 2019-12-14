package org.activiti.cloud.services.messages.aggregator.advice;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.integration.handler.advice.AbstractHandleMessageAdvice;
import org.springframework.messaging.Message;

public abstract class AbstractMessageConnectorHandlerAdvice extends AbstractHandleMessageAdvice 
                                                            implements MessageConnectorHandlerAdvice {
    @Override
    protected Object doInvoke(MethodInvocation invocation, 
                              Message<?> message) throws Throwable {
        
        if (canHandle(message)) {
            return doHandle(message);            
        }
        
        return invocation.proceed();
    }

    public abstract boolean canHandle(Message<?> message);

    public abstract <T> T doHandle(Message<?> message);
    
    @Override
    public String getComponentType() {
        return this.getClass().getSimpleName();
    }
    
}
