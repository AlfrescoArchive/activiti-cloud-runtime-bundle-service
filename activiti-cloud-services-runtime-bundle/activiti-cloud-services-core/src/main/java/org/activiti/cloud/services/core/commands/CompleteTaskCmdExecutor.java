package org.activiti.cloud.services.core.commands;

import org.activiti.runtime.api.TaskRuntime;
import org.activiti.runtime.api.model.Task;
import org.activiti.runtime.api.model.payloads.CompleteTaskPayload;
import org.activiti.runtime.api.model.results.TaskResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
public class CompleteTaskCmdExecutor implements CommandExecutor<CompleteTaskPayload> {

    private TaskRuntime taskRuntime;
    private MessageChannel commandResults;

    @Autowired
    public CompleteTaskCmdExecutor(TaskRuntime taskRuntime,
                                   MessageChannel commandResults) {
        this.taskRuntime = taskRuntime;
        this.commandResults = commandResults;
    }

    @Override
    public String getHandledType() {
        return CompleteTaskPayload.class.getName();
    }

    @Override
    public void execute(CompleteTaskPayload completeTaskPayload) {
        Task task = taskRuntime.complete(completeTaskPayload);
        TaskResult result = new TaskResult(completeTaskPayload,
                                         task);
        commandResults.send(MessageBuilder.withPayload(result).build());
    }
}
