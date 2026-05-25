package com.mycompany.tweet.audit;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.tweet.audit.model.Content;
import com.mycompany.tweet.audit.model.GeminiRequest;
import com.mycompany.tweet.audit.model.Part;
import com.mycompany.tweet.audit.model.TweetWrapper;
import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import com.fasterxml.jackson.databind.DeserializationFeature;
import static com.mycompany.tweet.audit.TweetAudit.BatchSplitter.splitWithLoop;
import io.github.cdimascio.dotenv.Dotenv;
import java.util.ArrayList;

public class TweetAudit {

    //Utility method for batch splitting
    public static class BatchSplitter {

        public static <T> List<List<T>> splitWithLoop(List<T> list, int batchSize) {
            List<List<T>> batches = new ArrayList<>();
            if (list == null || list.isEmpty() || batchSize <= 0) {
                return batches; //Handle edge cases
            }
            int totalElements = list.size();

            for (int start = 0; start < totalElements; start += batchSize) {
                int end = Math.min(start + batchSize, list.size());
                List<T> batch = new ArrayList<>(list.subList(start, end));
                batches.add(batch);
            }
            return batches;
        }
    }

    public static void main(String[] args) {

        Dotenv dotenv = Dotenv.load();
        String myEnvValue = dotenv.get("GEMINI_API_KEY").trim();
//        System.out.println("DEBUG KEY: " + myEnvValue);

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            // ignore any JSON fields that aren't in our Records
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            //JSON to java Object
            List<TweetWrapper> tweet = objectMapper.readValue(new File("real_tweets.json"), new TypeReference<List<TweetWrapper>>() {
            });

            //Splitting to batches
            List<List<TweetWrapper>> batches = splitWithLoop(tweet, 50);

            for (List<TweetWrapper> batch : batches) {
                //create new stringbuilder object
                StringBuilder sb = new StringBuilder();
                for (TweetWrapper wrapper : batch) {
//                    System.out.println(wrapper.tweet().fullText());

                    String id = wrapper.tweet().id();
                    String fullText = wrapper.tweet().fullText();

                    sb.append("ID: ").append(id).append("\n");
                    sb.append("Full Text ").append(fullText).append("\n");

                }
//                System.out.println(sb.toString());
                    Part part = new Part(sb.toString());
                    Content content = new Content(List.of(part));
                    GeminiRequest geminiRequest = new GeminiRequest(List.of(content));


                String jsonString = objectMapper.writeValueAsString(geminiRequest);



                String geminiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + myEnvValue;

                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .POST(HttpRequest.BodyPublishers.ofString(jsonString))
                        .header("accept", "application/json")
                        .header("Content-Type", "application/json")
                        .uri(URI.create(geminiUrl))
                        .build();



  HttpResponse <String> response =   client.send(request, HttpResponse.BodyHandlers.ofString());

//
                System.out.println(response.body());

                // Parse JSON into a tree of JsonNode
                JsonNode rootNode = objectMapper.readTree(response.body());

                // Access simple properties
                String name = rootNode.path("candidates")
                                        .get(0)
                                        .path("content")
                                        .path("parts")
                                         .get(0)
                                        .path("text").asText();

                System.out.println(name);

                break;


            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
