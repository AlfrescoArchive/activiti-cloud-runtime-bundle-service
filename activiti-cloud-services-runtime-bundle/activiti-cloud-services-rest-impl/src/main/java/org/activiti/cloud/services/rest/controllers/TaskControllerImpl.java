/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.activiti.cloud.services.rest.controllers;

import org.activiti.api.runtime.shared.query.Page;
import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.builders.TaskPayloadBuilder;
import org.activiti.api.task.model.payloads.CandidateGroupsPayload;
import org.activiti.api.task.model.payloads.CandidateUsersPayload;
import org.activiti.api.task.model.payloads.CompleteTaskPayload;
import org.activiti.api.task.model.payloads.CreateTaskPayload;
import org.activiti.api.task.model.payloads.SaveTaskPayload;
import org.activiti.api.task.model.payloads.UpdateTaskPayload;
import org.activiti.api.task.runtime.TaskRuntime;
import org.activiti.cloud.alfresco.data.domain.AlfrescoPagedResourcesAssembler;
import org.activiti.cloud.api.process.model.impl.CandidateGroup;
import org.activiti.cloud.api.process.model.impl.CandidateUser;
import org.activiti.cloud.api.task.model.CloudTask;
import org.activiti.cloud.services.core.pageable.SpringPageConverter;
import org.activiti.cloud.services.rest.api.TaskController;
import org.activiti.cloud.services.rest.assemblers.GroupCandidatesResourceAssembler;
import org.activiti.cloud.services.rest.assemblers.ResourcesAssembler;
import org.activiti.cloud.services.rest.assemblers.TaskResourceAssembler;
import org.activiti.cloud.services.rest.assemblers.ToCandidateGroupConverter;
import org.activiti.cloud.services.rest.assemblers.ToCandidateUserConverter;
import org.activiti.cloud.services.rest.assemblers.UserCandidatesResourceAssembler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TaskControllerImpl implements TaskController {

    private final TaskResourceAssembler taskResourceAssembler;

    private final AlfrescoPagedResourcesAssembler<Task> pagedResourcesAssembler;

    private final SpringPageConverter pageConverter;

    private final TaskRuntime taskRuntime;

    private final UserCandidatesResourceAssembler userCandidatesResourceAssembler;

    private final GroupCandidatesResourceAssembler groupCandidatesResourceAssembler;

    private final ResourcesAssembler resourcesAssembler;

    private final ToCandidateUserConverter toCandidateUserConverter;

    private final ToCandidateGroupConverter toCandidateGroupConverter;

    @Autowired
    public TaskControllerImpl(TaskResourceAssembler taskResourceAssembler,
                              AlfrescoPagedResourcesAssembler<Task> pagedResourcesAssembler,
                              SpringPageConverter pageConverter,
                              TaskRuntime taskRuntime,
                              UserCandidatesResourceAssembler userCandidatesResourceAssembler,
                              GroupCandidatesResourceAssembler groupCandidatesResourceAssembler,
                              ResourcesAssembler resourcesAssembler,
                              ToCandidateUserConverter toCandidateUserConverter,
                              ToCandidateGroupConverter toCandidateGroupConverter) {
        this.taskResourceAssembler = taskResourceAssembler;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
        this.pageConverter = pageConverter;
        this.taskRuntime = taskRuntime;
        this.userCandidatesResourceAssembler = userCandidatesResourceAssembler;
        this.groupCandidatesResourceAssembler = groupCandidatesResourceAssembler;
        this.resourcesAssembler = resourcesAssembler;
        this.toCandidateUserConverter = toCandidateUserConverter;
        this.toCandidateGroupConverter =  toCandidateGroupConverter;
    }

    @Override
    public PagedResources<Resource<CloudTask>> getTasks(Pageable pageable) {
        Page<Task> taskPage = taskRuntime.tasks(pageConverter.toAPIPageable(pageable));
        return pagedResourcesAssembler.toResource(pageable,
                                                  pageConverter.toSpringPage(pageable,
                                                                             taskPage),
                                                  taskResourceAssembler);
    }

    @Override
    public Resource<CloudTask> getTaskById(@PathVariable String taskId) {
        Task task = taskRuntime.task(taskId);
        return taskResourceAssembler.toResource(task);
    }

    @Override
    public Resource<CloudTask> claimTask(@PathVariable String taskId) {
        return taskResourceAssembler.toResource(
                taskRuntime.claim(
                        TaskPayloadBuilder.claim()
                                .withTaskId(taskId)
                                .build()));
    }

    @Override
    public Resource<CloudTask> releaseTask(@PathVariable String taskId) {

        return taskResourceAssembler.toResource(taskRuntime.release(TaskPayloadBuilder
                                                                            .release()
                                                                            .withTaskId(taskId)
                                                                            .build()));
    }

    @Override
    public Resource<CloudTask> completeTask(@PathVariable String taskId,
                                     @RequestBody(required = false) CompleteTaskPayload completeTaskPayload) {
        if (completeTaskPayload == null) {
            completeTaskPayload = TaskPayloadBuilder
                    .complete()
                    .withTaskId(taskId)
                    .build();
        } else {
            completeTaskPayload.setTaskId(taskId);
        }

        Task task = taskRuntime.complete(completeTaskPayload);
        return taskResourceAssembler.toResource(task);
    }

    @Override
    public Resource<CloudTask> deleteTask(@PathVariable String taskId) {
        Task task = taskRuntime.delete(TaskPayloadBuilder
                                                                .delete()
                                                                .withTaskId(taskId)
                                                                .build());
        return taskResourceAssembler.toResource(task);
    }

    @Override
    public Resource<CloudTask> createNewTask(@RequestBody CreateTaskPayload createTaskPayload) {
        return taskResourceAssembler.toResource(taskRuntime.create(createTaskPayload));
    }

    @Override
    public Resource<CloudTask> updateTask(@PathVariable String taskId,
                                   @RequestBody UpdateTaskPayload updateTaskPayload) {
        if (updateTaskPayload != null) {
            updateTaskPayload.setTaskId(taskId);
        }
        return taskResourceAssembler.toResource(taskRuntime.update(updateTaskPayload));
    }

    @Override
    public PagedResources<Resource<CloudTask>> getSubtasks(Pageable pageable,
                                                    @PathVariable String taskId) {
        Page<Task> taskPage = taskRuntime
                .tasks(pageConverter.toAPIPageable(pageable),
                       TaskPayloadBuilder
                               .tasks()
                               .withParentTaskId(taskId)
                               .build());

        return pagedResourcesAssembler.toResource(pageable,
                                                  pageConverter.toSpringPage(pageable,
                                                                             taskPage),
                                                  taskResourceAssembler);
    }


    @Override
    public void addCandidateUsers(@PathVariable String taskId,
                                  @RequestBody CandidateUsersPayload candidateUsersPayload) {
        if (candidateUsersPayload!=null)
            candidateUsersPayload.setTaskId(taskId);

        taskRuntime.addCandidateUsers(candidateUsersPayload);
    }

    @Override
    public void deleteCandidateUsers(@PathVariable String taskId,
                              @RequestBody CandidateUsersPayload candidateUsersPayload) {
        if (candidateUsersPayload!=null)
            candidateUsersPayload.setTaskId(taskId);

        taskRuntime.deleteCandidateUsers(candidateUsersPayload);

    }

    @Override
    public Resources<Resource<CandidateUser>> getUserCandidates(@PathVariable String taskId) {
        userCandidatesResourceAssembler.setTaskId(taskId);
        return resourcesAssembler.toResources(toCandidateUserConverter.from(taskRuntime.userCandidates(taskId)),
                                              userCandidatesResourceAssembler);
    }

    @Override
    public void addCandidateGroups(@PathVariable String taskId,
                                   @RequestBody CandidateGroupsPayload candidateGroupsPayload) {
        if (candidateGroupsPayload!=null)
            candidateGroupsPayload.setTaskId(taskId);

        taskRuntime.addCandidateGroups(candidateGroupsPayload);
    }

    @Override
    public void deleteCandidateGroups(@PathVariable String taskId,
                                      @RequestBody CandidateGroupsPayload candidateGroupsPayload) {
        if (candidateGroupsPayload!=null)
            candidateGroupsPayload.setTaskId(taskId);

        taskRuntime.deleteCandidateGroups(candidateGroupsPayload);
    }

    @Override
    public Resources<Resource<CandidateGroup>> getGroupCandidates(@PathVariable String taskId) {
        groupCandidatesResourceAssembler.setTaskId(taskId);
        return resourcesAssembler.toResources(toCandidateGroupConverter.from(taskRuntime.groupCandidates(taskId)),
                                              groupCandidatesResourceAssembler);
    }

    @Override
    public void saveTask(@PathVariable String taskId,
                         @RequestBody SaveTaskPayload saveTaskPayload) {
        if (saveTaskPayload != null) {
            saveTaskPayload.setTaskId(taskId);
        }
        
        taskRuntime.save(saveTaskPayload);
    }
}
