package com.iotspear.agent.data;

import lombok.Value;

@Value
public class BearerInformation {

    private String token;

    private BearerClaim claim;
}
