package org.activiti.cloud.services.events;

import org.activiti.cloud.services.api.model.TaskCandidateUser;
import org.activiti.engine.delegate.event.ActivitiEventType;

public class TaskCandidateUserAddedEventImpl extends AbstractProcessEngineEvent implements TaskCandidateUserAddedEvent {

    private TaskCandidateUser taskCandidateUser;

    public TaskCandidateUserAddedEventImpl() {
    }

    public TaskCandidateUserAddedEventImpl(String applicationName,
                                           String executionId,
                                           String processDefinitionId,
                                           String processInstanceId,
                                           TaskCandidateUser taskCandidateUser) {
        super(applicationName,
                executionId,
                processDefinitionId,
                processInstanceId);
        this.taskCandidateUser = taskCandidateUser;
    }

    public TaskCandidateUser getTaskCandidateUser() {
        return taskCandidateUser;
    }

    @Override
    public String getEventType() {
        return "TaskCandidateUserAddedEvent";
    }

    @Override
    public Object getEntity() {
        return taskCandidateUser;
    }

    @Override
    public ActivitiEventType getType() {
        return ActivitiEventType.ENTITY_CREATED;
    }
}
