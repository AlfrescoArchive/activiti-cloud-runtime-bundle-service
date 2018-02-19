package org.activiti.services.subscriptions.model;


public class BroadcastSignalEvent {

    public BroadcastSignalEvent() {
    }

    public BroadcastSignalEvent(String signalName, boolean isSignalAsync) {
        this.setSignalName(signalName);
        this.setSignalAsync(isSignalAsync);
    }
    
    public String getSignalName() {
        return signalName;
    }

    public void setSignalName(String signalName) {
        this.signalName = signalName;
    }

    public boolean isSignalAsync() {
        return isSignalAsync;
    }

    public void setSignalAsync(boolean isSignalAsync) {
        this.isSignalAsync = isSignalAsync;
    }

    private String signalName;

    private boolean isSignalAsync;

}

