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

    /**
     * Creates a new AGHPB instance with the default url
     */
    public AGHPB() {
        this(DEFAULT_URL);
    }

    /**
     * Creates a new AGHPB instance for the specified url
     * @param url The url of the AGHPB API.
     */
    public AGHPB(final String url) {
        this.url = url;
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
     * @return A RestAction for the categories.
     *
     * @see <a href="https://api.devgoldy.xyz/aghpb/v1/docs#/books/All_Available_Categories_categories_get">API Documentation</a>
     */
    public RestAction<List<String>> retrieveAllCategories() {
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
            return categories;
        });
    }

    /**
     * Retrieves a List of AGHPBooks from the AGHPB API. <br>
     * <b>NOTE:</b> The AGHPBooks will not contain any image data. Use #retrieveBook(String) with the search_id.<br>
     * @param query The query to search for.
     * @return A RestAction for the search.
     */
    public RestAction<List<AGHPBook>> retrieveSearch(String query) {
        return retrieveSearch(query, null, -1);
    }

    /**
     * Retrieves a List of AGHPBooks from the AGHPB API. <br>
     * <b>NOTE:</b> The AGHPBooks will not contain any image data. Use #retrieveBook(String) with the search_id.<br>
     * @param query The query to search for.
     * @param limit The limit of books to retrieve. Must be greater than 0.
     * @return A RestAction for the search.
     */
    public RestAction<List<AGHPBook>> retrieveSearch(String query, int limit) {
        return retrieveSearch(query, null, limit);
    }

    /**
     * Retrieves a List of AGHPBooks from the AGHPB API. <br>
     * <b>NOTE:</b> The AGHPBooks will not contain any image data. Use #retrieveBook(String) with the search_id.<br>
     * @param query The query to search for.
     * @param category The category to search in. Can be null.
     * @return A RestAction for the search.
     */
    public RestAction<List<AGHPBook>> retrieveSearch(String query, String category) {
        return retrieveSearch(query, category, -1);
    }

    /**
     * Retrieves a List of AGHPBooks from the AGHPB API. <br>
     * <b>NOTE:</b> The AGHPBooks will not contain any image data. Use #retrieveBook(String) with the search_id.<br>
     * @param query The query to search for.
     * @param category The category to search in. Can be null.
     * @param limit The limit of books to retrieve. Must be greater than 0.
     * @return A RestAction for the search.
     */
    public RestAction<List<AGHPBook>> retrieveSearch(String query, String category, int limit) {
        final String url = this.url + "/search?query=" + query + (category != null ? "&category=" + category : "") + (limit > 0 ? "&limit=" + limit : "");
        return new RestAction<>(url, "GET", null, (response) -> {
            JsonElement json = response.getAsJsonElement();
            if(json == null || !json.isJsonArray()) {
                throw new IllegalStateException("Response is not a JSON array");
            }
            JsonArray array = json.getAsJsonArray();
            List<AGHPBook> books = new ArrayList<>();
            for(JsonElement element : array) {
                if(!element.isJsonObject()) {
                    continue;
                }
                JsonObject obj = element.getAsJsonObject();
                if(
                        !obj.has("search_id")
                        || !obj.has("name")
                        || !obj.has("category")
                        || !obj.has("date_added")
                        || !obj.has("commit_url")
                        || !obj.has("commit_author")
                        || !obj.has("commit_hash")
                ) {
                    continue;
                }
                final int searchId = Integer.parseInt(obj.get("search_id").getAsString());
                final String name = obj.get("name").getAsString();
                final String category1 = obj.get("category").getAsString();
                final String dateAdded = obj.get("date_added").getAsString();
                final String commitUrl = obj.get("commit_url").getAsString();
                final String commitAuthor = obj.get("commit_author").getAsString();
                final String commitHash = obj.get("commit_hash").getAsString();
                books.add(new AGHPBook(null, null, category1, commitAuthor, commitHash, commitUrl, name, searchId));
            }
            return books;
        });
    }

    /**
     * Retrieves a book from the AGHPB API. <br>
     * This Method is used, to get the image data of a book, which was retrieved by #retrieveSearch(String) and does not contain any image data. <br>
     * @param book The book to retrieve.
     * @return An ImageAction for the book.
     */
    public ImageAction<AGHPBook> retrieveBook(AGHPBook book) {
        return retrieveBook(book.searchId());
    }

    /**
     * Retrieves a book from the AGHPB API. <br>
     * The returned book will contain the image data in PNG. <br>
     * @param searchId The search_id of the book to retrieve.
     * @return An ImageAction for the book.
     */
    public ImageAction<AGHPBook> retrieveBook(int searchId) {
        return retrieveBook(searchId, AGHPBook.BookImageType.PNG);
    }

    /**
     * Retrieves a book from the AGHPB API. <br>
     * @param book The book to retrieve.
     * @param type The format of the book.
     * @return An ImageAction for the book.
     */
    public ImageAction<AGHPBook> retrieveBook(AGHPBook book, AGHPBook.BookImageType type) {
        return retrieveBook(book.searchId(), type);
    }

    /**
     * Retrieves a book from the AGHPB API. <br>
     * @param searchId The search_id of the book to retrieve.
     * @param type The format of the book.
     * @return An ImageAction for the book.
     */
    public ImageAction<AGHPBook> retrieveBook(int searchId, AGHPBook.BookImageType type) {
        final String url = this.url + "/book?search_id=" + searchId;
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
            // final Date dateAdded = new Date(response.httpResponse().headers().firstValue("Book-Date-Added").orElse(null));
            return new AGHPBook(baos.toByteArray(), type, imageCategory, commitAuthor, commitHash, commitUrl, bookName, searchId);
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
