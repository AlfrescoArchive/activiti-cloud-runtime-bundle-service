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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.activiti.cloud.services.api.model.ProcessInstanceVariables;
import org.activiti.cloud.services.rest.assemblers.ProcessInstanceVariablesResourceAssembler;
import org.activiti.engine.RuntimeService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(ProcessInstanceVariableControllerImpl.class)
@EnableSpringDataWebSupport
@AutoConfigureMockMvc
@AutoConfigureRestDocs(outputDir = "target/snippets")
public class ProcessInstanceVariableControllerImplIT {

    private static final String DOCUMENTATION_IDENTIFIER = "process-instance-variables";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RuntimeService runtimeService;
    @MockBean
    private ProcessInstanceVariablesResourceAssembler variableResourceAssembler;

    @SpyBean
    private ObjectMapper mapper;

    @Test
    public void getVariables() throws Exception {
        when(runtimeService.getVariables("1")).thenReturn(new HashMap<>());

        this.mockMvc.perform(get("/v1/process-instances/{processInstanceId}/variables",
                                 1).accept(MediaTypes.HAL_JSON_VALUE))
                .andExpect(status().isOk())
                .andDo(document(DOCUMENTATION_IDENTIFIER + "/list",
                                pathParameters(parameterWithName("processInstanceId").description("The process instance id"))));
    }

    @Test
    public void setVariables() throws Exception {
        Map<String, Object> variables = new HashMap<>();
        variables.put("var1",
                      "varObj1");
        variables.put("var2",
                      "varObj2");

        ProcessInstanceVariables processInstanceVariables = new ProcessInstanceVariables("processInstanceId",
                                                                                         variables);
        this.mockMvc.perform(post("/v1/process-instances/{processInstanceId}/variables",
                                  1)
                                     .accept(MediaTypes.HAL_JSON_VALUE)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(mapper.writeValueAsString(processInstanceVariables))
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document(DOCUMENTATION_IDENTIFIER + "/upsert",
                                pathParameters(parameterWithName("processInstanceId").description("The process instance id"))));
    }

    @Test
    public void deleteVariables() throws Exception {
        this.mockMvc.perform(delete("/v1/process-instances/{processInstanceId}/variables",
                                    1)
                                     .accept(MediaTypes.HAL_JSON_VALUE)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(mapper.writeValueAsString(Arrays.asList("varName1",
                                                                                      "varName2"))))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document(DOCUMENTATION_IDENTIFIER + "/delete",
                                pathParameters(parameterWithName("processInstanceId").description("The process instance id"))));
    }
}
