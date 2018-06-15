package com.iotspear.agent.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Value;

@Value
@JsonIgnoreProperties(ignoreUnknown=true)
public class BearerClaim {

    private String device;

    private String host;
}
