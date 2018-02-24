package org.activiti.spring.bpmn.parser;

import java.util.List;

import org.activiti.bpmn.model.ActivitiListener;
import org.activiti.bpmn.model.EventListener;
import org.activiti.engine.delegate.CustomPropertiesResolver;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.delegate.TransactionDependentTaskListener;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.impl.bpmn.listener.DelegateExpressionTransactionDependentExecutionListener;
import org.activiti.engine.impl.bpmn.parser.factory.DefaultListenerFactory;
import org.springframework.core.io.support.SpringFactoriesLoader;


public class CloudListenerFactory extends DefaultListenerFactory {

    List<CloudListenerFactoryInterceptor> interceptors;

    public CloudListenerFactory() {
        ClassLoader classLoader=Thread.currentThread().getContextClassLoader();
        interceptors = SpringFactoriesLoader.loadFactories(CloudListenerFactoryInterceptor.class, classLoader);
    }

    @Override
    public TaskListener createClassDelegateTaskListener(ActivitiListener activitiListener) {
        for(CloudListenerFactoryInterceptor interceptor : interceptors){
            TaskListener listener= interceptor.createClassDelegateTaskListener(activitiListener);
            if (listener != null) {
                return listener;
            }
        }
        return super.createClassDelegateTaskListener(activitiListener);
    }

    public TaskListener createExpressionTaskListener(ActivitiListener activitiListener) {
        for(CloudListenerFactoryInterceptor interceptor : interceptors){
            TaskListener listener= interceptor.createExpressionTaskListener(activitiListener);
            if (listener != null) {
                return listener;
            }
        }
        return super.createExpressionTaskListener(activitiListener);
    }

    public TaskListener createDelegateExpressionTaskListener(ActivitiListener activitiListener) {
        for(CloudListenerFactoryInterceptor interceptor : interceptors){
            TaskListener listener= interceptor.createDelegateExpressionTaskListener(activitiListener);
            if (listener != null) {
                return listener;
            }
        }
        return super.createDelegateExpressionTaskListener(activitiListener);
    }

    public ExecutionListener createClassDelegateExecutionListener(ActivitiListener activitiListener) {
        for(CloudListenerFactoryInterceptor interceptor : interceptors){
            ExecutionListener listener= interceptor.createClassDelegateExecutionListener(activitiListener);
            if (listener != null) {
                return listener;
            }
        }
        return super.createClassDelegateExecutionListener(activitiListener);
    }

    public ExecutionListener createExpressionExecutionListener(ActivitiListener activitiListener){
        for(CloudListenerFactoryInterceptor interceptor : interceptors){
            ExecutionListener listener= interceptor.createExpressionExecutionListener(activitiListener);
            if (listener != null) {
                return listener;
            }
        }
        return super.createExpressionExecutionListener(activitiListener);
    }

    public ExecutionListener createDelegateExpressionExecutionListener(ActivitiListener activitiListener){
        for(CloudListenerFactoryInterceptor interceptor : interceptors){
            ExecutionListener listener= interceptor.createDelegateExpressionExecutionListener(activitiListener);
            if (listener != null) {
                return listener;
            }
        }
        return super.createDelegateExpressionExecutionListener(activitiListener);
    }

    @Override
    public DelegateExpressionTransactionDependentExecutionListener createTransactionDependentDelegateExpressionExecutionListener(ActivitiListener activitiListener){
        for(CloudListenerFactoryInterceptor interceptor : interceptors){
            DelegateExpressionTransactionDependentExecutionListener listener= interceptor.createTransactionDependentDelegateExpressionExecutionListener(activitiListener);
            if (listener != null) {
                return listener;
            }
        }
        return super.createTransactionDependentDelegateExpressionExecutionListener(activitiListener);
    }

    public ActivitiEventListener createClassDelegateEventListener(EventListener eventListener){
        for(CloudListenerFactoryInterceptor interceptor : interceptors){
            ActivitiEventListener listener= interceptor.createClassDelegateEventListener(eventListener);
            if (listener != null) {
                return listener;
            }
        }
        return super.createClassDelegateEventListener(eventListener);
    }

    public ActivitiEventListener createDelegateExpressionEventListener(EventListener eventListener){
        for(CloudListenerFactoryInterceptor interceptor : interceptors){
            ActivitiEventListener listener= interceptor.createDelegateExpressionEventListener(eventListener);
            if (listener != null) {
                return listener;
            }
        }
        return super.createDelegateExpressionEventListener(eventListener);
    }

    public ActivitiEventListener createEventThrowingEventListener(EventListener eventListener){
        for(CloudListenerFactoryInterceptor interceptor : interceptors){
            ActivitiEventListener listener= interceptor.createEventThrowingEventListener(eventListener);
            if (listener != null) {
                return listener;
            }
        }
        return super.createEventThrowingEventListener(eventListener);
    }

    public CustomPropertiesResolver createClassDelegateCustomPropertiesResolver(ActivitiListener activitiListener) {
        for(CloudListenerFactoryInterceptor interceptor : interceptors){
            CustomPropertiesResolver listener= interceptor.createClassDelegateCustomPropertiesResolver(activitiListener);
            if (listener != null) {
                return listener;
            }
        }
        return super.createClassDelegateCustomPropertiesResolver(activitiListener);
    }

    public CustomPropertiesResolver createExpressionCustomPropertiesResolver(ActivitiListener activitiListener) {
        for(CloudListenerFactoryInterceptor interceptor : interceptors){
            CustomPropertiesResolver listener= interceptor.createExpressionCustomPropertiesResolver(activitiListener);
            if (listener != null) {
                return listener;
            }
        }
        return super.createExpressionCustomPropertiesResolver(activitiListener);
    }

    public CustomPropertiesResolver createDelegateExpressionCustomPropertiesResolver(ActivitiListener activitiListener) {
        for(CloudListenerFactoryInterceptor interceptor : interceptors){
            CustomPropertiesResolver listener= interceptor.createDelegateExpressionCustomPropertiesResolver(activitiListener);
            if (listener != null) {
                return listener;
            }
        }
        return super.createDelegateExpressionCustomPropertiesResolver(activitiListener);
    }

    public TransactionDependentTaskListener createTransactionDependentDelegateExpressionTaskListener(ActivitiListener activitiListener){
        for(CloudListenerFactoryInterceptor interceptor : interceptors){
            TransactionDependentTaskListener listener= interceptor.createTransactionDependentDelegateExpressionTaskListener(activitiListener);
            if (listener != null) {
                return listener;
            }
        }
        return super.createTransactionDependentDelegateExpressionTaskListener(activitiListener);
    }
}
