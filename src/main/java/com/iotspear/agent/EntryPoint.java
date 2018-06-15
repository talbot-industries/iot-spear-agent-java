package com.iotspear.agent;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.google.inject.Guice;
import com.iotspear.agent.data.LoggingConfig;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.slf4j.LoggerFactory;

@Slf4j
public class EntryPoint {

    public static void main(String[] args) {

        log.info("Started IoT Spear Agent ...");

        run(new Configuration());
    }

    public static void run(Configuration configuration) {

        configuration.displaySettings().forEach((name, value) -> log.info(String.format("%s: %s", name, value)));

        val injector = Guice.createInjector(configuration);

        if (injector.getInstance(LoggingConfig.class).isDebugLogging()) {

            ((Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)).setLevel(Level.DEBUG);
        }

        injector.getInstance(Processor.class).processRequests();
    }
}
