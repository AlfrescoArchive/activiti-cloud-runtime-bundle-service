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
import java.util.stream.Collectors;

import org.activiti.api.task.model.Task;
import org.activiti.cloud.api.task.model.CloudTask;
import org.activiti.cloud.starter.tests.helper.TaskRestTemplate;
import org.activiti.steps.TaskProvider;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.hateoas.PagedResources;
import org.springframework.http.ResponseEntity;

@TestComponent
public class RestTemplateTaskProvider implements TaskProvider {

  private TaskRestTemplate taskRestTemplate;

    public RestTemplateTaskProvider(TaskRestTemplate taskRestTemplate) {
        this.taskRestTemplate = taskRestTemplate;
    }

    @Override
    public List<Task> getTasks(String processInstanceId) {
        ResponseEntity<PagedResources<CloudTask>> tasks = taskRestTemplate.getTasks();

        return tasks.getBody().getContent()
                .stream()
                .filter(task -> processInstanceId.equals(task.getProcessInstanceId()))
                .collect(Collectors.toList());
    }

    @Override
    public boolean canHandle(Task.TaskStatus taskStatus) {
        switch (taskStatus) {
            case CREATED:
            case ASSIGNED:
            case SUSPENDED:
                return true;
        }
        return false;
    }
}
