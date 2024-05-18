package de.joshicodes.aghpb4j.objects;

import java.io.*;

public record AGHPBook(
        byte[] imageBytes,
        BookImageType type,
        String category,
        String commitAuthor,
        String commitHash,
        String commitUrl,
        // Date dateAdded,
        String bookName,
        int searchId
) {

    /**
     * Enum for the image type of the book.
     */
    public enum BookImageType {
        PNG, JPEG
    }

    /**
     * Saves the image to a file.
     * @param file The file to save the image to.
     * @throws IOException If an I/O error occurs.
     */
    public void saveImage(File file) throws IOException {
        if(file == null) throw new IllegalArgumentException("File cannot be null. Please provide a valid file.");
        if(!file.exists()) {
            if(file.getParentFile() != null) file.getParentFile().mkdirs();
        }
        if(!file.getName().endsWith(".png") && !file.getName().endsWith(".jpg") && !file.getName().endsWith(".jpeg")) throw new IllegalArgumentException("File must be a valid image file (png, jpg, jpeg).");
        try (OutputStream os = new FileOutputStream(file)) {
            os.write(imageBytes);
        }
    }

    public ByteArrayInputStream getImageStream() {
        return new ByteArrayInputStream(imageBytes);
    }

}
