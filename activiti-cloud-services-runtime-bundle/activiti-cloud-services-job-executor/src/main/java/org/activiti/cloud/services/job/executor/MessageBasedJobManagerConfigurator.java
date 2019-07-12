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

package org.activiti.cloud.services.job.executor;

import org.activiti.engine.cfg.ProcessEngineConfigurator;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.cloud.stream.binder.ConsumerProperties;
import org.springframework.cloud.stream.binding.BindingService;
import org.springframework.cloud.stream.config.BindingProperties;
import org.springframework.context.SmartLifecycle;
import org.springframework.http.MediaType;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.SubscribableChannel;

public class MessageBasedJobManagerConfigurator implements ProcessEngineConfigurator, SmartLifecycle {
    public static final String JOB_MESSAGE_HANDLER = "jobMessageHandler";

    private String contentType = MediaType.APPLICATION_JSON_VALUE;

    private final BindingService bindingService;
    private final JobMessageInputChannelFactory inputChannelFactory;
    private final ConsumerProperties consumerProperties;
    private final MessageBasedJobManagerFactory messageBasedJobManagerFactory;
    private final JobMessageHandlerFactory jobMessageHandlerFactory;
    private final ConfigurableListableBeanFactory beanFactory;
    
    private MessageBasedJobManager messageBasedJobManager;
    private MessageHandler jobMessageHandler;
    private SubscribableChannel inputChannel;
    private ProcessEngineConfigurationImpl configuration;
    
    private boolean running = false;    
    
    public MessageBasedJobManagerConfigurator(ConfigurableListableBeanFactory beanFactory,
                                              BindingService bindingService,
                                              JobMessageInputChannelFactory inputChannelFactory,
                                              MessageBasedJobManagerFactory messageBasedJobManagerFactory,
                                              JobMessageHandlerFactory jobMessageHandlerFactory,
                                              ConsumerProperties consumerProperties) {
        this.bindingService = bindingService;
        this.inputChannelFactory = inputChannelFactory;
        this.consumerProperties = consumerProperties;
        this.messageBasedJobManagerFactory = messageBasedJobManagerFactory;
        this.jobMessageHandlerFactory = jobMessageHandlerFactory;
        this.beanFactory = beanFactory;
    }
    
    protected MessageHandler createJobMessageHandler(ProcessEngineConfigurationImpl configuration) {
        MessageHandler messageHandler = jobMessageHandlerFactory.create(configuration);
        
        beanFactory.registerSingleton(JOB_MESSAGE_HANDLER, messageHandler);

        return (MessageHandler) beanFactory.initializeBean(messageHandler, JOB_MESSAGE_HANDLER);
    }
    
    /**
     * Configures MessageBasedJobManager 
     */
    @Override
    public void beforeInit(ProcessEngineConfigurationImpl configuration) {
        
        messageBasedJobManager = messageBasedJobManagerFactory.create(configuration);
        
        // Let's manage async executor lifecycle manually on start/stop
        configuration.setAsyncExecutorActivate(false);
        configuration.setAsyncExecutorMessageQueueMode(true);
        configuration.setJobManager(messageBasedJobManager);
    }

    /**
     * Configures input channel 
     */
    @Override
    public void configure(ProcessEngineConfigurationImpl configuration) {
        this.configuration = configuration;
        
        String channelName = messageBasedJobManager.getInputChannelName();
        String destination = messageBasedJobManager.getDestination();
        
        BindingProperties bindingProperties = new BindingProperties();
        bindingProperties.setConsumer(consumerProperties);
        bindingProperties.setContentType(contentType);
        bindingProperties.setGroup(JOB_MESSAGE_HANDLER);
        // Let's use message job producer destination scope
        bindingProperties.setDestination(destination);

        // Let's create input channel 
        inputChannel = inputChannelFactory.createInputChannel(channelName, bindingProperties);
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public void start() {
        jobMessageHandler = createJobMessageHandler(configuration);
        
        // Let's subscribe and bind consumer channel   
        inputChannel.subscribe(jobMessageHandler);
        bindingService.bindConsumer(inputChannel, messageBasedJobManager.getInputChannelName());

        // Now start async executor
        if (!configuration.getAsyncExecutor().isActive()) {
            configuration.getAsyncExecutor()
                         .start();            
        }
        
        running = true;
    }

    @Override
    public void stop() {
        try {
            // Let's unbind consumer from input channel
            bindingService.unbindConsumers(messageBasedJobManager.getInputChannelName());
            inputChannel.unsubscribe(jobMessageHandler);

            // Let's gracefully shutdown executor
            if (configuration.getAsyncExecutor().isActive()) {
                configuration.getAsyncExecutor()
                             .shutdown();            
            }
            
        } finally {
            running = false;
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

}
