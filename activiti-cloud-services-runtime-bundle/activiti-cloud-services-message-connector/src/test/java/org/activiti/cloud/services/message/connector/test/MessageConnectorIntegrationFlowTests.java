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


package org.activiti.cloud.services.message.connector.test;


import static java.util.Collections.singletonMap;
import static org.activiti.cloud.services.message.connector.integration.MessageEventHeaders.MESSAGE_EVENT_CORRELATION_KEY;
import static org.activiti.cloud.services.message.connector.integration.MessageEventHeaders.MESSAGE_EVENT_ID;
import static org.activiti.cloud.services.message.connector.integration.MessageEventHeaders.MESSAGE_EVENT_NAME;
import static org.activiti.cloud.services.message.connector.integration.MessageEventHeaders.MESSAGE_EVENT_TYPE;
import static org.activiti.cloud.services.message.connector.integration.MessageEventHeaders.SERVICE_FULL_NAME;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import org.activiti.api.process.model.builders.MessageEventPayloadBuilder;
import org.activiti.api.process.model.events.BPMNMessageEvent.MessageEvents;
import org.activiti.api.process.model.events.MessageDefinitionEvent.MessageDefinitionEvents;
import org.activiti.api.process.model.events.MessageSubscriptionEvent.MessageSubscriptionEvents;
import org.activiti.api.process.model.payloads.MessageEventPayload;
import org.activiti.cloud.services.message.connector.aggregator.MessageConnectorAggregator;
import org.activiti.cloud.services.message.connector.channels.MessageConnectorProcessor;
import org.activiti.cloud.services.message.connector.config.MessageConnectorAutoConfiguration;
import org.activiti.cloud.services.message.connector.config.MessageConnectorIntegrationConfiguration;
import org.activiti.cloud.services.message.connector.integration.MessageConnectorIntegrationFlow;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.context.annotation.Import;
import org.springframework.integration.hazelcast.store.HazelcastMessageStore;
import org.springframework.integration.jdbc.store.JdbcMessageStore;
import org.springframework.integration.mongodb.store.ConfigurableMongoDbMessageStore;
import org.springframework.integration.redis.store.RedisMessageStore;
import org.springframework.integration.store.MessageGroup;
import org.springframework.integration.store.MessageGroupStore;
import org.springframework.integration.store.SimpleMessageStore;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * Tests for the Message Connector Aggregator Processor.
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "spring.cloud.stream.bindings.input.contentType=application/x-java-object",
                "spring.cloud.stream.bindings.output.contentType=application/x-java-object"
        }
)
@DirtiesContext
@Import({MessageConnectorIntegrationConfiguration.class})
public abstract class MessageConnectorIntegrationFlowTests {

    protected ObjectMapper objectMapper = new ObjectMapper();
    
    @Autowired
    protected MessageConnectorProcessor channels;
    
    @Autowired 
    protected MessageCollector collector;

    @Autowired
    protected MessageGroupStore messageGroupStore;

    @Autowired
    protected MessageConnectorAggregator aggregatingMessageHandler;
    
    // FIXME 
    @SpringBootApplication(exclude = MessageConnectorAutoConfiguration.class)
    public static class DefaultAggregatorApplication {
        
    }
    
    @TestPropertySource(properties = {
            "aggregator.message-store-type=simple"})
    public static class SimpleMessageStoreTests extends MessageConnectorIntegrationFlowTests {

        @Test
        public void testMessageStore() throws Exception {
            assertThat(this.aggregatingMessageHandler.getMessageStore()).isInstanceOf(SimpleMessageStore.class);
        }
    }

    @TestPropertySource(properties = {
            "aggregator.message-store-type=jdbc"})
    public static class JdbcMessageStoreTests extends MessageConnectorIntegrationFlowTests {

        @Test
        public void testMessageStore() throws Exception {
            assertThat(this.aggregatingMessageHandler.getMessageStore()).isInstanceOf(JdbcMessageStore.class);
        }
    }
    
    @TestPropertySource(properties = {
            "aggregator.message-store-type=mongodb",
            "spring.data.mongodb.uri=mongodb://localhost:27017/test?maxPoolSize=150&minPoolSize=50"})
    @Ignore
    public static class MongodbMessageStoreIT extends MessageConnectorIntegrationFlowTests {

