package org.activiti.services.subscription.channel;

import org.activiti.api.process.model.payloads.SignalPayload;
import org.activiti.engine.RuntimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.binding.BinderAwareChannelResolver;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@EnableBinding(ProcessEngineSignalChannels.class)
public class BroadcastSignaEventHandler {

    @Value("${spring.application.name")
    private String serviceName;

    private final RuntimeService runtimeService;

    @Autowired
    public BroadcastSignaEventHandler(BinderAwareChannelResolver resolver,
                                      RuntimeService runtimeService) {
        this.runtimeService = runtimeService;
    }

    @StreamListener(ProcessEngineSignalChannels.SIGNAL_CONSUMER)
    public void receive(@Payload SignalPayload signalPayload, @Header("sourceService") String serviceName) {
        if ((signalPayload.getVariables() == null) || (signalPayload.getVariables().isEmpty())) {
            runtimeService.signalEventReceived(signalPayload.getName());
        } else {
            runtimeService.signalEventReceived(signalPayload.getName(),
                                               signalPayload.getVariables());
        }
    }
}
