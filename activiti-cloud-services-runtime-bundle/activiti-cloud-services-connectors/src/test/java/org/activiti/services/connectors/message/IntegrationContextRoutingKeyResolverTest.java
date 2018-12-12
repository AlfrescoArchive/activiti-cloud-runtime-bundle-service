package org.activiti.services.connectors.message;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.activiti.cloud.services.events.message.RuntimeBundleInfoMessageHeaders;
import org.junit.Test;

 
public class IntegrationContextRoutingKeyResolverTest {

    private IntegrationContextRoutingKeyResolver subject = new IntegrationContextRoutingKeyResolver();
    
    @Test
    public void testResolveRoutingKeyFromValidHeadersInAnyOrder() {
        // given
        Map<String, Object> headers = MapBuilder.<String, Object> map(RuntimeBundleInfoMessageHeaders.SERVICE_NAME, "service-name")
                                                .with(IntegrationContextMessageHeaders.PROCESS_INSTANCE_ID, "process-instance-id")
                                                .with(RuntimeBundleInfoMessageHeaders.APP_NAME, "app-name")
                                                .with(IntegrationContextMessageHeaders.CONNECTOR_TYPE, "connector-type")
                                                .with(IntegrationContextMessageHeaders.BUSINESS_KEY, "business-key");
        // when
        String routingKey = subject.resolve(headers);
        
        // then
        assertThat(routingKey).isEqualTo("service-name.app-name.connector-type.process-instance-id.business-key");
                
    }
    
    private static class MapBuilder<K, V> extends java.util.HashMap<K, V> {
        private static final long serialVersionUID = 1L;

        public MapBuilder<K, V> with(K key, V value) {
            put(key, value);
            return this;
        }

        public static <K, V> MapBuilder<K, V> map(K key, V value) {
            return new MapBuilder<K, V>().with(key, value);
        }

        public static <K, V> MapBuilder<K, V> emptyMap() {
            return new MapBuilder<K, V>();
        }
        
    }    
}
