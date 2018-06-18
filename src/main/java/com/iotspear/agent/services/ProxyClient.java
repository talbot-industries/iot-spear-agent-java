package com.iotspear.agent.services;

import com.google.common.collect.ImmutableMap;
import com.iotspear.agent.data.AgentResponse;
import com.iotspear.agent.data.ProxyRequest;
import com.iotspear.agent.helpers.FutureResponse;
import com.iotspear.agent.interfaces.RequestDevice;
import com.mashape.unirest.http.HttpMethod;
import com.mashape.unirest.request.BaseRequest;
import com.mashape.unirest.request.GetRequest;
import com.mashape.unirest.request.HttpRequestWithBody;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Slf4j
public class ProxyClient implements RequestDevice {

    private final String deviceHostName;

    @Inject
    public ProxyClient(@Named("deviceHostName") String deviceHostName) {

        this.deviceHostName = deviceHostName;
    }

    @Override
    public CompletionStage<AgentResponse> processRequest(ProxyRequest request, Supplier<Optional<String>> accountHost) {

        log.info(String.format("Processing Request Id: %s from %s - %s %s", request.getId(), request.getIp(), request.getVerb(), request.getUri()));

        val callback = new FutureResponse();

        val url = accountHost.get().orElse(deviceHostName) + request.getUri();
        val headers = request.getHeaders().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().get(0)));

        uniRestRequest(request.getVerb(), url, headers, request.getBody()).asBinaryAsync(callback);

        return callback.stage().thenApply(response -> {

            log.info(String.format("Processed Request Id: %s - %s", request.getId(), response.getStatus()));
            return response;

        }).exceptionally(error -> {

            log.error("Failed to forward request", error);
            return new AgentResponse(502, ImmutableMap.of(), "");
        });
    }

    private BaseRequest uniRestRequest(String verb, String url, Map<String, String> headers, String body) {

        val httpMethod = HttpMethod.valueOf(verb.toUpperCase().trim());

        switch (httpMethod) {

            case HEAD:
            case GET:   return new GetRequest(httpMethod, url).headers(headers);

            default:    return new HttpRequestWithBody(httpMethod, url).headers(headers).body(Base64.getDecoder().decode(body));
        }
    }
}
