package org.activiti.cloud.services.events;

import org.activiti.cloud.services.api.model.TaskCandidateGroup;
import org.activiti.engine.delegate.event.ActivitiEntityEvent;

public interface TaskCandidateGroupRemovedEvent extends ActivitiEntityEvent {

    TaskCandidateGroup getTaskCandidateGroup();
}
