/*
 * Copyright 2017 Alfresco, Inc. and/or its affiliates.
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

package org.activiti.cloud.services.rest.controllers;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.activiti.api.runtime.shared.query.Page;
import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.builders.TaskPayloadBuilder;
import org.activiti.api.task.runtime.TaskAdminRuntime;
import org.activiti.cloud.services.core.pageable.SpringPageConverter;
import org.activiti.cloud.services.events.ProcessEngineChannels;
import org.activiti.cloud.services.events.configuration.CloudEventsAutoConfiguration;
import org.activiti.cloud.services.events.configuration.RuntimeBundleProperties;
import org.activiti.cloud.services.rest.conf.ServicesRestAutoConfiguration;
import org.activiti.runtime.api.query.impl.PageImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.activiti.alfresco.rest.docs.AlfrescoDocumentation.pageRequestParameters;
import static org.activiti.alfresco.rest.docs.AlfrescoDocumentation.pagedResourcesResponseFields;
import static org.activiti.alfresco.rest.docs.AlfrescoDocumentation.taskFields;
import static org.activiti.alfresco.rest.docs.AlfrescoDocumentation.taskIdParameter;
import static org.activiti.cloud.services.rest.controllers.TaskSamples.buildDefaultAssignedTask;
import static org.activiti.cloud.services.rest.controllers.TaskSamples.buildStandAloneTask;
import static org.activiti.cloud.services.rest.controllers.TaskSamples.buildTask;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = TaskAdminControllerImpl.class, secure = true)
@EnableSpringDataWebSupport
@AutoConfigureMockMvc(secure = false)
@AutoConfigureRestDocs(outputDir = "target/snippets")
@Import({RuntimeBundleProperties.class,
        CloudEventsAutoConfiguration.class,
        ServicesRestAutoConfiguration.class})
@ComponentScan(basePackages = {"org.activiti.cloud.services.rest.assemblers", "org.activiti.cloud.alfresco"})
public class TaskAdminControllerImplIT {

    private static final String DOCUMENTATION_IDENTIFIER = "task-admin";
    private static final String DOCUMENTATION_IDENTIFIER_ALFRESCO = "task-admin-alfresco";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private TaskAdminRuntime taskAdminRuntime;

    @SpyBean
    private SpringPageConverter pageConverter;

    @MockBean
    private ProcessEngineChannels processEngineChannels;

    @Before
    public void setUp() {
        assertThat(pageConverter).isNotNull();
        assertThat(processEngineChannels).isNotNull();
    }

    @Test
    public void getTasks() throws Exception {

        List<Task> taskList = Collections.singletonList(buildDefaultAssignedTask());
        Page<Task> tasks = new PageImpl<>(taskList,
                                          taskList.size());
        when(taskAdminRuntime.tasks(any())).thenReturn(tasks);

        this.mockMvc.perform(get("/admin/v1/tasks"))
                .andExpect(status().isOk())
                .andDo(document(DOCUMENTATION_IDENTIFIER + "/list",
                                responseFields(subsectionWithPath("page").description("Pagination details."),
                                               subsectionWithPath("_links").description("The hypermedia links."),
                                               subsectionWithPath("_embedded").description("The process definitions."))));
    }

    @Test
    public void getTasksShouldUseAlfrescoGuidelineWhenMediaTypeIsApplicationJson() throws Exception {
        List<Task> taskList = Collections.singletonList(buildDefaultAssignedTask());
        Page<Task> taskPage = new PageImpl<>(taskList,
                                             taskList.size());
        when(taskAdminRuntime.tasks(any())).thenReturn(taskPage);

        this.mockMvc.perform(get("/admin/v1/tasks?skipCount=10&maxItems=10").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document(DOCUMENTATION_IDENTIFIER_ALFRESCO + "/list",
                                pageRequestParameters(),
                                pagedResourcesResponseFields()));
    }

    @Test
    public void getTaskById() throws Exception {
        when(taskAdminRuntime.task("task1")).thenReturn(buildStandAloneTask("task1",
                                                                            "task1 description"));
        this.mockMvc.perform(get("/admin/v1/tasks/{taskId}",
                                 "task1"))
                .andExpect(status().isOk())
                .andDo(document(DOCUMENTATION_IDENTIFIER + "/task",
                                taskIdParameter(),
                                links(
                                        linkWithRel("claim").optional().description("The task's claim option."),
                                        linkWithRel("self").optional().description("The task's resource."),
                                        linkWithRel("home").optional().description("Home resource")
                                )));
    }

    @Test
    public void getTaskByIdShouldUseAlfrescoGuidelineWhenMediaTypeIsApplicationJson() throws Exception {
        when(taskAdminRuntime.task("task1")).thenReturn(buildStandAloneTask("task1",
                                                                            "task1 description"));
        this.mockMvc.perform(get("/admin/v1/tasks/{taskId}",
                                 "task1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document(DOCUMENTATION_IDENTIFIER_ALFRESCO + "/task",
                                taskIdParameter(),
                                taskFields()
                ));
    }

    @Test
    public void claimTask() throws Exception {
        Task claimedTask = buildTask(Task.TaskStatus.ASSIGNED,
                                     "task to be claimed",
                                     "user");

        when(taskAdminRuntime.claim(any())).thenReturn(claimedTask);
        this.mockMvc.perform(post("/admin/v1/tasks/{taskId}/claim",
                                  claimedTask.getId())
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(mapper.writeValueAsString(TaskPayloadBuilder
                                                                                .claim()
                                                                                .withTaskId(claimedTask.getId())
                                                                                .withAssignee(claimedTask.getAssignee())
                                                                                .build()
                                     )))
                .andExpect(status().isOk())
                .andDo(document(DOCUMENTATION_IDENTIFIER + "/task/claim",
                                taskIdParameter(),
                                links(
                                        linkWithRel("self").description("The task's resource."),
                                        linkWithRel("release").description("The task's release option."),
                                        linkWithRel("complete").description("The task's complete option."),
                                        linkWithRel("processInstance").description("The task's option to get the process instance associated with."),
                                        linkWithRel("home").description("Home resource")
                                )));
    }

    @Test
    public void claimTaskShouldUseAlfrescoGuidelineWhenMediaTypeIsApplicationJson() throws Exception {
        Task claimedTask = buildTask(Task.TaskStatus.ASSIGNED,
                                     "task to be claimed",
                                     "user");

        when(taskAdminRuntime.claim(any())).thenReturn(claimedTask);
        this.mockMvc.perform(post("/admin/v1/tasks/{taskId}/claim",
                                  claimedTask.getId())
                                     .accept(MediaType.APPLICATION_JSON)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(mapper.writeValueAsString(TaskPayloadBuilder
                                                                                .claim()
                                                                                .withTaskId(claimedTask.getId())
                                                                                .withAssignee(claimedTask.getAssignee())
                                                                                .build()
                                     )))
                .andExpect(status().isOk())
                .andDo(document(DOCUMENTATION_IDENTIFIER_ALFRESCO + "/task/claim",
                                taskIdParameter(),
                                taskFields()));
    }

    @Test
    public void releaseTask() throws Exception {
        Task released = buildTask("task to be released",
                                  Task.TaskStatus.CREATED);
        when(taskAdminRuntime.release(any())).thenReturn(released);

        this.mockMvc.perform(post("/admin/v1/tasks/{taskId}/release",
                                  released.getId())
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(mapper.writeValueAsString(TaskPayloadBuilder
                                                                                .release()
                                                                                .withTaskId(released.getId())
                                                                                .build()
                                     )))
                .andExpect(status().isOk())
                .andDo(document(DOCUMENTATION_IDENTIFIER + "/task/release",
                                taskIdParameter(),
                                links(
                                        linkWithRel("self").description("The task's resource."),
                                        linkWithRel("claim").optional().description("The task's claim option."),
                                        linkWithRel("processInstance").description("The task's option to get the process instance associated with."),
                                        linkWithRel("home").description("Home resource")
                                )));
    }

    @Test
    public void releaseTaskShouldUseAlfrescoGuidelineWhenMediaTypeIsApplicationJson() throws Exception {
        Task released = buildTask("task to be released",
                                  Task.TaskStatus.CREATED);
        when(taskAdminRuntime.release(any())).thenReturn(released);

        this.mockMvc.perform(post("/admin/v1/tasks/{taskId}/release",
                                  released.getId())
                                     .accept(MediaType.APPLICATION_JSON)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(mapper.writeValueAsString(TaskPayloadBuilder
                                                                                .release()
                                                                                .withTaskId(released.getId())
                                                                                .build()
                                     )))
                .andExpect(status().isOk())
                .andDo(document(DOCUMENTATION_IDENTIFIER_ALFRESCO + "/task/release",
                                taskIdParameter(),
                                taskFields()));
    }

    @Test
    public void completeTask() throws Exception {
        Task completed = buildTask("task to be completed",
                                   Task.TaskStatus.COMPLETED);
        when(taskAdminRuntime.complete(any())).thenReturn(completed);

        this.mockMvc.perform(post("/admin/v1/tasks/{taskId}/complete",
                                  completed.getId())
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(mapper.writeValueAsString(TaskPayloadBuilder
                                                                                .complete()
                                                                                .withTaskId(completed.getId())
                                                                                .withVariable("var-key",
                                                                                              "var-value")
                                                                                .build()
                                     )))
                .andExpect(status().isOk())
                .andDo(document(DOCUMENTATION_IDENTIFIER + "/task/complete",
                                taskIdParameter(),
                                links(
                                        linkWithRel("self").description("The task's resource."),
                                        linkWithRel("claim").optional().description("The task's claim option."),
                                        linkWithRel("processInstance").description("The task's option to get the process instance associated with."),
                                        linkWithRel("home").description("Home resource")
                                )));
    }

    @Test
    public void completeTaskShouldUseAlfrescoGuidelineWhenMediaTypeIsApplicationJson() throws Exception {
        Task completed = buildTask("task to be completed",
                                   Task.TaskStatus.COMPLETED);
        when(taskAdminRuntime.complete(any())).thenReturn(completed);

        this.mockMvc.perform(post("/admin/v1/tasks/{taskId}/complete",
                                  completed.getId())
                                     .accept(MediaType.APPLICATION_JSON)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(mapper.writeValueAsString(TaskPayloadBuilder
                                                                                .complete()
                                                                                .withTaskId(completed.getId())
                                                                                .withVariable("var-key",
                                                                                              "var-value")
                                                                                .build()
                                     )))
                .andExpect(status().isOk())
                .andDo(document(DOCUMENTATION_IDENTIFIER_ALFRESCO + "/task/complete",
                                taskIdParameter(),
                                taskFields()));
    }

    @Test
    public void deleteTask() throws Exception {
        Task deleted = buildTask("task to be deleted",
                                 Task.TaskStatus.DELETED);
        when(taskAdminRuntime.delete(any())).thenReturn(deleted);

        this.mockMvc.perform(delete("/admin/v1/tasks/{taskId}",
                                    deleted.getId())
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(mapper.writeValueAsString(TaskPayloadBuilder
                                                                                .delete()
                                                                                .withTaskId(deleted.getId())
                                                                                .withReason("it has to be deleted")
                                                                                .build()
                                     )))
                .andExpect(status().isOk())
                .andDo(document(DOCUMENTATION_IDENTIFIER + "/task/delete",
                                taskIdParameter(),
                                links(
                                        linkWithRel("self").description("The task's resource."),
                                        linkWithRel("claim").optional().description("The task's claim option."),
                                        linkWithRel("processInstance").description("The task's option to get the process instance associated with."),
                                        linkWithRel("home").description("Home resource")
                                )));
    }

    @Test
    public void deleteTaskShouldUseAlfrescoGuidelineWhenMediaTypeIsApplicationJson() throws Exception {
        Task deleted = buildTask("task to be deleted",
                                 Task.TaskStatus.DELETED);
        when(taskAdminRuntime.delete(any())).thenReturn(deleted);

        this.mockMvc.perform(delete("/admin/v1/tasks/{taskId}",
                                    deleted.getId())
                                     .accept(MediaType.APPLICATION_JSON)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(mapper.writeValueAsString(TaskPayloadBuilder
                                                                                .delete()
                                                                                .withTaskId(deleted.getId())
                                                                                .withReason("it has to be deleted")
                                                                                .build()
                                     )))
                .andExpect(status().isOk())
                .andDo(document(DOCUMENTATION_IDENTIFIER_ALFRESCO + "/task/delete",
                                taskIdParameter(),
                                taskFields()));
    }

    @Test
    public void setTaskVariables() throws Exception {
        Task task = buildTask("task to be deleted",
                              Task.TaskStatus.DELETED);

        Map<String, Object> variables = new HashMap<>();
        variables.put("var- key1",
                      "var-value1");
        this.mockMvc.perform(post("/admin/v1/tasks/{taskId}/variables",
                                  task.getId())
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(mapper.writeValueAsString(TaskPayloadBuilder
                                                                                .setVariables()
                                                                                .withTaskId(task.getId())
                                                                                .withVariables(variables)
                                                                                .build()
                                     )))
                .andExpect(status().isOk());
    }
}
