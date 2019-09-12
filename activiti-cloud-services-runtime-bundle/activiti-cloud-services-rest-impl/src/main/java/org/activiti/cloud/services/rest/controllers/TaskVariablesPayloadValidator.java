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

import java.util.Map;

import org.activiti.api.task.model.payloads.CreateTaskVariablePayload;
import org.activiti.api.task.model.payloads.UpdateTaskVariablePayload;
import org.activiti.spring.process.variable.DateFormatterProvider;

public class TaskVariablesPayloadValidator  {
   
    private final DateFormatterProvider dateFormatterProvider;
 
    public TaskVariablesPayloadValidator(DateFormatterProvider dateFormatterProvider) {
        this.dateFormatterProvider = dateFormatterProvider;
    }
    
    public void checkPayloadVariables(Map<String, Object> variablePayloadMap) {
       if (variablePayloadMap != null) {
           for (Map.Entry<String, Object> payloadVar : variablePayloadMap.entrySet()) {
                Object value = payloadVar.getValue();
                           
                if (value instanceof String) {
                    try {
                        payloadVar.setValue(dateFormatterProvider.parse((String)value));
                    } catch (Exception e) {}
                }
           }
       }
    }
    
    public void checkPayloadVariable(CreateTaskVariablePayload createTaskVariablePayload) {
        Object value = createTaskVariablePayload.getValue();
        
        if (value instanceof String) {
            try {
                createTaskVariablePayload.setValue(dateFormatterProvider.parse((String)value));
            } catch (Exception e) {}
        }
    }
    
    public void checkPayloadVariable(UpdateTaskVariablePayload updateTaskVariablePayload) {
        Object value = updateTaskVariablePayload.getValue();
        
        if (value instanceof String) {
            try {
                updateTaskVariablePayload.setValue(dateFormatterProvider.parse((String)value));
            } catch (Exception e) {}
        }
    }
}
