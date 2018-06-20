package com.iotspear.agent.services;

import com.mashape.unirest.http.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.io.IOException;

@Slf4j
public class JacksonMapper implements ObjectMapper {

    private final com.fasterxml.jackson.databind.ObjectMapper jacksonMapper;

    @Inject
    public JacksonMapper(com.fasterxml.jackson.databind.ObjectMapper jacksonMapper) {

        this.jacksonMapper = jacksonMapper;
    }

    @Override
    public <T> T readValue(String value, Class<T> valueType) {

        try {
            return jacksonMapper.readValue(value, valueType);

        } catch (Throwable error) {

            log.error("Failed to Parse JSON", error);
            return null;
        }
    }

    @Override
    public String writeValue(Object value) {

        try {
            return jacksonMapper.writeValueAsString(value);

        } catch (Throwable error) {

            log.error("Failed to Encode JSON", error);
            return "";
        }
    }
}
