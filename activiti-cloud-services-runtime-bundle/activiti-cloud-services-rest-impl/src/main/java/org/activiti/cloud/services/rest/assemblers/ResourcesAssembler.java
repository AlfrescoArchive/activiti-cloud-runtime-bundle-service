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

package org.activiti.cloud.services.rest.assemblers;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.activiti.cloud.services.rest.controllers.TaskControllerImpl;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceAssembler;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.Resources;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

public class ResourcesAssembler {

    public <T, D extends ResourceSupport> Resources<D> toResources(List<T> entities,
                                                                   ResourceAssembler<T, D> resourceAssembler) {
        List<Link> links = new ArrayList<>();

        if (resourceAssembler instanceof UserCandidatesResourceAssembler) {
            links.add(linkTo(methodOn(TaskControllerImpl.class)
                                     .getGroupCandidates(
                                             ((UserCandidatesResourceAssembler) resourceAssembler).getTaskId()
                                     )).withSelfRel());

        } else if (resourceAssembler instanceof GroupCandidatesResourceAssembler) {
            links.add(linkTo(methodOn(TaskControllerImpl.class)
                                     .getGroupCandidates(
                                             ((GroupCandidatesResourceAssembler) resourceAssembler).getTaskId()
                                     )).withSelfRel());
        }

        return new Resources<>(entities.stream()
                                       .map(resourceAssembler::toResource)
                                       .collect(Collectors.toList()),
                               links);
    }
}
