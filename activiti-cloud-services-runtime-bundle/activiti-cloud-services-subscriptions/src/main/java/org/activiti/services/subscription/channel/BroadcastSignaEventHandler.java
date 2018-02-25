package org.activiti.services.subscription.channel;

import org.activiti.engine.RuntimeService;
import org.activiti.services.subscriptions.model.BroadcastSignalEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.binding.BinderAwareChannelResolver;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
@EnableBinding(ProcessEngineSignalChannels.class)
public class BroadcastSignaEventHandler {

    @Autowired
    private BinderAwareChannelResolver resolver;

    @Autowired
    private RuntimeService runtimeService;

    @StreamListener(ProcessEngineSignalChannels.SIGNAL_CONSUMER)
    public void receive(BroadcastSignalEvent event) {
        if (event.isSignalAsync()) {
            runtimeService.signalEventReceivedAsync(event.getSignalName());
        } else {
            runtimeService.signalEventReceived(event.getSignalName());
        }
    }

    public void broadcastSignal(BroadcastSignalEvent event) {
        Message<BroadcastSignalEvent> message = MessageBuilder.withPayload(event).build();
        resolver.resolveDestination("signalEvent").send(message);
    }
}
