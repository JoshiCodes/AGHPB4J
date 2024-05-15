package de.joshicodes.aghpb4j;

public class Test {

    public static void main(String[] args) {
        AGHPB aghpb = new AGHPB();
        System.out.println("Starting test");
        aghpb.retrieveStatus().queue(status -> {
            System.out.println("Received status: " + status.version());
        });
        aghpb.retrieveInfo().queue(info -> {
            System.out.println("Received info: " + info.bookCount() + " books, API version: " + info.apiVersion());
        });
        aghpb.retrieveAllCategories(true).queue(list -> {
System.out.println("Received categories: " + list);
        });
    }

}
