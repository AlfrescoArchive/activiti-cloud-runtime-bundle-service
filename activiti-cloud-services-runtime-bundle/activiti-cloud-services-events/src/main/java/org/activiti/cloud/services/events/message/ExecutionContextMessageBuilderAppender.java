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

import org.activiti.cloud.services.events.listeners.MessageProducerCommandContextCloseListener;
import org.activiti.engine.impl.context.ExecutionContext;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.DeploymentEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.repository.ProcessDefinition;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.Assert;

public class ExecutionContextMessageBuilderAppender implements MessageBuilderAppender {

    private final CommandContext commandContext;

    public ExecutionContextMessageBuilderAppender(CommandContext commandContext) {
        this.commandContext = commandContext;
    }

    @Override
    public <P> MessageBuilder<P> apply(MessageBuilder<P> request) {
        Assert.notNull(request, "request must not be null");
        
        if(commandContext == null)
            return request;
        
        ExecutionContext executionContext = commandContext
                .getGenericAttribute(MessageProducerCommandContextCloseListener.EXECUTION_CONTEXT);

        if(executionContext != null) {
            ExecutionEntity processInstance = executionContext.getProcessInstance();
            ProcessDefinition processDefinition = executionContext.getProcessDefinition();
            DeploymentEntity deploymentEntity = executionContext.getDeployment();

            if(processInstance != null) { 
                request.setHeader(CloudRuntimeEventMessageHeaders.BUSINESS_KEY, processInstance.getBusinessKey())
                    .setHeader(CloudRuntimeEventMessageHeaders.TENANT_ID, processInstance.getTenantId())
                    .setHeader(CloudRuntimeEventMessageHeaders.SUPER_EXECUTION_ID, processInstance.getSuperExecutionId())
                    .setHeader(CloudRuntimeEventMessageHeaders.PROCESS_INSTANCE_ID, processInstance.getId())
                    .setHeader(CloudRuntimeEventMessageHeaders.PROCESS_NAME, processInstance.getName());

                if (processInstance.getSuperExecution() != null) {
                    request.setHeader(CloudRuntimeEventMessageHeaders.SUPER_EXECUTION_NAME, processInstance.getSuperExecution().getName());
                }
            }

            if(processDefinition != null) { 
                request.setHeader(CloudRuntimeEventMessageHeaders.PROCESS_DEFINITION_ID, processDefinition.getId())
                    .setHeader(CloudRuntimeEventMessageHeaders.PROCESS_DEFINITION_KEY, processDefinition.getKey())
                    .setHeader(CloudRuntimeEventMessageHeaders.PROCESS_DEFINITION_VERSION, processDefinition.getVersion())
                    .setHeader(CloudRuntimeEventMessageHeaders.PROCESS_DEFINITION_NAME, processDefinition.getName());
            }

            if(deploymentEntity != null) {
                request.setHeader(CloudRuntimeEventMessageHeaders.DEPLOYMENT_ID, deploymentEntity.getId())
                    .setHeader(CloudRuntimeEventMessageHeaders.DEPLOYMENT_NAME, deploymentEntity.getName());
            }
            
        }

        return request;

    }

}
