package org.activiti.cloud.services.events;

import org.activiti.cloud.services.api.model.TaskCandidateUser;
import org.activiti.engine.delegate.event.ActivitiEntityEvent;

public interface TaskCandidateUserRemovedEvent extends ActivitiEntityEvent {

    TaskCandidateUser getTaskCandidateUser();
}
