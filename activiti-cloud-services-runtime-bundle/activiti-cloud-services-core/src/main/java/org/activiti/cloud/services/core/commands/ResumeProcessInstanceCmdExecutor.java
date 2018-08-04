package org.activiti.cloud.services.core.commands;

import org.activiti.runtime.api.ProcessRuntime;
import org.activiti.runtime.api.model.ProcessInstance;
import org.activiti.runtime.api.model.payloads.ResumeProcessPayload;
import org.activiti.runtime.api.model.results.ProcessInstanceResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
public class ResumeProcessInstanceCmdExecutor implements CommandExecutor<ResumeProcessPayload> {

    private ProcessRuntime processRuntime;
    private MessageChannel commandResults;

    @Autowired
    public ResumeProcessInstanceCmdExecutor(ProcessRuntime processRuntime,
                                            MessageChannel commandResults) {
        this.processRuntime = processRuntime;
        this.commandResults = commandResults;
    }

    @Override
    public String getHandledType() {
        return ResumeProcessPayload.class.getName();
    }

    @Override
    public void execute(ResumeProcessPayload resumeProcessPayload) {
        ProcessInstance processInstance = processRuntime.resume(resumeProcessPayload);
        ProcessInstanceResult result = new ProcessInstanceResult(resumeProcessPayload,
                                                    processInstance);
        commandResults.send(MessageBuilder.withPayload(result).build());
    }
}
