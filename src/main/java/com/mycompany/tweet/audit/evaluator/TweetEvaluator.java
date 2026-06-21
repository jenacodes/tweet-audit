package com.mycompany.tweet.audit.evaluator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.tweet.audit.api.GeminiClient;
import com.mycompany.tweet.audit.config.CriteriaLoader;
import com.mycompany.tweet.audit.model.*;
import com.mycompany.tweet.audit.output.ResultsWriter;
import com.mycompany.tweet.audit.utility.BatchSplitter;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class TweetEvaluator {
    public static List<AuditResult>evaluateAll(List<TweetWrapper> tweets, String criteria, String myEnvValue) throws JsonProcessingException {
    List<AuditResult> masterList = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();

        //Splitting to batches
        List<List<TweetWrapper>> batches = BatchSplitter.splitWithLoop(tweets, 50);

        //Loop through each batch, send to Gemini and handle response
        for (List<TweetWrapper> batch : batches) {

            //create new stringBuilder object to construct prompt text
            StringBuilder sb = new StringBuilder();
            sb.append(criteria);

            for (TweetWrapper wrapper : batch) {

                String id = wrapper.tweet().id();
                String fullText = wrapper.tweet().fullText();

                sb.append("ID: ").append(id).append("\n");
                sb.append("Full Text ").append(fullText).append("\n");

            }

            //create GeminiRequest object with the constructed prompt text
            Part part = new Part(sb.toString());
            Content content = new Content(List.of(part));
            GeminiRequest geminiRequest = new GeminiRequest(List.of(content));

            //Serialize to JSON
            String jsonString = objectMapper.writeValueAsString(geminiRequest);


            //Send the HTTp request
            HttpResponse<String> response = GeminiClient.sendRequest(jsonString, myEnvValue);

            // Parse JSON into a tree of JsonNode
            assert response != null;
            JsonNode rootNode = objectMapper.readTree(response.body());

            //Error logging
            if (rootNode.has("error")){
                String errorMessage = rootNode.path("error").path("message").asText();
                System.err.println("Google API failed: "+ errorMessage);
                continue;
            }
            // Extract AI response
            String rawAiText = rootNode.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text").asText();

            String cleanJson = rawAiText.replace("```json", "").replace("```", "");


            //Deserialize cleaned JSON into List of AuditResult objects
            List<AuditResult> results = objectMapper.readValue(cleanJson, new TypeReference<>() {});

            for (AuditResult result : results ){
                System.out.println(result.reason());
            }

            masterList.addAll(results);


            // Give Google's servers time to breathe before the next batch(Rate-limiting)
            try {
                System.out.println("Batch complete. Sleeping for 5 seconds to avoid API rate limits...");
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                System.err.println("Sleep interrupted: " + e.getMessage());
            }

            break;



        }


        return masterList;
    }
}