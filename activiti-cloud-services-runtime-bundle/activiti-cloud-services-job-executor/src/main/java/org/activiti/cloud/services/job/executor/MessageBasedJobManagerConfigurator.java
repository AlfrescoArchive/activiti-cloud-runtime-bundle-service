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

import org.activiti.cloud.services.events.configuration.RuntimeBundleProperties;
import org.activiti.engine.cfg.ProcessEngineConfigurator;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.springframework.cloud.stream.binder.ConsumerProperties;
import org.springframework.cloud.stream.binding.BinderAwareChannelResolver;
import org.springframework.cloud.stream.binding.BindingService;
import org.springframework.cloud.stream.config.BindingProperties;
import org.springframework.context.SmartLifecycle;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.SubscribableChannel;

public class MessageBasedJobManagerConfigurator implements ProcessEngineConfigurator, SmartLifecycle {
    
    private static final String JOB_MESSAGE_CONSUMER = "jobMessageConsumer";
    
    private final RuntimeBundleProperties runtimeBundleProperties;
    private final BinderAwareChannelResolver resolver;
    private final BindingService bindingService;
    private final JobMessageInputChannelFactory inputChannelFactory;
    private final ConsumerProperties consumerProperties;
    
    private MessageBasedJobManager messageBasedJobManager;
    private MessageHandler jobMessageHandler;
    private SubscribableChannel inputChannel;
    private ProcessEngineConfigurationImpl configuration;
    
    private boolean running = false;    
    
    public MessageBasedJobManagerConfigurator(RuntimeBundleProperties runtimeBundleProperties,
                                              BinderAwareChannelResolver resolver,
                                              BindingService bindingService,
                                              JobMessageInputChannelFactory inputChannelFactory,
                                              ConsumerProperties consumerProperties) {
        this.runtimeBundleProperties = runtimeBundleProperties;
        this.resolver = resolver;
        this.bindingService = bindingService;
        this.inputChannelFactory = inputChannelFactory;
        this.consumerProperties = consumerProperties;
    }
    
    protected MessageHandler createMessageHandler(ProcessEngineConfigurationImpl configuration) {
        return new JobMessageHandler(configuration);
    }
    
    protected String getInputChannelName() {
        return JOB_MESSAGE_CONSUMER;
    }
    
    /**
     * Configures MessageBasedJobManager 
     */
    @Override
    public void beforeInit(ProcessEngineConfigurationImpl configuration) {
        
        messageBasedJobManager = new MessageBasedJobManager(configuration,
                                                            runtimeBundleProperties,
                                                            resolver);
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
        
        BindingProperties bindingProperties = new BindingProperties();
        bindingProperties.setConsumer(consumerProperties);
        bindingProperties.setContentType("application/json");
        bindingProperties.setGroup("jobMessageHandler");
        // Let's use message job producer destination scope
        bindingProperties.setDestination(messageBasedJobManager.getDestination());

        // Let's create input channel 
        inputChannel = inputChannelFactory.createInputChannel(getInputChannelName(), bindingProperties);
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public void start() {
        jobMessageHandler = createMessageHandler(configuration);
        
        // Let's subscribe and bind consumer channel   
        inputChannel.subscribe(jobMessageHandler);
        bindingService.bindConsumer(inputChannel, getInputChannelName());

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
            bindingService.unbindConsumers(getInputChannelName());
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

}
