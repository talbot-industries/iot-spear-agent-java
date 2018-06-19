package com.iotspear.agent.helpers;

import com.iotspear.agent.data.AgentResponse;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.async.Callback;
import com.mashape.unirest.http.exceptions.UnirestException;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Slf4j
public class FutureResponse extends FutureAdapter<AgentResponse> implements Callback<InputStream> {

    private final Function<Map<String, List<String>>, Map<String, List<String>>> headersTransformer;

    public FutureResponse(Function<Map<String, List<String>>, Map<String, List<String>>> headersTransformer) {

        this.headersTransformer = headersTransformer;
    }

    @Override
    public void completed(HttpResponse<InputStream> response) {

        try {
            promise.complete(
                    new AgentResponse(
                            response.getStatus(),
                            headersTransformer.apply(response.getHeaders()),
                            Base64Source.fromStream(response.getBody())
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
