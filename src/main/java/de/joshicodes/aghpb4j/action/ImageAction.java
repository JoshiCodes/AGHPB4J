package de.joshicodes.aghpb4j.action;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.function.Function;

public class ImageAction<T> extends RestAction<T> {

    protected Function<RestResponse<byte[]>, T> responseHandler;

    public ImageAction(final String url, final Class<T> tClass, final Function<HttpRequest.Builder, HttpRequest.Builder> clientModifier, final Function<RestResponse<byte[]>, T> responseHandler) {
        super(url, "GET", tClass, clientModifier, null);
        this.responseHandler = responseHandler;
    }

    @Override
    public T execute() {
        try {
            final HttpClient.Builder client = build();
            final HttpRequest.Builder request = buildRequest();
            final HttpResponse<byte[]> response = sendRequest(client.build(), request.build(), HttpResponse.BodyHandlers.ofByteArray(), 3);
            return this.responseHandler.apply(new RestResponse<>(response, byte[].class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
