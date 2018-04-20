package org.activiti.spring.bpmn.parser;

import java.util.List;

import org.activiti.bpmn.model.Activity;
import org.activiti.bpmn.model.BoundaryEvent;
import org.activiti.bpmn.model.BusinessRuleTask;
import org.activiti.bpmn.model.CallActivity;
import org.activiti.bpmn.model.CancelEventDefinition;
import org.activiti.bpmn.model.CompensateEventDefinition;
import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.ErrorEventDefinition;
import org.activiti.bpmn.model.EventGateway;
import org.activiti.bpmn.model.ExclusiveGateway;
import org.activiti.bpmn.model.InclusiveGateway;
import org.activiti.bpmn.model.IntermediateCatchEvent;
import org.activiti.bpmn.model.ManualTask;
import org.activiti.bpmn.model.MessageEventDefinition;
import org.activiti.bpmn.model.ParallelGateway;
import org.activiti.bpmn.model.ReceiveTask;
import org.activiti.bpmn.model.ScriptTask;
import org.activiti.bpmn.model.SendTask;
import org.activiti.bpmn.model.ServiceTask;
import org.activiti.bpmn.model.Signal;
import org.activiti.bpmn.model.SignalEventDefinition;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.bpmn.model.Task;
import org.activiti.bpmn.model.ThrowEvent;
import org.activiti.bpmn.model.TimerEventDefinition;
import org.activiti.bpmn.model.Transaction;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.impl.bpmn.behavior.AbstractBpmnActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.AdhocSubProcessActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.BoundaryCancelEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.BoundaryCompensateEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.BoundaryEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.BoundaryMessageEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.BoundarySignalEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.BoundaryTimerEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.CallActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.CancelEndEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.ErrorEndEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.EventBasedGatewayActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.EventSubProcessErrorStartEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.EventSubProcessMessageStartEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.ExclusiveGatewayActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.InclusiveGatewayActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.IntermediateCatchEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.IntermediateCatchMessageEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.IntermediateCatchSignalEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.IntermediateCatchTimerEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.IntermediateThrowCompensationEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.IntermediateThrowNoneEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.IntermediateThrowSignalEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.MailActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.ManualTaskActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.NoneEndEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.NoneStartEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.ParallelGatewayActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.ParallelMultiInstanceBehavior;
import org.activiti.engine.impl.bpmn.behavior.ReceiveTaskActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.ScriptTaskActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.SequentialMultiInstanceBehavior;
import org.activiti.engine.impl.bpmn.behavior.ServiceTaskDelegateExpressionActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.ServiceTaskExpressionActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.ShellActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.SubProcessActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.TaskActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.TerminateEndEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.TransactionActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.WebServiceActivityBehavior;
import org.activiti.engine.impl.bpmn.helper.ClassDelegate;
import org.activiti.engine.impl.bpmn.parser.factory.DefaultActivityBehaviorFactory;
import org.activiti.engine.impl.delegate.ActivityBehavior;
import org.springframework.core.io.support.SpringFactoriesLoader;


public class CloudActivityBehaviorFactory extends DefaultActivityBehaviorFactory {

    private List<CloudActivityBehaviorFactoryInterceptor> interceptors;

    public CloudActivityBehaviorFactory() {
        ClassLoader classLoader=Thread.currentThread().getContextClassLoader();
        interceptors = SpringFactoriesLoader.loadFactories(CloudActivityBehaviorFactoryInterceptor.class, classLoader);
    }

    @Override
    public NoneStartEventActivityBehavior createNoneStartEventActivityBehavior(StartEvent startEvent) {
        for(CloudActivityBehaviorFactoryInterceptor interceptor : interceptors){
            NoneStartEventActivityBehavior behavior = interceptor.createNoneStartEventActivityBehavior(startEvent);
            if (behavior != null) {
                return behavior;
            }
        }
        return super.createNoneStartEventActivityBehavior(startEvent);
    }

