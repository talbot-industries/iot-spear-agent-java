package com.iotspear.agent;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.iotspear.agent.data.RelayCredentials;
import com.iotspear.agent.injection.RelayCredentialsProvider;
import com.iotspear.agent.interfaces.RequestDevice;
import com.iotspear.agent.interfaces.RequestsRelay;
import com.iotspear.agent.mixin.EnvironmentBinder;
import com.iotspear.agent.services.JacksonMapper;
import com.iotspear.agent.services.ProxyClient;
import com.iotspear.agent.services.RelayClient;
import com.mashape.unirest.http.ObjectMapper;

import java.util.Map;
import java.util.function.Function;

public class Configuration extends AbstractModule implements EnvironmentBinder {

    @Override
    public Map<String, Map.Entry<String, Boolean>> envDefaults() {

        return ImmutableMap.of(
                "IOT_SPEAR_DEVICE_ACCESS_ID", EnvironmentBinder.setting("00000000-0000-0000-0000-000000000000", false),
                "IOT_SPEAR_DEVICE_SECRET_KEY", EnvironmentBinder.setting("aBcDeFgHiJkLmNoPqRsTuVwXyZ1234567890", true),
                "IOT_SPEAR_DEVICE_HOST_NAME", EnvironmentBinder.setting("http://localhost:8080", false),
                "IOT_SPEAR_RELAY_HOST_NAME", EnvironmentBinder.setting("https://relay.iotspear.com", false),
                "IOT_SPEAR_DEBUG_LOGGING", EnvironmentBinder.setting("false", false)
        );
    }

    @Override
    protected final void configure() {

        bindConfiguration(
                String.class,
                Function.identity(),
                ImmutableMap.of(
                        "deviceAccessId", "IOT_SPEAR_DEVICE_ACCESS_ID",
                        "deviceSecretKey", "IOT_SPEAR_DEVICE_SECRET_KEY",
                        "deviceHostName", "IOT_SPEAR_DEVICE_HOST_NAME",
                        "relayHostName", "IOT_SPEAR_RELAY_HOST_NAME"
                )
        );

        bindConfiguration(
                Boolean.class,
                Boolean::parseBoolean,
                ImmutableMap.of(
                        "debugLogging", "IOT_SPEAR_DEBUG_LOGGING"
                )
        );

        bindInterfaceImplementations();
    }

    protected void bindInterfaceImplementations() {

        bind(ObjectMapper.class).to(JacksonMapper.class);
        bind(RequestDevice.class).to(ProxyClient.class);
        bind(RequestsRelay.class).to(RelayClient.class);

        bind(RelayCredentials.class).toProvider(RelayCredentialsProvider.class).asEagerSingleton();
    }

    @Override
    public final Binder theBinder() {

        return binder();
    }
}
