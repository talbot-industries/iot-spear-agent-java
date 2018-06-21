package com.iotspear.agent.services;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.iotspear.agent.data.*;
import com.iotspear.agent.helpers.FutureCallback;
import com.iotspear.agent.interfaces.RequestsRelay;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class RelayClient implements RequestsRelay {

    private final String relayHostName;
    private final ObjectMapper mapper;

    @Inject
    public RelayClient(ObjectMapper mapper, @Named("relayHostName") String relayHostName) {

        Unirest.setObjectMapper(mapper);
        Unirest.setTimeouts(10000, 30000);

//      Unirest.setProxy(new HttpHost("")); // @TODO: make available in Config as a proxy option

        this.mapper = mapper;
        this.relayHostName = relayHostName;
    }

    @Override
    public CompletionStage<BearerInformation> login(RelayCredentials credentials) {

        log.info("Refreshing Relay Bearer Token ...");

        val callback = new FutureCallback<String>();

        Unirest.post(relayHostName + "/login").
                header("Content-Type", "application/json").
                body(credentials).
                asStringAsync(callback);

        return callback.stage().thenApply(bearerToken -> {

            val bearerInfo = Arrays.stream(bearerToken.split(Pattern.quote("."))).
                    skip(1).
                    findFirst().
                    map(claimBase64 -> Base64.getDecoder().decode(claimBase64)).
                    map(String::new).
                    map(claimJson -> mapper.readValue(claimJson, BearerClaim.class)).
                    map(bearerClaim -> new BearerInformation(bearerToken, bearerClaim));

            bearerInfo.map(BearerInformation::getClaim).map(BearerClaim::getDevice).
                    ifPresent(deviceName -> log.info("Relay Login successful for Device: " + deviceName));

            return bearerInfo.orElseThrow(() -> new RuntimeException("Invalid Bearer Token"));
        });
    }

    @Override
    public CompletionStage<ProxyRequests> getRequests(String bearerToken) {

        log.info("Waiting for Relay requests ...");

        val callback = new FutureCallback<ProxyRequests>();

        Unirest.get(relayHostName + "/requests").
                headers(bearerTokenHeader(bearerToken)).
                asObjectAsync(ProxyRequests.class, callback);

        return callback.stage().thenApply(requests -> {

            Optional.ofNullable(requests).map(ProxyRequests::getBatch).map(List::size).ifPresent(batchSize ->
                    log.info(String.format("Received batch of %s Relay request(s)", batchSize)));

            return requests;

        }).exceptionally(error -> {

            log.error("Failed to retrieve Relay requests", error);
            return new ProxyRequests(ImmutableList.of());
        });
    }

    @Override
    public CompletionStage<Void> putResponse(String bearerToken, String requestId, String correlation, AgentResponse response) {

        log.info(String.format("Sending response for Relay request Id: %s", requestId));

        val callback = new FutureCallback<String>();

        Unirest.put(relayHostName + "/response/" + requestId).
                headers(bearerTokenHeader(bearerToken)).
                header("X-Correlation-ID", correlation).
                header("Content-Type", "application/json").
                body(response).
                asStringAsync(callback);

        val result = callback.stage().thenApply(ignored -> {

            log.info(String.format("Successfully sent response for Relay request Id: %s", requestId));
            return null;

        }).exceptionally(error -> {

            log.error(String.format("Failed to send response for Relay request Id: %s", requestId), error);
            return null;
        });

        return CompletableFuture.allOf(result.toCompletableFuture());
    }

    private Map<String, String> bearerTokenHeader(String bearerToken) {

        return ImmutableMap.of("Authorization", "Bearer " + bearerToken);
    }
}