    @Override
    public TaskActivityBehavior createTaskActivityBehavior(Task task) {
        for(CloudActivityBehaviorFactoryInterceptor interceptor : interceptors){
            TaskActivityBehavior behavior = interceptor.createTaskActivityBehavior(task);
            if (behavior != null) {
                return behavior;
            }
        }
        return super.createTaskActivityBehavior(task);
    }

    @Override
    public ManualTaskActivityBehavior createManualTaskActivityBehavior(ManualTask manualTask) {
        for(CloudActivityBehaviorFactoryInterceptor interceptor : interceptors){
            ManualTaskActivityBehavior behavior = interceptor.createManualTaskActivityBehavior(manualTask);
            if (behavior != null) {
                return behavior;
            }
        }
        return super.createManualTaskActivityBehavior(manualTask);
    }

    @Override
    public ReceiveTaskActivityBehavior createReceiveTaskActivityBehavior(ReceiveTask receiveTask) {
        for(CloudActivityBehaviorFactoryInterceptor interceptor : interceptors){
            ReceiveTaskActivityBehavior behavior = interceptor.createReceiveTaskActivityBehavior(receiveTask);
            if (behavior != null) {
                return behavior;
            }
        }
        return super.createReceiveTaskActivityBehavior(receiveTask);
    }

    @Override
    public UserTaskActivityBehavior createUserTaskActivityBehavior(UserTask userTask) {
        for(CloudActivityBehaviorFactoryInterceptor interceptor : interceptors){
            UserTaskActivityBehavior behavior = interceptor.createUserTaskActivityBehavior(userTask);
            if (behavior != null) {
                return behavior;
            }
        }
        return super.createUserTaskActivityBehavior(userTask);
    }

    @Override
    public ClassDelegate createClassDelegateServiceTask(ServiceTask serviceTask) {
        for(CloudActivityBehaviorFactoryInterceptor interceptor : interceptors){
            ClassDelegate behavior = interceptor.createClassDelegateServiceTask(serviceTask);
            if (behavior != null) {
                return behavior;
            }
        }
        return super.createClassDelegateServiceTask(serviceTask);
    }

    @Override
    public ServiceTaskDelegateExpressionActivityBehavior createServiceTaskDelegateExpressionActivityBehavior(ServiceTask serviceTask) {
        for(CloudActivityBehaviorFactoryInterceptor interceptor : interceptors){
            ServiceTaskDelegateExpressionActivityBehavior behavior = interceptor.createServiceTaskDelegateExpressionActivityBehavior(serviceTask);
            if (behavior != null) {
                return behavior;
            }
        }
        return super.createServiceTaskDelegateExpressionActivityBehavior(serviceTask);
    }

    @Override
    public ServiceTaskDelegateExpressionActivityBehavior createDefaultServiceTaskBehavior(ServiceTask serviceTask) {
        for(CloudActivityBehaviorFactoryInterceptor interceptor : interceptors){
            ServiceTaskDelegateExpressionActivityBehavior behavior = interceptor.createDefaultServiceTaskBehavior(serviceTask);
            if (behavior != null) {
                return behavior;
            }
        }
        return super.createDefaultServiceTaskBehavior(serviceTask);
    }

    @Override
    public ServiceTaskExpressionActivityBehavior createServiceTaskExpressionActivityBehavior(ServiceTask serviceTask) {
        for(CloudActivityBehaviorFactoryInterceptor interceptor : interceptors){
            ServiceTaskExpressionActivityBehavior behavior = interceptor.createServiceTaskExpressionActivityBehavior(serviceTask);
            if (behavior != null) {
                return behavior;
            }
        }
        return super.createServiceTaskExpressionActivityBehavior(serviceTask);
    }

    @Override
    public WebServiceActivityBehavior createWebServiceActivityBehavior(ServiceTask serviceTask) {
        for(CloudActivityBehaviorFactoryInterceptor interceptor : interceptors){
            WebServiceActivityBehavior behavior = interceptor.createWebServiceActivityBehavior(serviceTask);
            if (behavior != null) {
                return behavior;
            }
        }
        return super.createWebServiceActivityBehavior(serviceTask);
    }

