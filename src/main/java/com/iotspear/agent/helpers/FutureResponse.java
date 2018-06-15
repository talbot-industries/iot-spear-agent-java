package com.iotspear.agent.helpers;

import com.iotspear.agent.data.AgentResponse;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.async.Callback;
import com.mashape.unirest.http.exceptions.UnirestException;

public class FutureResponse extends FutureAdapter<AgentResponse> implements Callback<String> {

    @Override
    public void completed(HttpResponse<String> response) {

        promise.complete(new AgentResponse(response.getStatus(), response.getHeaders(), response.getBody()));
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
