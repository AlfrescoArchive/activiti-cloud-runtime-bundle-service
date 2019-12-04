package org.activiti.cloud.services.message.connector.config;

import org.activiti.cloud.services.message.connector.MessageConnectorConsumer;
import org.activiti.cloud.services.message.connector.channels.MessageConnectorChannels;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;

@Configuration
@EnableBinding({
    MessageConnectorChannels.Consumer.class,
    MessageConnectorChannels.Producer.class
})
@PropertySource("classpath:config/message-connector-channels.properties")
@Import(MessageConnectorAggregatorConfiguration.class)
public class MessageConnectorAutoConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    public MessageConnectorConsumer messageConnectorConsumer(Processor processor) {
        return new MessageConnectorConsumer(processor);
    }
    
    @Bean
    public IntegrationFlow outputProducerIntegrationFlow(Processor processor,
                                                         MessageConnectorChannels.Producer producer) {
        return IntegrationFlows.from(processor.output())
                               .wireTap(spec -> spec.filter("headers.payloadType == 'StartMessagePayload'")
                                                    .log()
                                                    .channel(producer.startMessagePayloadProducerChannel()))
                               .wireTap(spec -> spec.filter("headers.payloadType == 'ReceiveMessagePayload'")
                                                    .log()
                                                    .channel(producer.receiveMessagePayloadProducerChannel()))
                               .get();
    }
    
}
