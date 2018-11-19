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

package org.activiti.cloud.starter.tests.runtime;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.activiti.api.process.model.IntegrationContext;
import org.activiti.cloud.api.process.model.IntegrationRequest;
import org.activiti.cloud.api.process.model.impl.IntegrationResultImpl;
import org.activiti.cloud.services.events.configuration.RuntimeBundleProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.binding.BinderAwareChannelResolver;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
@EnableBinding(ConnectorIntegrationChannels.class)
public class ServiceTaskConsumerHandler {

    private final BinderAwareChannelResolver resolver;
    private final RuntimeBundleProperties runtimeBundleProperties;
    private final ObjectMapper objectMapper;

    @Autowired
    public ServiceTaskConsumerHandler(BinderAwareChannelResolver resolver,
                                      RuntimeBundleProperties runtimeBundleProperties,
                                      ObjectMapper objectMapper) {
        this.resolver = resolver;
        this.runtimeBundleProperties = runtimeBundleProperties;
        this.objectMapper = objectMapper;
    }

    @StreamListener(value = ConnectorIntegrationChannels.INTEGRATION_EVENTS_CONSUMER)
    public void receive(IntegrationRequest integrationRequest) {
        IntegrationContext integrationContext = integrationRequest.getIntegrationContext();
        Map<String, Object> requestVariables = integrationContext.getInBoundVariables();

        Object customPojo = requestVariables.get("customPojo");

        String variableToUpdate = "age";

        HashMap<String, Object> resultVariables = new HashMap<>();
        resultVariables.put(variableToUpdate,
                            ((Integer) requestVariables.get(variableToUpdate)) + 1);
        //invert value of boolean
        resultVariables.put("boolVar",!(Boolean)requestVariables.get("boolVar"));

        resultVariables.put("customPojoTypeInConnector","Type of customPojo var in connector is "+customPojo.getClass());
        resultVariables.put("customPojoField1InConnector", "Value of field1 on customPojo is " + objectMapper.convertValue(customPojo,CustomPojo.class).getField1());
        //even the annotated pojo in connector won't be deserialized as the relevant type unless we tell objectMapper to do so
        resultVariables.put("customPojoAnnotatedTypeInConnector", "Type of customPojoAnnotated var in connector is " + requestVariables.get("customPojoAnnotated").getClass());

        integrationContext.addOutBoundVariables(resultVariables);

        IntegrationResultImpl integrationResult = new IntegrationResultImpl(integrationRequest, integrationContext);
        Message<IntegrationResultImpl> message = MessageBuilder.withPayload(integrationResult).build();
        resolver.resolveDestination("integrationResult:" + runtimeBundleProperties.getServiceFullName()).send(message);
    }
}
