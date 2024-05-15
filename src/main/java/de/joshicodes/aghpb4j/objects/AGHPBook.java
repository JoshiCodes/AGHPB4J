package de.joshicodes.aghpb4j.objects;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public record AGHPBook(
        byte[] imageBytes,
        String category,
        String commitAuthor,
        String commitHash,
        String commitUrl,
        // Date dateAdded,
        String bookName,
        int searchId
) {

    public void saveImage(File file) throws IOException {
        if(file == null) throw new IllegalArgumentException("File cannot be null. Please provide a valid file.");
        if(!file.exists()) file.getParentFile().mkdirs();
        if(!file.getName().endsWith(".png") && !file.getName().endsWith(".jpg") && !file.getName().endsWith(".jpeg")) throw new IllegalArgumentException("File must be a valid image file (png, jpg, jpeg).");
        FileWriter writer = new FileWriter(file);
        writer.write(new String(imageBytes));
        writer.close();
    }

}
