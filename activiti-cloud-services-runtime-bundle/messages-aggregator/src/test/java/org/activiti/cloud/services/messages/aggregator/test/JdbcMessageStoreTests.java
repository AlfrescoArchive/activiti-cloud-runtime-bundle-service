package org.activiti.cloud.services.messages.aggregator.test;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.springframework.integration.jdbc.store.JdbcMessageStore;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {
        "activiti.cloud.services.messages.aggregator.message-store-type=jdbc"})
public class JdbcMessageStoreTests extends AbstractIntegrationFlowTests {

    @Test
    public void testMessageStore() throws Exception {
        assertThat(this.aggregatingMessageHandler.getMessageStore()).isInstanceOf(JdbcMessageStore.class);
    }
}