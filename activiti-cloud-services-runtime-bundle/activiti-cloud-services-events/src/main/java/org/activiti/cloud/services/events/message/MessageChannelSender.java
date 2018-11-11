package org.activiti.cloud.services.events.message;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.util.Assert;

public class MessageChannelSender {
    
    private final Message<?> message;
    
    public MessageChannelSender(Message<?> message) {
        Assert.notNull(message, "message must not be null");

        this.message = message;
    }

    public boolean sendTo(MessageChannel messageChannel) {
        Assert.notNull(messageChannel, "messageChannel must not be null");

        return messageChannel.send(message);
    }
        
    public <T> Message<T> getMessage() {
        return (Message<T>) message;
    }
    

}
