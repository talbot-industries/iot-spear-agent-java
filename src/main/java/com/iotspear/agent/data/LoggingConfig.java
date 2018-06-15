package com.iotspear.agent.data;

import lombok.Getter;

import javax.inject.Inject;
import javax.inject.Named;

public class LoggingConfig {

    @Getter
    private final boolean debugLogging;

    @Inject
    public LoggingConfig(@Named("debugLogging") boolean debugLogging) {

        this.debugLogging = debugLogging;
    }
}
