package org.activiti.cloud.services.rest.assemblers;

import java.util.ArrayList;
import java.util.List;

import org.activiti.cloud.services.rest.controllers.TaskControllerImpl;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceAssembler;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

public class GroupCandidatesResourceAssembler implements ResourceAssembler<String, Resource<String>> {

    private String taskId;

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTaskId() {
        return taskId;
    }

    @Override
    public Resource<String> toResource(String groupCandidates) {
        return new Resource<>(groupCandidates);
    }
}
