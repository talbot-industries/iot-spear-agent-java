package com.iotspear.agent.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Value;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

@Value
@JsonIgnoreProperties(ignoreUnknown=true)
public class ProxyRequest {

    private String id;

    private String correlation;

    private String t;

    private String clientId;

    private String ip;

    private String verb;

    private String host;

    private String uri;

    private Map<String, List<String>> headers;

    private String body;


    public ProcessedRequest toProcessed(Function<ProxyRequest, CompletionStage<AgentResponse>> response) {

        return new ProcessedRequest(id, clientId, correlation, response.apply(this));
    }
}
