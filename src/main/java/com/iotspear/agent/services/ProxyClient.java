package com.iotspear.agent.services;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.CharStreams;
import com.iotspear.agent.data.AgentResponse;
import com.iotspear.agent.data.ProxyRequest;
import com.iotspear.agent.helpers.ContextClient;
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
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;
import java.util.function.Function;
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
    public CompletionStage<AgentResponse> processRequest(ProxyRequest request, String publicHost, Supplier<Optional<String>> accountHost) {

        log.info(String.format("Processing Request Id: %s from %s (Client Id: %s) - %s %s", request.getId(), request.getIp(), request.getClientId(), request.getVerb(), request.getUri()));

        val privateHost = accountHost.get().orElse(deviceHostName);

        final Function<Map<String, List<String>>, Map<String, List<String>>> headersTransformer = headers ->
                headers.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry ->
                        entry.getKey().equals("Location") ?
                                entry.getValue().stream().map(header ->
                                        header.replace(privateHost, publicHost)).collect(Collectors.toList()) :
                                entry.getValue()));

        final BiFunction<Map<String, List<String>>, InputStream, InputStream> bodyTransformer = (headers, bodyStream) -> {

            val replaceableType = headers.getOrDefault("Content-Type", ImmutableList.of()).stream().findFirst().filter(type ->
                    type.startsWith("text/html") || type.startsWith("text/javascript"));

            val replacementStream = replaceableType.flatMap(contentType -> {

                log.debug(String.format("Replacing host names in file of Content-Type %s", contentType));

                val charset = Arrays.stream(contentType.split("charset=")).skip(1).findFirst().orElse("UTF-8");

                try {
                    val bodyReader = new InputStreamReader(bodyStream, charset);
                    val bodyText = CharStreams.toString(bodyReader);
                    val convertedText = bodyText.replace(privateHost, publicHost);

                    final InputStream convertedStream = new ByteArrayInputStream(convertedText.getBytes(charset));

                    return Optional.of(convertedStream);

                } catch (Exception error) {

                    log.error(String.format("Failed to convert Body text of Content-Type: %s", contentType), error);
                    return Optional.empty();
                }
            });

            return replacementStream.orElse(bodyStream);
        };

        val callback = new FutureResponse(headersTransformer, bodyTransformer);

        val url = privateHost + request.getUri();
        val headers = request.getHeaders().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().get(0)));

        ContextClient.proxyRequest.set(request);
        uniRestRequest(request.getVerb(), url, headers, request.getBody()).asBinaryAsync(callback);

        return callback.stage().thenApply(response -> {

            log.info(String.format("Processed Request Id: %s (Client Id: %s) - %s", request.getId(), request.getClientId(), response.getStatus()));
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
