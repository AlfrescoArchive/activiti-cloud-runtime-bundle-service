package org.activiti.cloud.services.message.connector.support;

import java.util.Comparator;

import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

public class MessageTimestampComparator implements Comparator<Message<?>> {
    
    public static final MessageTimestampComparator INSTANCE = new MessageTimestampComparator();

    @Override
    public int compare(Message<?> o1, Message<?> o2) {
        Long sequenceNumber1 = getTimestamp(o1);
        Long sequenceNumber2 = getTimestamp(o2);

        return Long.compare(sequenceNumber1, sequenceNumber2);
    }
    
    @Nullable
    public Long getTimestamp(Message<?> m) {
        Object value = m.getHeaders().get(MessageHeaders.TIMESTAMP);
        if (value == null) {
            return null;
        }
        return (value instanceof Long ? (Long) value : Long.parseLong(value.toString()));
    }
    

}
