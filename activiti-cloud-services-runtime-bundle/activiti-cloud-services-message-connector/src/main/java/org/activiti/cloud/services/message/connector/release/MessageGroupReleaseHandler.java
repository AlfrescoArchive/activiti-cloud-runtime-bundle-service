package org.activiti.cloud.services.message.connector.release;

import org.activiti.cloud.services.message.connector.support.Handler;
import org.springframework.integration.store.MessageGroup;

public interface MessageGroupReleaseHandler extends Handler<MessageGroup, Boolean> {

}
