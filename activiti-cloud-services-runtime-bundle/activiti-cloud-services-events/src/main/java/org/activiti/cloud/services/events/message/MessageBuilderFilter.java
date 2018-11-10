package org.activiti.cloud.services.events.message;

import org.springframework.messaging.support.MessageBuilder;

public interface MessageBuilderFilter<T> {
	public MessageBuilder<T> apply(MessageBuilder<T> request);
}
