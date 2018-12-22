package org.activiti.services.subscription;

import org.activiti.api.process.model.payloads.SignalPayload;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.binding.BinderAwareChannelResolver;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class SignalSender {

    @Value("${spring.application.name")
    private String serviceName;

    private final BinderAwareChannelResolver resolver;

    public SignalSender(BinderAwareChannelResolver resolver) {
        this.resolver = resolver;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendSignal(SignalPayload signalPayload) {
        Message<SignalPayload> message = MessageBuilder.withPayload(signalPayload).setHeader("sourceService", serviceName).build();
        resolver.resolveDestination("signalEvent").send(message);
    }
}