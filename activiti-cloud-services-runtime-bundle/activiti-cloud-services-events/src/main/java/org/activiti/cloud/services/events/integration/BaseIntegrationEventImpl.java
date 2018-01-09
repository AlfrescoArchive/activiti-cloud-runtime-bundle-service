/*
 * Copyright 2017 Alfresco, Inc. and/or its affiliates.
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

package org.activiti.cloud.services.events.integration;

import org.activiti.cloud.services.events.AbstractProcessEngineEvent;

public abstract class BaseIntegrationEventImpl extends AbstractProcessEngineEvent implements IntegrationEvent {

    private String integrationContextId;
    private String flowNodeId;

    //used to deserialize from json
    public BaseIntegrationEventImpl() {
    }

    public BaseIntegrationEventImpl(String applicationName,
                                    String executionId,
                                    String processDefinitionId,
                                    String processInstanceId,
                                    String integrationContextId,
                                    String flowNodeId) {
        super(applicationName,
              executionId,
              processDefinitionId,
              processInstanceId);
        this.integrationContextId = integrationContextId;
        this.flowNodeId = flowNodeId;
    }

    @Override
    public String getIntegrationContextId() {
        return integrationContextId;
    }

    @Override
    public String getFlowNodeId() {
        return flowNodeId;
    }

}
