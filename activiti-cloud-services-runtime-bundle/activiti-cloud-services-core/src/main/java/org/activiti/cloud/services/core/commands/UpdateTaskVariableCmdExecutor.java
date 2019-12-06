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

import org.activiti.api.model.shared.EmptyResult;
import org.activiti.api.task.model.payloads.UpdateTaskVariablePayload;
import org.activiti.api.task.runtime.TaskAdminRuntime;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;

public class UpdateTaskVariableCmdExecutor implements CommandExecutor<UpdateTaskVariablePayload> {

    private TaskAdminRuntime taskAdminRuntime;
    private MessageChannel commandResults;

    public UpdateTaskVariableCmdExecutor(TaskAdminRuntime taskAdminRuntime,
                                       	 MessageChannel commandResults) {
        this.taskAdminRuntime = taskAdminRuntime;
        this.commandResults = commandResults;
    }

    @Override
    public String getHandledType() {
        return UpdateTaskVariablePayload.class.getName();
    }

    @Override
    public void execute(UpdateTaskVariablePayload updateTaskVariablePayload) {
        taskAdminRuntime.updateVariable(updateTaskVariablePayload);
        commandResults.send(MessageBuilder.withPayload(new EmptyResult(updateTaskVariablePayload)).build());
    }
}
