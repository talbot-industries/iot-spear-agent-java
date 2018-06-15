package com.iotspear.agent.data;

import lombok.Value;

@Value
public class RelayCredentials {

    private String deviceAccessId;

    private String deviceSecretKey;
}
