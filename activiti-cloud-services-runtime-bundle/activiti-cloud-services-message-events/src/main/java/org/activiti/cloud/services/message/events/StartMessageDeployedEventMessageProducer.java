package org.activiti.cloud.services.message.events;

import org.activiti.api.process.model.StartMessageSubscription;
import org.activiti.api.process.model.builders.MessageEventPayloadBuilder;
import org.activiti.api.process.model.events.StartMessageDeployedEvent;
import org.activiti.api.process.model.payloads.MessageEventPayload;
import org.activiti.api.process.runtime.events.listener.ProcessRuntimeEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class StartMessageDeployedEventMessageProducer implements ProcessRuntimeEventListener<StartMessageDeployedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(BpmnMessageSentEventMessageProducer.class);

    private final StartMessageDeployedEventMessageBuilderFactory messageBuilderFactory;
    private final MessageChannel messageChannel;

    public StartMessageDeployedEventMessageProducer(@NonNull MessageChannel messageChannel,
                                                    @NonNull StartMessageDeployedEventMessageBuilderFactory messageBuilderFactory) {
        this.messageChannel = messageChannel;
        this.messageBuilderFactory = messageBuilderFactory;
    }
    
    @Override
    public void onEvent(StartMessageDeployedEvent event) {
        logger.debug("onEvent: {}", event);

        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            throw new IllegalStateException("requires active transaction synchronization");
        }
        
        StartMessageSubscription messageSubscription = event.getEntity()
                                                            .getMessageSubscription();

        MessageEventPayload messageEventPayload = MessageEventPayloadBuilder.messageEvent(messageSubscription.getEventName())
                                                                            .withCorrelationKey(messageSubscription.getConfiguration())
                                                                            .build();
        
        
        Message<MessageEventPayload> message = messageBuilderFactory.create(event)
                                                                    .withPayload(messageEventPayload)
                                                                    .setHeader(MessageEventHeaders.MESSAGE_EVENT_TYPE,
                                                                               event.getEventType()
                                                                                    .name())
                                                                    .build();

        TransactionSynchronizationManager.registerSynchronization(new MessageSenderTransactionSynchronization(message,
                                                                                                              messageChannel));
        
    }

}
