package org.activiti.services.subscription.impl.cmd;

import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.JobEntity;
import org.activiti.engine.impl.persistence.entity.JobEntityManager;
import org.activiti.engine.impl.util.json.JSONObject;
import org.activiti.services.subscription.channel.BroadcastSignaEventHandler;
import org.activiti.services.subscriptions.impl.jobexecutor.BroadcastSignalJobHandler;
import org.activiti.services.subscriptions.model.BroadcastSignalEvent;
import org.activiti.spring.SpringProcessEngineConfiguration;


public class BroadcastSignalCmd implements Command<Void> {

    private String signalName;
    private boolean isSignalAsync;
    private String tenantId;

    public BroadcastSignalCmd(String signalName, boolean isSignalAsync, String tenantId) {
        this.signalName = signalName;
        this.isSignalAsync = isSignalAsync;
        this.tenantId = tenantId;
    }

    public Void execute(CommandContext commandContext) {
        if ( commandContext.isReused() ) {
            JobEntityManager jobEntityManager = commandContext.getProcessEngineConfiguration().getJobEntityManager();
            JobEntity message = jobEntityManager.create();
            message.setJobType(JobEntity.JOB_TYPE_MESSAGE);
            message.setJobHandlerType(BroadcastSignalJobHandler.TYPE);
    
            JSONObject json = new JSONObject();
            json.put(BroadcastSignalJobHandler.JOB_HANDLER_CFG_SIGNAL_NAME, signalName);
            json.put(BroadcastSignalJobHandler.JOB_HANDLER_CFG_IS_SIGNAL_ASYNC, false);
            message.setJobHandlerConfiguration(json.toString());
            message.setTenantId(tenantId);

            jobEntityManager.insert(message);
        } else {
            SpringProcessEngineConfiguration springProcessEngineConfiguration = (SpringProcessEngineConfiguration) commandContext.getProcessEngineConfiguration();
            BroadcastSignaEventHandler eventHandler = springProcessEngineConfiguration.getApplicationContext().getBean(BroadcastSignaEventHandler.class);
            eventHandler.broadcastSignal(new BroadcastSignalEvent(signalName, isSignalAsync));
        }
        return null;
    }
}
