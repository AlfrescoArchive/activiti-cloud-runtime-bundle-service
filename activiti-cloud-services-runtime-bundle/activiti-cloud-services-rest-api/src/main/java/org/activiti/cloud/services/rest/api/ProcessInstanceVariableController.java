package org.activiti.cloud.services.rest.api;

import java.util.ArrayList;
import java.util.Map;

import org.activiti.cloud.services.api.commands.SetProcessVariablesCmd;
import org.activiti.cloud.services.api.model.ProcessInstanceVariables;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@RequestMapping(value = "/v1/process-instances/{processInstanceId}/variables", produces = MediaTypes.HAL_JSON_VALUE)
public interface ProcessInstanceVariableController {

    @RequestMapping(method = RequestMethod.GET)
    Resource<Map<String, Object>> getVariables(@PathVariable String processInstanceId);

    @RequestMapping(method = RequestMethod.POST)
    ResponseEntity<Void> setVariables(@PathVariable String processInstanceId,
                                      @RequestBody(required = true) SetProcessVariablesCmd setTaskVariablesCmd);

    @RequestMapping(method = RequestMethod.DELETE)
    ResponseEntity<Void> removeVariables(@PathVariable String processInstanceId,
                                         @RequestAttribute ArrayList<String> variablesNames);
}
