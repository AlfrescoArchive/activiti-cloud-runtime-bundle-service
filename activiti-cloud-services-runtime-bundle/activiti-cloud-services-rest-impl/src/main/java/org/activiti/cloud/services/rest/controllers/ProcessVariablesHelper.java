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

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;

import org.activiti.api.process.model.payloads.SetProcessVariablesPayload;
import org.activiti.engine.ActivitiException;
import org.activiti.spring.process.model.Extension;
import org.activiti.spring.process.model.ProcessExtensionModel;
import org.activiti.spring.process.model.VariableDefinition;
import org.activiti.spring.process.variable.VariableValidationService;

public class ProcessVariablesHelper  {
   
    private final Map<String, ProcessExtensionModel> processExtensionModelMap;
    private final VariableValidationService variableValidationService;
    private String dateTimeFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    private TimeZone timeZone = TimeZone.getTimeZone("UTC");
 
    public ProcessVariablesHelper(Map<String, ProcessExtensionModel> processExtensionModelMap,
                                  VariableValidationService variableValidationService) {
        this.processExtensionModelMap = processExtensionModelMap;
        this.variableValidationService = variableValidationService;
    }
    
    public String getDateTimeFormat() {
        return dateTimeFormat;
    }
    
    public void setDateTimeFormat(String dateTimeFormat) {
        this.dateTimeFormat = dateTimeFormat;
    }
    
    public TimeZone getTimeZone() {
        return timeZone;
    }
   
    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }
    
    private Optional<Map<String, VariableDefinition>> getVariableDefinitionMap(String processDefinitionKey) {
        ProcessExtensionModel processExtensionModel = processExtensionModelMap.get(processDefinitionKey);

        return Optional.ofNullable(processExtensionModel)
                .map(ProcessExtensionModel::getExtensions)
                .map(Extension::getProperties);
    }
    
    private Date convertObject2Date(Object value) throws Exception{
        Date d = null;
        if (value instanceof Date) {
            return (Date)value;
        }
        if (value instanceof String) {
            SimpleDateFormat format = new SimpleDateFormat(dateTimeFormat);
            format.setTimeZone(timeZone);
            d = format.parse(String.valueOf(value)); 
        }
        if (value instanceof Long) {
            d = new Date((long)value);                       
        }
        return d;
    }
    
    public List<ActivitiException> checkPayloadVariables(SetProcessVariablesPayload setProcessVariablesPayload,
                                                         String processDefinitionKey) {
           
           final String errorMessage = "Variable with name {0} does not exists.";
           final Optional<Map<String, VariableDefinition>> variableDefinitionMap = getVariableDefinitionMap(processDefinitionKey);
           List<ActivitiException> activitiExceptions = new ArrayList<>();
           
           if (variableDefinitionMap.isPresent()) {
               
               Map<String, Object> variablePayloadMap = setProcessVariablesPayload.getVariables();
              
               for (Map.Entry<String, Object> var : variablePayloadMap.entrySet()) {
                   String name = var.getKey();
                   Object value = var.getValue();
                   boolean found = false;
                   for (Map.Entry<String, VariableDefinition> entry : variableDefinitionMap.get().entrySet()) {
                       
                       if (entry.getKey().equals(name)) {
                           String type = entry.getValue().getType();
                           
                           if ("date".equals(type) &&  value != null) {
                               try {
                                   Date d = convertObject2Date(value);
                                   if (d != null) {
                                       var.setValue(value = d);
                                   }   
                               } catch (Exception e) {}
                           }

                           found = true;
                           activitiExceptions.addAll(variableValidationService.validateWithErrors(value, entry.getValue()));
                           break;
                       }  
                   }
                   
                   if (!found) {
                       activitiExceptions.add(new ActivitiException(MessageFormat.format(errorMessage, name)));
                   }
               }
           }        
           return activitiExceptions;
    }
}
