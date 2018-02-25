package org.activiti.services.subscriptions.impl.jobexecutor;

import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.jobexecutor.JobHandler;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.JobEntity;
import org.activiti.engine.impl.util.json.JSONObject;
import org.activiti.services.subscription.channel.BroadcastSignaEventHandler;
import org.activiti.services.subscriptions.model.BroadcastSignalEvent;
import org.activiti.spring.SpringProcessEngineConfiguration;


public class BroadcastSignalJobHandler implements JobHandler {

    public final static String TYPE = "signal-broadcast";

    public static final String JOB_HANDLER_CFG_SIGNAL_NAME = "signal-name";

    public static final String JOB_HANDLER_CFG_IS_SIGNAL_ASYNC = "is-signal-async";

    private BroadcastSignaEventHandler eventHandler;

    public String getType() {
        return TYPE;
    }

    public void execute(JobEntity job, String configuration, ExecutionEntity execution, CommandContext commandContext) {
        if ( eventHandler == null ) {
            SpringProcessEngineConfiguration springProcessEngineConfiguration = (SpringProcessEngineConfiguration) commandContext.getProcessEngineConfiguration();
            eventHandler = springProcessEngineConfiguration.getApplicationContext().getBean(BroadcastSignaEventHandler.class);
        }

        JSONObject cfgJson = new JSONObject(configuration);
        String signalName = cfgJson.getString(JOB_HANDLER_CFG_SIGNAL_NAME);
        boolean isSignalAsync = cfgJson.getBoolean(JOB_HANDLER_CFG_IS_SIGNAL_ASYNC);

        eventHandler.broadcastSignal(new BroadcastSignalEvent(signalName, isSignalAsync));
    }
}