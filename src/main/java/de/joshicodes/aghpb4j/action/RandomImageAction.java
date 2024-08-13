package de.joshicodes.aghpb4j.action;

import de.joshicodes.aghpb4j.objects.AGHPBook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class RandomImageAction extends ImageAction<AGHPBook> {

    public static final String URL = "%s/random";

    private final String initialUrl;
    private AGHPBook.BookImageType type = AGHPBook.BookImageType.PNG;

    public RandomImageAction(final String apiUrl) {
        super(String.format(URL, apiUrl), AGHPBook.class, null, null);
        initialUrl = apiUrl;
        clientModifier = (request) -> {
            request.header("Accept", "image/" + type.name().toLowerCase());
            return request;
        };
        responseHandler = response -> {
            final String contentType = response.httpResponse().headers().firstValue("Content-Type").orElse(null);
            if(contentType == null || !contentType.startsWith("image/")) {
                throw new IllegalStateException("Failed request to " + url + "! Response is not an image! " + "( " + response.rawBody() + " )." + " Content Type is " + contentType + ". Expected image/*.");
            }
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
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
        };
    }

    public RandomImageAction useType(final AGHPBook.BookImageType type) {
        this.type = type;
        return this;
    }

    public RandomImageAction withCategory(final String category) {
        final String cat = category != null ? ("?category=" + category) : "";
        url = String.format(URL, this.initialUrl) + cat;
        return this;
    }

}
