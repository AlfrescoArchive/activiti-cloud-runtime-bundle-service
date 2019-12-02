package org.activiti.cloud.services.message.connector.support;

import org.springframework.integration.aggregator.ExpressionEvaluatingMessageListProcessor;

public class SpELEvaluatingMessageListProcessor extends ExpressionEvaluatingMessageListProcessor {

    public SpELEvaluatingMessageListProcessor(String expression) {
        super(expression);
        
        getEvaluationContext(false);
    }
}
