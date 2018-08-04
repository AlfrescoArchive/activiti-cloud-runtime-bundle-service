package org.activiti.cloud.services.core.commands;

import org.activiti.runtime.api.EmptyResult;
import org.activiti.runtime.api.ProcessRuntime;
import org.activiti.runtime.api.model.payloads.SetProcessVariablesPayload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
public class SetProcessVariablesCmdExecutor implements CommandExecutor<SetProcessVariablesPayload> {

    private ProcessRuntime processRuntime;
    private MessageChannel commandResults;

    @Autowired
    public SetProcessVariablesCmdExecutor(ProcessRuntime processRuntime,
                                          MessageChannel commandResults) {
        this.processRuntime = processRuntime;
        this.commandResults = commandResults;
    }

    @Override
    public String getHandledType() {
        return SetProcessVariablesPayload.class.getName();
    }

    @Override
    public void execute(SetProcessVariablesPayload setProcessVariablesPayload) {
        processRuntime.setVariables(setProcessVariablesPayload);
        commandResults.send(MessageBuilder.withPayload(new EmptyResult(setProcessVariablesPayload)).build());
    }
}