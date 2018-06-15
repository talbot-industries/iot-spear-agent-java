package com.iotspear.agent.data;

import lombok.Value;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

@Value
public class ProxyRequest {

    private String id;

    private String correlation;

    private String t;

    private String ip;

    private String verb;

    private String host;

    private String uri;

    private Map<String, List<String>> headers;

    private String body;


    public ProcessedRequest toProcessed(Function<ProxyRequest, CompletionStage<AgentResponse>> response) {

        return new ProcessedRequest(id, correlation, response.apply(this));
    }
}
