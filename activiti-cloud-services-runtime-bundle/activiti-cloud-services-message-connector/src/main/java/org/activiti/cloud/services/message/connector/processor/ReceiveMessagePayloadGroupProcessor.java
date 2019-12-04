package org.activiti.cloud.services.message.connector.processor;

import java.util.Collection;
import java.util.Comparator;

import org.activiti.api.process.model.builders.MessagePayloadBuilder;
import org.activiti.api.process.model.payloads.MessageEventPayload;
import org.activiti.api.process.model.payloads.ReceiveMessagePayload;
import org.activiti.cloud.services.message.connector.support.MessageTimestampComparator;
import org.activiti.cloud.services.message.connector.support.SpELEvaluatingMessageListProcessor;
import org.activiti.cloud.services.message.connector.support.SpELEvaluatingReleaseStrategy;
import org.springframework.integration.aggregator.MessageListProcessor;
import org.springframework.integration.aggregator.ReleaseStrategy;
import org.springframework.integration.store.MessageGroup;
import org.springframework.integration.store.MessageGroupStore;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;

public class ReceiveMessagePayloadGroupProcessor implements MessageGroupProcessorHandler {
    
    private final static String condition = "!messages.?[headers['eventType'] == 'MESSAGE_WAITING'].empty " 
                                             + "&& !messages.?[headers['eventType'] == 'MESSAGE_SENT'].empty";

    private final Comparator<Message<?>> comparator = new MessageTimestampComparator();

    private final static ReleaseStrategy strategy = new SpELEvaluatingReleaseStrategy(condition);
    
    private final static MessageListProcessor processor = new SpELEvaluatingMessageListProcessor("#this.?[headers['eventType'] == 'MESSAGE_SENT']");

    private final MessageGroupStore messageGroupStore;

    public ReceiveMessagePayloadGroupProcessor(MessageGroupStore messageGroupStore) {
        this.messageGroupStore = messageGroupStore;
    }
    
    @Override
    public Message<?> handle(MessageGroup group) {
        
        if (canProcess(group)) {
            Message<?> result = process(group.getMessages()).stream()
                                                            .min(comparator)
                                                            .get();
                                    
            messageGroupStore.removeMessagesFromGroup(group.getGroupId(), 
                                                      result);
            
            return receiveMessagePayload(result);
        }
        
        return null;
    }
    
    private Message<?> receiveMessagePayload(Message<?> message) {
        MessageEventPayload messageEventPayload = MessageEventPayload.class.cast(message.getPayload());

        ReceiveMessagePayload payload = MessagePayloadBuilder.receive(messageEventPayload.getName())
                                                             .withCorrelationKey(messageEventPayload.getCorrelationKey())
                                                             .withVariables(messageEventPayload.getVariables())
                                                             .build();
        return MessageBuilder.withPayload(payload)
                             .copyHeaders(message.getHeaders())
                             .setHeader("payloadType", ReceiveMessagePayload.class.getSimpleName())
                             .build();
    }
    
    private boolean canProcess(MessageGroup group) {
        return strategy.canRelease(group);
    }

    private Collection<Message<?>> process(Collection<Message<?>> messages) {
        return (Collection<Message<?>>) processor.process(messages);
    }

}
