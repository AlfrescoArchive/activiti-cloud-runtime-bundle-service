package org.activiti.services.subscriptions.behavior;

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
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.delegate.ActivityBehavior;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.activiti.spring.bpmn.parser.CloudActivityBehaviorFactoryInterceptor;

public class BroadcastSignalEventActivityBehaviorInterceptor implements CloudActivityBehaviorFactoryInterceptor {

    public NoneStartEventActivityBehavior createNoneStartEventActivityBehavior(StartEvent startEvent) {

        return null;
    }

    public TaskActivityBehavior createTaskActivityBehavior(Task task) {

        return null;
    }

    public ManualTaskActivityBehavior createManualTaskActivityBehavior(ManualTask manualTask) {

        return null;
    }

    public ReceiveTaskActivityBehavior createReceiveTaskActivityBehavior(ReceiveTask receiveTask) {

        return null;
    }

    public UserTaskActivityBehavior createUserTaskActivityBehavior(UserTask userTask) {

        return null;
    }

    public ClassDelegate createClassDelegateServiceTask(ServiceTask serviceTask) {

        return null;
    }

    public ServiceTaskDelegateExpressionActivityBehavior createServiceTaskDelegateExpressionActivityBehavior(
                                                                                                             ServiceTask serviceTask) {

        return null;
    }

    public ServiceTaskDelegateExpressionActivityBehavior createDefaultServiceTaskBehavior(ServiceTask serviceTask) {

        return null;
    }

    public ServiceTaskExpressionActivityBehavior createServiceTaskExpressionActivityBehavior(ServiceTask serviceTask) {

        return null;
    }

    public WebServiceActivityBehavior createWebServiceActivityBehavior(ServiceTask serviceTask) {

        return null;
    }

    public WebServiceActivityBehavior createWebServiceActivityBehavior(SendTask sendTask) {

        return null;
    }

    public MailActivityBehavior createMailActivityBehavior(ServiceTask serviceTask) {

        return null;
    }

    public MailActivityBehavior createMailActivityBehavior(SendTask sendTask) {

        return null;
    }

    public ActivityBehavior createMuleActivityBehavior(ServiceTask serviceTask) {

        return null;
    }

    public ActivityBehavior createMuleActivityBehavior(SendTask sendTask) {

        return null;
    }

    public ActivityBehavior createCamelActivityBehavior(ServiceTask serviceTask) {

        return null;
    }

    public ActivityBehavior createCamelActivityBehavior(SendTask sendTask) {

        return null;
    }

    public ShellActivityBehavior createShellActivityBehavior(ServiceTask serviceTask) {

        return null;
    }

    public ActivityBehavior createBusinessRuleTaskActivityBehavior(BusinessRuleTask businessRuleTask) {

        return null;
    }

    public ScriptTaskActivityBehavior createScriptTaskActivityBehavior(ScriptTask scriptTask) {

        return null;
    }

    public ExclusiveGatewayActivityBehavior createExclusiveGatewayActivityBehavior(ExclusiveGateway exclusiveGateway) {

        return null;
    }

    public ParallelGatewayActivityBehavior createParallelGatewayActivityBehavior(ParallelGateway parallelGateway) {

        return null;
    }

    public InclusiveGatewayActivityBehavior createInclusiveGatewayActivityBehavior(InclusiveGateway inclusiveGateway) {

        return null;
    }

    public EventBasedGatewayActivityBehavior createEventBasedGatewayActivityBehavior(EventGateway eventGateway) {

        return null;
    }

    public SequentialMultiInstanceBehavior createSequentialMultiInstanceBehavior(Activity activity,
                                                                                 AbstractBpmnActivityBehavior innerActivityBehavior) {

        return null;
    }

    public ParallelMultiInstanceBehavior createParallelMultiInstanceBehavior(Activity activity,
                                                                             AbstractBpmnActivityBehavior innerActivityBehavior) {

        return null;
    }

    public SubProcessActivityBehavior createSubprocessActivityBehavior(SubProcess subProcess) {

        return null;
    }

    public EventSubProcessErrorStartEventActivityBehavior createEventSubProcessErrorStartEventActivityBehavior(
                                                                                                               StartEvent startEvent) {

        return null;
    }

    public EventSubProcessMessageStartEventActivityBehavior createEventSubProcessMessageStartEventActivityBehavior(
                                                                                                                   StartEvent startEvent,
                                                                                                                   MessageEventDefinition messageEventDefinition) {

        return null;
    }

