package org.activiti.services.subscriptions.behavior;

import org.activiti.bpmn.model.Signal;
import org.activiti.bpmn.model.SignalEventDefinition;
import org.activiti.cloud.services.api.commands.SignalCmd;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.bpmn.behavior.IntermediateThrowSignalEventActivityBehavior;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component("defaultThrowSignalEventBehavior")
public class BroadcastSignalEventActivityBehavior extends IntermediateThrowSignalEventActivityBehavior {
    
    private static final long serialVersionUID = 1L;

    protected final SignalEventDefinition signalEventDefinition;

    private final ApplicationEventPublisher eventPublisher;

    public BroadcastSignalEventActivityBehavior(ApplicationEventPublisher eventPublisher, SignalEventDefinition signalEventDefinition,
                                                        Signal signal) {
        super(signalEventDefinition, signal);
        this.eventPublisher = eventPublisher;
        this.signalEventDefinition = signalEventDefinition;
    }

    public void execute(DelegateExecution execution) {

        CommandContext commandContext = Context.getCommandContext();
        String eventSubscriptionName = null;
        if (signalEventName != null) {
            eventSubscriptionName = signalEventName;
        } else {
            Expression expressionObject = commandContext.getProcessEngineConfiguration().getExpressionManager().createExpression(signalExpression);
            eventSubscriptionName = expressionObject.getValue(execution).toString();
        }
        
        eventPublisher.publishEvent(new SignalCmd(eventSubscriptionName, null));
        
        Context.getAgenda().planTakeOutgoingSequenceFlowsOperation((ExecutionEntity) execution,
                                                                   true);
    }
}