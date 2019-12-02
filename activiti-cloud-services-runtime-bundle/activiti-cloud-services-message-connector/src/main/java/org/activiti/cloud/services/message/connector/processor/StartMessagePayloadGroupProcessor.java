package org.activiti.cloud.services.message.connector.processor;

import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

import org.activiti.api.process.model.payloads.StartMessagePayload;
import org.activiti.cloud.services.message.connector.support.MessageTimestampComparator;
import org.activiti.cloud.services.message.connector.support.SpELEvaluatingMessageListProcessor;
import org.activiti.cloud.services.message.connector.support.SpELEvaluatingReleaseStrategy;
import org.springframework.integration.aggregator.MessageListProcessor;
import org.springframework.integration.aggregator.ReleaseStrategy;
import org.springframework.integration.store.MessageGroup;
import org.springframework.integration.store.MessageGroupStore;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;

public class StartMessagePayloadGroupProcessor implements MessageGroupProcessorHandler {
    
    private final static String condition = "!messages.?[headers['eventType'] == 'START_MESSAGE_DEPLOYED'].empty " 
                                             + "&& !messages.?[headers['eventType'] == 'MESSAGE_SENT'].empty";

    private final static ReleaseStrategy strategy = new SpELEvaluatingReleaseStrategy(condition);
    
    private final Comparator<Message<?>> comparator = new MessageTimestampComparator();
    
    private final static MessageListProcessor processor = new SpELEvaluatingMessageListProcessor("#this.?[headers['eventType'] == 'MESSAGE_SENT']");

    private final MessageGroupStore messageGroupStore;

    public StartMessagePayloadGroupProcessor(MessageGroupStore messageGroupStore) {
        this.messageGroupStore = messageGroupStore;
    }    
    @Override
    public Collection<Message<?>> handle(MessageGroup group) {
        
        if (canProcess(group)) {
            Collection<Message<?>> result = process(group.getMessages());

            messageGroupStore.removeMessagesFromGroup(group.getGroupId(),
                                                      result);
            return result.stream()
                         .sorted(comparator)
                         .map(this::startMessagePayload)
                         .collect(Collectors.toList());
        }
        
        return null;
    }
    
    private Message<?> startMessagePayload(Message<?> message) {
        return MessageBuilder.fromMessage(message)
                             .setHeader("payloadType",
                                        StartMessagePayload.class.getSimpleName())
                             .build();
    }
    
    public boolean canProcess(MessageGroup group) {
        return strategy.canRelease(group);
    }
    
    public <R> R process(Collection<Message<?>> messages) {
        return (R) processor.process(messages);
    }

}
