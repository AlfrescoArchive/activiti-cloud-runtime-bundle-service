package org.activiti.cloud.starter.tests.helper;

import org.activiti.api.process.model.payloads.SignalPayload;
import org.activiti.engine.RuntimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class TestEventListener {

    @Autowired
    private RuntimeService runtimeService;

    private boolean isActive;

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    @EventListener
    public void sendSignal(SignalPayload signalPayload) {
        if ( isActive ) {
            runtimeService.startProcessInstanceByKey("broadcastSignalCatchEventProcess");
        }
    }
}