    public AdhocSubProcessActivityBehavior createAdhocSubprocessActivityBehavior(SubProcess subProcess) {

        return null;
    }

    public CallActivityBehavior createCallActivityBehavior(CallActivity callActivity) {

        return null;
    }

    public TransactionActivityBehavior createTransactionActivityBehavior(Transaction transaction) {

        return null;
    }

    public IntermediateCatchEventActivityBehavior createIntermediateCatchEventActivityBehavior(
                                                                                               IntermediateCatchEvent intermediateCatchEvent) {

        return null;
    }

    public IntermediateCatchMessageEventActivityBehavior createIntermediateCatchMessageEventActivityBehavior(
                                                                                                             IntermediateCatchEvent intermediateCatchEvent,
                                                                                                             MessageEventDefinition messageEventDefinition) {

        return null;
    }

    public IntermediateCatchTimerEventActivityBehavior createIntermediateCatchTimerEventActivityBehavior(
                                                                                                         IntermediateCatchEvent intermediateCatchEvent,
                                                                                                         TimerEventDefinition timerEventDefinition) {

        return null;
    }

    public IntermediateCatchSignalEventActivityBehavior createIntermediateCatchSignalEventActivityBehavior(
                                                                                                           IntermediateCatchEvent intermediateCatchEvent,
                                                                                                           SignalEventDefinition signalEventDefinition,
                                                                                                           Signal signal) {

        return null;
    }

    public IntermediateThrowNoneEventActivityBehavior createIntermediateThrowNoneEventActivityBehavior(
                                                                                                       ThrowEvent throwEvent) {

        return null;
    }

    public IntermediateThrowSignalEventActivityBehavior createIntermediateThrowSignalEventActivityBehavior(
                                                                                                           ThrowEvent throwEvent,
                                                                                                           SignalEventDefinition signalEventDefinition,
                                                                                                           Signal signal) {
        SpringProcessEngineConfiguration springProcessEngineConfiguration = (SpringProcessEngineConfiguration) Context.getProcessEngineConfiguration();
    	return new BroadcastSignalEventActivityBehavior(springProcessEngineConfiguration.getApplicationContext(), signalEventDefinition, signal);
    }

    public IntermediateThrowCompensationEventActivityBehavior createIntermediateThrowCompensationEventActivityBehavior(
                                                                                                                       ThrowEvent throwEvent,
                                                                                                                       CompensateEventDefinition compensateEventDefinition) {

        return null;
    }

    public NoneEndEventActivityBehavior createNoneEndEventActivityBehavior(EndEvent endEvent) {

        return null;
    }

    public ErrorEndEventActivityBehavior createErrorEndEventActivityBehavior(EndEvent endEvent,
                                                                             ErrorEventDefinition errorEventDefinition) {

        return null;
    }

    public CancelEndEventActivityBehavior createCancelEndEventActivityBehavior(EndEvent endEvent) {

        return null;
    }

    public TerminateEndEventActivityBehavior createTerminateEndEventActivityBehavior(EndEvent endEvent) {

        return null;
    }

    public BoundaryEventActivityBehavior createBoundaryEventActivityBehavior(BoundaryEvent boundaryEvent,
                                                                             boolean interrupting) {

        return null;
    }

    public BoundaryCancelEventActivityBehavior createBoundaryCancelEventActivityBehavior(
                                                                                         CancelEventDefinition cancelEventDefinition) {

        return null;
    }

    public BoundaryTimerEventActivityBehavior createBoundaryTimerEventActivityBehavior(BoundaryEvent boundaryEvent,
                                                                                       TimerEventDefinition timerEventDefinition,
                                                                                       boolean interrupting) {

        return null;
    }

    public BoundarySignalEventActivityBehavior createBoundarySignalEventActivityBehavior(BoundaryEvent boundaryEvent,
                                                                                         SignalEventDefinition signalEventDefinition,
                                                                                         Signal signal,
                                                                                         boolean interrupting) {

        return null;
    }

    public BoundaryMessageEventActivityBehavior createBoundaryMessageEventActivityBehavior(BoundaryEvent boundaryEvent,
                                                                                           MessageEventDefinition messageEventDefinition,
                                                                                           boolean interrupting) {

        return null;
    }

    public BoundaryCompensateEventActivityBehavior createBoundaryCompensateEventActivityBehavior(
                                                                                                 BoundaryEvent boundaryEvent,
                                                                                                 CompensateEventDefinition compensateEventDefinition,
                                                                                                 boolean interrupting) {

        return null;
    }

}
