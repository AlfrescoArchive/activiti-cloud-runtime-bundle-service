package org.activiti.cloud.services.rest.assemblers;

import java.util.ArrayList;
import java.util.List;

import org.activiti.cloud.services.rest.controllers.TaskControllerImpl;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceAssembler;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

public class UserCandidatesResourceAssembler implements ResourceAssembler<String, Resource<String>>{

    private String taskId;

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    @Override
    public Resource<String> toResource(String userCandidates) {

        List<Link> links = new ArrayList<>();
        links.add(linkTo(methodOn(TaskControllerImpl.class).getUserCandidates(taskId)).withSelfRel());

        return new Resource<>(userCandidates, links);
    }
}
