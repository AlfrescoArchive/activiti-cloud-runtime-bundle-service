package org.activiti.cloud.starter.tests.runtime;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;

public interface MessageConnectorChannels {

    public static final String DELIVER_MESSAGES = "deliverMessages";
    public static final String RECEIVED_MESSAGES = "receivedMessages";
    public static final String WAITING_MESSAGES = "waitingMessages";
    public static final String SENT_MESSAGES = "sentMessages";

    @Input(SENT_MESSAGES)
    SubscribableChannel sent();

    @Input(WAITING_MESSAGES)
    SubscribableChannel waiting();

    @Input(RECEIVED_MESSAGES)
    SubscribableChannel received();
    
    @Output(DELIVER_MESSAGES)
    MessageChannel deliver();

}
