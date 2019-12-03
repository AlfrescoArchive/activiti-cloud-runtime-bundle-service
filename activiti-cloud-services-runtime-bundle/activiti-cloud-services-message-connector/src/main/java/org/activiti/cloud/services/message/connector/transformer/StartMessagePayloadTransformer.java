package org.activiti.cloud.services.message.connector.transformer;

import org.activiti.api.process.model.builders.MessagePayloadBuilder;
import org.activiti.api.process.model.payloads.MessageEventPayload;
import org.activiti.api.process.model.payloads.StartMessagePayload;
import org.springframework.integration.transformer.AbstractPayloadTransformer;

public class StartMessagePayloadTransformer extends AbstractPayloadTransformer<MessageEventPayload, StartMessagePayload> {

    public static StartMessagePayloadTransformer transform() {
        return new StartMessagePayloadTransformer();
    }
    
    @Override
    protected StartMessagePayload transformPayload(MessageEventPayload eventPayload) throws Exception {
        return MessagePayloadBuilder.start(eventPayload.getName())
                                    .withBusinessKey(eventPayload.getBusinessKey())
                                    .withVariables(eventPayload.getVariables())
                                    .build();
    }
}
