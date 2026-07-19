package com.mycompany.tweet.audit.evaluator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.tweet.audit.api.ResilientGeminiClient;
import com.mycompany.tweet.audit.model.*;
import com.mycompany.tweet.audit.output.ResultsWriter;
import com.mycompany.tweet.audit.utility.BatchSplitter;
import com.mycompany.tweet.audit.utility.CheckpointManager;
import java.util.List;

public class TweetEvaluator {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void evaluateAll(List<Tweet> tweets, String criteria, String geminiModel, String apiKey, String myUsername, int batchSize, String checkpointPath, String outputPath) throws Exception {

        // Load the checkpoint and slice the list before we do any batching
        String lastProcessedId = CheckpointManager.loadCheckpoint(checkpointPath);
        tweets = CheckpointManager.applyCheckpoint(tweets, lastProcessedId);

        if (tweets.isEmpty()) {
            return; // Exit early if the checkpoint was the very last tweet in the file
        }

        //Splitting to batches
        List<List<Tweet>> batches = BatchSplitter.splitWithLoop(tweets, batchSize);

        //Loop through each batch, send to Gemini and handle response
        for (List<Tweet> batch : batches) {
            //create new stringBuilder object to construct prompt text
            StringBuilder sb = new StringBuilder();
            sb.append("You are evaluating tweets for professional appropriateness.\n");
            sb.append("Flag tweets that:\n");
            sb.append(criteria);
            sb.append("\nRespond ONLY with a valid JSON array containing 'id', 'tweet' and reason.\n");
            sb.append("\n The Reason should not be more than 15 words.\n");

            //Loop through each tweet in the batch and append its ID and full text to the prompt
            for (Tweet tweet : batch) {
                String id = tweet.id();
                String fullText = tweet.fullText();
                sb.append("ID: ").append(id).append("\n");
                sb.append("Full Text ").append(fullText).append("\n");
            }

            //create GeminiRequest object with the constructed prompt text
            Part part = new Part(sb.toString());
            Content content = new Content(List.of(part));
            GeminiRequest geminiRequest = new GeminiRequest(List.of(content));

            //Serialize to JSON
            String jsonString = objectMapper.writeValueAsString(geminiRequest);

            //Send the request to Gemini and get the results
            List<AuditResult> results;

            try{
                //Send the request to Gemini and get the results
                results = ResilientGeminiClient.executeWithRetry(jsonString, apiKey, geminiModel);
            } catch (Exception e) {
                System.err.println("FATAL: Batch evaluation failed. Aborting.");
                System.err.println("Error: " + e.getMessage());
                System.err.println("Checkpoint: Previous batches have been saved to output.csv. Please check the file for progress");
                System.err.println("Next run will process from this batch onward.");
                return; //Exit the method - stop processing
            }

            if (results!= null && !results.isEmpty()){
                for (AuditResult result : results ){
                    System.out.println(result.reason());
                }

            }
            // Auto-save the results to CSV after each batch is processed. This ensures that even if the program is interrupted, progress is saved.
            try {
                System.out.println("Auto-saving progress to CSV...");
                ResultsWriter.writeToCsv(results, myUsername, outputPath);

                Tweet lastTweetInBatch = batch.getLast();


                CheckpointManager.saveCheckpoint(lastTweetInBatch.id(), checkpointPath);
                System.out.println("Checkpoint Saved: " + lastTweetInBatch.id());

            } catch (Exception e) {
                System.err.println("Failed to auto-save CSV: " + e.getMessage());
                return; //Stop if we can't save
            }

            // Give Google's servers time to breathe before the next batch(Rate-limiting)
            try {
                System.out.println("Batch complete. Sleeping for 15 seconds to avoid API rate limits...");
                Thread.sleep(15000);
            } catch (InterruptedException e) {
                System.err.println("Sleep interrupted: " + e.getMessage());
                return;
            }
        }
    }
}