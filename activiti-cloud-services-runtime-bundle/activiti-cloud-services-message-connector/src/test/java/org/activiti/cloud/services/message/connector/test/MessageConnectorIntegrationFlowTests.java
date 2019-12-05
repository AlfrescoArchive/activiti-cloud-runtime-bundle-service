package org.activiti.cloud.services.message.connector.test;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.Map;
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
import org.activiti.api.process.model.payloads.MessageEventPayload;
import org.activiti.cloud.services.message.connector.aggregator.MessageConnectorAggregator;
import org.activiti.cloud.services.message.connector.channels.MessageConnectorProcessor;
import org.activiti.cloud.services.message.connector.config.MessageConnectorIntegrationConfiguration;
import org.activiti.cloud.services.message.connector.config.MessageConnectorAutoConfiguration;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.context.annotation.Import;
import org.springframework.integration.IntegrationMessageHeaderAccessor;
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
        String messageName = "start";
        Integer count = 100;
        messageGroupStore.removeMessageGroup(messageName);

        send(startMessageDeployedEvent(messageName));

        // when
        final CountDownLatch start = new CountDownLatch(1);
        final CountDownLatch sent = new CountDownLatch(count);
        
        ExecutorService exec = Executors.newSingleThreadExecutor();
        
        IntStream.range(0, count)
                 .forEach(i -> sendAsync(messageSentEvent(messageName, "sent" + i),
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
        
        assertThat(peek()).isNull();
    }
    
    @Test(timeout = 10000)
    public void shouldProcessMessageEventsConcurrentlyInReversedOrder() throws InterruptedException, JsonProcessingException {
        // given
        String messageName = "start";
        Integer count = 100;
        messageGroupStore.removeMessageGroup(messageName);
        
        final CountDownLatch start = new CountDownLatch(1);
        final CountDownLatch sent = new CountDownLatch(count);
        
        ExecutorService exec = Executors.newSingleThreadExecutor();
        
        IntStream.range(0, count)
                 .forEach(i -> sendAsync(messageSentEvent(messageName, "sent" + i),
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

        // when
        send(startMessageDeployedEvent(messageName));

        // then
        IntStream.range(0, count)
                 .mapToObj(i -> Try.call(() -> poll(3, TimeUnit.SECONDS)))
                 .forEach(out ->
                     assertThat(out).isNotNull()
                 );
        
        exec.shutdownNow();
        
        assertThat(peek()).isNull();
    }
    
    @Test
    public void testStartMessageBeforeSent() throws Exception {
        // given
        String messageName = "start1";
        messageGroupStore.removeMessageGroup(messageName);
        
        send(startMessageDeployedEvent(messageName));
        
        // when
        send(messageSentEvent(messageName, "sent1"));
        
        // then
        Message<?> out = poll(0, TimeUnit.SECONDS);
        
        assertThat(peek()).isNull();

        assertThat(out).isNotNull()
                       .extracting(Message::getPayload)
                       .extracting("name")
                       .contains("sent1");

        MessageGroup group = messageGroup(messageName);
        
        assertThat(group.getMessages()).hasSize(1)
                                       .extracting(Message::getPayload)
                                       .asList()
                                       .extracting("name")
                                       .containsOnly(messageName);
        // when
        send(messageSentEvent(messageName, "sent2"));
        
        // then
        out = poll(0, TimeUnit.SECONDS);

        assertThat(peek()).isNull();

        assertThat(out).isNotNull()
                       .extracting(Message::getPayload)
                       .extracting("name")
                       .contains("sent2");

        group = messageGroup(messageName);
        
        assertThat(group.getMessages()).hasSize(1)
                                       .extracting(Message::getPayload)
                                       .asList()
                                       .extracting("name")
                                       .containsOnly(messageName);
    }

    @Test
    public void testStartMessageAfterSent() throws Exception {
        // given
        String messageName = "start2";
        messageGroupStore.removeMessageGroup(messageName);

        send(messageSentEvent(messageName, "sent1"));
        
        // when
        send(startMessageDeployedEvent(messageName));
        
        // then
        Message<?> out = poll(0, TimeUnit.SECONDS);
        
        assertThat(peek()).isNull();

        assertThat(out).isNotNull()
                       .extracting(Message::getPayload)
                       .extracting("name")
                       .contains("sent1");

        MessageGroup group = messageGroup(messageName);
        
        assertThat(group.getMessages()).hasSize(1)
                                       .extracting(Message::getPayload)
                                       .asList()
                                       .extracting("name")
                                       .containsOnly("start2");

        // when
        send(messageSentEvent(messageName, "sent2"));
        
        // then
        out = poll(0, TimeUnit.SECONDS);

        assertThat(peek()).isNull();

        assertThat(out).isNotNull()
                       .extracting(Message::getPayload)
                       .extracting("name")
                       .contains("sent2");

        group = messageGroup(messageName);
        
        assertThat(group.getMessages()).hasSize(1)
                                       .extracting(Message::getPayload)
                                       .asList()
                                       .extracting("name")
                                       .containsOnly("start2");
    }
    
    @Test
    public void testSentMessagesWithBuffer() throws Exception {
        // given
        String correlationId = "message:1";
        messageGroupStore.removeMessageGroup(correlationId);

        send(messageSentEvent(correlationId, "sent1"));
        
        // when
        send(messageWaitingEvent(correlationId, "waiting1"));
        send(messageWaitingEvent(correlationId, "waiting2"));
        
        Message<?> out = poll(0, TimeUnit.SECONDS);
        
        assertThat(peek()).isNull();

        assertThat(out).isNotNull()
                       .extracting(Message::getPayload)
                       .extracting("name")
                       .contains("sent1");

        MessageGroup group = messageGroup(correlationId);
        
        assertThat(group.getMessages()).hasSize(2)
                                       .extracting(Message::getPayload)
                                       .asList()
                                       .extracting("name")
                                       .containsOnly("waiting1", "waiting2");
        
        send(messageReceivedEvent(correlationId, "received1"));

        assertThat(peek()).isNull();
        
        group = messageGroup(correlationId);
        
        assertThat(group.getMessages()).hasSize(1)
                                       .extracting(Message::getPayload)
                                       .asList()
                                       .extracting("name")
                                       .containsOnly("waiting2");
        
        send(messageSentEvent(correlationId, "sent2"));

        out = poll(1, TimeUnit.SECONDS);

        assertThat(peek()).isNull();

        assertThat(out).isNotNull()
                       .extracting(Message::getPayload)
                       .extracting("name")
                       .contains("sent2");

        group = messageGroup(correlationId);
        
        assertThat(group.getMessages()).hasSize(1)
                                       .extracting(Message::getPayload)
                                       .asList()
                                       .extracting("name")
                                       .containsOnly("waiting2");
        
        send(messageReceivedEvent(correlationId, "received2"));

        assertThat(peek()).isNull();

        group = messageGroup(correlationId);

        assertThat(group.getMessages()).isEmpty();
    }
    
    @Test
    public void testSentMessagesWithBufferInDifferentOrder() throws Exception {
        // given
        String correlationId = "message:1";
        messageGroupStore.removeMessageGroup(correlationId);

        send(messageSentEvent(correlationId, "sent1"));
        send(messageSentEvent(correlationId, "sent2"));
        
        // when
        send(messageWaitingEvent(correlationId, "waiting1"));
        
        // then
        Message<?> out = poll(0, TimeUnit.SECONDS);
        
        assertThat(out).isNotNull()
                       .extracting(Message::getPayload)
                       .extracting("name")
                       .contains("sent1");

        assertThat(peek()).isNull();
        
        MessageGroup group = messageGroup(correlationId);
        
        assertThat(group.getMessages()).hasSize(2)
                                       .extracting(Message::getPayload)
                                       .asList()
                                       .extracting("name")
                                       .contains("sent2", "waiting1");
        // when 
        send(messageReceivedEvent(correlationId, "received1"));

        // then
        assertThat(peek()).isNull();
        
        group = messageGroup(correlationId);
        
        assertThat(group.getMessages()).hasSize(1)
                                       .extracting(Message::getPayload)
                                       .asList()
                                       .extracting("name")
                                       .containsOnly("sent2");
        // when
        send(messageWaitingEvent(correlationId, "waiting2"));

        out = poll(0, TimeUnit.SECONDS);

        assertThat(peek()).isNull();

        assertThat(out).isNotNull()
                       .extracting(Message::getPayload)
                       .extracting("name")
                       .contains("sent2");

        group = messageGroup(correlationId);
        
        assertThat(group.getMessages()).hasSize(1)
                                       .extracting(Message::getPayload)
                                       .asList()
                                       .extracting("name")
                                       .containsOnly("waiting2");
        
        send(messageReceivedEvent(correlationId, "received2"));

        assertThat(peek()).isNull();

        group = messageGroup(correlationId);

        assertThat(group.getMessages()).isEmpty();
    }
    
    
    @Test
    public void testSubscriptionCancelled() throws Exception {
        // given
        String correlationId = "message:1";
        messageGroupStore.removeMessageGroup(correlationId);

        send(messageWaitingEvent(correlationId, "waiting1"));
        send(messageWaitingEvent(correlationId, "waiting2"));
        
        // when
        send(subscriptionCancelledEvent(correlationId, "cancelled1"));
        
        // then
        assertThat(peek()).isNull();

        MessageGroup group = messageGroup(correlationId);
        
        assertThat(group.getMessages()).isEmpty();
    }
    

    @Test
    public void testIdempotentMessageInterceptor() throws Exception {
        // given
        String correlationId = "message:1";
        messageGroupStore.removeMessageGroup(correlationId);

        Message<MessageEventPayload> waitingMessage = messageWaitingEvent(correlationId, "waiting"); 

        // when                                      
        send(waitingMessage);
        send(waitingMessage);

        // then
        assertThat(peek()).isNull();

        MessageGroup group = messageGroup(correlationId);
        
        assertThat(group.getMessages()).isNotNull()
                                       .hasSize(1);
        // given
        Message<MessageEventPayload> receivedMessage = messageReceivedEvent(correlationId, "recieved");
        
        // when
        send(receivedMessage);
        send(receivedMessage);
        
        // then 
        assertThat(peek()).isNull();
        
        group = messageGroup(correlationId);

        assertThat(group.getMessages()).hasSize(0);
        
    }        
    
    
    
    
    private Message<?> startMessageDeployedEvent(String messageName) {
        MessageEventPayload payload = messageEventPayload(messageName,
                                                          "businessKey",
                                                          null,
                                                          Collections.emptyMap());
        return MessageBuilder.withPayload(payload)
                             .setHeader(IntegrationMessageHeaderAccessor.CORRELATION_ID, messageName)
                             .setHeader("eventType", "START_MESSAGE_DEPLOYED")
                             .setHeader("messageId", UUID.randomUUID())
                             .build();
    }
    
    private Message<MessageEventPayload> messageSentEvent(String correlationId, String name) {
        MessageEventPayload payload = messageEventPayload(name,
                                                          "businessKey",
                                                          correlationId,
                                                          Collections.emptyMap());
        return MessageBuilder.withPayload(payload)
                             .setHeader(IntegrationMessageHeaderAccessor.CORRELATION_ID, correlationId)
                             .setHeader("eventType", "MESSAGE_SENT")
                             .setHeader("messageId", UUID.randomUUID())
                             .build();
    }

    private Message<MessageEventPayload> messageWaitingEvent(String correlationId, String name) {
        MessageEventPayload payload = messageEventPayload(name,
                                                          "businessKey",
                                                          correlationId,
                                                          Collections.emptyMap());
        return MessageBuilder.withPayload(payload)
                             .setHeader(IntegrationMessageHeaderAccessor.CORRELATION_ID, correlationId)
                             .setHeader("eventType", "MESSAGE_WAITING")
                             .setHeader("messageId", UUID.randomUUID())
                             .build();
    }

    private Message<MessageEventPayload> messageReceivedEvent(String correlationId, String name) {
        MessageEventPayload payload = messageEventPayload(name,
                                                          "businessKey",
                                                          correlationId,
                                                          Collections.emptyMap());

        return MessageBuilder.withPayload(payload)
                             .setHeader(IntegrationMessageHeaderAccessor.CORRELATION_ID, correlationId)
                             .setHeader("eventType", "MESSAGE_RECEIVED")
                             .setHeader("messageId", UUID.randomUUID())
                             .build();
    }

    private Message<MessageEventPayload> subscriptionCancelledEvent(String correlationId, String name) {
        MessageEventPayload payload = messageEventPayload(name,
                                                          "businessKey",
                                                          correlationId,
                                                          Collections.emptyMap());
        return MessageBuilder.withPayload(payload)
                             .setHeader(IntegrationMessageHeaderAccessor.CORRELATION_ID, correlationId)
                             .setHeader("eventType", "MESSAGE_SUBSCRIPTION_CANCELLED")
                             .setHeader("messageId", UUID.randomUUID())
                             .build();
    }

    private MessageEventPayload messageEventPayload(String name,
                                                    String businessKey,
                                                    String correlationKey,
                                                    Map<String, Object> variables) {
        return MessageEventPayloadBuilder.messageEvent(name)
                                         .withBusinessKey(businessKey)
                                         .withVariables(variables)
                                         .withCorrelationKey(correlationKey)
                                         .build();
    }    

    protected void send(Message<?> message) throws JsonProcessingException {
        String json = objectMapper.writeValueAsString(message.getPayload());
        
        this.channels.input()
                     .send(MessageBuilder.withPayload(json)
                                         .copyHeaders(message.getHeaders())
                                         .build());        
    }
 
    protected <T> Message<T> poll(long timeout, TimeUnit unit) throws InterruptedException {
        return (Message<T>) this.collector.forChannel(this.channels.output())
                                          .poll(timeout, unit);
    }

    protected <T> Message<T> peek() {
        return (Message<T>) this.collector.forChannel(this.channels.output())
                                          .peek();
    }
 
    protected MessageGroup messageGroup(String groupName) {
        return aggregatingMessageHandler.getMessageStore()
                                        .getMessageGroup(groupName);
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