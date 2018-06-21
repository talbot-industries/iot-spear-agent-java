package com.iotspear.agent.helpers;

import com.iotspear.agent.data.AgentResponse;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.async.Callback;
import com.mashape.unirest.http.exceptions.UnirestException;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

@Slf4j
public class FutureResponse extends FutureAdapter<AgentResponse> implements Callback<InputStream> {

    private final Function<Map<String, List<String>>, Map<String, List<String>>> headersTransformer;
    private final BiFunction<Map<String, List<String>>, InputStream, InputStream> bodyTransformer;

    public FutureResponse(Function<Map<String, List<String>>, Map<String, List<String>>> headersTransformer,
                          BiFunction<Map<String, List<String>>, InputStream, InputStream> bodyTransformer) {

        this.headersTransformer = headersTransformer;
        this.bodyTransformer = bodyTransformer;
    }

    @Override
    public void completed(HttpResponse<InputStream> response) {

        try {
            val headers = headersTransformer.apply(response.getHeaders());

            promise.complete(
                    new AgentResponse(
                            response.getStatus(),
                            headers,
                            Base64Source.fromStream(bodyTransformer.apply(headers, response.getBody()))
                    )
            );
        } catch (Exception error) {

            log.error("Completion Error", error);
            promise.completeExceptionally(error);
        }
    }

    @Override
    public void failed(UnirestException e) {

        promise.completeExceptionally(e);
    }

    @Override
    public void cancelled() {

        promise.cancel(true);
    }
}
