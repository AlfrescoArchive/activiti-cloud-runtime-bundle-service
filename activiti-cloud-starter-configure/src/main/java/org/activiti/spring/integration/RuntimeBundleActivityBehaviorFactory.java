package org.activiti.spring.integration;

import org.activiti.bpmn.model.ServiceTask;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.bpmn.parser.factory.DefaultActivityBehaviorFactory;
import org.activiti.engine.impl.delegate.ActivityBehavior;
import org.springframework.stereotype.Component;

@Component
public class RuntimeBundleActivityBehaviorFactory extends DefaultActivityBehaviorFactory {

    @Override
    public ActivityBehavior createDefaultServiceTaskBehavior(ServiceTask serviceTask) {
        Expression delegateExpression = expressionManager.createExpression("${MQServiceTaskBehavior}");
        return createServiceTaskBehavior(serviceTask,
                                         delegateExpression);
    }
}
