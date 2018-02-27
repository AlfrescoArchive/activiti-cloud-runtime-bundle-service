package org.activiti.cloud.starter.configuration;

import javax.annotation.PostConstruct;

import org.activiti.spring.boot.ProcessEngineAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ActivitiEngineConfiguration {
    
    @Autowired
    private ProcessEngineAutoConfiguration processEngineConfiguration;
    
    @Autowired
    private RuntimeBundleActivityBehaviorFactory activityBehaviorFactory;
    
    @PostConstruct
    private void configure() {
        processEngineConfiguration.setActivityBehaviorFactory(activityBehaviorFactory);
    }
}
