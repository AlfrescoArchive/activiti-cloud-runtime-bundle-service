package org.activiti.cloud.services.message.connector.controlbus;

import org.springframework.integration.annotation.MessagingGateway;

@MessagingGateway
public interface ControlBusGateway {

    void send(String command);

}