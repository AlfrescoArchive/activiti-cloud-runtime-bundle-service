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

package org.activiti.cloud.services.message.connector.release;

import org.activiti.cloud.services.message.connector.support.SpELEvaluatingReleaseStrategy;
import org.springframework.integration.aggregator.ReleaseStrategy;
import org.springframework.integration.store.MessageGroup;

public class MessageSentReleaseHandler implements MessageGroupReleaseHandler {

    private final static String condition = "!messages.?[headers['eventType'] == 'MESSAGE_WAITING' || headers['eventType'] == 'START_MESSAGE_DEPLOYED'].empty " 
                                            + "&& !messages.?[headers['eventType'] == 'MESSAGE_SENT'].empty";
    
    private final static ReleaseStrategy strategy = new SpELEvaluatingReleaseStrategy(condition);

    public MessageSentReleaseHandler() {
    }
    
    @Override
    public Boolean handle(MessageGroup group) {
        if (strategy.canRelease(group)) {
            return true;
        }
        
        return null;
    }

}
