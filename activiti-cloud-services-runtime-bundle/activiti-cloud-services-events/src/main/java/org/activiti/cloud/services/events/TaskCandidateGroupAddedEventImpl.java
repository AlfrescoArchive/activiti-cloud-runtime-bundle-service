package org.activiti.cloud.services.events;

import org.activiti.cloud.services.api.model.TaskCandidateGroup;
import org.activiti.engine.delegate.event.ActivitiEventType;

public class TaskCandidateGroupAddedEventImpl extends AbstractProcessEngineEvent implements TaskCandidateGroupAddedEvent {

    private TaskCandidateGroup taskCandidateGroup;

    public TaskCandidateGroupAddedEventImpl() {
    }

    public TaskCandidateGroupAddedEventImpl(String applicationName,
                                            String executionId,
                                            String processDefinitionId,
                                            String processInstanceId,
                                            TaskCandidateGroup taskCandidateGroup) {
        super(applicationName,
                executionId,
                processDefinitionId,
                processInstanceId);
        this.taskCandidateGroup = taskCandidateGroup;
    }

    public TaskCandidateGroup getTaskCandidateGroup() {
        return taskCandidateGroup;
    }

    @Override
    public String getEventType() {
        return "TaskCandidateGroupAddedEvent";
    }

    @Override
    public Object getEntity() {
        return taskCandidateGroup;
    }

    @Override
    public ActivitiEventType getType() {
        return ActivitiEventType.ENTITY_CREATED;
    }
}
