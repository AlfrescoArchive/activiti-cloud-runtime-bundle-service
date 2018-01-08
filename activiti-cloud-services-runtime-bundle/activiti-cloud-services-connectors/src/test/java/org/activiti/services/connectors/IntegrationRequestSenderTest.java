package org.activiti.services.connectors;

import org.activiti.bpmn.model.ServiceTask;
import org.activiti.cloud.services.api.events.ProcessEngineEvent;
import org.activiti.cloud.services.events.ProcessEngineChannels;
import org.activiti.cloud.services.events.configuration.RuntimeBundleProperties;
import org.activiti.cloud.services.events.integration.IntegrationRequestSentEvent;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.integration.IntegrationContextEntity;
import org.activiti.engine.impl.persistence.entity.integration.IntegrationContextManager;
import org.activiti.services.connectors.channel.ProcessEngineIntegrationChannels;
import org.activiti.services.connectors.model.IntegrationRequestEvent;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class IntegrationRequestSenderTest {

    private static final String CONNECTOR_TYPE = "payment";
    private static final String EXECUTION_ID = "execId";
    private static final String PROC_INST_ID = "procInstId";
    private static final String PROC_DEF_ID = "procDefId";
    private static final String INTEGRATION_CONTEXT_ID = "intContextId";
    private static final String APP_NAME = "myApp";

    private IntegrationRequestSender integrationRequestSender;

    @Mock
    private ProcessEngineIntegrationChannels integrationChannels;

    @Mock
    private MessageChannel integrationProducerChannel;

    @Mock
    private ProcessEngineChannels processEngineChannels;

    @Mock
    private MessageChannel auditProducerChannel;

    @Mock
    private RuntimeBundleProperties runtimeBundleProperties;

    @Mock
    private RuntimeBundleProperties.RuntimeBundleEventsProperties eventsProperties;

    @Mock
    private Message<IntegrationRequestEvent> firstIntegration;

    @Mock
    private Message<IntegrationRequestEvent> secondIntegration;

    @Mock
    private IntegrationContextManager integrationContextManager;

    @Mock
    private IntegrationContextEntity integrationContextEntity;

    @Mock
    private DelegateExecution delegateExecution;

    private ServiceTask serviceTask;

    @Captor
    private ArgumentCaptor<Message<ProcessEngineEvent[]>> messageArgumentCaptor;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        when(integrationChannels.integrationEventsProducer()).thenReturn(integrationProducerChannel);
        when(processEngineChannels.auditProducer()).thenReturn(auditProducerChannel);
        when(runtimeBundleProperties.getEventsProperties()).thenReturn(eventsProperties);
        serviceTask = new ServiceTask();
        serviceTask.setImplementation(CONNECTOR_TYPE);
        when(delegateExecution.getCurrentFlowElement()).thenReturn(serviceTask);
        integrationRequestSender = new IntegrationRequestSender(integrationContextManager,integrationChannels,runtimeBundleProperties,processEngineChannels,integrationContextEntity,delegateExecution);

        when(delegateExecution.getProcessDefinitionId()).thenReturn(PROC_DEF_ID);
        when(delegateExecution.getProcessInstanceId()).thenReturn(PROC_INST_ID);
        when(integrationContextEntity.getExecutionId()).thenReturn(EXECUTION_ID);
        when(integrationContextEntity.getId()).thenReturn(INTEGRATION_CONTEXT_ID);
    }


    @Test
    public void executeShouldNotSendIntegrationAuditEventWhenIntegrationAuditEventsAreDisabled() throws Exception {
        //given
        given(runtimeBundleProperties.getEventsProperties().isIntegrationAuditEventsEnabled()).willReturn(false);
        given(runtimeBundleProperties.getName()).willReturn(APP_NAME);

        //when
        integrationRequestSender.afterCommit();

        //then
        verify(auditProducerChannel,
                never()).send(ArgumentMatchers.any());
    }

    @Test
    public void executeShouldSendIntegrationAuditEventWhenIntegrationAuditEventsAreEnabled() throws Exception {
        //given
        given(runtimeBundleProperties.getEventsProperties().isIntegrationAuditEventsEnabled()).willReturn(true);
        given(runtimeBundleProperties.getName()).willReturn(APP_NAME);

        ServiceTask serviceTask = new ServiceTask();
        serviceTask.setImplementation(CONNECTOR_TYPE);

        //when
        integrationRequestSender.afterCommit();

        //then
        verify(auditProducerChannel).send(messageArgumentCaptor.capture());

        Message<ProcessEngineEvent[]> message = messageArgumentCaptor.getValue();
        assertThat(message.getPayload()).hasSize(1);
        assertThat(message.getPayload()[0]).isInstanceOf(IntegrationRequestSentEvent.class);

        IntegrationRequestSentEvent integrationRequestSentEvent = (IntegrationRequestSentEvent) message.getPayload()[0];

        assertThat(integrationRequestSentEvent.getIntegrationContextId()).isEqualTo(INTEGRATION_CONTEXT_ID);
        assertThat(integrationRequestSentEvent.getProcessInstanceId()).isEqualTo(PROC_INST_ID);
        assertThat(integrationRequestSentEvent.getProcessDefinitionId()).isEqualTo(PROC_DEF_ID);
        assertThat(integrationRequestSentEvent.getApplicationName()).isEqualTo(APP_NAME);
    }
}
