package com.mycompany.tweet.audit.output;

import com.mycompany.tweet.audit.model.AuditResult;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
public class ResultsWriter {
    public static void writeToCsv(List<AuditResult>flaggedTweets, String myUsername) throws IOException {
        String fileName = "output/output.csv";
        Path path = Path.of(fileName);

        // Create the output directory if it doesn't exist
            Files.createDirectories(path.getParent());

        // Check if the file already exists before we open it
        boolean fileExists = Files.exists(path);

        try (PrintWriter writer =new PrintWriter(new FileWriter(fileName, true))) {
          // Only print the header if this is a brand-new file
            if (!fileExists) {
                writer.println("Tweet ID,Tweet,Reason,URL");
            }
            for(AuditResult result: flaggedTweets){

                String tweetUrl = "https://x.com/" + myUsername + "/status/" + result.id();
                    writer.printf("%s,\"%s\",\"%s\",%s%n", result.id(), result.tweet(), result.reason(), tweetUrl);
            }
        } catch (Exception e) {
            throw new IOException("Error writing to CSV file: " + e.getMessage());
        }
    }
}
