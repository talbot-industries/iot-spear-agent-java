package com.iotspear.agent.data;

import lombok.Value;

import java.util.List;
import java.util.Map;

@Value
public class AgentResponse {

    private int status;

    private Map<String, List<String>> headers;

    private String body;
}
