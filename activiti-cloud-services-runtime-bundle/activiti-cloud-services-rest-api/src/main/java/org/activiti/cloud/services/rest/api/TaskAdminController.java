package org.activiti.cloud.services.rest.api;

import org.activiti.api.task.model.payloads.ClaimTaskPayload;
import org.activiti.api.task.model.payloads.CompleteTaskPayload;
import org.activiti.api.task.model.payloads.DeleteTaskPayload;
import org.activiti.api.task.model.payloads.ReleaseTaskPayload;
import org.activiti.api.task.model.payloads.SetTaskVariablesPayload;
import org.activiti.cloud.services.rest.api.resources.TaskResource;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedResources;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@RequestMapping(value = "/admin/v1/tasks", produces = {MediaTypes.HAL_JSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
public interface TaskAdminController {

    @RequestMapping(method = RequestMethod.GET)
    PagedResources<TaskResource> getAllTasks(Pageable pageable);

    @RequestMapping(value = "/{taskId}", method = RequestMethod.GET)
    TaskResource getTaskById(@PathVariable String taskId);

    @RequestMapping(value = "/{taskId}/claim", method = RequestMethod.POST)
    TaskResource claimTask(@PathVariable String taskId,
                           @RequestBody ClaimTaskPayload claimTaskPayload);

    @RequestMapping(value = "/{taskId}/release", method = RequestMethod.POST)
    TaskResource releaseTask(@PathVariable String taskId,
                             @RequestBody ReleaseTaskPayload releaseTaskPayload);

    @RequestMapping(value = "/{taskId}/complete", method = RequestMethod.POST)
    TaskResource completeTask(@PathVariable String taskId,
                              @RequestBody CompleteTaskPayload completeTaskPayload);

    @RequestMapping(value = "/{taskId}", method = RequestMethod.DELETE)
    TaskResource delete(@PathVariable String taskId,
                        @RequestBody DeleteTaskPayload deleteTaskPayload);

    @RequestMapping(value = "/{taskId}/variables", method = RequestMethod.POST)
    ResponseEntity<Void> setVariables(@PathVariable String taskId,
                                      @RequestBody SetTaskVariablesPayload setVariablesPayload);

}
