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

    protected String url;
    protected final String method;
    protected final Class<T> tClass;

    protected Function<HttpRequest.Builder, HttpRequest.Builder> clientModifier;
    protected final Function<RestResponse, T> responseHandler;

    public RestAction(final String url, final String method, final Class<T> tClass, final Function<HttpRequest.Builder, HttpRequest.Builder> clientModifier, final Function<RestResponse, T> responseHandler) {
        this.url = url;
        this.method = method;
        this.tClass = tClass;
        this.clientModifier = clientModifier;
        this.responseHandler = responseHandler;
    }

    public RestAction(final String url, final String method, final Class<T> tClass, final Function<RestResponse, T> responseHandler) {
        this(url, method, tClass, Function.identity(), responseHandler);
    }

    public RestAction(final String url, final String method, final Class<T> tClass) {
        this(url, method, tClass, Function.identity(), response -> null);
    }

    public RestAction(final String url, final Class<T> tClass, final Function<HttpRequest.Builder, HttpRequest.Builder> clientModifier, final Function<RestResponse, T> responseHandler) {
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
    public void queue(final Consumer<T> success) {
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
    public void queue(final Consumer<T> success, final Consumer<Throwable> failure) {
        CompletableFuture.runAsync(() -> {
            try {
                final T result = execute();
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
            final HttpClient.Builder client = build();
            final HttpRequest.Builder request = buildRequest();
            final HttpResponse<String> response = sendRequest(client.build(), request.build(), HttpResponse.BodyHandlers.ofString(), 3);
            return responseHandler.apply(new RestResponse<>(response, String.class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected <C> HttpResponse<C> sendRequest(HttpClient client, HttpRequest request, HttpResponse.BodyHandler<C> handler, int retries) {
        try {
            final HttpResponse<C> response = client.send(request, handler);
            if(response.headers().firstValue("x-ratelimit-remaining").orElse("1").equals("0")) {
                // Rate limit
                final long reset = Long.parseLong(response.headers().firstValue("x-ratelimit-reset").orElse("0"));
                final long now = System.currentTimeMillis() / 1000;
                final long diff = reset - now;
                if(diff > 0) {
                    // Sleep
                    Thread.sleep(diff * 1000);
                    // Retry
                    return sendRequest(client, request, handler, retries - 1);
                }
            }
            return response;
        } catch (Exception e) {
            if(retries > 0) {
                return sendRequest(client, request, handler, retries - 1);
            }
            throw new RuntimeException(e);
        }
    }

    protected HttpClient.Builder build() {
        return HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .version(HttpClient.Version.HTTP_2);
    }

    protected HttpRequest.Builder buildRequest() {
        HttpRequest.Builder request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .method(method, HttpRequest.BodyPublishers.noBody());
        if(clientModifier != null) {
            request = clientModifier.apply(request);
        }
        return request;
    }

    public record RestResponse<A>(HttpResponse<A> httpResponse, Class<A> aClass) {

        public String rawBody() {
            if(httpResponse.body() == null)
                return null;
            return String.valueOf(httpResponse.body());
        }

        public JsonElement getAsJsonElement() {
            if(httpResponse.statusCode() != 200)
                return null;
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
