package com.iotspear.agent;

import com.google.common.base.Strings;
import com.iotspear.agent.data.*;
import com.iotspear.agent.interfaces.RequestDevice;
import com.iotspear.agent.interfaces.RequestsRelay;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.iotspear.agent.helpers.FluentHelper.not;

@Slf4j
public class Processor {

    private final RelayCredentials relayCredentials;
    private final RequestsRelay requestsRelay;
    private final RequestDevice requestDevice;

    @Inject
    public Processor(RelayCredentials relayCredentials, RequestsRelay requestsRelay, RequestDevice requestDevice) {

        this.relayCredentials = relayCredentials;
        this.requestsRelay = requestsRelay;
        this.requestDevice = requestDevice;
    }

    public void processRequests() {

        log.info("Processing Requests ...");

        do {
            processBatch().toCompletableFuture().join();
        } while (true);
    }

    private CompletionStage<Void> processBatch() {

        return requestsRelay.login(relayCredentials).thenCompose(bearerInfo -> {

            final Function<Stream<ProcessedRequest>, CompletionStage<Void>> putResponses = processed -> {

                val results = processed.map(request -> request.getResponse().thenCompose(response ->

                        requestsRelay.putResponse(bearerInfo.getToken(), request.getId(), request.getCorrelation(), response)
                ));

                return CompletableFuture.allOf(results.toArray(CompletableFuture[]::new));
            };

            val publicHost = String.format("https://%s.iotspear.com", bearerInfo.getClaim().getDevice());
            val accountHost = Optional.ofNullable(bearerInfo.getClaim().getHost()).filter(not(Strings::isNullOrEmpty));

            final Function<ProxyRequest, CompletionStage<AgentResponse>> processRequest = response -> requestDevice.processRequest(response, publicHost, () -> accountHost);
            final Function<Stream<ProxyRequest>, Stream<ProcessedRequest>> processRequests = requests -> requests.map(request -> request.toProcessed(processRequest));

            return requestsRelay.getRequests(bearerInfo.getToken()).
                    thenApply(ProxyRequests::getBatch).
                    thenApply(Collection::stream).
                    thenApply(processRequests).
                    thenCompose(putResponses);

        }).exceptionally(error -> {

            log.error("Error processing request batch", error);

            return CompletableFuture.allOf(CompletableFuture.completedFuture(null)).join();
        });
    }
}
