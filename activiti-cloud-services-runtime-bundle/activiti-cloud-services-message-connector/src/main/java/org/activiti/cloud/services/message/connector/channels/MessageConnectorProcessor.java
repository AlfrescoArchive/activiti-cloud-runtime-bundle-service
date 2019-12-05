package org.activiti.cloud.services.message.connector.channels;

import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.cloud.stream.messaging.Source;

public interface MessageConnectorProcessor extends Source, Sink {

}