    @Override
    public WebServiceActivityBehavior createWebServiceActivityBehavior(SendTask sendTask) {
        for(CloudActivityBehaviorFactoryInterceptor interceptor : interceptors){
            WebServiceActivityBehavior behavior = interceptor.createWebServiceActivityBehavior(sendTask);
            if (behavior != null) {
                return behavior;
            }
        }
        return super.createWebServiceActivityBehavior(sendTask);
    }

    @Override
    public MailActivityBehavior createMailActivityBehavior(ServiceTask serviceTask) {
        for(CloudActivityBehaviorFactoryInterceptor interceptor : interceptors){
            MailActivityBehavior behavior = interceptor.createMailActivityBehavior(serviceTask);
            if (behavior != null) {
                return behavior;
            }
        }
        return super.createMailActivityBehavior(serviceTask);
    }

    @Override
    public MailActivityBehavior createMailActivityBehavior(SendTask sendTask) {
        for(CloudActivityBehaviorFactoryInterceptor interceptor : interceptors){
            MailActivityBehavior behavior = interceptor.createMailActivityBehavior(sendTask);
            if (behavior != null) {
                return behavior;
            }
        }
        return super.createMailActivityBehavior(sendTask);
    }

    @Override
    public ActivityBehavior createMuleActivityBehavior(ServiceTask serviceTask) {
        for(CloudActivityBehaviorFactoryInterceptor interceptor : interceptors){
            ActivityBehavior behavior = interceptor.createMuleActivityBehavior(serviceTask);
            if (behavior != null) {
                return behavior;
            }
        }
        return super.createMuleActivityBehavior(serviceTask);
    }

    @Override
    public ActivityBehavior createMuleActivityBehavior(SendTask sendTask) {
        for(CloudActivityBehaviorFactoryInterceptor interceptor : interceptors){
            ActivityBehavior behavior = interceptor.createMuleActivityBehavior(sendTask);
            if (behavior != null) {
                return behavior;
            }
        }
        return super.createMuleActivityBehavior(sendTask);
    }

    @Override
    public ActivityBehavior createCamelActivityBehavior(ServiceTask serviceTask) {
        for(CloudActivityBehaviorFactoryInterceptor interceptor : interceptors){
            ActivityBehavior behavior = interceptor.createCamelActivityBehavior(serviceTask);
            if (behavior != null) {
                return behavior;
            }
        }
        return super.createCamelActivityBehavior(serviceTask);
    }

    @Override
    public ActivityBehavior createCamelActivityBehavior(SendTask sendTask) {
        for(CloudActivityBehaviorFactoryInterceptor interceptor : interceptors){
            ActivityBehavior behavior = interceptor.createCamelActivityBehavior(sendTask);
            if (behavior != null) {
                return behavior;
            }
        }
        return super.createCamelActivityBehavior(sendTask);
    }

    @Override
    public ShellActivityBehavior createShellActivityBehavior(ServiceTask serviceTask) {
        for(CloudActivityBehaviorFactoryInterceptor interceptor : interceptors){
            ShellActivityBehavior behavior = interceptor.createShellActivityBehavior(serviceTask);
            if (behavior != null) {
                return behavior;
            }
        }
        return super.createShellActivityBehavior(serviceTask);
    }

    @Override
    public ActivityBehavior createBusinessRuleTaskActivityBehavior(BusinessRuleTask businessRuleTask) {
        for(CloudActivityBehaviorFactoryInterceptor interceptor : interceptors){
            ActivityBehavior behavior = interceptor.createBusinessRuleTaskActivityBehavior(businessRuleTask);
            if (behavior != null) {
                return behavior;
            }
        }
        return super.createBusinessRuleTaskActivityBehavior(businessRuleTask);
    }

    @Override
    public ScriptTaskActivityBehavior createScriptTaskActivityBehavior(ScriptTask scriptTask) {
        for(CloudActivityBehaviorFactoryInterceptor interceptor : interceptors){
            ScriptTaskActivityBehavior behavior = interceptor.createScriptTaskActivityBehavior(scriptTask);
            if (behavior != null) {
                return behavior;
            }
        }
        return super.createScriptTaskActivityBehavior(scriptTask);
    }

