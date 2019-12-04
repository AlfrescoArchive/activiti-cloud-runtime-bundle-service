package org.activiti.cloud.services.rest.controllers;

import org.activiti.api.task.model.payloads.CandidateGroupsPayload;
import org.activiti.api.task.model.payloads.CandidateUsersPayload;
import org.activiti.api.task.runtime.TaskAdminRuntime;
import org.activiti.cloud.api.process.model.impl.CandidateGroup;
import org.activiti.cloud.api.process.model.impl.CandidateUser;
import org.activiti.cloud.services.rest.api.CandidateAdminController;
import org.activiti.cloud.services.rest.assemblers.GroupCandidatesResourceAssembler;
import org.activiti.cloud.services.rest.assemblers.ResourcesAssembler;
import org.activiti.cloud.services.rest.assemblers.ToCandidateGroupConverter;
import org.activiti.cloud.services.rest.assemblers.ToCandidateUserConverter;
import org.activiti.cloud.services.rest.assemblers.UserCandidatesResourceAssembler;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@RestController
public class CandidateAdminControllerImpl implements CandidateAdminController {

    private final TaskAdminRuntime taskAdminRuntime;

    private final UserCandidatesResourceAssembler userCandidatesResourceAssembler;

    private final GroupCandidatesResourceAssembler groupCandidatesResourceAssembler;

    private final ToCandidateUserConverter toCandidateUserConverter;

    private final ToCandidateGroupConverter toCandidateGroupConverter;

    private final ResourcesAssembler resourcesAssembler;

    public CandidateAdminControllerImpl(TaskAdminRuntime taskAdminRuntime,
                                        UserCandidatesResourceAssembler userCandidatesResourceAssembler,
                                        GroupCandidatesResourceAssembler groupCandidatesResourceAssembler,
                                        ToCandidateUserConverter toCandidateUserConverter,
                                        ToCandidateGroupConverter toCandidateGroupConverter,
                                        ResourcesAssembler resourcesAssembler) {
        this.taskAdminRuntime = taskAdminRuntime;
        this.userCandidatesResourceAssembler = userCandidatesResourceAssembler;
        this.groupCandidatesResourceAssembler = groupCandidatesResourceAssembler;
        this.toCandidateUserConverter = toCandidateUserConverter;
        this.toCandidateGroupConverter = toCandidateGroupConverter;
        this.resourcesAssembler = resourcesAssembler;
    }

    @Override
    public void addCandidateUsers(@PathVariable String taskId,
                                  @RequestBody CandidateUsersPayload candidateUsersPayload) {
        if (candidateUsersPayload!=null)
            candidateUsersPayload.setTaskId(taskId);

        taskAdminRuntime.addCandidateUsers(candidateUsersPayload);
    }

    @Override
    public void deleteCandidateUsers(@PathVariable String taskId,
                                     @RequestBody CandidateUsersPayload candidateUsersPayload) {
        if (candidateUsersPayload!=null)
            candidateUsersPayload.setTaskId(taskId);

        taskAdminRuntime.deleteCandidateUsers(candidateUsersPayload);

    }

    @Override
    public Resources<Resource<CandidateUser>> getUserCandidates(@PathVariable String taskId) {
        userCandidatesResourceAssembler.setTaskId(taskId);
        return resourcesAssembler.toResources(toCandidateUserConverter.from(taskAdminRuntime.userCandidates(taskId)),
                                              userCandidatesResourceAssembler,
                                              linkTo(methodOn(this.getClass())
                                                             .getGroupCandidates(userCandidatesResourceAssembler.getTaskId()))
                                                             .withSelfRel());
    }

    @Override
    public void addCandidateGroups(@PathVariable String taskId,
                                   @RequestBody CandidateGroupsPayload candidateGroupsPayload) {
        if (candidateGroupsPayload!=null)
            candidateGroupsPayload.setTaskId(taskId);

        taskAdminRuntime.addCandidateGroups(candidateGroupsPayload);
    }

    @Override
    public void deleteCandidateGroups(@PathVariable String taskId,
                                      @RequestBody CandidateGroupsPayload candidateGroupsPayload) {
        if (candidateGroupsPayload!=null)
            candidateGroupsPayload.setTaskId(taskId);

        taskAdminRuntime.deleteCandidateGroups(candidateGroupsPayload);
    }

    @Override
    public Resources<Resource<CandidateGroup>> getGroupCandidates(@PathVariable String taskId) {
        groupCandidatesResourceAssembler.setTaskId(taskId);
        return resourcesAssembler.toResources(toCandidateGroupConverter.from(taskAdminRuntime.groupCandidates(taskId)),
                                              groupCandidatesResourceAssembler,
                                              linkTo(methodOn(this.getClass())
                                                             .getGroupCandidates(groupCandidatesResourceAssembler.getTaskId()))
                                                             .withSelfRel());
    }


}
