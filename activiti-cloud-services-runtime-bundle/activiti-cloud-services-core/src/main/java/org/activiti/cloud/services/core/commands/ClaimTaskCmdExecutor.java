package org.activiti.cloud.services.core.commands;

import org.activiti.runtime.api.TaskAdminRuntime;
import org.activiti.runtime.api.TaskRuntime;
import org.activiti.api.task.model.Task;
import org.activiti.runtime.api.model.payloads.ClaimTaskPayload;
import org.activiti.runtime.api.model.results.TaskResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
public class ClaimTaskCmdExecutor implements CommandExecutor<ClaimTaskPayload> {

    private TaskAdminRuntime taskAdminRuntime;
    private MessageChannel commandResults;

    @Autowired
    public ClaimTaskCmdExecutor(TaskAdminRuntime taskAdminRuntime,
                                MessageChannel commandResults) {
        this.taskAdminRuntime = taskAdminRuntime;
        this.commandResults = commandResults;
    }

    @Override
    public String getHandledType() {
        return ClaimTaskPayload.class.getName();
    }

    @Override
    public void execute(ClaimTaskPayload claimTaskPayload) {
        Task task = taskAdminRuntime.claim(claimTaskPayload);
        TaskResult result = new TaskResult(claimTaskPayload, task);
        commandResults.send(MessageBuilder.withPayload(result).build());
    }
}