    @Override
    public ExclusiveGatewayActivityBehavior createExclusiveGatewayActivityBehavior(ExclusiveGateway exclusiveGateway) {
        for(CloudActivityBehaviorFactoryInterceptor interceptor : interceptors){
            ExclusiveGatewayActivityBehavior behavior = interceptor.createExclusiveGatewayActivityBehavior(exclusiveGateway);
            if (behavior != null) {
                return behavior;
            }
        }
        return super.createExclusiveGatewayActivityBehavior(exclusiveGateway);
    }

    @Override
    public ParallelGatewayActivityBehavior createParallelGatewayActivityBehavior(ParallelGateway parallelGateway) {
        for(CloudActivityBehaviorFactoryInterceptor interceptor : interceptors){
            ParallelGatewayActivityBehavior behavior = interceptor.createParallelGatewayActivityBehavior(parallelGateway);
            if (behavior != null) {
                return behavior;
            }
        }
        return super.createParallelGatewayActivityBehavior(parallelGateway);
    }

    @Override
    public InclusiveGatewayActivityBehavior createInclusiveGatewayActivityBehavior(InclusiveGateway inclusiveGateway) {
        for(CloudActivityBehaviorFactoryInterceptor interceptor : interceptors){
            InclusiveGatewayActivityBehavior behavior = interceptor.createInclusiveGatewayActivityBehavior(inclusiveGateway);
            if (behavior != null) {
                return behavior;
            }
        }
        return super.createInclusiveGatewayActivityBehavior(inclusiveGateway);
    }

    @Override
    public EventBasedGatewayActivityBehavior createEventBasedGatewayActivityBehavior(EventGateway eventGateway) {
        for(CloudActivityBehaviorFactoryInterceptor interceptor : interceptors){
            EventBasedGatewayActivityBehavior behavior = interceptor.createEventBasedGatewayActivityBehavior(eventGateway);
            if (behavior != null) {
                return behavior;
            }
        }
        return super.createEventBasedGatewayActivityBehavior(eventGateway);
    }

    @Override
    public SequentialMultiInstanceBehavior createSequentialMultiInstanceBehavior(Activity activity,
                                                                                 AbstractBpmnActivityBehavior innerActivityBehavior) {
        for(CloudActivityBehaviorFactoryInterceptor interceptor : interceptors){
            SequentialMultiInstanceBehavior behavior = interceptor.createSequentialMultiInstanceBehavior(activity, innerActivityBehavior);
            if (behavior != null) {
                return behavior;
            }
        }
        return super.createSequentialMultiInstanceBehavior(activity, innerActivityBehavior);
    }

    @Override
    public ParallelMultiInstanceBehavior createParallelMultiInstanceBehavior(Activity activity,
                                                                             AbstractBpmnActivityBehavior innerActivityBehavior) {
        for(CloudActivityBehaviorFactoryInterceptor interceptor : interceptors){
            ParallelMultiInstanceBehavior behavior = interceptor.createParallelMultiInstanceBehavior(activity, innerActivityBehavior);
            if (behavior != null) {
                return behavior;
            }
        }
        return super.createParallelMultiInstanceBehavior(activity, innerActivityBehavior);
    }

    @Override
    public SubProcessActivityBehavior createSubprocessActivityBehavior(SubProcess subProcess) {
        for(CloudActivityBehaviorFactoryInterceptor interceptor : interceptors){
            SubProcessActivityBehavior behavior = interceptor.createSubprocessActivityBehavior(subProcess);
            if (behavior != null) {
                return behavior;
            }
        }
        return super.createSubprocessActivityBehavior(subProcess);
    }

    @Override
    public EventSubProcessErrorStartEventActivityBehavior createEventSubProcessErrorStartEventActivityBehavior(StartEvent startEvent) 
    {
        for(CloudActivityBehaviorFactoryInterceptor interceptor : interceptors){
            EventSubProcessErrorStartEventActivityBehavior behavior = interceptor.createEventSubProcessErrorStartEventActivityBehavior(startEvent);
            if (behavior != null) {
                return behavior;
            }
        }
        return super.createEventSubProcessErrorStartEventActivityBehavior(startEvent);
    }

