package org.activiti.cloud.services.message.connector.processor;

import org.activiti.cloud.services.message.connector.support.Chain;
import org.springframework.integration.store.MessageGroup;

public interface MessageGroupProcessorChain extends Chain<MessageGroup, Object>{

}
