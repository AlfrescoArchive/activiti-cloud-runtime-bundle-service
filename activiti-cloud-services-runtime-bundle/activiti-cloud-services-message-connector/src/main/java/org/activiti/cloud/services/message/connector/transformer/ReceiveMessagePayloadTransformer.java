package org.activiti.cloud.services.message.connector.transformer;

import org.activiti.api.process.model.builders.MessagePayloadBuilder;
import org.activiti.api.process.model.payloads.MessageEventPayload;
import org.activiti.api.process.model.payloads.ReceiveMessagePayload;
import org.springframework.integration.transformer.AbstractPayloadTransformer;

public class ReceiveMessagePayloadTransformer extends AbstractPayloadTransformer<MessageEventPayload, ReceiveMessagePayload> {

    public static ReceiveMessagePayloadTransformer transform() {
        return new ReceiveMessagePayloadTransformer();
    }
    
    @Override
    protected ReceiveMessagePayload transformPayload(MessageEventPayload eventPayload) throws Exception {
        return MessagePayloadBuilder.receive(eventPayload.getName())
                                    .withCorrelationKey(eventPayload.getCorrelationKey())
                                    .withVariables(eventPayload.getVariables())
                                    .build();
    }
}
