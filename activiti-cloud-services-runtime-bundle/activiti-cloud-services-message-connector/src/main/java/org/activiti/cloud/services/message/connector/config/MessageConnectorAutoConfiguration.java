package org.activiti.cloud.services.message.connector.config;

import org.activiti.cloud.services.message.connector.MessageConnectorChannels;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@EnableBinding({
    MessageConnectorChannels.Consumer.class,
    MessageConnectorChannels.Producer.class
})
@PropertySource("classpath:config/message-connector-channels.properties")
public class MessageConnectorAutoConfiguration {

}
