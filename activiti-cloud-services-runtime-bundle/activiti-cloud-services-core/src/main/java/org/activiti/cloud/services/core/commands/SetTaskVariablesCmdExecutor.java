package org.activiti.cloud.services.core.commands;

import org.activiti.cloud.services.core.pageable.SecurityAwareTaskService;
import org.activiti.runtime.api.model.payloads.SetTaskVariablesPayload;
import org.activiti.runtime.api.model.results.ProcessInstanceResult;
import org.activiti.runtime.api.model.results.TaskResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
public class SetTaskVariablesCmdExecutor implements CommandExecutor<SetTaskVariablesPayload> {

    private SecurityAwareTaskService securityAwareTaskService;
    private MessageChannel commandResults;

    @Autowired
    public SetTaskVariablesCmdExecutor(SecurityAwareTaskService securityAwareTaskService,
                                       MessageChannel commandResults) {
        this.securityAwareTaskService = securityAwareTaskService;
        this.commandResults = commandResults;
    }

    @Override
    public String getHandledType() {
        return SetTaskVariablesPayload.class.getName();
    }

    @Override
    public void execute(SetTaskVariablesPayload setTaskVariablesPayload) {
        securityAwareTaskService.setTaskVariables(setTaskVariablesPayload);
        TaskResult result = new TaskResult(setTaskVariablesPayload,
                                                      null);
        commandResults.send(MessageBuilder.withPayload(result).build());
    }
}
