package org.activiti.cloud.starters.test;

import org.activiti.cloud.services.api.events.ProcessEngineEvent;
import org.activiti.cloud.services.api.model.TaskCandidateUser;

public class MockTaskCandidateUserEvent extends MockProcessEngineEvent {
    private TaskCandidateUser taskCandidateUser;

    public MockTaskCandidateUserEvent(Long timestamp, String eventType) {
        super(timestamp,
                eventType);
    }

    public static ProcessEngineEvent[] aTaskCandidateUserAddedEvent(long timestamp,
                                                         TaskCandidateUser taskCandidateUser,
                                                         String processInstanceId) {
        MockTaskCandidateUserEvent taskCreatedEvent = new MockTaskCandidateUserEvent(timestamp,
                "TaskCandidateUserAddedEvent");
        taskCreatedEvent.setTaskCandidateUser(taskCandidateUser);
        taskCreatedEvent.setProcessInstanceId(processInstanceId);
        ProcessEngineEvent[] events = {taskCreatedEvent};
        return events;
    }

    public TaskCandidateUser getTaskCandidateUser() {
        return taskCandidateUser;
    }

    public void setTaskCandidateUser(TaskCandidateUser taskCandidateUser) {
        this.taskCandidateUser = taskCandidateUser;
    }
}
