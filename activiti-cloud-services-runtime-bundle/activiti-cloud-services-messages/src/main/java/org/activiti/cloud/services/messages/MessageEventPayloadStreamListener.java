package org.activiti.cloud.services.messages;

import org.activiti.api.process.model.builders.MessagePayloadBuilder;
import org.activiti.api.process.model.payloads.MessageEventPayload;
import org.activiti.api.process.model.payloads.ReceiveMessagePayload;
import org.activiti.cloud.services.messages.channels.BpmnMessageChannels;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.MessagingException;

public class MessageEventPayloadStreamListener {
    
    private final ApplicationEventPublisher eventPublisher;
    
    public MessageEventPayloadStreamListener(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }
    
    @StreamListener(BpmnMessageChannels.MESSAGE_DELIVERED)
    public void handleMessage(MessageEventPayload message) throws MessagingException {

        ReceiveMessagePayload receiveMessagePayload = MessagePayloadBuilder.receive(message.getName())
                                                                           .withCorrelationKey(message.getCorrelationKey())
                                                                           .withVariables(message.getVariables())
                                                                           .build();
        eventPublisher.publishEvent(receiveMessagePayload);
    }
}