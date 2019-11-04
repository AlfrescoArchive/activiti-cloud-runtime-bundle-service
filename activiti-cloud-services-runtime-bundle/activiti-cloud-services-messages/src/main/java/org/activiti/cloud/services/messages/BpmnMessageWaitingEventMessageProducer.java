package org.activiti.cloud.services.messages;

import org.activiti.api.process.model.events.BPMNMessageWaitingEvent;
import org.activiti.api.process.model.payloads.MessageEventPayload;
import org.activiti.api.process.runtime.events.listener.BPMNElementEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class BpmnMessageWaitingEventMessageProducer implements BPMNElementEventListener<BPMNMessageWaitingEvent> {

    private static final Logger logger = LoggerFactory.getLogger(BpmnMessageWaitingEventMessageProducer.class);

    private final MessageEventPayloadMessageBuilderFactory messageBuilderFactory;
    private final MessageChannel messageChannel;

    public BpmnMessageWaitingEventMessageProducer(@NonNull MessageChannel messageChannel,
                                                  @NonNull MessageEventPayloadMessageBuilderFactory messageBuilderFactory) {
        this.messageChannel = messageChannel;
        this.messageBuilderFactory = messageBuilderFactory;
    }

    @Override
    public void onEvent(BPMNMessageWaitingEvent event) {
        logger.debug("onEvent: {}", event);
        
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            throw new IllegalStateException("requires active transaction synchronization");
        }

        MessageEventPayload messageEventPayload = event.getEntity()
                                                       .getMessagePayload();

        Message<MessageEventPayload> message = messageBuilderFactory.create(messageEventPayload)
                                                                    .withPayload(messageEventPayload)
                                                                    //.setHeader(ROUTING_KEY, destination)
                                                                    .build();

        // Let's send message right after the main transaction has successfully committed. 
        TransactionSynchronizationManager.registerSynchronization(new BpmnMessageTransactionSynchronization(message,
                                                                                                            messageChannel));
    }

}
