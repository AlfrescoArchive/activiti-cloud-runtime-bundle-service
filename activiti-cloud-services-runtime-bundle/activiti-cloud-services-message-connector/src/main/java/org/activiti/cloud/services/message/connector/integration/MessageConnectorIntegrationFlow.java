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

package org.activiti.cloud.services.message.connector.integration;

import static org.activiti.cloud.services.message.connector.integration.MessageEventHeaders.MESSAGE_EVENT_CORRELATION_KEY;
import static org.activiti.cloud.services.message.connector.integration.MessageEventHeaders.MESSAGE_EVENT_NAME;
import static org.activiti.cloud.services.message.connector.integration.MessageEventHeaders.MESSAGE_EVENT_TYPE;
import static org.activiti.cloud.services.message.connector.integration.MessageEventHeaders.SERVICE_FULL_NAME;

import java.util.Objects;

import org.activiti.api.process.model.payloads.MessageEventPayload;
import org.activiti.cloud.services.message.connector.advice.MessageReceivedHandlerAdvice;
import org.activiti.cloud.services.message.connector.advice.SubscriptionCancelledHandlerAdvice;
import org.activiti.cloud.services.message.connector.aggregator.MessageConnectorAggregator;
import org.activiti.cloud.services.message.connector.channels.MessageConnectorProcessor;
import org.activiti.cloud.services.message.connector.support.LockTemplate;
import org.aopalliance.aop.Advice;
import org.springframework.integration.IntegrationMessageHeaderAccessor;
import org.springframework.integration.aggregator.CorrelationStrategy;
import org.springframework.integration.annotation.Filter;
import org.springframework.integration.annotation.Transformer;
import org.springframework.integration.dsl.IntegrationFlowAdapter;
import org.springframework.integration.dsl.IntegrationFlowDefinition;
import org.springframework.integration.dsl.Transformers;
import org.springframework.integration.handler.advice.IdempotentReceiverInterceptor;
import org.springframework.integration.store.MessageGroupStore;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

public class MessageConnectorIntegrationFlow extends IntegrationFlowAdapter {

    private final MessageConnectorProcessor processor;
    private final MessageGroupStore messageStore;
    private final CorrelationStrategy correlationStrategy;
    private final LockTemplate lockTemplate;
    private final MessageConnectorAggregator messageConnectorAggregator;
    private final IdempotentReceiverInterceptor idempotentReceiverInterceptor;

    public MessageConnectorIntegrationFlow(MessageConnectorProcessor processor,
                                           MessageGroupStore messageStore,
                                           CorrelationStrategy correlationStrategy,
                                           LockTemplate lockTemplate,
                                           MessageConnectorAggregator messageConnectorAggregator,
                                           IdempotentReceiverInterceptor idempotentReceiverInterceptor) {
        this.processor = processor;
        this.messageStore = messageStore;
        this.correlationStrategy = correlationStrategy;
        this.lockTemplate = lockTemplate;
        this.messageConnectorAggregator = messageConnectorAggregator;
        this.idempotentReceiverInterceptor = idempotentReceiverInterceptor;
    }

    @Override
    protected IntegrationFlowDefinition<?> buildFlow() {
        return this.from(processor.input())
                   .gateway(flow -> flow.log()
                                        .filter(Message.class,
                                                this::hasMessageEventTypeHeader,
                                                filterSpec -> filterSpec.id("filter-message-headers")
                                                                        .discardChannel("errorChannel")
                                        )
                                        .enrichHeaders(enricher -> enricher.id("enrich-correlation-id")
                                                                           .headerFunction(IntegrationMessageHeaderAccessor.CORRELATION_ID, 
                                                                                           MessageConnectorIntegrationFlow::getCorrelationId)
                                        )
                                        .transform(Transformers.fromJson(MessageEventPayload.class))
                                        .handle(messageConnectorAggregator,
                                                handler -> handler.id("message-aggregator")
                                                                  .advice(getMessageReceivedHandlerAdvice())
                                                                  .advice(getSubscriptionCancelledHandlerAdvice())
                                        )
                                        .log()
                                        .channel(processor.output()),
                            flowSpec -> flowSpec.transactional()
                                                .id("message-gateway")
                                                .requiresReply(false)
                                                .async(true)
                                                .replyTimeout(0L)
                                                //.advice(retry)
                                                //.notPropagatedHeaders(headerPatterns)
                                                .advice(idempotentReceiverInterceptor));
    }
    
    public Advice getMessageReceivedHandlerAdvice() {
        return new MessageReceivedHandlerAdvice(messageStore,
                                                correlationStrategy,
                                                lockTemplate);
    }    

    public Advice getSubscriptionCancelledHandlerAdvice() {
        return new SubscriptionCancelledHandlerAdvice(messageStore,
                                                      correlationStrategy,
                                                      lockTemplate);
    }

    @Filter
    public boolean hasMessageEventTypeHeader(Message<?> message) {
        return Objects.nonNull(message.getHeaders()
                                      .get(MESSAGE_EVENT_TYPE));
    }
    
    @Transformer
    public static String getCorrelationId(Message<?> message) {
        MessageHeaders headers = message.getHeaders();
        String serviceFullName = headers.get(SERVICE_FULL_NAME, String.class);
        String messageEventName = headers.get(MESSAGE_EVENT_NAME, String.class);
        String messageCorrelationKey = headers.get(MESSAGE_EVENT_CORRELATION_KEY, String.class);
        
        StringBuilder builder = new StringBuilder();
        builder.append(serviceFullName)
               .append(":")
               .append(messageEventName);
               
        if (messageCorrelationKey != null) {
            builder.append(":")
                   .append(messageCorrelationKey);
        }
        
        return builder.toString();        
    }

}
