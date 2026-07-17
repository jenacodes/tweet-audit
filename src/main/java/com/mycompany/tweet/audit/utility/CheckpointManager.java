package com.mycompany.tweet.audit.utility;

import com.mycompany.tweet.audit.model.Tweet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class CheckpointManager {
    public static String loadCheckpoint(String fileName){

//        String fileName = "output/checkpoint.txt";
        Path path = Path.of(fileName);
        if (Files.exists(path)){
            //read the file and return the last processed tweet id
            try {
                return Files.readString(path).trim();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    public static void saveCheckpoint(String tweetId, String fileName) throws IOException {
//        String fileName = "output/checkpoint.txt";
        try {
            // Create the output directory if it doesn't exist
            Files.createDirectories(Path.of("output"));
            Files.writeString(Path.of(fileName), tweetId);
        } catch (IOException e) {
            throw new IOException("Error handling checkpoint File: " + e.getMessage());
        }
    }

    public static List<Tweet> applyCheckpoint(List<Tweet> tweets, String lastProcessedId){

        if (lastProcessedId != null) {
            int index = -1;
            for (int i = 0; i < tweets.size(); i++) {
                if (tweets.get(i).id().equals(lastProcessedId)) {
                    index = i;
                    break;
                }
            }
            if (index == -1) {
                throw new RuntimeException("Checkpoint ID not found in the archive. Please check the checkpoint file or the archive.");
            }

            if (index + 1 < tweets.size()) {
                tweets = tweets.subList(index + 1, tweets.size());
                System.out.println("Resuming from checkpoint. Skipping " + (index + 1) + " previously processed tweets.");
            } else {
                tweets.clear();
                System.out.println("All tweets have already been processed.");
            }
        }
        return tweets;
    }
}

