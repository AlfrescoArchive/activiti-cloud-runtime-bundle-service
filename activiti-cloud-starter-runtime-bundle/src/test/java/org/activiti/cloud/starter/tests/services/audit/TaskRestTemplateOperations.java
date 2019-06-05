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

package org.activiti.cloud.starter.tests.services.audit;

import java.util.List;

import org.activiti.api.task.model.payloads.ClaimTaskPayload;
import org.activiti.api.task.model.payloads.CompleteTaskPayload;
import org.activiti.cloud.api.task.model.CloudTask;
import org.activiti.cloud.starter.tests.helper.TaskRestTemplate;
import org.activiti.steps.EventProvider;
import org.activiti.steps.TaskProvider;
import org.activiti.steps.assertions.TaskAssertions;
import org.activiti.steps.assertions.TaskAssertionsImpl;
import org.activiti.steps.operations.TaskOperations;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.http.ResponseEntity;

@TestComponent
public class TaskRestTemplateOperations implements TaskOperations {

    private TaskRestTemplate taskRestTemplate;
    private EventProvider eventProvider;
    private List<TaskProvider> taskProviders;

    public TaskRestTemplateOperations(TaskRestTemplate taskRestTemplate,
                                      EventProvider eventProvider,
                                      List<TaskProvider> taskProviders) {
        this.taskRestTemplate = taskRestTemplate;
        this.eventProvider = eventProvider;
        this.taskProviders = taskProviders;
    }

    @Override
    public TaskAssertions claim(ClaimTaskPayload claimTaskPayload) {
        ResponseEntity<CloudTask> taskResponseEntity = taskRestTemplate.claim(claimTaskPayload.getTaskId());
        return new TaskAssertionsImpl(taskResponseEntity.getBody(), taskProviders, eventProvider);
    }

    @Override
    public TaskAssertions complete(CompleteTaskPayload completeTaskPayload) {
        ResponseEntity<CloudTask> responseEntity = taskRestTemplate.complete(completeTaskPayload.getTaskId(),
                                                                       completeTaskPayload);
        return new TaskAssertionsImpl(responseEntity.getBody(), taskProviders, eventProvider);
    }

}
