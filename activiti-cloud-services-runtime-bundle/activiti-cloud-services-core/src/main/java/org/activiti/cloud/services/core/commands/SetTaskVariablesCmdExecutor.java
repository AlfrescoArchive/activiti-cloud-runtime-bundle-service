package org.activiti.cloud.services.core.commands;

import org.activiti.runtime.api.EmptyResult;
import org.activiti.runtime.api.TaskRuntime;
import org.activiti.runtime.api.model.payloads.SetTaskVariablesPayload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
public class SetTaskVariablesCmdExecutor implements CommandExecutor<SetTaskVariablesPayload> {

    private TaskRuntime taskRuntime;
    private MessageChannel commandResults;

    @Autowired
    public SetTaskVariablesCmdExecutor(TaskRuntime taskRuntime,
                                       MessageChannel commandResults) {
        this.taskRuntime = taskRuntime;
        this.commandResults = commandResults;
    }

    @Override
    public String getHandledType() {
        return SetTaskVariablesPayload.class.getName();
    }

    @Override
    public void execute(SetTaskVariablesPayload setTaskVariablesPayload) {
        taskRuntime.setVariables(setTaskVariablesPayload);
        commandResults.send(MessageBuilder.withPayload(new EmptyResult(setTaskVariablesPayload)).build());
    }
}
