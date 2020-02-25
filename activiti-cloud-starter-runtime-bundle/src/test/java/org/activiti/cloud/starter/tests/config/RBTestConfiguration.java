package org.activiti.cloud.starter.tests.config;

import org.activiti.spring.ProcessDeployedEventProducer;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("test")
@Configuration
public class RBTestConfiguration {

    @MockBean
    public ProcessDeployedEventProducer producer;

}
