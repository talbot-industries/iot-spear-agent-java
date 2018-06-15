package com.iotspear.agent.data;

import lombok.Value;

import java.util.List;

@Value
public class ProxyRequests {

    private List<ProxyRequest> batch;
}
