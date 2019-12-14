package org.activiti.cloud.services.messages.aggregator.test;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.integration.mongodb.store.ConfigurableMongoDbMessageStore;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {
        "activiti.cloud.services.messages.aggregator.message-store-type=mongodb",
        "spring.data.mongodb.uri=mongodb://localhost:27017/test?maxPoolSize=150&minPoolSize=50"})
@Ignore
public class MongodbMessageStoreIT extends AbstractIntegrationFlowTests {

    @Test
    public void testMessageStore() throws Exception {
        assertThat(this.aggregatingMessageHandler.getMessageStore()).isInstanceOf(ConfigurableMongoDbMessageStore.class);
    }
}