package de.joshicodes.aghpb4j.action;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.function.Function;

public class ImageAction<T> extends RestAction<T> {

    private final Function<RestResponse<byte[]>, T> responseHandler;

    public ImageAction(String url, Class<T> tClass, Function<HttpRequest.Builder, HttpRequest.Builder> clientModifier, Function<RestResponse<byte[]>, T> responseHandler) {
        super(url, "GET", tClass, clientModifier, null);
        this.responseHandler = responseHandler;
    }

    @Override
    public T execute() {
        try {
            HttpClient.Builder client = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .version(HttpClient.Version.HTTP_2);
            HttpRequest.Builder request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .method(method, HttpRequest.BodyPublishers.noBody());
            if(clientModifier != null) {
                request = clientModifier.apply(request);
            }
            HttpResponse<byte[]> response = client.build().send(request.build(), HttpResponse.BodyHandlers.ofByteArray());
            return this.responseHandler.apply(new RestResponse<>(response, byte[].class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
