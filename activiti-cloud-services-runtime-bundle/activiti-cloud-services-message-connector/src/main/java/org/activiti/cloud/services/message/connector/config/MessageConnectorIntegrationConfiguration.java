/*
 * Copyright 2018 Alfresco, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.cloud.services.message.connector.config;

import java.util.Optional;

import org.activiti.cloud.services.message.connector.aggregator.MessageConnectorAggregator;
import org.activiti.cloud.services.message.connector.aggregator.MessageConnectorAggregatorFactoryBean;
import org.activiti.cloud.services.message.connector.channels.MessageConnectorProcessor;
import org.activiti.cloud.services.message.connector.integration.MessageConnectorIntegrationFlow;
import org.activiti.cloud.services.message.connector.processor.MessageGroupProcessorChain;
import org.activiti.cloud.services.message.connector.processor.MessageGroupProcessorHandlerChain;
import org.activiti.cloud.services.message.connector.processor.ReceiveMessagePayloadGroupProcessor;
import org.activiti.cloud.services.message.connector.processor.StartMessagePayloadGroupProcessor;
import org.activiti.cloud.services.message.connector.release.MessageGroupReleaseChain;
import org.activiti.cloud.services.message.connector.release.MessageGroupReleaseStrategyChain;
import org.activiti.cloud.services.message.connector.release.MessageSentReleaseHandler;
import org.activiti.cloud.services.message.connector.support.ChainBuilder;
import org.activiti.cloud.services.message.connector.support.LockTemplate;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.IntegrationMessageHeaderAccessor;
import org.springframework.integration.aggregator.CorrelationStrategy;
import org.springframework.integration.aggregator.DefaultAggregatingMessageGroupProcessor;
import org.springframework.integration.aggregator.HeaderAttributeCorrelationStrategy;
import org.springframework.integration.aggregator.MessageGroupProcessor;
import org.springframework.integration.aggregator.ReleaseStrategy;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.config.EnableIntegrationManagement;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.handler.MessageProcessor;
import org.springframework.integration.handler.advice.IdempotentReceiverInterceptor;
import org.springframework.integration.metadata.ConcurrentMetadataStore;
import org.springframework.integration.selector.MetadataStoreSelector;
import org.springframework.integration.store.MessageGroupStore;
import org.springframework.integration.support.locks.LockRegistry;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * A Processor app that performs aggregation.
 *
 */
@Configuration
@EnableIntegration
@EnableBinding(MessageConnectorProcessor.class)
//@EnableMessageHistory // FIXME does not work with RabbitMQ binder
@EnableIntegrationManagement
@EnableConfigurationProperties(MessageAggregatorProperties.class)
@EnableTransactionManagement
public class MessageConnectorIntegrationConfiguration {

    @Autowired
    private MessageAggregatorProperties properties;
    
    // TODO controlBus()
    
    @Bean("messageConnectorIntegrationFlow")
    public IntegrationFlow messageConnectorIntegrationFlow(MessageConnectorProcessor processor,
                                                           MessageGroupStore messageStore,
                                                           CorrelationStrategy correlationStrategy,
                                                           LockTemplate lockTemplate,
                                                           MessageConnectorAggregator messageConnectorAggregator,
                                                           IdempotentReceiverInterceptor idempotentReceiverInterceptor) {
        return new MessageConnectorIntegrationFlow(processor,
                                                   messageStore,
                                                   correlationStrategy,
                                                   lockTemplate,
                                                   messageConnectorAggregator,
                                                   idempotentReceiverInterceptor);
    }
    
