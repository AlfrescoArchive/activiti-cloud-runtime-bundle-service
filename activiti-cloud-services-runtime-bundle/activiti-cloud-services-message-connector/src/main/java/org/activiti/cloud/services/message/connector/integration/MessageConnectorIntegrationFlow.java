package org.activiti.cloud.services.message.connector.integration;

import org.activiti.api.process.model.payloads.MessageEventPayload;
import org.activiti.cloud.services.message.connector.advice.MessageReceivedHandlerAdvice;
import org.activiti.cloud.services.message.connector.advice.SubscriptionCancelledHandlerAdvice;
import org.activiti.cloud.services.message.connector.aggregator.MessageConnectorAggregator;
import org.activiti.cloud.services.message.connector.channels.MessageConnectorProcessor;
import org.activiti.cloud.services.message.connector.support.LockTemplate;
import org.springframework.integration.aggregator.CorrelationStrategy;
import org.springframework.integration.dsl.IntegrationFlowAdapter;
import org.springframework.integration.dsl.IntegrationFlowDefinition;
import org.springframework.integration.dsl.Transformers;
import org.springframework.integration.handler.advice.IdempotentReceiverInterceptor;
import org.springframework.integration.store.MessageGroupStore;

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
                                        .filter("headers.eventType != null", // FIXME use MessageSelector
                                                filter -> filter.id("filter")
                                                                .discardChannel("errorChannel"))
                                        .transform(Transformers.fromJson(MessageEventPayload.class))
                                        .handle(messageConnectorAggregator,
                                                handler -> handler.id("aggregator")
                                                                  .advice(new MessageReceivedHandlerAdvice(messageStore,
                                                                                                           correlationStrategy,
                                                                                                           lockTemplate))
                                                                  .advice(new SubscriptionCancelledHandlerAdvice(messageStore,
                                                                                                                 correlationStrategy,
                                                                                                                 lockTemplate)))
                                        .log()
                                        .channel(processor.output()),
                            flowSpec -> flowSpec.transactional()
                                                .id("gateway")
                                                .requiresReply(false)
                                                .async(true)
                                                .replyTimeout(0L)
                                                .advice(idempotentReceiverInterceptor));
    }

}
