package org.activiti.cloud.services.rest.conf;

import org.activiti.model.connector.ConnectorDefinition;
import org.activiti.spring.connector.ConnectorDefinitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.List;

@Configuration
public class ConnectorsAutoConfiguration {

    @Autowired
    private ConnectorDefinitionService connectorDefinitionService;

    @Bean
    public List<ConnectorDefinition> connectorDefinitions() throws IOException {
        return connectorDefinitionService.get();
    }

}
