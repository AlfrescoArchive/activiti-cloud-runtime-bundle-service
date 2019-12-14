package org.activiti.cloud.services.messages.aggregator.test;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.integration.redis.store.RedisMessageStore;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {
        "activiti.cloud.services.messages.aggregator.message-store-type=redis",
        "spring.redis.host=localhost",
        "spring.redis.port=6379"})
@Ignore
public class RedisMessageStoreIT extends AbstractIntegrationFlowTests {
    
    @Test
    public void testMessageStore() throws Exception {
        assertThat(this.aggregatingMessageHandler.getMessageStore()).isInstanceOf(RedisMessageStore.class);
    }
}