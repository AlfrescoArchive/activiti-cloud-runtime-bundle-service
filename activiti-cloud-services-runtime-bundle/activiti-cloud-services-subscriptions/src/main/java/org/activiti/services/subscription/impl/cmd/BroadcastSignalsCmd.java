package org.activiti.services.subscription.impl.cmd;

import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.services.subscription.channel.BroadcastSignaEventHandler;
import org.activiti.services.subscriptions.model.BroadcastSignalEvent;
import org.activiti.spring.SpringProcessEngineConfiguration;


public class BroadcastSignalsCmd implements Command<Void> {

    private String signalName;

    private boolean isSignalAsync;

    public BroadcastSignalsCmd(String signalName, boolean isSignalAsync) {
        this.signalName = signalName;
        this.isSignalAsync = isSignalAsync;
    }
    
    public Void execute(CommandContext commandContext) {
        SpringProcessEngineConfiguration springProcessEngineConfiguration = (SpringProcessEngineConfiguration) commandContext.getProcessEngineConfiguration();
        BroadcastSignaEventHandler eventHandler = springProcessEngineConfiguration.getApplicationContext().getBean(BroadcastSignaEventHandler.class);
        eventHandler.broadcastSignals(new BroadcastSignalEvent(signalName, isSignalAsync));
        return null;
    }

}
