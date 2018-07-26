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

package org.activiti.cloud.services.core.pageable;

import org.activiti.cloud.services.common.security.SpringSecurityAuthenticationWrapper;
import org.activiti.runtime.api.TaskRuntime;
import org.activiti.runtime.api.model.Task;
import org.activiti.runtime.api.model.builders.TaskPayloadBuilder;
import org.activiti.runtime.api.model.payloads.ClaimTaskPayload;
import org.activiti.runtime.api.model.payloads.DeleteTaskPayload;
import org.activiti.runtime.api.model.payloads.ReleaseTaskPayload;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class SecurityAwareTaskServiceTest {

    @InjectMocks
    private SecurityAwareTaskService taskService;

    @Mock
    private TaskRuntime taskRuntime;

    @Mock
    private SpringSecurityAuthenticationWrapper authenticationWrapper;

    @Before
    public void setUp() {
        initMocks(this);
        given(authenticationWrapper.getAuthenticatedUserId()).willReturn("joan");
    }

    /**
     * Test that delete task method on process engine wrapper
     * will trigger delete task method on process engine
     * if the task exists.
     */
    @Test
    public void deleteTaskShouldCallDeleteOnFluentTask() {
        //GIVEN
        Task task = mock(Task.class);
        given(taskRuntime.task("taskId")).willReturn(task);

        //WHEN
        DeleteTaskPayload deleteTaskPayload = TaskPayloadBuilder.delete().withTaskId("taskId").build();
        taskService.deleteTask(deleteTaskPayload);

        //THEN
        verify(taskRuntime).delete(deleteTaskPayload);
    }

    @Test
    public void claimShouldCallClaimOnFluentTask() {
        //given
        Task task = mock(Task.class);
        given(taskRuntime.task("taskId")).willReturn(task);

        ClaimTaskPayload claimTaskPayload = TaskPayloadBuilder.claim().withTaskId("taskId").withAssignee("user").build();
        //when
        taskService.claimTask(claimTaskPayload);

        //
        verify(taskRuntime).claim(claimTaskPayload);
    }

    @Test
    public void releaseTaskShouldClearAssignee() {
        //given
        Task task = mock(Task.class);
        given(taskRuntime.task("taskId")).willReturn(task);

        //when
        ReleaseTaskPayload releaseTaskPayload = TaskPayloadBuilder.release().withTaskId("taskId").build();

        taskService.releaseTask(releaseTaskPayload);

        //then
        verify(taskRuntime).release(releaseTaskPayload);
    }

//    @Test
//    public void completeTaskShouldCallCompleteOnFluentTask() {
//        Task task = mock(Task.class);
//        given(taskRuntime.task("taskId")).willReturn(task);
//
//        CompleteTaskPayload completeTaskPayload = mock(CompleteTaskPayload.class,
//                                                       Answers.RETURNS_SELF);
//        doReturn(null).when(completeTaskPayload).doIt();
//        given(task.completeWith()).willReturn(completeTaskPayload);
//
//        Map<String, Object> variables = Collections.singletonMap("name",
//                                                                 "paul");
//
//        //when
//        taskService.completeTask(new CompleteTaskImpl("taskId",
//                                                      variables));
//        verify(completeTaskPayload).variables(variables);
//        verify(completeTaskPayload).doIt();
//    }
//
//    @Test
//    public void setTaskVariablesShouldSetVariablesOnFluentTask() {
//        //given
//        SetTaskVariablesImpl setTaskVariablesCmd = new SetTaskVariablesImpl("taskId",
//                                                                            Collections.singletonMap("name",
//                                                                                                     "john"));
//        Task task = mock(Task.class);
//        given(taskRuntime.task(setTaskVariablesCmd.getTaskId())).willReturn(task);
//
//        //when
//        taskService.setTaskVariables(setTaskVariablesCmd);
//
//        //then
//        verify(task).variables(setTaskVariablesCmd.getVariables());
//    }
//
//    @Test
//    public void shouldSetTaskVariablesLocal() {
//        //given
//        SetTaskVariablesImpl cmd = new SetTaskVariablesImpl("taskId",
//                                                            Collections.singletonMap("local",
//                                                                                     "myLocalVar"));
//
//        Task task = mock(Task.class);
//        given(taskRuntime.task(cmd.getTaskId())).willReturn(task);
//
//        //when
//        taskService.setTaskVariablesLocal(cmd);
//
//        //when
//        verify(task).localVariables(cmd.getVariables());
//    }
}