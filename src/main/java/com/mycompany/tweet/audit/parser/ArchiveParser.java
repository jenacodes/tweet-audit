package com.mycompany.tweet.audit.parser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.tweet.audit.model.Tweet;
import com.mycompany.tweet.audit.model.TweetWrapper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ArchiveParser {

    public static List<Tweet> parse(String filePath) throws Exception {

        //Read the entire content into a string
        String tweetsJson = Files.readString(Path.of(filePath));

        // Find the index of the first '[' character
        int index = tweetsJson.indexOf("[");

        if (index == -1){
            throw new Exception("Invalid JSON format: No array found");
        }

        String tweetsArrayJson = tweetsJson.substring(index);

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            // ignore any JSON fields that aren't in our Records
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            //JSON to tweetWrapper Objects
         List<TweetWrapper> tweets = objectMapper.readValue(tweetsArrayJson, new TypeReference<>() {});
         //New Array
         ArrayList<Tweet> cleanTweets = new ArrayList<>();

         for (TweetWrapper Wrapper : tweets){
             cleanTweets.add(Wrapper.tweet());
         }

         return cleanTweets;

        } catch (Exception e) {
            throw new Exception("Failed to parse: " + e.getMessage());
        }

    }
}
