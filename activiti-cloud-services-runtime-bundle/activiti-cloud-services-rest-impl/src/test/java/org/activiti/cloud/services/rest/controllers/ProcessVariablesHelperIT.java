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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.runtime.conf.impl.CommonModelAutoConfiguration;
import org.activiti.api.runtime.conf.impl.ProcessModelAutoConfiguration;
import org.activiti.cloud.services.events.ProcessEngineChannels;
import org.activiti.cloud.services.events.configuration.CloudEventsAutoConfiguration;
import org.activiti.cloud.services.events.configuration.RuntimeBundleProperties;
import org.activiti.cloud.services.rest.conf.ServicesRestAutoConfiguration;
import org.activiti.engine.ActivitiException;
import org.activiti.spring.process.conf.ProcessExtensionsAutoConfiguration;
import org.activiti.spring.process.model.Extension;
import org.activiti.spring.process.model.ProcessExtensionModel;
import org.activiti.spring.process.model.VariableDefinition;
import org.activiti.spring.process.variable.VariableValidationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@WebMvcTest(ProcessVariablesHelper.class)
@EnableSpringDataWebSupport
@AutoConfigureMockMvc(secure = false)
@AutoConfigureRestDocs(outputDir = "target/snippets")
@Import({CommonModelAutoConfiguration.class,
        ProcessModelAutoConfiguration.class,
        RuntimeBundleProperties.class,
        CloudEventsAutoConfiguration.class,
        ServicesRestAutoConfiguration.class,
        ProcessExtensionsAutoConfiguration.class})
@ComponentScan(basePackages = {"org.activiti.cloud.services.rest.assemblers", "org.activiti.cloud.alfresco"})
public class ProcessVariablesHelperIT {
    @MockBean
    private ProcessEngineChannels processEngineChannels;
    
    @Autowired
    private VariableValidationService variableValidationService;

    @MockBean
    private Map<String, ProcessExtensionModel> processExtensionModelMap;
    
    private ProcessVariablesHelper processVariablesHelper;

    @Before
    public void setUp() {
        ProcessExtensionModel processExtensionModel;
        
        VariableDefinition variableDefinitionName = new VariableDefinition();
        variableDefinitionName.setName("name");
        variableDefinitionName.setType("string");

        VariableDefinition variableDefinitionAge = new VariableDefinition();
        variableDefinitionAge.setName("age");
        variableDefinitionAge.setType("integer");

        VariableDefinition variableDefinitionSubscribe = new VariableDefinition();
        variableDefinitionSubscribe.setName("subscribe");
        variableDefinitionSubscribe.setType("boolean");

        Map<String, VariableDefinition> properties = new HashMap<>();
        properties.put("name", variableDefinitionName);
        properties.put("age", variableDefinitionAge);
        properties.put("subscribe", variableDefinitionSubscribe);

        Extension extension = new Extension();
        extension.setProperties(properties);

        processExtensionModel = new ProcessExtensionModel();
        processExtensionModel.setId("1");
        processExtensionModel.setExtensions(extension);
        
        processVariablesHelper = new ProcessVariablesHelper(processExtensionModelMap,
                                                            variableValidationService);

        given(processExtensionModelMap.get(any()))
                .willReturn(processExtensionModel);
    }
 
    @Test
    public void shouldReturnErrorListWhenSetVariablesWithWrongNames() throws Exception {
        //GIVEN
        Map<String, Object> variables = new HashMap<>();
        variables.put("name", "Alice");
        variables.put("age", 24);
        variables.put("subs", false);
        
        //WHEN
        List<ActivitiException> activitiExceptions = processVariablesHelper.checkPayloadVariables(ProcessPayloadBuilder.setVariables()
                                                                           .withVariables(variables)
                                                                           .build(),
                                                                            "10");
        
        String expectedMsg = "Variable with name subs does not exists.";
        
        assertThat(activitiExceptions)
        .isNotEmpty()
        .extracting(ex -> ex.getMessage())
        .containsOnly(expectedMsg);
    }

    @Test
    public void shouldReturnErrorListWhenSetVariablesWithWrongType() throws Exception {
        //GIVEN
        Map<String, Object> variables = new HashMap<>();
        variables.put("name", "Alice");
        variables.put("age", "24");
        variables.put("subscribe", "false");
        String expectedTypeErrorMessage1 = "class java.lang.String is not assignable from class java.lang.Boolean";
        String expectedTypeErrorMessage2 = "class java.lang.String is not assignable from class java.lang.Integer";

        //WHEN
        List<ActivitiException> activitiExceptions = processVariablesHelper.checkPayloadVariables(ProcessPayloadBuilder.setVariables()
                                                                           .withVariables(variables)
                                                                           .build(),
                                                                            "10");  
        assertThat(activitiExceptions)
        .isNotEmpty()
        .extracting(ex -> ex.getMessage())
        .containsOnly(expectedTypeErrorMessage1,
                      expectedTypeErrorMessage2);
    }
 
    @Test
    public void shouldReturnErrorListWhenSetVariablesWithWrongNameAndType() throws Exception {
        //GIVEN
        Map<String, Object> variables = new HashMap<>();
        variables.put("name", "Alice");
        variables.put("gender", "female");
        variables.put("age", "24");
        variables.put("subs", true);
        variables.put("subscribe", true);
        String expectedTypeErrorMessage = "class java.lang.String is not assignable from class java.lang.Integer";
        String expectedNameErrorMessage1 = "Variable with name gender does not exists.";
        String expectedNameErrorMessage2 = "Variable with name subs does not exists.";

        //WHEN
        List<ActivitiException> activitiExceptions = processVariablesHelper.checkPayloadVariables(ProcessPayloadBuilder.setVariables()
                                                                           .withVariables(variables)
                                                                           .build(),
                                                                            "10");  
        
        assertThat(activitiExceptions)
        .isNotEmpty()
        .extracting(ex -> ex.getMessage())
        .containsOnly(expectedTypeErrorMessage,
                      expectedNameErrorMessage1,
                      expectedNameErrorMessage2);
    }
}