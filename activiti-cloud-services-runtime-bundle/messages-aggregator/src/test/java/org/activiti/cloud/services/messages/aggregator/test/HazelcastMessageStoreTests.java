package org.activiti.cloud.services.messages.aggregator.test;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.integration.hazelcast.store.HazelcastMessageStore;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {
        "activiti.cloud.services.messages.aggregator.message-store-type=hazelcast"})
@Ignore
public class HazelcastMessageStoreTests extends AbstractIntegrationFlowTests {

    @Test
    public void testMessageStore() throws Exception {
        assertThat(this.aggregatingMessageHandler.getMessageStore()).isInstanceOf(HazelcastMessageStore.class);
    }
}
