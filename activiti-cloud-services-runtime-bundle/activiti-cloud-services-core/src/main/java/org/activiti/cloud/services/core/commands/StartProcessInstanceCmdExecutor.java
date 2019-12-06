/*
 * Copyright 2019 Alfresco, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.cloud.services.core.commands;

import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.payloads.StartProcessPayload;
import org.activiti.api.process.model.results.ProcessInstanceResult;
import org.activiti.api.process.runtime.ProcessAdminRuntime;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;

public class StartProcessInstanceCmdExecutor implements CommandExecutor<StartProcessPayload> {

    private ProcessAdminRuntime processAdminRuntime;
    private MessageChannel commandResults;

    public StartProcessInstanceCmdExecutor(ProcessAdminRuntime processAdminRuntime,
                                           MessageChannel commandResults) {
        this.processAdminRuntime = processAdminRuntime;
        this.commandResults = commandResults;
    }

    @Override
    public String getHandledType() {
        return StartProcessPayload.class.getName();
    }

    @Override
    public void execute(StartProcessPayload startProcessPayload) {
        ProcessInstance processInstance = processAdminRuntime.start(startProcessPayload);
        if (processInstance != null) {
            ProcessInstanceResult result = new ProcessInstanceResult(startProcessPayload,
                                                                     processInstance);
            commandResults.send(MessageBuilder.withPayload(result).build());
        } else {
            throw new IllegalStateException("Failed to start processInstance");
        }
    }
}
