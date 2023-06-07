package org.flagmatch;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.Binary;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A class that uploads flag images to MongoDB.
 * Reminder: Connection was to localhost, port 27017,
 *  db name is "flagsdb", with the collection of flags.
 */
public class FlagImageUploader {

    private static final String MONGO_HOST = "localhost";
    private static final int MONGO_PORT = 27017;
    private static final String MONGO_DB_NAME = "flagsdb";
    private static final String MONGO_COLLECTION_NAME = "flags";
    private static final String FLAGS_DIRECTORY = "flags";

    /**
     * Main method that uploads flag images to MongoDB.
     *
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        try (MongoClient mongoClient = MongoClients.create("mongodb://" + MONGO_HOST + ":" + MONGO_PORT)) {
            MongoDatabase database = mongoClient.getDatabase(MONGO_DB_NAME);
            MongoCollection<Document> collection = database.getCollection(MONGO_COLLECTION_NAME);

            List<String> flagsList = getFlagImageFilePaths();
            for (String flagFilePath : flagsList) {
                byte[] flagData = readImageFileAsBinary(flagFilePath);

                Document document = new Document();
                document.put("country", extractCountryNameFromFilePath(flagFilePath));
                document.put("flag", new Binary(flagData));

                collection.insertOne(document);
            }

            System.out.println("Flag images inserted into MongoDB successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves the file paths of flag images in the specified directory.
     *
     * @return a list of flag image file paths
     */
    private static List<String> getFlagImageFilePaths() {
        List<String> filePaths = new ArrayList<>();
        File directory = new File(FLAGS_DIRECTORY);

        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        filePaths.add(file.getAbsolutePath());
                    }
                }
            }
        }

        return filePaths;
    }

    /**
     * Reads the contents of an image file and returns them as a byte array.
     *
     * @param filePath the path of the image file
     * @return the image file contents as a byte array
     * @throws IOException if an I/O error occurs
     */
    private static byte[] readImageFileAsBinary(String filePath) throws IOException {
        File file = new File(filePath);
        byte[] fileData = new byte[(int) file.length()];

        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            int bytesRead = fileInputStream.read(fileData, 0, fileData.length);
            if (bytesRead == -1) {
                throw new IOException("End of file reached before reading any data");
            }
        }

        return fileData;
    }

    /**
     * Extracts the country name from a file path.
     *
     * @param filePath the file path
     * @return the extracted country name
     */
    private static String extractCountryNameFromFilePath(String filePath) {
        String fileName = new File(filePath).getName();
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) {
            return fileName.substring(0, dotIndex);
        } else {
            return fileName;
        }
    }
}
