package de.joshicodes.aghpb4j;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.joshicodes.aghpb4j.action.ImageAction;
import de.joshicodes.aghpb4j.action.RestAction;
import de.joshicodes.aghpb4j.objects.AGHPBook;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class AGHPB {

    public static final String DEFAULT_URL = "https://api.devgoldy.xyz/aghpb/v1";

    private final String url;
    private final boolean doCache;

    private ApiStatus cachedStatus;
    private List<String> cachedCategories;

    public AGHPB() {
        this(DEFAULT_URL, false);
    }

    public AGHPB(final String url) {
        this(url, false);
    }

    private AGHPB(final boolean doCache) {
        this(DEFAULT_URL, doCache);
    }

    public AGHPB(final String url, final boolean doCache) {
        this.url = url;
        this.doCache = doCache;
    }

    public ApiStatus getStatus() {
        return cachedStatus;
    }

    public List<String> getCategories() {
        return cachedCategories;
    }

    public ImageAction<AGHPBook> retrieveRandomBook() {
        return retrieveRandomBook(null, AGHPBook.BookImageType.PNG);
    }

    public ImageAction<AGHPBook> retrieveRandomBook(String category) {
        return retrieveRandomBook(category, AGHPBook.BookImageType.PNG);
    }

    public ImageAction<AGHPBook> retrieveRandomBook(AGHPBook.BookImageType type) {
        return retrieveRandomBook(null, type);
    }

    public ImageAction<AGHPBook> retrieveRandomBook(String category, AGHPBook.BookImageType type) {
        final String url = this.url + "/random" + (category == null ? "" : "?category=" + category);
        return new ImageAction<>(url, AGHPBook.class, (request) -> {
            request.header("Accept", "image/" + type.name().toLowerCase());
            return request;
        }, response -> {
            final String contentType = response.httpResponse().headers().firstValue("Content-Type").orElse(null);
            if(contentType == null || !contentType.startsWith("image/")) {
                JsonElement json = response.getAsJsonElement();
                throw new IllegalStateException("Response is not an image! " + (json != null ? ("( " + json + " )") : ""));
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                baos.write(response.httpResponse().body());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            final String imageCategory = response.httpResponse().headers().firstValue("Book-Category").orElse(null);
            final String bookName = response.httpResponse().headers().firstValue("Book-Name").orElse(null);
            final String commitAuthor = response.httpResponse().headers().firstValue("Book-Commit-Author").orElse(null);
            final String commitHash = response.httpResponse().headers().firstValue("Book-Commit-Hash").orElse(null);
            final String commitUrl = response.httpResponse().headers().firstValue("Book-Commit-Url").orElse(null);
            final int searchId = Integer.parseInt(response.httpResponse().headers().firstValue("Book-Search-Id").orElse("-1"));
            // final Date dateAdded = new Date(response.httpResponse().headers().firstValue("Book-Date-Added").orElse(null));
            return new AGHPBook(baos.toByteArray(), type, imageCategory, commitAuthor, commitHash, commitUrl, bookName, searchId);
        });
    }

    public RestAction<ApiStatus> retrieveStatus() {
        return new RestAction<>(url + "/nya", "GET", ApiStatus.class, response -> {
            JsonObject json = response.getAsJsonObject();
            if(!json.has("version")) {
                throw new IllegalStateException("Response does not contain a version field");
            }
            return new ApiStatus(json.get("version").getAsString());
        });
    }

    public RestAction<List<String>> retrieveAllCategories() {
        return retrieveAllCategories(doCache);
    }

    public RestAction<List<String>> retrieveAllCategories(boolean doCache) {
        return new RestAction<List<String>>(url + "/categories", "GET", null, (response) -> {
            JsonElement json = response.getAsJsonElement();
            if(json == null || !json.isJsonArray()) {
                throw new IllegalStateException("Response is not a JSON array");
            }
            JsonArray array = json.getAsJsonArray();
            List<String> categories = new ArrayList<>();
            for(JsonElement element : array) {
                if(!element.isJsonPrimitive()) {
                    throw new IllegalStateException("Element is not a JSON primitive");
                }
                categories.add(element.getAsString());
            }
            if(doCache || this.doCache) {
                cachedCategories = categories;
            }
            return categories;
        });
    }

    public RestAction<ApiInfo> retrieveInfo() {
        return new RestAction<>(url + "/info", "GET", ApiInfo.class, response -> {
            JsonObject json = response.getAsJsonObject();
            if(!json.has("book_count") || !json.has("api_version")) {
                throw new IllegalStateException("Response does not contain bookCount or apiVersion field");
            }
            return new ApiInfo(json.get("book_count").getAsInt(), json.get("api_version").getAsString());
        });
    }

    public record ApiStatus(String version) { }
    public record ApiInfo(int bookCount, String apiVersion) { }

}
