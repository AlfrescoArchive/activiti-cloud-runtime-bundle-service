package org.activiti.cloud.services.message.connector.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:config/message-connector-channels.properties")
@Import(MessageConnectorIntegrationConfiguration.class)
public class MessageConnectorAutoConfiguration {
    
}
