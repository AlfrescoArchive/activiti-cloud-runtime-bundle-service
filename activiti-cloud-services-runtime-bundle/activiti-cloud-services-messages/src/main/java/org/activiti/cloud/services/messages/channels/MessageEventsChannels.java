/*
 * Copyright 2019 Alfresco, Inc. and/or its affiliates.
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

package org.activiti.cloud.services.messages.channels;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;

public interface MessageEventsChannels {
    public static final String RECEIVE_MESSAGE_PAYLOAD_CONSUMER_CHANNEL = "receiveMessagePayloadConsumerChannel";
    public static final String BPMN_MESSAGE_SENT_EVENT_PRODUCER_CHANNEL = "bpmnMessageSentEventProducerChannel";
    public static final String BPMN_MESSAGE_RECEIVED_EVENT_PRODUCER_CHANNEL = "bpmnMessageReceivedEventProducerChannel";
    public static final String BPMN_MESSAGE_WAITING_EVENT_RPODUCER_CHANNEL = "bpmnMessageWaitingEventProducerChannel";

    interface Consumer {

        @Input(RECEIVE_MESSAGE_PAYLOAD_CONSUMER_CHANNEL)
        SubscribableChannel receiveMessagePayloadConsumerChannel();
    }

    interface Producer {


        @Output(BPMN_MESSAGE_WAITING_EVENT_RPODUCER_CHANNEL)
        MessageChannel bpmnMessageWaitingEventProducerChannel();

        @Output(BPMN_MESSAGE_RECEIVED_EVENT_PRODUCER_CHANNEL)
        MessageChannel bpmnMessageReceivedEventProducerChannel();
        
        @Output(BPMN_MESSAGE_SENT_EVENT_PRODUCER_CHANNEL)
        MessageChannel bpmnMessageSentEventProducerChannel();
    }
    
    

}
