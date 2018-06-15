package com.iotspear.agent.mixin;

import com.google.common.base.Strings;
import com.google.inject.Binder;
import com.google.inject.name.Names;
import lombok.val;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface EnvironmentBinder {

    Binder theBinder();

    Map<String, Map.Entry<String, Boolean>> envDefaults();

    default String envOrDefault(String key) {

        return Optional.ofNullable(System.getenv(key)).orElse(envDefaults().get(key).getKey());
    }

    default <T> void bindNamedValue(Class<T> type, String name, T value) {

        theBinder().bind(type).annotatedWith(Names.named(name)).toInstance(value);
    }

    default <T> void bindConfiguration(Class<T> type, Function<String, T> transform, Map<String, String> map) {

        map.forEach((name, value) -> bindNamedValue(type, name, transform.apply(envOrDefault(value))));
    }

    default Map<String, String> displaySettings() {

        val envSensitivity = envDefaults().entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().getValue())
        );

        return envSensitivity.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> obscureIfSensitive(envOrDefault(entry.getKey()), entry.getValue())
        ));
    }

    static String obscureIfSensitive(String setting, boolean sensitive) {

        return sensitive ? Strings.repeat("*", setting.length()) : setting;
    }

    static Map.Entry<String, Boolean> setting(String name, boolean sensitive) {

        return new AbstractMap.SimpleImmutableEntry<>(name, sensitive);
    }
}
