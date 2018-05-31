package org.activiti.services.subscription.channel;

import org.activiti.cloud.services.api.commands.SendSignalCmd;
import org.activiti.engine.RuntimeService;
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
    public void receive(SendSignalCmd sendSignalCmd) {
        if ((sendSignalCmd.getInputVariables() == null) || (sendSignalCmd.getInputVariables().isEmpty())) {
            runtimeService.signalEventReceived(sendSignalCmd.getName());
        } else {
            runtimeService.signalEventReceived(sendSignalCmd.getName(), sendSignalCmd.getInputVariables());
        }
    }

    public void broadcastSignal(SendSignalCmd sendSignalCmd) {
        Message<SendSignalCmd> message = MessageBuilder.withPayload(sendSignalCmd).build();
        resolver.resolveDestination("signalEvent").send(message);
    }
}
