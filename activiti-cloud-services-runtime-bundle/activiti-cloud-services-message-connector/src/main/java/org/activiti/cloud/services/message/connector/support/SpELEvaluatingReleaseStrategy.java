package org.activiti.cloud.services.message.connector.support;

import org.springframework.integration.aggregator.ExpressionEvaluatingReleaseStrategy;

public class SpELEvaluatingReleaseStrategy extends ExpressionEvaluatingReleaseStrategy {

    public SpELEvaluatingReleaseStrategy(String expression) {
        super(expression);
        
        getEvaluationContext(false);
    }

    
}
