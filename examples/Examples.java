import de.joshicodes.aghpb4j.AGHPB;
import de.joshicodes.aghpb4j.objects.AGHPBook;

public class Examples {

    public static void main(String[] args) {
        // To use a custom url, provide it as a parameter:
        // AGHPB aghpb = new AGHPB("http://localhost:5000");
        // If no url is provided, the default public api will be used (https://api.devgoldy.xyz/aghpb/v1/)
        AGHPB aghpb = new AGHPB();

        // Get the status (also available as aghpb.retrieveStatus().queue(status -> {}) for non-blocking)
        // The ApiStatus only holds the version - which should be the same as in the ApiInfo
        AGHPB.ApiStatus status = aghpb.retrieveStatus().execute();
        System.out.println("API Version: " + status.version());

        // Get the info (also available as aghpb.retrieveInfo().queue(info -> {}) for non-blocking)
        // The ApiInfo holds the version and the book count of the api
        AGHPB.ApiInfo info = aghpb.retrieveInfo().execute();
        System.out.println("API Version: " + info.apiVersion() + " - bookCount: " + info.bookCount());

        // Retrieve an Image

        // blocking
        AGHPBook book = aghpb.retrieveRandomBook().execute();
        System.out.println("Book: " + book.bookName());

        // non-blocking
        aghpb.retrieveRandomBook().queue(book1 -> {
            System.out.println("Book: " + book1.bookName());
        });

        // You can also modify the request before executing it
        // or use different methods (AGHPB#retrieveRandomBook(String category, AGHPBook.BookImageType type))
        // to retrieve a book with a specific category and image type
        aghpb.retrieveRandomBook().withCategory("java").useType(AGHPBook.BookImageType.PNG).queue(book1 -> {
            System.out.println("Book: " + book1.bookName());
        });
        // is the same as
        aghpb.retrieveRandomBook("java", AGHPBook.BookImageType.PNG).queue(book1 -> {
            System.out.println("Book: " + book1.bookName());
        });


        // Search by query (also available as blocking)
        aghpb.retrieveSearch("frieren", 50).queue(list -> {
            // list is a list of AGHPBook objects that match the query
            System.out.println("Searched Books: " + list.size());
            if(!list.isEmpty()) {
                AGHPBook book1 = list.get(0);
                System.out.println("Searched Book: " + book1.bookName());
            }
        });

        // get a specific book by searchId
        aghpb.retrieveBook(368 ).queue(book1 -> {
            System.out.println("Book: " + book1.bookName());
        });

    }

}
