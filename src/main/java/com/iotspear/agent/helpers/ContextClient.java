package com.iotspear.agent.helpers;

import com.iotspear.agent.data.ProxyRequest;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;
import org.apache.http.nio.protocol.HttpAsyncResponseConsumer;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.Future;

@Slf4j
@EqualsAndHashCode(callSuper = true)
@Value
public class ContextClient extends CloseableHttpAsyncClient {

    public static final ThreadLocal<ProxyRequest> proxyRequest = new ThreadLocal<>();

    private CloseableHttpAsyncClient innerClient;

    @Override
    public boolean isRunning() {

        return innerClient.isRunning();
    }

    @Override
    public void start() {

        innerClient.start();
    }

    @Override
    public void close() throws IOException {

        innerClient.close();
    }

    @Override
    public <T> Future<T> execute(
            HttpAsyncRequestProducer requestProducer,
            HttpAsyncResponseConsumer<T> responseConsumer,
            HttpContext context,
            FutureCallback<T> callback) {

        return innerClient.execute(requestProducer, responseConsumer, context, callback);
    }

    @Override
    public Future<HttpResponse> execute(
            final HttpUriRequest request,
            final FutureCallback<HttpResponse> callback) {

        val context = HttpClientContext.create();

        Optional.ofNullable(proxyRequest.get()).map(ProxyRequest::getClientId).ifPresent(context::setUserToken);
        proxyRequest.remove();

        log.debug(String.format("HttpAsyncClient User Token: %s",
                Optional.ofNullable(context.getUserToken()).map(Object::toString).orElse("<null>")));

        return execute(request, context, callback); // Context ensures different public clients don't share a TCP connection
    }

    @Override
    public Future<HttpResponse> execute(
            final HttpUriRequest request,
            final HttpContext context,
            final FutureCallback<HttpResponse> callback) {

        return innerClient.execute(request, context, callback);
    }
}
