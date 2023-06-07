package org.flagmatch;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * The FlagRetriever class downloads flag images from the Rest Countries API and saves them to the specified directory.
 */
public class FlagRetriever {

    /**
     * The directory where flag images will be saved.
     */
    private static final String FLAGS_DIRECTORY = "flags";

    /**
     * The entry point of the program.
     *
     * @param args The command line arguments.
     */
    public static void main(String[] args) {
        HttpClient httpClient = HttpClients.createDefault();
        HttpGet request = new HttpGet("https://restcountries.com/v3.1/all?fields=flags");

        try {
            HttpResponse response = httpClient.execute(request);
            HttpEntity entity = response.getEntity();
            String jsonString = EntityUtils.toString(entity);

            JSONArray flagsArray = new JSONArray(jsonString);

            for (int i = 0; i < flagsArray.length(); i++) {
                JSONObject flagObject = flagsArray.getJSONObject(i);
                JSONObject flags = flagObject.getJSONObject("flags");
                String flagUrl = flags.getString("png");
                downloadFlagImage(flagUrl, FLAGS_DIRECTORY + File.separator + extractCountryNameFromUrl(flagUrl) + ".png");
                System.out.println("Flag URL: " + flagUrl);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Downloads a flag image from the specified URL and saves it to the given file path.
     *
     * @param imageUrl The URL of the flag image.
     * @param filePath The file path where the flag image will be saved.
     */
    private static void downloadFlagImage(String imageUrl, String filePath) {
        try (InputStream inputStream = new URL(imageUrl).openStream();
             OutputStream outputStream = Files.newOutputStream(Paths.get(filePath))) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Extracts the country name from the flag URL.
     *
     * @param flagUrl The URL of the flag image.
     * @return The country name extracted from the flag URL.
     */
    private static String extractCountryNameFromUrl(String flagUrl) {
        int lastSlashIndex = flagUrl.lastIndexOf("/");
        int lastDotIndex = flagUrl.lastIndexOf(".");
        if (lastSlashIndex != -1 && lastDotIndex != -1 && lastDotIndex > lastSlashIndex) {
            return flagUrl.substring(lastSlashIndex + 1, lastDotIndex);
        } else {
            return flagUrl;
        }
    }
}