    @Override
    public EventSubProcessMessageStartEventActivityBehavior createEventSubProcessMessageStartEventActivityBehavior(StartEvent startEvent,
                                                                                                                   MessageEventDefinition messageEventDefinition) {
        for(CloudActivityBehaviorFactoryInterceptor interceptor : interceptors){
            EventSubProcessMessageStartEventActivityBehavior behavior = interceptor.createEventSubProcessMessageStartEventActivityBehavior(startEvent, messageEventDefinition);
            if (behavior != null) {
                return behavior;
            }
        }
        return super.createEventSubProcessMessageStartEventActivityBehavior(startEvent, messageEventDefinition);
    }

    @Override
    public AdhocSubProcessActivityBehavior createAdhocSubprocessActivityBehavior(SubProcess subProcess) {
        for(CloudActivityBehaviorFactoryInterceptor interceptor : interceptors){
            AdhocSubProcessActivityBehavior behavior = interceptor.createAdhocSubprocessActivityBehavior(subProcess);
            if (behavior != null) {
                return behavior;
            }
        }
        return super.createAdhocSubprocessActivityBehavior(subProcess);
    }

    @Override
    public CallActivityBehavior createCallActivityBehavior(CallActivity callActivity) {
        for(CloudActivityBehaviorFactoryInterceptor interceptor : interceptors){
            CallActivityBehavior behavior = interceptor.createCallActivityBehavior(callActivity);
            if (behavior != null) {
                return behavior;
            }
        }
        return super.createCallActivityBehavior(callActivity);
    }

    @Override
    public TransactionActivityBehavior createTransactionActivityBehavior(Transaction transaction) {
        for(CloudActivityBehaviorFactoryInterceptor interceptor : interceptors){
            TransactionActivityBehavior behavior = interceptor.createTransactionActivityBehavior(transaction);
            if (behavior != null) {
                return behavior;
            }
        }
        return super.createTransactionActivityBehavior(transaction);
    }

    @Override
    public IntermediateCatchEventActivityBehavior createIntermediateCatchEventActivityBehavior(IntermediateCatchEvent intermediateCatchEvent) {
        for(CloudActivityBehaviorFactoryInterceptor interceptor : interceptors){
            IntermediateCatchEventActivityBehavior behavior = interceptor.createIntermediateCatchEventActivityBehavior(intermediateCatchEvent);
            if (behavior != null) {
                return behavior;
            }
        }
        return super.createIntermediateCatchEventActivityBehavior(intermediateCatchEvent);
    }

    @Override
    public IntermediateCatchMessageEventActivityBehavior createIntermediateCatchMessageEventActivityBehavior(IntermediateCatchEvent intermediateCatchEvent,
                                                                                                             MessageEventDefinition messageEventDefinition) {
        for(CloudActivityBehaviorFactoryInterceptor interceptor : interceptors){
            IntermediateCatchMessageEventActivityBehavior behavior = interceptor.createIntermediateCatchMessageEventActivityBehavior(intermediateCatchEvent, messageEventDefinition);
            if (behavior != null) {
                return behavior;
            }
        }
        return super.createIntermediateCatchMessageEventActivityBehavior(intermediateCatchEvent, messageEventDefinition);
    }

    @Override
    public IntermediateCatchTimerEventActivityBehavior createIntermediateCatchTimerEventActivityBehavior(IntermediateCatchEvent intermediateCatchEvent,
                                                                                                         TimerEventDefinition timerEventDefinition) {
        for(CloudActivityBehaviorFactoryInterceptor interceptor : interceptors){
            IntermediateCatchTimerEventActivityBehavior behavior = interceptor.createIntermediateCatchTimerEventActivityBehavior(intermediateCatchEvent, timerEventDefinition);
            if (behavior != null) {
                return behavior;
            }
        }
        return super.createIntermediateCatchTimerEventActivityBehavior(intermediateCatchEvent, timerEventDefinition);
    }

