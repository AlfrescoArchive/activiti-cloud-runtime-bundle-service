package org.activiti.services.subscriptions.impl.bpmn.helper;

import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.impl.bpmn.helper.BaseDelegateEventListener;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.JobEntity;
import org.activiti.engine.impl.persistence.entity.JobEntityManager;
import org.activiti.engine.impl.util.json.JSONObject;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.services.subscriptions.impl.jobexecutor.BroadcastSignalJobHandler;


public class BroadcastSignalThrowingEventListener extends BaseDelegateEventListener {

    protected String signalName;
    
    public BroadcastSignalThrowingEventListener(String signalName) {
        this.signalName = signalName;
    }

    public void onEvent(ActivitiEvent event) {
        if (isValidEvent(event)) {
            CommandContext commandContext = Context.getCommandContext();

            JobEntityManager jobEntityManager = commandContext.getProcessEngineConfiguration().getJobEntityManager();
            JobEntity message = jobEntityManager.create();
            message.setJobType(JobEntity.JOB_TYPE_MESSAGE);
            message.setJobHandlerType(BroadcastSignalJobHandler.TYPE);
    
            JSONObject json = new JSONObject();
            json.put(BroadcastSignalJobHandler.JOB_HANDLER_CFG_SIGNAL_NAME, signalName);
            json.put(BroadcastSignalJobHandler.JOB_HANDLER_CFG_IS_SIGNAL_ASYNC, false);
            message.setJobHandlerConfiguration(json.toString());

            if (event.getProcessDefinitionId() != null) {
                ProcessDefinition processDefinition = commandContext.getProcessEngineConfiguration().getDeploymentManager().findDeployedProcessDefinitionById(event.getProcessDefinitionId());
                message.setTenantId(processDefinition.getTenantId());
            }

            jobEntityManager.insert(message);
        }
    }

    public boolean isFailOnException() {
        return true;
    }
}
