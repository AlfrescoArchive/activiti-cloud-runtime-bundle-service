package org.activiti.cloud.services.messages.channels;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;

public interface BpmnMessageChannels {

    String MESSAGE_WAITING = "messageWaiting";
    String MESSAGE_SENT = "messageSent";
    String MESSAGE_RECEIVED = "messageReceived";
    String MESSAGE_DELIVERED = "messageDelivered";

    @Output(MESSAGE_WAITING)
    MessageChannel waiting();

    @Output(MESSAGE_RECEIVED)
    MessageChannel received();
    
    @Input(MESSAGE_DELIVERED)
    SubscribableChannel delivered();
    
    @Output(MESSAGE_SENT)
    MessageChannel sent();

}
