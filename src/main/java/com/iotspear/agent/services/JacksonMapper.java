package com.iotspear.agent.services;

import com.mashape.unirest.http.ObjectMapper;

import javax.inject.Inject;
import java.io.IOException;

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
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String writeValue(Object value) {

        try {
            return jacksonMapper.writeValueAsString(value);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