        @Test
        public void testMessageStore() throws Exception {
            assertThat(this.aggregatingMessageHandler.getMessageStore()).isInstanceOf(ConfigurableMongoDbMessageStore.class);
        }
    }

    @TestPropertySource(properties = {
            "aggregator.message-store-type=redis",
            "spring.redis.host=localhost",
            "spring.redis.port=6379"})
    @Ignore
    public static class RedisMessageStoreIT extends MessageConnectorIntegrationFlowTests {
        
        @Test
        public void testMessageStore() throws Exception {
            assertThat(this.aggregatingMessageHandler.getMessageStore()).isInstanceOf(RedisMessageStore.class);
        }
    }

    @TestPropertySource(properties = {
            "aggregator.message-store-type=hazelcast"})
    @Ignore
    public static class HazelcastMessageStoreTests extends MessageConnectorIntegrationFlowTests {

        @Test
        public void testMessageStore() throws Exception {
            assertThat(this.aggregatingMessageHandler.getMessageStore()).isInstanceOf(HazelcastMessageStore.class);
        }
    }
    

    @Test(timeout = 10000)
    public void shouldProcessMessageEventsConcurrently() throws InterruptedException, JsonProcessingException {
        // given
        String messageEventName = "start";
        Integer count = 100;

        Message<?> startMessage = startMessageDeployedEvent(messageEventName);
        String correlationId = correlationId(startMessage);
        removeMessageGroup(correlationId);

        send(startMessage);

        assertThat(messageGroup(correlationId).getMessages()).hasSize(1);
        
        // when
        final CountDownLatch start = new CountDownLatch(1);
        final CountDownLatch sent = new CountDownLatch(count);
        
        ExecutorService exec = Executors.newSingleThreadExecutor();
        
        IntStream.range(0, count)
                 .forEach(i -> sendAsync(messageSentEvent(messageEventName),
                                         start,
                                         sent,
                                         exec));
        start.countDown();

        try {
            sent.await();
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // then
        IntStream.range(0, count)
                 .mapToObj(i -> Try.call(() -> poll(1, TimeUnit.SECONDS)))
                 .forEach(out -> assertThat(out).isNotNull());
        
        exec.shutdownNow();

        assertThat(messageGroup(correlationId).getMessages()).hasSize(1);
        
        assertThat(peek()).isNull();
    }
    
    @Test(timeout = 10000)
    public void shouldProcessMessageEventsConcurrentlyInReversedOrder() throws InterruptedException, JsonProcessingException {
        // given
        String messageEventName = "start";
        Integer count = 100;
        Message<?> startMessage = startMessageDeployedEvent(messageEventName);
        String correlationId = correlationId(startMessage);
        
        removeMessageGroup(correlationId);
        
        final CountDownLatch start = new CountDownLatch(1);
        final CountDownLatch sent = new CountDownLatch(count);
        
        ExecutorService exec = Executors.newSingleThreadExecutor();
        
        IntStream.range(0, count)
                 .forEach(i -> sendAsync(messageSentEvent(messageEventName),
                                         start,
                                         sent,
                                         exec));
        start.countDown();

        try {
            sent.await();
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        assertThat(messageGroup(correlationId).getMessages()).hasSize(count);
        
        // when
        send(startMessage);

        // then
        IntStream.range(0, count)
                 .mapToObj(i -> Try.call(() -> poll(3, TimeUnit.SECONDS)))
                 .forEach(out ->
                     assertThat(out).isNotNull()
                 );
        
        exec.shutdownNow();

        assertThat(messageGroup(correlationId).getMessages()).hasSize(1);
        
        assertThat(peek()).isNull();
    }
    
    @Test
    public void testStartMessageBeforeSent() throws Exception {
        // given
        String messageName = "start1";
        Message<?> startMessage = startMessageDeployedEvent(messageName);
        String correlationId = correlationId(startMessage);
        removeMessageGroup(correlationId);
        
        send(startMessage);

        assertThat(messageGroup(correlationId).getMessages()).hasSize(1);
        
        // when
        send(messageSentEvent(messageName, null, "sent1"));
        
        // then
        Message<?> out = poll(0, TimeUnit.SECONDS);
        
        assertThat(peek()).isNull();

        assertThat(out).isNotNull()
                       .extracting(Message::getPayload)
                       .extracting("name", "variables")
                       .contains("start1", 
                                 singletonMap("key", "sent1"));

        assertThat(messageGroup(correlationId).getMessages()).hasSize(1)
                                                             .extracting(Message::getPayload)
                                                             .asList()
                                                             .extracting("name")
                                                             .containsOnly(messageName);
        // when
        send(messageSentEvent(messageName, null, "sent2"));
        
        // then
        out = poll(0, TimeUnit.SECONDS);

        assertThat(peek()).isNull();

        assertThat(out).isNotNull()
                       .extracting(Message::getPayload)
                       .extracting("name", "variables")
                       .contains("start1", 
                                 singletonMap("key", "sent2"));

        assertThat(messageGroup(correlationId).getMessages()).hasSize(1)
                                                             .extracting(Message::getPayload)
                                                             .asList()
                                                             .extracting("name")
                                                             .containsOnly(messageName);
    }

    @Test
    public void testStartMessageAfterSent() throws Exception {
        // given
        String messageName = "start2";
        Message<?> messageSentEvent = messageSentEvent(messageName, null, "sent1");
        String correlationId = correlationId(messageSentEvent);

        messageGroupStore.removeMessageGroup(correlationId);

        send(messageSentEvent);
        
        // when
        send(startMessageDeployedEvent(messageName));
        
        // then
        Message<?> out = poll(0, TimeUnit.SECONDS);
        
        assertThat(peek()).isNull();

        assertThat(out).isNotNull()
                       .extracting(Message::getPayload)
                       .extracting("name", "variables")
                       .contains(messageName,
                                 singletonMap("key", "sent1"));

        assertThat(messageGroup(correlationId).getMessages()).hasSize(1)
                                                             .extracting(Message::getPayload)
                                                             .asList()
                                                             .extracting("name")
                                                             .containsOnly("start2");

        // when
        send(messageSentEvent(messageName, null, "sent2"));
        
        // then
        out = poll(0, TimeUnit.SECONDS);

        assertThat(peek()).isNull();

        assertThat(out).isNotNull()
                       .extracting(Message::getPayload)
                       .extracting("name", "variables")
                       .contains(messageName,
                                 singletonMap("key", "sent2"));

        assertThat(messageGroup(correlationId).getMessages()).hasSize(1)
                                                             .extracting(Message::getPayload)
                                                             .asList()
                                                             .extracting("name")
                                                             .containsOnly("start2");
    }
    
    @Test
    public void testSentMessagesWithBuffer() throws Exception {
        // given
        String messageName = "message";
        String correlationKey = "1";
        Message<?> messageSentEvent = messageSentEvent(messageName, correlationKey, "sent1");
        String correlationId = correlationId(messageSentEvent);

        messageGroupStore.removeMessageGroup(correlationId);

        send(messageSentEvent);
        
        // when
        send(messageWaitingEvent(messageName, correlationKey, "waiting1"));
        send(messageWaitingEvent(messageName, correlationKey, "waiting2"));
        
        // then
        Message<?> out = poll(0, TimeUnit.SECONDS);
        
        assertThat(out).isNotNull()
                       .extracting(Message::getPayload)
                       .extracting("variables")
                       .contains(singletonMap("key", "sent1"));

        assertThat(peek()).isNull();

        assertThat(messageGroup(correlationId).getMessages()).hasSize(2)
                                       .extracting(Message::getPayload)
                                       .asList()
                                       .extracting("variables")
                                       .containsOnly(singletonMap("key", "waiting1"), 
                                                     singletonMap("key", "waiting2"));
        
        // when
        send(messageReceivedEvent(messageName, correlationKey));

        // then
        assertThat(peek()).isNull();
        
        assertThat(messageGroup(correlationId).getMessages()).hasSize(1)
                                                             .extracting(Message::getPayload)
                                                             .asList()
                                                             .extracting("variables")
                                                             .containsOnly(singletonMap("key", "waiting2"));

        // when
        send(messageSentEvent(messageName, correlationKey, "sent2"));

        // then
        out = poll(1, TimeUnit.SECONDS);

        assertThat(out).isNotNull()
                       .extracting(Message::getPayload)
                       .extracting("variables")
                       .contains(singletonMap("key", "sent2"));

        assertThat(peek()).isNull();

        assertThat(messageGroup(correlationId).getMessages()).hasSize(1)
                                                             .extracting(Message::getPayload)
                                                             .asList()
                                                             .extracting("variables")
                                                             .containsOnly(singletonMap("key", "waiting2"));
        
        // then
        send(messageReceivedEvent(messageName, correlationKey));

        assertThat(peek()).isNull();

        assertThat(messageGroup(correlationId).getMessages()).isEmpty();
    }
    
    @Test
    public void testSentMessagesWithBufferInDifferentOrder() throws Exception {
        // given
        String messageName = "message";
        String correlationKey = "1";
        String correlationId = correlationId(messageWaitingEvent(messageName, correlationKey));

        messageGroupStore.removeMessageGroup(correlationId);

        send(messageSentEvent(messageName, correlationKey, "sent1"));
        send(messageSentEvent(messageName, correlationKey, "sent2"));
        
        // when
        send(messageWaitingEvent(messageName, correlationKey, "waiting1"));
        
        // then
        Message<?> out = poll(0, TimeUnit.SECONDS);
        
        assertThat(out).isNotNull()
                       .extracting(Message::getPayload)
                       .extracting("variables")
                       .contains(singletonMap("key", "sent1"));

        assertThat(peek()).isNull();
        
        assertThat(messageGroup(correlationId).getMessages()).hasSize(2)
                                                             .extracting(Message::getPayload)
                                                             .asList()
                                                             .extracting("variables")
                                                             .contains(singletonMap("key", "sent2"),
                                                                       singletonMap("key", "waiting1"));
        // when 
        send(messageReceivedEvent(messageName, correlationKey, "received1"));

        // then
        assertThat(peek()).isNull();
        
        assertThat(messageGroup(correlationId).getMessages()).hasSize(1)
                                                             .extracting(Message::getPayload)
                                                             .asList()
                                                             .extracting("variables")
                                                             .containsOnly(singletonMap("key", "sent2"));
        // when
        send(messageWaitingEvent(messageName, correlationKey, "waiting2"));

        // then
        out = poll(0, TimeUnit.SECONDS);

        assertThat(peek()).isNull();

        assertThat(out).isNotNull()
                       .extracting(Message::getPayload)
                       .extracting("variables")
                       .contains(singletonMap("key", "sent2"));

        assertThat(messageGroup(correlationId).getMessages()).hasSize(1)
                                                             .extracting(Message::getPayload)
                                                             .asList()
                                                             .extracting("variables")
                                                             .containsOnly(singletonMap("key", "waiting2"));
        
        // when
        send(messageReceivedEvent(messageName, correlationKey, "received2"));

        // then
        assertThat(peek()).isNull();

        assertThat(messageGroup(correlationId).getMessages()).isEmpty();
    }
    
    
    @Test
    public void testSubscriptionCancelled() throws Exception {
        // given
        String messageName = "message";
        String correlationKey = "1";
        Message<?> subscriptionCancelled = subscriptionCancelledEvent(messageName, correlationKey);
        String groupName = correlationId(subscriptionCancelled);
        
        messageGroupStore.removeMessageGroup(groupName);

        send(messageWaitingEvent(messageName, correlationKey));
        send(messageWaitingEvent(messageName, correlationKey));

        assertThat(messageGroup(groupName).getMessages()).hasSize(2);
        
        // when
        send(subscriptionCancelled);
        
        // then
        assertThat(peek()).isNull();

        assertThat(messageGroup(groupName).getMessages()).isEmpty();
    }
    

    @Test
    public void testIdempotentMessageInterceptor() throws Exception {
        // given
        String messageName = "message";
        String correlationKey = "1";
        Message<MessageEventPayload> waitingMessage = messageWaitingEvent(messageName, correlationKey);
        String correlationId = MessageConnectorIntegrationFlow.getCorrelationId(waitingMessage);

        messageGroupStore.removeMessageGroup(correlationId);
        
        // when                                      
        send(waitingMessage);
        send(waitingMessage);

        // then
        assertThat(peek()).isNull();

        assertThat(messageGroup(correlationId).getMessages()).isNotNull()
                                                             .hasSize(1);
        // given
        Message<MessageEventPayload> receivedMessage = messageReceivedEvent(messageName, correlationKey);
        
        // when
        send(receivedMessage);
        send(receivedMessage);
        
        // then 
        assertThat(peek()).isNull();
        
        assertThat(messageGroup(correlationId).getMessages()).hasSize(0);
        
    }        
    

    protected MessageBuilder<MessageEventPayload> messageBuilder(String messageName) {
        return messageBuilder(messageName,
                              null,
                              null);
    }
    
    protected MessageBuilder<MessageEventPayload> messageBuilder(String messageName,
                                                                 String correlationKey) {
        return messageBuilder(messageName,
                              correlationKey,
                              null);
    }
    
    protected MessageBuilder<MessageEventPayload> messageBuilder(String messageName,
                                                                 String correlationKey,
                                                                 String businessKey) {
        MessageEventPayload payload = MessageEventPayloadBuilder.messageEvent(messageName)
                                                                .withCorrelationKey(correlationKey)
                                                                .withBusinessKey(businessKey)
                                                                .withVariables(Collections.singletonMap("key", businessKey))
                                                                .build();
        return MessageBuilder.withPayload(payload)
                             .setHeader(MESSAGE_EVENT_NAME, messageName)
                             .setHeader(MESSAGE_EVENT_CORRELATION_KEY, correlationKey)
                             .setHeader(MESSAGE_EVENT_ID, UUID.randomUUID())
                             .setHeader(SERVICE_FULL_NAME, "rb");

    }
    
    protected Message<MessageEventPayload> startMessageDeployedEvent(String messageName) {
        return messageBuilder(messageName,
                              null).setHeader(MESSAGE_EVENT_TYPE, 
                                              MessageDefinitionEvents.START_MESSAGE_DEPLOYED.name())
                                   .build();
    }

    protected Message<MessageEventPayload> messageSentEvent(String messageName) {
        return messageSentEvent(messageName,
                                null);
    }
    
    protected Message<MessageEventPayload> messageSentEvent(String messageName, 
                                                            String correlationKey) {
        return messageSentEvent(messageName,
                                correlationKey,
                                null);
    }

    protected Message<MessageEventPayload> messageSentEvent(String messageName, 
                                                            String correlationKey,
                                                            String businessKey) {
        return messageBuilder(messageName,
                              correlationKey,
                              businessKey).setHeader(MESSAGE_EVENT_TYPE, 
                                                        MessageEvents.MESSAGE_SENT.name())
                                             .build();
    }
    
    protected Message<MessageEventPayload> messageWaitingEvent(String messageName) {
        return messageWaitingEvent(messageName,
                                   null);
    }

    protected Message<MessageEventPayload> messageWaitingEvent(String messageName, 
                                                               String correlationKey,
                                                               String businessKey) {
        return messageBuilder(messageName,
                              correlationKey,
                              businessKey).setHeader(MESSAGE_EVENT_TYPE, 
                                                        MessageEvents.MESSAGE_WAITING.name())
                                             .build();
    }
    
    protected Message<MessageEventPayload> messageWaitingEvent(String messageName, 
                                                               String correlationKey) {
        return messageBuilder(messageName,
                              correlationKey).setHeader(MESSAGE_EVENT_TYPE, 
                                                        MessageEvents.MESSAGE_WAITING.name())
                                             .build();
    }

    protected Message<MessageEventPayload> messageReceivedEvent(String messageName, 
                                                                String correlationKey) {
        return messageReceivedEvent(messageName,
                                    correlationKey,
                                    null);
    }

    protected Message<MessageEventPayload> messageReceivedEvent(String messageName, 
                                                                String correlationKey,
                                                                String businessKey) {
        return messageBuilder(messageName,
                              correlationKey,
                              businessKey).setHeader(MESSAGE_EVENT_TYPE,
                                                     MessageEvents.MESSAGE_RECEIVED.name())
                                          .build();
    }
    
    protected Message<MessageEventPayload> subscriptionCancelledEvent(String messageName, 
                                                                      String correlationKey) {
        return messageBuilder(messageName,
                              correlationKey).setHeader(MESSAGE_EVENT_TYPE, 
                                                        MessageSubscriptionEvents.MESSAGE_SUBSCRIPTION_CANCELLED.name())
                                             .build();
    }

    protected void send(Message<?> message) throws JsonProcessingException {
        String json = objectMapper.writeValueAsString(message.getPayload());
        
        this.channels.input()
                     .send(MessageBuilder.withPayload(json)
                                         .copyHeaders(message.getHeaders())
                                         .build());        
    }
 
    @SuppressWarnings("unchecked")
    protected <T> Message<T> poll(long timeout, TimeUnit unit) throws InterruptedException {
        return (Message<T>) this.collector.forChannel(this.channels.output())
                                          .poll(timeout, unit);
    }

    @SuppressWarnings("unchecked")
    protected <T> Message<T> peek() {
        return (Message<T>) this.collector.forChannel(this.channels.output())
                                          .peek();
    }
 
    protected MessageGroup messageGroup(String groupName) {
        return aggregatingMessageHandler.getMessageStore()
                                        .getMessageGroup(groupName);
    }

    protected String correlationId(Message<?> message) { 
        return MessageConnectorIntegrationFlow.getCorrelationId(message);
    }

    protected void removeMessageGroup(String correlationId) {
        messageGroupStore.removeMessageGroup(correlationId);
    }
    
    protected void sendAsync(final Message<?> message,
                             final CountDownLatch start,
                             final CountDownLatch sent,
                             ExecutorService exec) {
        exec.execute(() -> {
            try {
                start.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            Try.run(() -> send(message));
            
            sent.countDown();
        });
    }
    
    static class Try {

        @FunctionalInterface
        public interface ExceptionWrapper<E> {
            E wrap(Exception e);
        }

        @FunctionalInterface
        public interface ConsumerExceptionWrapper<T, E extends Exception> {
            void accept(T t) throws E;
        }
        
        @FunctionalInterface
        public interface RunnableExceptionWrapper {
            void run() throws Exception;
        }
        
        @FunctionalInterface
        public interface FunctionExceptionWrapper<T, R, E extends Exception> {
            R apply(T t) throws E;
        }

        @FunctionalInterface
        public interface SupplierExceptionWrapper<T, E extends Exception> {
            T get() throws E;
        }    
        
        public static <T> T call(Callable<T> callable) throws RuntimeException {
            return call(callable, RuntimeException::new);
        }
        
        public static <T> Consumer<T> consumer(Consumer<T> consumer) throws RuntimeException {
            return consumer(consumer, RuntimeException::new);
        }

        public static <T,R> Function<T,R> function(Function<T,R> function) throws RuntimeException {
            return function(function, RuntimeException::new);
        }
        

        public static void run(RunnableExceptionWrapper runnable) {
            try {
                runnable.run();
            } catch (Exception e) {
                sneakyThrow(e);
            }
        }
        

        /** .forEach(Try.consumer(name -> System.out.println(Class.forName(name)))); or .forEach(Try.consumer(ClassNameUtil::println)); */
        public static <T, E extends Throwable> Consumer<T> consumer(Consumer<T> consumer, ExceptionWrapper<E> wrapper) throws E {
            return t -> {
                try {
                    consumer.accept(t);
                } catch (Exception exception) {
                    sneakyThrow(exception);
                }
            };
        }

        /** .map(Try.function(name -> Class.forName(name))) or .map(Try.function(Class::forName)) */
        public static <T, R, E extends Throwable> Function<T, R> function(Function<T, R> function, ExceptionWrapper<E> wrapper) throws E {
            return t -> {
                try {
                    return function.apply(t);
                } catch (Exception exception) {
                    sneakyThrow(exception);
                    return null;
                }
            };
        }
        
        /** rethrowSupplier(() -> new StringJoiner(new String(new byte[]{77, 97, 114, 107}, "UTF-8"))), */
        public static <T, E extends Exception> Supplier<T> supplier(SupplierExceptionWrapper<T, E> function) throws E {
            return () -> {
                try {
                    return function.get();
                } catch (Exception exception) {
                    sneakyThrow(exception);
                    return null;
                }
            };
        }

        
        public static <T, E extends Throwable> T call(Callable<T> callable, ExceptionWrapper<E> wrapper) throws E {
            try {
                return callable.call();
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw wrapper.wrap(e);
            }
        }
        
        @SuppressWarnings("unchecked")
        private static <T extends Throwable> void sneakyThrow(Throwable t) throws T {
            throw (T) t;
        }
    }
    
}