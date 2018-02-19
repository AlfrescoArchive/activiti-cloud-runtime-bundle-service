package org.activiti.services.subscriptions.behavior;

import org.activiti.bpmn.model.Signal;
import org.activiti.bpmn.model.SignalEventDefinition;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.bpmn.behavior.IntermediateThrowSignalEventActivityBehavior;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.JobEntity;
import org.activiti.engine.impl.persistence.entity.JobEntityManager;
import org.activiti.engine.impl.util.json.JSONObject;
import org.activiti.services.subscriptions.impl.jobexecutor.BroadcastSignalJobHandler;


public class BroadcastSignalEventActivityBehavior extends IntermediateThrowSignalEventActivityBehavior {
    
    private static final long serialVersionUID = 1L;

    protected final SignalEventDefinition signalEventDefinition;

    public BroadcastSignalEventActivityBehavior(SignalEventDefinition signalEventDefinition,
                                                        Signal signal) {
        super(signalEventDefinition, signal);
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
        
        JobEntityManager jobEntityManager = commandContext.getProcessEngineConfiguration().getJobEntityManager();
        JobEntity message = jobEntityManager.create();
        message.setJobType(JobEntity.JOB_TYPE_MESSAGE);
        message.setJobHandlerType(BroadcastSignalJobHandler.TYPE);

        JSONObject json = new JSONObject();
        json.put(BroadcastSignalJobHandler.JOB_HANDLER_CFG_SIGNAL_NAME, eventSubscriptionName);
        json.put(BroadcastSignalJobHandler.JOB_HANDLER_CFG_IS_SIGNAL_ASYNC, signalEventDefinition.isAsync());
        message.setJobHandlerConfiguration(json.toString());

        message.setTenantId(execution.getTenantId());

        jobEntityManager.insert(message);
        
        Context.getAgenda().planTakeOutgoingSequenceFlowsOperation((ExecutionEntity) execution,
                                                                   true);
    }
}