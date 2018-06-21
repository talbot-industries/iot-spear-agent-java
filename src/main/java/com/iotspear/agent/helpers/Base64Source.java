package com.iotspear.agent.helpers;

import com.google.common.io.ByteSource;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Optional;

@Slf4j
@EqualsAndHashCode(callSuper = true)
@Value
public class Base64Source extends ByteSource {

    private InputStream inputStream;

    @Override
    public InputStream openStream() {
        return inputStream;
    }

    public String asBase64() {

        try {

            return Base64.getEncoder().encodeToString(read());

        } catch (IOException error) {

            log.error("Response Stream Error", error);
            return null;
        }
    }

    public static String fromStream(InputStream inputStream) {

        return Optional.ofNullable(inputStream).map(Base64Source::new).map(Base64Source::asBase64).orElse("");
    }
}
