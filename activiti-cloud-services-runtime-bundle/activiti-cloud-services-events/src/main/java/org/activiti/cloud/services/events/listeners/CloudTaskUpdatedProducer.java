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

package org.activiti.cloud.services.events.listeners;

import org.activiti.api.task.runtime.events.TaskUpdatedEvent;
import org.activiti.api.task.runtime.events.listener.TaskEventListener;
import org.activiti.cloud.services.events.ProcessEngineChannels;
import org.activiti.cloud.services.events.converter.ToCloudTaskRuntimeEventConverter;
import org.springframework.messaging.support.MessageBuilder;

import java.util.Collections;

public class CloudTaskUpdatedProducer implements TaskEventListener<TaskUpdatedEvent> {

    private ToCloudTaskRuntimeEventConverter converter;
    private ProcessEngineChannels producer;

    public CloudTaskUpdatedProducer(ToCloudTaskRuntimeEventConverter converter,
                                    ProcessEngineChannels producer) {
        this.converter = converter;
        this.producer = producer;
    }

    @Override
    public void onEvent(TaskUpdatedEvent event) {
        producer.auditProducer().send(MessageBuilder.withPayload(
                Collections.singletonList(converter.from(event)).toArray()).build());
    }

}
