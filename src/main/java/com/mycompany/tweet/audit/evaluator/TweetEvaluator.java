package com.mycompany.tweet.audit.evaluator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.tweet.audit.api.ResilientGeminiClient;
import com.mycompany.tweet.audit.model.*;
import com.mycompany.tweet.audit.output.ResultsWriter;
import com.mycompany.tweet.audit.utility.BatchSplitter;
//import java.util.ArrayList;
import java.util.List;

public class TweetEvaluator {
    public static void evaluateAll(List<TweetWrapper> tweets, String criteria, String geminiModel, String myEnvValue, String myUsername, int batchSize) throws JsonProcessingException, InterruptedException {
//    List<AuditResult> masterList = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();

        //Splitting to batches
        List<List<TweetWrapper>> batches = BatchSplitter.splitWithLoop(tweets, batchSize);

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

            //Send the request to Gemini and get the results with retry logic. It handles the API call, retries, and JSON parsing.
           List<AuditResult> results = ResilientGeminiClient.executeWithRetry(jsonString, myEnvValue, geminiModel);

            for (AuditResult result : results ){
                System.out.println(result.reason());
            }

            //Add new batch to masterList
//            masterList.addAll(results);

            // This rewrites the CSV file with the updated masterList after every successful batch
            try {
                System.out.println("Auto-saving progress to CSV...");
                ResultsWriter.writeToCsv(results, myUsername);
            } catch (Exception e) {
                System.err.println("Failed to auto-save CSV: " + e.getMessage());
            }

            // Give Google's servers time to breathe before the next batch(Rate-limiting)
            try {
                System.out.println("Batch complete. Sleeping for 15 seconds to avoid API rate limits...");
                Thread.sleep(15000);
            } catch (InterruptedException e) {
                System.err.println("Sleep interrupted: " + e.getMessage());
            }
            break;
        }
    }
}