    @Bean
    public MessageConnectorAggregator messageConnectorAggregator(ObjectProvider<CorrelationStrategy> correlationStrategy,
                                                                 ObjectProvider<ReleaseStrategy> releaseStrategy,
                                                                 ObjectProvider<MessageGroupProcessor> messageGroupProcessor,
                                                                 ObjectProvider<MessageGroupStore> messageStore,
                                                                 ObjectProvider<LockRegistry> lockRegistry,
                                                                 ObjectProvider<BeanFactory> beanFactory) {
        MessageConnectorAggregatorFactoryBean factoryBean = new MessageConnectorAggregatorFactoryBean();
        factoryBean.setOutputChannelName(Processor.OUTPUT);
        factoryBean.setExpireGroupsUponCompletion(true);
        factoryBean.setCompleteGroupsWhenEmpty(true);
        factoryBean.setSendPartialResultOnExpiry(true);
        factoryBean.setGroupTimeoutExpression(this.properties.getGroupTimeout());
        factoryBean.setPopSequence(false);
        factoryBean.setLockRegistry(lockRegistry.getIfAvailable());
        factoryBean.setCorrelationStrategy(correlationStrategy.getIfAvailable());
        factoryBean.setReleaseStrategy(releaseStrategy.getIfAvailable());
        factoryBean.setBeanFactory(beanFactory.getObject());

        MessageGroupProcessor groupProcessor = messageGroupProcessor.getIfAvailable();

        if (groupProcessor == null) {
            groupProcessor = new DefaultAggregatingMessageGroupProcessor();
            ((BeanFactoryAware) groupProcessor).setBeanFactory(beanFactory.getObject());
        }
        factoryBean.setProcessorBean(groupProcessor);

        factoryBean.setMessageStore(messageStore.getIfAvailable());

        return factoryBean.getObject();
    }

    @Bean
    public LockTemplate lockTemplate(LockRegistry lockRegistry) {
        return new LockTemplate(lockRegistry);
    }
    
    @Bean
    @ConditionalOnMissingBean
    public CorrelationStrategy correlationStrategy() {
        // FIXME refactor correlation strategy by Rb, message, correlationKey headers
        return new HeaderAttributeCorrelationStrategy(IntegrationMessageHeaderAccessor.CORRELATION_ID); 
    }
    
    @Bean
    @ConditionalOnMissingBean(name = "metadataStoreKeyStrategy")
    public MessageProcessor<String> metadataStoreKeyStrategy() {
        // FIXME refactor idempotent message key strategy using dynamic headers
        return m -> Optional.ofNullable(m.getHeaders().get("messageId"))
                            .map(Object::toString)
                            .orElseGet(() -> m.getHeaders().getId()
                                                           .toString());
    }
    
    @Bean
    @ConditionalOnMissingBean
    public MetadataStoreSelector metadataStoreSelector(ConcurrentMetadataStore metadataStore,
                                                       MessageProcessor<String> metadataStoreKeyStrategy) {
        return new MetadataStoreSelector(metadataStoreKeyStrategy,
                                         metadataStore);
    }
    
    @Bean
    @ConditionalOnMissingBean
    public IdempotentReceiverInterceptor idempotentReceiverInterceptor(MetadataStoreSelector metadataStoreSelector) {
        IdempotentReceiverInterceptor interceptor = new IdempotentReceiverInterceptor(metadataStoreSelector);
        
        interceptor.setDiscardChannelName("errorChannel");
        
        return interceptor;
    }

    @Bean
    @ConditionalOnMissingBean
    public MessageGroupProcessorChain messageGroupProcessorChain(MessageGroupStore messageGroupStore) {
        return ChainBuilder.of(MessageGroupProcessorChain.class)
                           .first(new StartMessagePayloadGroupProcessor(messageGroupStore))
                           .then(new ReceiveMessagePayloadGroupProcessor(messageGroupStore))
                           .build();
    }
    
    @Bean
    @ConditionalOnMissingBean
    public MessageGroupProcessor messageConnectorPayloadGroupProcessor(MessageGroupProcessorChain messageGroupProcessorChain) {
        return new MessageGroupProcessorHandlerChain(messageGroupProcessorChain);
    }

    @Bean
    @ConditionalOnMissingBean
    public MessageGroupReleaseChain messageGroupReleaseChain(MessageGroupStore messageGroupStore) {
        return ChainBuilder.of(MessageGroupReleaseChain.class)
                           .first(new MessageSentReleaseHandler())
                           .build();
    }
    
    @Bean
    @ConditionalOnMissingBean
    public ReleaseStrategy messageConnectorReleaseStrategy(MessageGroupReleaseChain messageGroupReleaseChain) {
        return new MessageGroupReleaseStrategyChain(messageGroupReleaseChain);
    }
    
}
