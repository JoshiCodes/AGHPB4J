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

    private List<String> cachedCategories;

    /**
     * Creates a new AGHPB instance with the default url and no default caching.
     */
    public AGHPB() {
        this(DEFAULT_URL, false);
    }

    /**
     * Creates a new AGHPB instance for the specified url and no default caching.
     * @param url The url of the AGHPB API.
     */
    public AGHPB(final String url) {
        this(url, false);
    }

    /**
     * Creates a new AGHPB instance with the default url and the specified caching.
     * @param doCache Whether to cache by default or not.
     */
    private AGHPB(final boolean doCache) {
        this(DEFAULT_URL, doCache);
    }

    /**
     * Creates a new AGHPB instance for the specified url and the specified caching.
     * @param url The url of the AGHPB API.
     * @param doCache Whether to cache by default or not.
     */
    public AGHPB(final String url, final boolean doCache) {
        this.url = url;
        this.doCache = doCache;
    }

    /**
     * Returns the url of the AGHPB API. <br>
     * If no cached categories are present and cached is <b>disabled</b>, an IllegalStateException will be thrown. <br>
     * If no cached categories are present and cached is <b>enabled</b>, the categories will be retrieved and cached. <b>This is a blocking operation.</b>
     * @return The cached categories or null if no categories are cached and caching is disabled.
     */
    public List<String> getCategories() {
        if(cachedCategories == null) {
            if(!doCache) {
                throw new IllegalStateException("You are trying to access cached categories, without retrieving them first. Please use #retrieveAllCategories(true) first or enable caching in the constructor.");
            }
            try {
                cachedCategories = retrieveAllCategories().execute();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return cachedCategories;
    }

    /**
     * Retrieves a random book from the AGHPB API. <br>
     * The book will be in PNG format and will be retrieved from any category. <br>
     * @return An ImageAction for the random book.
     *
     * @see #retrieveRandomBook(String)
     * @see #retrieveRandomBook(AGHPBook.BookImageType)
     * @see #retrieveRandomBook(String, AGHPBook.BookImageType)
     */
    public ImageAction<AGHPBook> retrieveRandomBook() {
        return retrieveRandomBook(null, AGHPBook.BookImageType.PNG);
    }

    /**
     * Retrieves a random book from the AGHPB API. <br>
     * The book will be in PNG format and will be retrieved from the specified category. <br>
     * @param category The category to retrieve the book from.
     * @return An ImageAction for the random book.
     *
     * @see #retrieveRandomBook()
     * @see #retrieveRandomBook(AGHPBook.BookImageType)
     * @see #retrieveRandomBook(String, AGHPBook.BookImageType)
     */
    public ImageAction<AGHPBook> retrieveRandomBook(String category) {
        return retrieveRandomBook(category, AGHPBook.BookImageType.PNG);
    }

    /**
     * Retrieves a random book from the AGHPB API. <br>
     * The book will be in the specified format and will be retrieved from any category. <br>
     * @param type The format of the book.
     * @return An ImageAction for the random book.
     *
     * @see #retrieveRandomBook()
     * @see #retrieveRandomBook(String)
     * @see #retrieveRandomBook(String, AGHPBook.BookImageType)
     */
    public ImageAction<AGHPBook> retrieveRandomBook(AGHPBook.BookImageType type) {
        return retrieveRandomBook(null, type);
    }

    /**
     * Retrieves a random book from the AGHPB API. <br>
     * @param category The category to retrieve the book from. Can be null.
     * @param type The format of the book. Can be null.
     * @return An ImageAction for the random book.
     *
     * @see #retrieveRandomBook()
     * @see #retrieveRandomBook(String)
     * @see #retrieveRandomBook(AGHPBook.BookImageType)
     */
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

    /**
     * Retrieves the status of the AGHPB API.
     * @return A RestAction for the status.
     *
     * @see <a href="https://api.devgoldy.xyz/aghpb/v1/docs#/misc/status_nya_get">API Documentation</a>
     */
    public RestAction<ApiStatus> retrieveStatus() {
        return new RestAction<>(url + "/nya", "GET", ApiStatus.class, response -> {
            JsonObject json = response.getAsJsonObject();
            if(!json.has("version")) {
                throw new IllegalStateException("Response does not contain a version field");
            }
            return new ApiStatus(json.get("version").getAsString());
        });
    }

    /**
     * Retrieves all categories from the AGHPB API. <br>
     * This method will cache the categories if caching is enabled. <br>
     * @return A RestAction for the categories.
     *
     * @see <a href="https://api.devgoldy.xyz/aghpb/v1/docs#/books/All_Available_Categories_categories_get">API Documentation</a>
     * @see #retrieveAllCategories(boolean) to specify whether to cache or not.
     * @see #getCategories() when caching is enabled.
     */
    public RestAction<List<String>> retrieveAllCategories() {
        return retrieveAllCategories(doCache);
    }

    /**
     * Retrieves all categories from the AGHPB API. <br>
     * @param doCache Whether to cache the categories or not.
     * @return A RestAction for the categories.
     *
     * @see <a href="https://api.devgoldy.xyz/aghpb/v1/docs#/books/All_Available_Categories_categories_get">API Documentation</a>
     * @see #retrieveAllCategories() to cache when doCache is enabled.
     * @see #getCategories() when caching is enabled.
     */
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

    /**
     * Retrieves the info of the AGHPB API. <br>
     * The Info contains the book count and the API version. <br>
     * @return A RestAction for the info.
     *
     * @see <a href="https://api.devgoldy.xyz/aghpb/v1/docs#/other/Info_about_the_current_instance__info_get">API Documentation</a>
     */
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
