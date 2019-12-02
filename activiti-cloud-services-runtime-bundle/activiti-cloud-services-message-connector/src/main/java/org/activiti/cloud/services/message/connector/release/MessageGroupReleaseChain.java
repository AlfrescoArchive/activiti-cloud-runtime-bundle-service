package org.activiti.cloud.services.message.connector.release;

import org.activiti.cloud.services.message.connector.support.Chain;
import org.springframework.integration.store.MessageGroup;

public interface MessageGroupReleaseChain extends Chain<MessageGroup, Boolean> {

}
