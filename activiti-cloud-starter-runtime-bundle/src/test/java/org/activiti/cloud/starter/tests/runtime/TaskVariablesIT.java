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

package org.activiti.cloud.starter.tests.runtime;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.activiti.api.model.shared.model.VariableInstance;
import org.activiti.api.process.model.ProcessDefinition;
import org.activiti.cloud.api.model.shared.CloudVariableInstance;
import org.activiti.cloud.api.process.model.CloudProcessDefinition;
import org.activiti.cloud.api.process.model.CloudProcessInstance;
import org.activiti.cloud.api.task.model.CloudTask;
import org.activiti.cloud.services.test.identity.keycloak.interceptor.KeycloakTokenProducer;
import org.activiti.cloud.starter.tests.definition.ProcessDefinitionIT;
import org.activiti.cloud.starter.tests.helper.ProcessInstanceRestTemplate;
import org.activiti.cloud.starter.tests.helper.TaskRestTemplate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource("classpath:application-test.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class TaskVariablesIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ProcessInstanceRestTemplate processInstanceRestTemplate;

    @Autowired
    private TaskRestTemplate taskRestTemplate;
    
    @Autowired
    private KeycloakTokenProducer keycloakSecurityContextClientRequestInterceptor;

    private Map<String, String> processDefinitionIds = new HashMap<>();

    private static final String SIMPLE_PROCESS = "SimpleProcess";
    
    @Before
    public void setUp() {
    	keycloakSecurityContextClientRequestInterceptor.setKeycloakTestUser("hruser");
    	
        ResponseEntity<PagedResources<CloudProcessDefinition>> processDefinitions = getProcessDefinitions();
        assertThat(processDefinitions.getStatusCode()).isEqualTo(HttpStatus.OK);
        for (ProcessDefinition pd : processDefinitions.getBody().getContent()) {
            processDefinitionIds.put(pd.getName(), pd.getId());
        }
    }

    @Test
    public void shouldRetrieveTaskVariables() {
        //given
        Map<String, Object> variables = new HashMap<>();
        variables.put("var1",
                      "test1");
        ResponseEntity<CloudProcessInstance> startResponse = processInstanceRestTemplate.startProcess(processDefinitionIds.get(SIMPLE_PROCESS),
                                                                                                      variables);
        ResponseEntity<PagedResources<CloudTask>> tasks = processInstanceRestTemplate.getTasks(startResponse);

        String taskId = tasks.getBody().getContent().iterator().next().getId();
      
        taskRestTemplate.claim(taskId);
        
        //taskRestTemplate.createVariable(taskId, "var2", "test2");
        Map<String, Object> taskVariables = new HashMap<>();
        taskVariables.put("var2",
                          "test2");
        taskRestTemplate.setVariables(taskId, taskVariables);

        //when
        ResponseEntity<Resources<CloudVariableInstance>> variablesResponse = taskRestTemplate.getVariables(taskId);

        //then
        assertThat(variablesResponse).isNotNull();
        assertThat(variablesContainEntry("var2","test2",variablesResponse.getBody().getContent())).isTrue();
        //process variables also at task level
        assertThat(variablesContainEntry("var1","test1",variablesResponse.getBody().getContent())).isTrue();

        // when
        variablesResponse = taskRestTemplate.getVariables(taskId);

        // then
        assertThat(variablesResponse).isNotNull();
        assertThat(variablesContainEntry("var2","test2",variablesResponse.getBody().getContent())).isTrue();
        assertThat(variablesContainEntry("var1","test1",variablesResponse.getBody().getContent())).isTrue();

        // give
        taskRestTemplate.updateVariable(taskId, "var2", "test2-update" );
        
        
        //taskRestTemplate.createVariable(taskId, "var3", "test3" );
        taskVariables = new HashMap<>();
        taskVariables.put("var3",
                          "test3");
        taskRestTemplate.setVariables(taskId, taskVariables);
        

        // when
        variablesResponse = taskRestTemplate.getVariables(taskId);

        // then
        assertThat(variablesResponse).isNotNull();
        assertThat(variablesContainEntry("var2","test2-update",variablesResponse.getBody().getContent())).isTrue();
        assertThat(variablesContainEntry("var1","test1",variablesResponse.getBody().getContent())).isTrue();
        assertThat(variablesContainEntry("var3","test3",variablesResponse.getBody().getContent())).isTrue();
        
        //given
        taskRestTemplate.updateVariable(taskId, "var3", "test3-update");

        // when
        variablesResponse = taskRestTemplate.getVariables(taskId);

        // then
        assertThat(variablesResponse).isNotNull();
        assertThat(variablesContainEntry("var2","test2-update",variablesResponse.getBody().getContent())).isTrue();
        assertThat(variablesContainEntry("var1","test1",variablesResponse.getBody().getContent())).isTrue();
        assertThat(variablesContainEntry("var3","test3-update",variablesResponse.getBody().getContent())).isTrue();


    }
    
    @Test
    public void adminShouldSetGetUpdateTaskVariables() {
        //given
        Map<String, Object> variables = new HashMap<>();
        variables.put("var1",
                      "test1");
        ResponseEntity<CloudProcessInstance> startResponse = processInstanceRestTemplate.startProcess(processDefinitionIds.get(SIMPLE_PROCESS),
                                                                                                      variables);
        ResponseEntity<PagedResources<CloudTask>> tasks = processInstanceRestTemplate.getTasks(startResponse);

        String taskId = tasks.getBody().getContent().iterator().next().getId();
        
        keycloakSecurityContextClientRequestInterceptor.setKeycloakTestUser("testadmin");
        taskRestTemplate.adminCreateVariable(taskId, "var2", "test2");

        //when
        ResponseEntity<Resources<CloudVariableInstance>> variablesResponse = taskRestTemplate.adminGetVariables(taskId);

        //then
        assertThat(variablesResponse).isNotNull();
        assertThat(variablesContainEntry("var2","test2",variablesResponse.getBody().getContent())).isTrue();
        assertThat(variablesContainEntry("var1","test1",variablesResponse.getBody().getContent())).isTrue();

    
        //given
        taskRestTemplate.adminUpdateVariable(taskId, "var2","test2-update");
        taskRestTemplate.adminCreateVariable(taskId, "var3","test3");

        // when
        variablesResponse = taskRestTemplate.adminGetVariables(taskId);

        // then
        assertThat(variablesResponse).isNotNull();
        assertThat(variablesContainEntry("var2","test2-update",variablesResponse.getBody().getContent())).isTrue();
        assertThat(variablesContainEntry("var1","test1",variablesResponse.getBody().getContent())).isTrue();
        assertThat(variablesContainEntry("var3","test3",variablesResponse.getBody().getContent())).isTrue();
        
        //given
        taskRestTemplate.adminUpdateVariable(taskId, "var3", "test3-update");

        // when
        variablesResponse = taskRestTemplate.adminGetVariables(taskId);

        // then
        assertThat(variablesResponse).isNotNull();
        assertThat(variablesContainEntry("var2","test2-update",variablesResponse.getBody().getContent())).isTrue();
        assertThat(variablesContainEntry("var1","test1",variablesResponse.getBody().getContent())).isTrue();
        assertThat(variablesContainEntry("var3","test3-update",variablesResponse.getBody().getContent())).isTrue();
    }

    private boolean variablesContainEntry(String key, Object value, Collection<CloudVariableInstance> variableCollection){
        Iterator<CloudVariableInstance> iterator = variableCollection.iterator();
        while(iterator.hasNext()){
            VariableInstance variable = iterator.next();
            if(variable.getName().equalsIgnoreCase(key) && variable.getValue().equals(value)){
                assertThat(variable.getType()).isEqualToIgnoringCase(variable.getValue().getClass().getSimpleName());
                return true;
            }
        }
        return false;
    }


    private ResponseEntity<PagedResources<CloudProcessDefinition>> getProcessDefinitions() {
        ParameterizedTypeReference<PagedResources<CloudProcessDefinition>> responseType = new ParameterizedTypeReference<PagedResources<CloudProcessDefinition>>() {
        };
        return restTemplate.exchange(ProcessDefinitionIT.PROCESS_DEFINITIONS_URL,
                                     HttpMethod.GET,
                                     null,
                                     responseType);
    }
}
