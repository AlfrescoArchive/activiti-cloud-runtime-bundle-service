/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.activiti.cloud.services.rest.controllers;

import java.util.ArrayList;
import java.util.Map;

import org.activiti.cloud.services.api.commands.SetProcessVariablesCmd;
import org.activiti.cloud.services.api.commands.SetTaskVariablesCmd;
import org.activiti.cloud.services.api.model.ProcessInstance;
import org.activiti.cloud.services.api.model.ProcessInstanceVariables;
import org.activiti.cloud.services.core.ActivitiForbiddenException;
import org.activiti.cloud.services.core.ProcessEngineWrapper;
import org.activiti.cloud.services.core.SecurityPoliciesApplicationService;
import org.activiti.cloud.services.rest.api.ProcessInstanceVariableController;
import org.activiti.cloud.services.rest.assemblers.ProcessInstanceVariablesResourceAssembler;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.RuntimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**

 */
@RestController
public class ProcessInstanceVariableControllerImpl implements ProcessInstanceVariableController {

    private final RuntimeService runtimeService;
    private final ProcessInstanceVariablesResourceAssembler variableResourceBuilder;
    private ProcessEngineWrapper processEngine;
    private final SecurityPoliciesApplicationService securityService;

    @ExceptionHandler(ActivitiForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleAppException(ActivitiForbiddenException ex) {
        return ex.getMessage();
    }


    @ExceptionHandler(ActivitiObjectNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleAppException(ActivitiObjectNotFoundException ex) {
        return ex.getMessage();
    }

    @Autowired
    public ProcessInstanceVariableControllerImpl(RuntimeService runtimeService,
                                                 ProcessInstanceVariablesResourceAssembler variableResourceBuilder,
                                                 SecurityPoliciesApplicationService securityService,
                                                 ProcessEngineWrapper processEngineWrapper) {
        this.runtimeService = runtimeService;
        this.variableResourceBuilder = variableResourceBuilder;
        this.securityService = securityService;
        this.processEngine = processEngineWrapper;
    }

    @Override
    public Resource<Map<String, Object>> getVariables(@PathVariable String processInstanceId) {
        Map<String, Object> variables = runtimeService.getVariables(processInstanceId);
        return variableResourceBuilder.toResource(new ProcessInstanceVariables(processInstanceId,
                                                                               variables));
    }

    @Override
    public ResponseEntity<Void> setVariables(@PathVariable String processInstanceId,
                                             @RequestBody(required = true) SetProcessVariablesCmd setProcessVariablesCmd) {

        processEngine.setProcessVariables(setProcessVariablesCmd);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> removeVariables(@PathVariable String processInstanceId,
                                                @RequestBody ArrayList<String> variablesNames) {
        this.runtimeService.removeVariables(processInstanceId,
                                            variablesNames);

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
