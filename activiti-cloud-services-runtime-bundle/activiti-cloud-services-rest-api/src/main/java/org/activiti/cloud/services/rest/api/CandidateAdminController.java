package org.activiti.cloud.services.rest.api;

import org.activiti.api.task.model.payloads.CandidateGroupsPayload;
import org.activiti.api.task.model.payloads.CandidateUsersPayload;
import org.activiti.cloud.api.process.model.impl.CandidateGroup;
import org.activiti.cloud.api.process.model.impl.CandidateUser;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@RequestMapping(value = "/admin/v1/tasks/{taskId}",
        produces = {MediaTypes.HAL_JSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
public interface CandidateAdminController {

    @RequestMapping(value = "/candidate-users", method = RequestMethod.POST)
    void addCandidateUsers(@PathVariable("taskId") String taskId,
                           @RequestBody CandidateUsersPayload candidateUsersPayload);

    @RequestMapping(value = "/candidate-users", method = RequestMethod.DELETE)
    void deleteCandidateUsers(@PathVariable("taskId") String taskId,
                              @RequestBody CandidateUsersPayload candidateUsersPayload);

    @RequestMapping(value = "/candidate-users", method = RequestMethod.GET)
    Resources<Resource<CandidateUser>> getUserCandidates(@PathVariable("taskId") String taskId);

    @RequestMapping(value = "/candidate-groups", method = RequestMethod.POST)
    void addCandidateGroups(@PathVariable("taskId") String taskId,
                            @RequestBody CandidateGroupsPayload candidateGroupsPayload);

    @RequestMapping(value = "/candidate-groups", method = RequestMethod.DELETE)
    void deleteCandidateGroups(@PathVariable("taskId") String taskId,
                               @RequestBody CandidateGroupsPayload candidateGroupsPayload);

    @RequestMapping(value = "/candidate-groups", method = RequestMethod.GET)
    Resources<Resource<CandidateGroup>> getGroupCandidates(@PathVariable("taskId") String taskId);
}
