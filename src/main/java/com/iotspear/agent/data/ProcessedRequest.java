package com.iotspear.agent.data;

import lombok.Value;

import java.util.concurrent.CompletionStage;

@Value
public class ProcessedRequest {

    private String id;

    private String correlation;

    CompletionStage<AgentResponse> response;
}
