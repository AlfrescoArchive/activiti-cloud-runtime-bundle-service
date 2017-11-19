package org.activiti.services.connectors.behavior;

import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandContextCloseListener;
import org.activiti.services.connectors.channel.ProcessEngineIntegrationChannels;
import org.activiti.services.connectors.model.IntegrationRequestEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Component
public class IntegrationProducerCommandContextCloseListener implements CommandContextCloseListener {

    public static final String PROCESS_ENGINE_INTEGRATION_EVENTS = "processEngineIntegrationEvents";

    private final ProcessEngineIntegrationChannels producer;

    @Autowired
    public IntegrationProducerCommandContextCloseListener(ProcessEngineIntegrationChannels producer) {
        this.producer = producer;
    }

    @Override
    public void closed(CommandContext commandContext) {
        CommandContext currentCommandContext = Context.getCommandContext();
        Message<IntegrationRequestEvent> message = currentCommandContext
                .getGenericAttribute(PROCESS_ENGINE_INTEGRATION_EVENTS);
        System.out.println("Sending Message for executionId: " + message.getPayload().getExecutionId());
        if (message != null) {
            producer.integrationEventsProducer().send(message);
        }
    }

    @Override
    public void closing(CommandContext commandContext) {
        // No need to implement this method in this class
    }

    @Override
    public void afterSessionsFlush(CommandContext commandContext) {
        // No need to implement this method in this class
    }

    @Override
    public void closeFailure(CommandContext commandContext) {
        // No need to implement this method in this class
    }
}
