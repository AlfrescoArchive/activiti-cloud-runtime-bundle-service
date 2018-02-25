package org.activiti.cloud.services.api.commands;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BroadcastSignalCmd implements Command {

    private final String id;
    private String name;
    private boolean isSignalAsync;

    public BroadcastSignalCmd() {
        this.id = UUID.randomUUID().toString();
    }

    @JsonCreator
    public BroadcastSignalCmd(@JsonProperty("name") String name,
                                     @JsonProperty("isSignalAsync") boolean isSignalAsync) {
        this();
        this.name = name;
        this.isSignalAsync = isSignalAsync;
    }

    @Override
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isSignalAsync() {
        return isSignalAsync;
    }
}
