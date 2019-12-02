package org.activiti.cloud.services.message.connector.release;

import org.springframework.expression.Expression;
import org.springframework.integration.aggregator.ExpressionEvaluatingReleaseStrategy;
import org.springframework.integration.aggregator.ReleaseStrategy;
import org.springframework.integration.store.MessageGroup;

public class MessageGroupReleaseDelegateHandler implements MessageGroupReleaseHandler {
    
    private final ReleaseStrategy delegate; 

    public MessageGroupReleaseDelegateHandler(String expression) {
        this.delegate = new ExpressionEvaluatingReleaseStrategy(expression);
    }
    
    public MessageGroupReleaseDelegateHandler(Expression expression) {
        this.delegate = new ExpressionEvaluatingReleaseStrategy(expression);
    }
    
    @Override
    public Boolean handle(MessageGroup group) {
        return delegate.canRelease(group);
    }
    
}
