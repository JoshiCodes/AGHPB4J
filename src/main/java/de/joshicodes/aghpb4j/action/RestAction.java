package de.joshicodes.aghpb4j.action;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public class RestAction<T> {

    protected final String url;
    protected final String method;
    protected final Class<T> tClass;

    protected final Function<HttpRequest.Builder, HttpRequest.Builder> clientModifier;
    protected final Function<RestResponse, T> responseHandler;

    public RestAction(String url, String method, Class<T> tClass, Function<HttpRequest.Builder, HttpRequest.Builder> clientModifier, Function<RestResponse, T> responseHandler) {
        this.url = url;
        this.method = method;
        this.tClass = tClass;
        this.clientModifier = clientModifier;
        this.responseHandler = responseHandler;
    }

    public RestAction(String url, String method, Class<T> tClass, Function<RestResponse, T> responseHandler) {
        this(url, method, tClass, Function.identity(), responseHandler);
    }

    public RestAction(String url, String method, Class<T> tClass) {
        this(url, method, tClass, Function.identity(), response -> null);
    }

    public RestAction(String url, Class<T> tClass, Function<HttpRequest.Builder, HttpRequest.Builder> clientModifier, Function<RestResponse, T> responseHandler) {
        this(url, "GET", tClass, clientModifier, responseHandler);
    }

    /**
     * Executes the request asynchronously. <br>
     * If you want to execute the request synchronously, use {@link #execute()} instead.
     *
     * @see #execute()
     * @see #queue(Consumer)
     * @see #queue(Consumer, Consumer)
     */
    public void queue() {
        queue(null, null);
    }

    /**
     * Executes the request asynchronously. <br>
     * If you want to execute the request synchronously, use {@link #execute()} instead.
     * @param success The consumer that will be called when the request was successful. Can be null.
     *
     * @see #execute()
     * @see #queue()
     * @see #queue(Consumer, Consumer)
     */
    public void queue(Consumer<T> success) {
        queue(success, (e) -> {
            throw new RuntimeException(e);
        });
    }

    /**
     * Executes the request asynchronously. <br>
     * If you want to execute the request synchronously, use {@link #execute()} instead.
     * @param success The consumer that will be called when the request was successful. Can be null.
     * @param failure The consumer that will be called when the request failed. Can be null.
     *
     * @see #execute()
     * @see #queue()
     * @see #queue(Consumer)
     */
    public void queue(Consumer<T> success, Consumer<Throwable> failure) {
        CompletableFuture.runAsync(() -> {
            try {
                T result = execute();
                if(success != null) success.accept(result);
            } catch (Throwable e) {
                if(failure != null) failure.accept(e);
            }
        }).join();
    }

    /**
     * Executes the request and returns the result. This method is blocking. <br>
     * If you want to execute the request asynchronously, use {@link #queue()} instead.
     * @return The result of the request
     */
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
            HttpResponse<String> response = client.build().send(request.build(), HttpResponse.BodyHandlers.ofString());
            return responseHandler.apply(new RestResponse<>(response, String.class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public record RestResponse<A>(HttpResponse<A> httpResponse, Class<A> aClass) {

        public JsonElement getAsJsonElement() {
            if(httpResponse.body() == null)
                return null;
            String body = String.valueOf(httpResponse.body());
            if(body.isEmpty())
                return null;
            return JsonParser.parseString(body);
        }

        public JsonObject getAsJsonObject() {
            JsonElement element = getAsJsonElement();
            if(element == null || !element.isJsonObject())
                return new JsonObject();
            return element.getAsJsonObject();
        }

    }

}
