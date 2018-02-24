package org.activiti.spring.bpmn.parser;

import org.activiti.bpmn.model.ActivitiListener;
import org.activiti.engine.impl.bpmn.listener.DelegateExpressionTransactionDependentExecutionListener;
import org.activiti.engine.impl.bpmn.parser.factory.ListenerFactory;

public interface CloudListenerFactoryInterceptor extends ListenerFactory {

    @Override
    public DelegateExpressionTransactionDependentExecutionListener createTransactionDependentDelegateExpressionExecutionListener(ActivitiListener activitiListener);
}