    @Override
    public IntermediateCatchSignalEventActivityBehavior createIntermediateCatchSignalEventActivityBehavior(IntermediateCatchEvent intermediateCatchEvent,
                                                                                                           SignalEventDefinition signalEventDefinition,
                                                                                                           Signal signal) {
        for(CloudActivityBehaviorFactoryInterceptor interceptor : interceptors){
            IntermediateCatchSignalEventActivityBehavior behavior = interceptor.createIntermediateCatchSignalEventActivityBehavior(intermediateCatchEvent, signalEventDefinition, signal);
            if (behavior != null) {
                return behavior;
            }
        }
        return super.createIntermediateCatchSignalEventActivityBehavior(intermediateCatchEvent, signalEventDefinition, signal);
    }

    @Override
    public IntermediateThrowNoneEventActivityBehavior createIntermediateThrowNoneEventActivityBehavior(ThrowEvent throwEvent) {
        for(CloudActivityBehaviorFactoryInterceptor interceptor : interceptors){
            IntermediateThrowNoneEventActivityBehavior behavior = interceptor.createIntermediateThrowNoneEventActivityBehavior(throwEvent);
            if (behavior != null) {
                return behavior;
            }
        }
        return super.createIntermediateThrowNoneEventActivityBehavior(throwEvent);
    }

    @Override
    public IntermediateThrowSignalEventActivityBehavior createIntermediateThrowSignalEventActivityBehavior(ThrowEvent throwEvent,
                                                                                                           SignalEventDefinition signalEventDefinition,
                                                                                                           Signal signal) {
        for(CloudActivityBehaviorFactoryInterceptor interceptor : interceptors){
            IntermediateThrowSignalEventActivityBehavior behavior = interceptor.createIntermediateThrowSignalEventActivityBehavior(throwEvent, signalEventDefinition, signal);
            if (behavior != null) {
                return behavior;
            }
        }
        return super.createIntermediateThrowSignalEventActivityBehavior(throwEvent, signalEventDefinition, signal);
    }

    @Override
    public IntermediateThrowCompensationEventActivityBehavior createIntermediateThrowCompensationEventActivityBehavior(ThrowEvent throwEvent,
                                                                                                                       CompensateEventDefinition compensateEventDefinition) {
        for(CloudActivityBehaviorFactoryInterceptor interceptor : interceptors){
            IntermediateThrowCompensationEventActivityBehavior behavior = interceptor.createIntermediateThrowCompensationEventActivityBehavior(throwEvent, compensateEventDefinition);
            if (behavior != null) {
                return behavior;
            }
        }
        return super.createIntermediateThrowCompensationEventActivityBehavior(throwEvent, compensateEventDefinition);
    }

    @Override
    public NoneEndEventActivityBehavior createNoneEndEventActivityBehavior(EndEvent endEvent) {
        for(CloudActivityBehaviorFactoryInterceptor interceptor : interceptors){
            NoneEndEventActivityBehavior behavior = interceptor.createNoneEndEventActivityBehavior(endEvent);
            if (behavior != null) {
                return behavior;
            }
        }
        return super.createNoneEndEventActivityBehavior(endEvent);
    }

    @Override
    public ErrorEndEventActivityBehavior createErrorEndEventActivityBehavior(EndEvent endEvent,
                                                                             ErrorEventDefinition errorEventDefinition) {
        for(CloudActivityBehaviorFactoryInterceptor interceptor : interceptors){
            ErrorEndEventActivityBehavior behavior = interceptor.createErrorEndEventActivityBehavior(endEvent, errorEventDefinition);
            if (behavior != null) {
                return behavior;
            }
        }
        return super.createErrorEndEventActivityBehavior(endEvent, errorEventDefinition);
    }

    @Override
    public CancelEndEventActivityBehavior createCancelEndEventActivityBehavior(EndEvent endEvent) {
        for(CloudActivityBehaviorFactoryInterceptor interceptor : interceptors){
            CancelEndEventActivityBehavior behavior = interceptor.createCancelEndEventActivityBehavior(endEvent);
            if (behavior != null) {
                return behavior;
            }
        }
        return super.createCancelEndEventActivityBehavior(endEvent);
    }

    @Override
    public TerminateEndEventActivityBehavior createTerminateEndEventActivityBehavior(EndEvent endEvent) {
        for(CloudActivityBehaviorFactoryInterceptor interceptor : interceptors){
            TerminateEndEventActivityBehavior behavior = interceptor.createTerminateEndEventActivityBehavior(endEvent);
            if (behavior != null) {
                return behavior;
            }
        }
        return super.createTerminateEndEventActivityBehavior(endEvent);
    }

