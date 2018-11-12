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

package org.activiti.cloud.services.events.listeners;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Collections;

import org.activiti.cloud.api.model.shared.events.CloudRuntimeEvent;
import org.activiti.cloud.services.events.ProcessEngineChannels;
import org.activiti.cloud.services.events.configuration.RuntimeBundleProperties;
import org.activiti.cloud.services.events.message.CommandContextMessageBuilderFactory;
import org.activiti.engine.impl.context.ExecutionContext;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.DeploymentEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.repository.ProcessDefinition;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

public class MessageProducerCommandContextCloseListenerTest {

    @InjectMocks
    private MessageProducerCommandContextCloseListener closeListener;

    @Mock
    private ProcessEngineChannels producer;

    @Spy
    private RuntimeBundleProperties properties = new RuntimeBundleProperties() {
        {
            setAppName("appName");
            setAppVersion("appVersion");
            setServiceType("serviceType");
            setServiceVersion("serviceVersion");
            setRbSpringAppName("springAppName");
        }
    };

    @Spy
    private CommandContextMessageBuilderFactory messageBuilderFilterChainFactory = 
                new CommandContextMessageBuilderFactory(properties);

    @Mock
    private MessageChannel auditChannel;

    @Mock
    private CommandContext commandContext;

    @Captor
    private ArgumentCaptor<Message<CloudRuntimeEvent<?, ?>[]>> messageArgumentCaptor;

    @Mock
    private CloudRuntimeEvent<?, ?> event;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        when(producer.auditProducer()).thenReturn(auditChannel);
        
        ExecutionContext executionContext = mockExecutionContext();
        given(commandContext.getGenericAttribute(MessageProducerCommandContextCloseListener.EXECUTION_CONTEXT))
                .willReturn(executionContext);
        
    }

    @Test
    public void closedShouldSendEventsRegisteredOnTheCommandContext() {
        // given
        given(commandContext.getGenericAttribute(MessageProducerCommandContextCloseListener.PROCESS_ENGINE_EVENTS))
                .willReturn(Collections.singletonList(event));

        // when
        closeListener.closed(commandContext);

        // then
        verify(auditChannel).send(messageArgumentCaptor.capture());
        assertThat(messageArgumentCaptor.getValue()
                                        .getPayload()).containsExactly(event);
    }

    @Test
    public void closedShouldDoNothingWhenRegisteredEventsIsNull() {
        // given
        given(commandContext.getGenericAttribute(MessageProducerCommandContextCloseListener.PROCESS_ENGINE_EVENTS))
                .willReturn(null);

        // when
        closeListener.closed(commandContext);

        // then
        verify(auditChannel,
               never()).send(any());
    }

    @Test
    public void closedShouldDoNothingWhenRegisteredEventsIsEmpty() {
        // given
        given(commandContext.getGenericAttribute(MessageProducerCommandContextCloseListener.PROCESS_ENGINE_EVENTS))
                .willReturn(Collections.emptyList());

        // when
        closeListener.closed(commandContext);

        // then
        verify(auditChannel,
               never()).send(any());
    }

    @Test
    public void closedShouldSendMessageHeadersWithExecutionContext() {
        // given
        given(commandContext.getGenericAttribute(MessageProducerCommandContextCloseListener.PROCESS_ENGINE_EVENTS))
                .willReturn(Collections.singletonList(event));

        // when
        closeListener.closed(commandContext);

        // then
        verify(auditChannel).send(messageArgumentCaptor.capture());
        assertThat(messageArgumentCaptor.getValue()
                                        .getHeaders()).containsEntry("messagePayloadType","[Lorg.activiti.cloud.api.model.shared.events.CloudRuntimeEvent;")
                                                      .containsEntry("businessKey","mockBusinessKey")
                                                      .containsEntry("processDefinitionId","mockProcessDefinitionId")
                                                      .containsEntry("processDefinitionKey","mockProcessDefinitionKey")
                                                      .containsEntry("deploymentId","mockDeploymentId")
                                                      .containsEntry("deploymentName","mockDeploymentName")
                                                      .containsEntry("appName","appName")
                                                      .containsEntry("appVersion","appVersion")
                                                      .containsEntry("serviceName","springAppName")
                                                      .containsEntry("serviceType","serviceType")
                                                      .containsEntry("serviceVersion","serviceVersion")
                                                      .containsEntry("serviceFullName","springAppName");
    }

    private ExecutionContext mockExecutionContext() {
        ExecutionContext context = mock(ExecutionContext.class);
        ExecutionEntity processInstance = mock(ExecutionEntity.class);
        DeploymentEntity deploymentEntity = mock(DeploymentEntity.class);
        ProcessDefinition processDefinition = mock(ProcessDefinition.class);

        when(context.getProcessInstance()).thenReturn(processInstance);
        when(context.getDeployment()).thenReturn(deploymentEntity);
        when(context.getProcessDefinition()).thenReturn(processDefinition);

        when(processInstance.getBusinessKey()).thenReturn("mockBusinessKey");

        when(processDefinition.getId()).thenReturn("mockProcessDefinitionId");
        when(processDefinition.getKey()).thenReturn("mockProcessDefinitionKey");

        when(deploymentEntity.getId()).thenReturn("mockDeploymentId");
        when(deploymentEntity.getName()).thenReturn("mockDeploymentName");
        
        return context;
    }

}