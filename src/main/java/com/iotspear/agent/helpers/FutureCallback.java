package com.iotspear.agent.helpers;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.async.Callback;
import com.mashape.unirest.http.exceptions.UnirestException;

public class FutureCallback<T> extends FutureAdapter<T> implements Callback<T> {

    @Override
    public void completed(HttpResponse<T> response) {

        if (response.getStatus() >= 400) {

            promise.completeExceptionally(new Exception(response.getStatusText()));

        } else {

            promise.complete(response.getBody());
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
