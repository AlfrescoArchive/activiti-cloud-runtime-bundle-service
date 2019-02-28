package org.activiti.cloud.services.rest.api;

import java.util.List;

import org.activiti.api.task.model.payloads.SetTaskVariablesPayload;
import org.activiti.cloud.services.rest.api.resources.VariableInstanceResource;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resources;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@RequestMapping(value = "/admin/v1/tasks/{taskId}/variables",
        produces = {MediaTypes.HAL_JSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
public interface TaskVariableAdminController {

    @RequestMapping(method = RequestMethod.GET)
    Resources<VariableInstanceResource> getVariables(@PathVariable String taskId);

    @RequestMapping(method = RequestMethod.POST)
    ResponseEntity<Void> newVariable(@PathVariable String taskId,
                                      @RequestBody SetTaskVariablesPayload setTaskVariablesPayload);
    
    @RequestMapping(method = RequestMethod.PUT)
    ResponseEntity<Void> updateVariable(@PathVariable String taskId,
                                      	 @RequestBody SetTaskVariablesPayload setTaskVariablesPayload);
}
