/*
 * Copyright 2018 Alfresco, Inc. and/or its affiliates.
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
package org.activiti.cloud.services.events.message;

import java.util.Collection;

import org.activiti.cloud.api.model.shared.events.CloudRuntimeEvent;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.springframework.messaging.support.MessageBuilder;

public class CommandContextMessageBuilderFilter implements MessageBuilderFilter<CloudRuntimeEvent<?, ?>[]> {

    private final CommandContext commandContext;

    public CommandContextMessageBuilderFilter(CommandContext commandContext) {
        this.commandContext = commandContext;
    }

    @Override
    public MessageBuilder<CloudRuntimeEvent<?, ?>[]> apply(MessageBuilder<CloudRuntimeEvent<?, ?>[]> request) {
        Collection<ExecutionEntity> executions = commandContext.getInvolvedExecutions();

        if (!executions.isEmpty()) {
            ExecutionEntity processInstance = executions.iterator().next().getProcessInstance();

            if(processInstance != null) {
                request.setHeader("businessKey", processInstance.getBusinessKey())
                    .setHeader("processDefinitionId", processInstance.getProcessDefinitionId())
                    .setHeader("processDefinitionKey", processInstance.getProcessDefinitionKey())
                    .setHeader("tenantId", processInstance.getTenantId())
                    .setHeader("processDefinitionVersion", processInstance.getProcessDefinitionVersion())
                    .setHeader("processDefinitionName", processInstance.getProcessDefinitionName())
                    .setHeader("superExecutionId", processInstance.getSuperExecutionId())
                    .setHeader("processInstanceId", processInstance.getId())
                    .setHeader("processName", processInstance.getName());
    
                if (processInstance.getSuperExecution() != null) {
                    request.setHeader("superExecutionName", processInstance.getSuperExecution().getName());
                }
            }
        }

        return request;

    }

}
