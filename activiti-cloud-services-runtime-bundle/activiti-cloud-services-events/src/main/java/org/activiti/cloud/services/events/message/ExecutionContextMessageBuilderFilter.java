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

import org.activiti.cloud.api.model.shared.events.CloudRuntimeEvent;
import org.activiti.cloud.services.events.listeners.MessageProducerCommandContextCloseListener;
import org.activiti.engine.impl.context.ExecutionContext;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.DeploymentEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.repository.ProcessDefinition;
import org.springframework.messaging.support.MessageBuilder;

public class ExecutionContextMessageBuilderFilter implements MessageBuilderFilter<CloudRuntimeEvent<?, ?>[]> {

    private final CommandContext commandContext;

    public ExecutionContextMessageBuilderFilter(CommandContext commandContext) {
        this.commandContext = commandContext;
    }

    @Override
    public MessageBuilder<CloudRuntimeEvent<?, ?>[]> apply(MessageBuilder<CloudRuntimeEvent<?, ?>[]> request) {
        ExecutionContext executionContext = commandContext.getGenericAttribute(MessageProducerCommandContextCloseListener.EXECUTION_CONTEXT);

        if(executionContext != null) {
            ExecutionEntity processInstance = executionContext.getProcessInstance();
            ProcessDefinition processDefinition = executionContext.getProcessDefinition();
            DeploymentEntity deploymentEntity = executionContext.getDeployment();

            if(processInstance != null) { 
                request.setHeader("businessKey", processInstance.getBusinessKey())
                    .setHeader("tenantId", processInstance.getTenantId())
                    .setHeader("superExecutionId", processInstance.getSuperExecutionId())
                    .setHeader("processInstanceId", processInstance.getId())
                    .setHeader("processName", processInstance.getName());
            }

            if(processDefinition != null) { 
                request.setHeader("processDefinitionId", processDefinition.getId())
                    .setHeader("processDefinitionKey", processDefinition.getKey())
                    .setHeader("processDefinitionVersion", processDefinition.getVersion())
                    .setHeader("processDefinitionName", processDefinition.getName());
            }

            if(deploymentEntity != null) {
                request.setHeader("deploymentId", deploymentEntity.getId())
                    .setHeader("deploymentName", deploymentEntity.getName());
            }
            
            if (processInstance.getSuperExecution() != null) {
                request.setHeader("superExecutionName", processInstance.getSuperExecution().getName());
            }
        }

        return request;

    }

}
