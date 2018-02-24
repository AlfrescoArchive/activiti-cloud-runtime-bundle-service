package org.activiti.services.subscriptions.impl.bpmn.helper;

import org.activiti.bpmn.model.ActivitiListener;
import org.activiti.bpmn.model.EventListener;
import org.activiti.bpmn.model.ImplementationType;
import org.activiti.engine.delegate.CustomPropertiesResolver;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.delegate.TransactionDependentTaskListener;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.impl.bpmn.listener.DelegateExpressionTransactionDependentExecutionListener;
import org.activiti.spring.bpmn.parser.CloudListenerFactoryInterceptor;


public class BroadcastSignalEventListenerInterceptor implements CloudListenerFactoryInterceptor {

    public CustomPropertiesResolver createClassDelegateCustomPropertiesResolver(ActivitiListener activitiListener) {
        return null;
    }

    public ActivitiEventListener createClassDelegateEventListener(EventListener eventListener) {
        return null;
    }

    public ExecutionListener createClassDelegateExecutionListener(ActivitiListener activitiListener) {
        return null;
    }

    public TaskListener createClassDelegateTaskListener(ActivitiListener activitiListener) {
        return null;
    }

    public CustomPropertiesResolver createDelegateExpressionCustomPropertiesResolver(ActivitiListener activitiListener) {
        return null;
    }

    public ActivitiEventListener createDelegateExpressionEventListener(EventListener eventListener) {
        return null;
    }

    public ExecutionListener createDelegateExpressionExecutionListener(ActivitiListener activitiListener) {
        return null;
    }

    public TaskListener createDelegateExpressionTaskListener(ActivitiListener activitiListener) {
        return null;
    }

    public ActivitiEventListener createEventThrowingEventListener(EventListener eventListener) {
        if(ImplementationType.IMPLEMENTATION_TYPE_THROW_BROADCAST_SIGNAL_EVENT.equals(eventListener.getImplementationType())){
            return new BroadcastSignalThrowingEventListener(eventListener.getImplementation());
        }
        return null;
    }

    public CustomPropertiesResolver createExpressionCustomPropertiesResolver(ActivitiListener activitiListener) {
        return null;
    }

    public ExecutionListener createExpressionExecutionListener(ActivitiListener activitiListener) {
        return null;
    }

    public TaskListener createExpressionTaskListener(ActivitiListener activitiListener) {
        return null;
    }

    public TransactionDependentTaskListener createTransactionDependentDelegateExpressionTaskListener(
                                                                                                     ActivitiListener activitiListener) {
        return null;
    }

    public DelegateExpressionTransactionDependentExecutionListener
           createTransactionDependentDelegateExpressionExecutionListener(ActivitiListener activitiListener) {
        return null;
    }

}