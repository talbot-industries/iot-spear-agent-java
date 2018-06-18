package com.iotspear.agent.helpers;

import com.iotspear.agent.data.AgentResponse;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.async.Callback;
import com.mashape.unirest.http.exceptions.UnirestException;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;

@Slf4j
public class FutureResponse extends FutureAdapter<AgentResponse> implements Callback<InputStream> {

    @Override
    public void completed(HttpResponse<InputStream> response) {

        try {
            promise.complete(
                    new AgentResponse(
                            response.getStatus(),
                            response.getHeaders(),
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
