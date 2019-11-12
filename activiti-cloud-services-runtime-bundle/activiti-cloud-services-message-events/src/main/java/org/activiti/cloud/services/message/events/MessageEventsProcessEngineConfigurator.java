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

package org.activiti.cloud.services.message.events;

import org.activiti.engine.cfg.ProcessEngineConfigurator;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;

public class MessageEventsProcessEngineConfigurator  implements ProcessEngineConfigurator, SmartLifecycle {

    private static final Logger logger = LoggerFactory.getLogger(MessageEventsProcessEngineConfigurator.class);
    
    public MessageEventsProcessEngineConfigurator() {
    }
    
    private boolean running = false;

    @Override
    public void start() {
        logger.info("start");
        
        this.running = true;
    }

    @Override
    public void stop() {
        logger.info("stop");
        
        this.running = false;
    }

    @Override
    public boolean isRunning() {
        return this.running;
    }

    @Override
    public void beforeInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
    }

    @Override
    public void configure(ProcessEngineConfigurationImpl processEngineConfiguration) {
        logger.info("configure: {}", processEngineConfiguration);
        
    }

    @Override
    public int getPriority() {
        return 0;
    }

}