    @Override
    public BoundaryEventActivityBehavior createBoundaryEventActivityBehavior(BoundaryEvent boundaryEvent,
                                                                             boolean interrupting) {
        for(CloudActivityBehaviorFactoryInterceptor interceptor : interceptors){
            BoundaryEventActivityBehavior behavior = interceptor.createBoundaryEventActivityBehavior(boundaryEvent, interrupting);
            if (behavior != null) {
                return behavior;
            }
        }
        return super.createBoundaryEventActivityBehavior(boundaryEvent, interrupting);
    }

    @Override
    public BoundaryCancelEventActivityBehavior createBoundaryCancelEventActivityBehavior(CancelEventDefinition cancelEventDefinition) {
        for(CloudActivityBehaviorFactoryInterceptor interceptor : interceptors){
            BoundaryCancelEventActivityBehavior behavior = interceptor.createBoundaryCancelEventActivityBehavior(cancelEventDefinition);
            if (behavior != null) {
                return behavior;
            }
        }
        return super.createBoundaryCancelEventActivityBehavior(cancelEventDefinition);
    }

    @Override
    public BoundaryCompensateEventActivityBehavior createBoundaryCompensateEventActivityBehavior(BoundaryEvent boundaryEvent,
                                                                                                 CompensateEventDefinition compensateEventDefinition,
                                                                                                 boolean interrupting) {
        for(CloudActivityBehaviorFactoryInterceptor interceptor : interceptors){
            BoundaryCompensateEventActivityBehavior behavior = interceptor.createBoundaryCompensateEventActivityBehavior(boundaryEvent, compensateEventDefinition, interrupting);
            if (behavior != null) {
                return behavior;
            }
        }
        return super.createBoundaryCompensateEventActivityBehavior(boundaryEvent, compensateEventDefinition, interrupting);
    }

    @Override
    public BoundaryTimerEventActivityBehavior createBoundaryTimerEventActivityBehavior(BoundaryEvent boundaryEvent,
                                                                                       TimerEventDefinition timerEventDefinition,
                                                                                       boolean interrupting) {
        for(CloudActivityBehaviorFactoryInterceptor interceptor : interceptors){
            BoundaryTimerEventActivityBehavior behavior = interceptor.createBoundaryTimerEventActivityBehavior(boundaryEvent, timerEventDefinition, interrupting);
            if (behavior != null) {
                return behavior;
            }
        }
        return super.createBoundaryTimerEventActivityBehavior(boundaryEvent, timerEventDefinition, interrupting);
    }

    @Override
    public BoundarySignalEventActivityBehavior createBoundarySignalEventActivityBehavior(BoundaryEvent boundaryEvent,
                                                                                         SignalEventDefinition signalEventDefinition,
                                                                                         Signal signal,
                                                                                         boolean interrupting) {
        for(CloudActivityBehaviorFactoryInterceptor interceptor : interceptors){
            BoundarySignalEventActivityBehavior behavior = interceptor.createBoundarySignalEventActivityBehavior(boundaryEvent, signalEventDefinition, signal, interrupting);
            if (behavior != null) {
                return behavior;
            }
        }
        return super.createBoundarySignalEventActivityBehavior(boundaryEvent, signalEventDefinition, signal, interrupting);
    }

    @Override
    public BoundaryMessageEventActivityBehavior createBoundaryMessageEventActivityBehavior(BoundaryEvent boundaryEvent,
                                                                                           MessageEventDefinition messageEventDefinition,
                                                                                           boolean interrupting) {
        for(CloudActivityBehaviorFactoryInterceptor interceptor : interceptors){
            BoundaryMessageEventActivityBehavior behavior = interceptor.createBoundaryMessageEventActivityBehavior(boundaryEvent, messageEventDefinition, interrupting);
            if (behavior != null) {
                return behavior;
            }
        }
        return super.createBoundaryMessageEventActivityBehavior(boundaryEvent, messageEventDefinition, interrupting);
    }
}
