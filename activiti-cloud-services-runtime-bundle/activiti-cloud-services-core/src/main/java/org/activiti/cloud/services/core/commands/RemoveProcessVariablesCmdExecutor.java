package org.activiti.cloud.services.core.commands;

import org.activiti.runtime.api.EmptyResult;
import org.activiti.runtime.api.ProcessRuntime;
import org.activiti.runtime.api.model.payloads.RemoveProcessVariablesPayload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
public class RemoveProcessVariablesCmdExecutor implements CommandExecutor<RemoveProcessVariablesPayload> {

    private ProcessRuntime processRuntime;
    private MessageChannel commandResults;

    @Autowired
    public RemoveProcessVariablesCmdExecutor(ProcessRuntime processRuntime,
                                             MessageChannel commandResults) {
        this.processRuntime = processRuntime;
        this.commandResults = commandResults;
    }

    @Override
    public String getHandledType() {
        return RemoveProcessVariablesPayload.class.getName();
    }

    @Override
    public void execute(RemoveProcessVariablesPayload removeProcessVariablesPayload) {
        processRuntime.removeVariables(removeProcessVariablesPayload);
        commandResults.send(MessageBuilder.withPayload(new EmptyResult(removeProcessVariablesPayload)).build());
    }
}