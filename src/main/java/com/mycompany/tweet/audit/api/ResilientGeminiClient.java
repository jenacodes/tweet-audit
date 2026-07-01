package com.mycompany.tweet.audit.api;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.tweet.audit.model.AuditResult;
import java.net.http.HttpResponse;
import java.util.List;

public class ResilientGeminiClient {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static List<AuditResult> executeWithRetry(String jsonString, String apiKey, String geminiModel) throws Exception {
        int attempt = 0;
        int maxAttempts = 5;

        while (attempt < maxAttempts) {
            attempt++;
            //Send the request to Gemini and handle the response
            try {
                HttpResponse<String> response = GeminiClient.sendRequest(jsonString, apiKey, geminiModel);

                if (response == null) {
                    throw new Exception("No response from Gemini API.");
                }

                int statusCode = response.statusCode();

                //Check the Http status code
                if (statusCode == 429) {
                    // Rate limited - retry with backoff
                    System.out.println("Rate limited (429). Retrying...");
                    long sleepTime = calculateBackOff(attempt);
                    Thread.sleep(sleepTime);
                    continue;
                }

                if (statusCode >= 400 && statusCode < 500) {
                    throw new Exception("Gemini API error " + statusCode + ": " + response.body());
                }

                if (statusCode >= 500) {
                    //Server Error - might be transient, retry with backoff
                    System.out.println("Server error (" + statusCode + "). Retrying...");
                    long sleepTime = calculateBackOff(attempt);
                    Thread.sleep(sleepTime);
                    continue;
                }

//                Success
                if (statusCode == 200) {
                    //Parse the response
                    JsonNode rootNode = objectMapper.readTree(response.body());

                    //Check if Gemini returned an error inside the success response
                    if (rootNode.has("error")) {
                        String errorMessage = rootNode.path("error").path("message").asText();
                        throw new Exception("Gemini API returned an error: " + errorMessage);
                    }
//                    Extract the AI's text response
                    String rawAiText = extractAiText(rootNode);
                    String cleanJson = rawAiText.replace("```json", "").replace("```", "").trim();

                     //Parse into AuditResult objects
                    return objectMapper.readValue(cleanJson, new TypeReference<>() {}); //Success - exit the retry loop
                }

            } catch (InterruptedException e) {
                //Thread.sleep was interrupted - don't retry, fail
                throw new Exception("Interrupted while waiting for retry: " + e.getMessage());
            } catch (Exception e) {

                if (e.getMessage() != null && e.getMessage().contains("Gemini API returned an error: ")){
                    //Client error - don't retry
                    throw new Exception("Fatal Gemini API error: " + e.getMessage());
                }
                //Network error or JSON parsing error - retry with backoff
                System.err.println("Error on attempt " + attempt + ": " + e.getMessage());


                if (attempt >= maxAttempts) {
                    //Out of retries
                    throw new Exception("Max retry attempts reached. Last error: " + e.getMessage());
                }
                long sleepTime = calculateBackOff(attempt);
                Thread.sleep(sleepTime);
            }
        }

            throw new Exception("Failed to get a successful response from Gemini API after " + maxAttempts + " attempts.");
    }


    //Helper functions

    private static long calculateBackOff(int attempt){
        // 1 second × 2^(attempt-1) = 1s, 2s, 4s, 8s, 16s
        long baseDelay = 1000;
        long exponentialBackoff = (long) Math.pow(2, attempt - 1);
        long totalDelay = baseDelay * exponentialBackoff;

        System.out.println("Sleeping for " + totalDelay + "milliseconds( " + (totalDelay / 1000) + " seconds)");

        return totalDelay;
    }

    private static String extractAiText (JsonNode rootNode){
        return rootNode.path("candidates")
                .get(0)
                .path("content")
                .path("parts")
                .get(0)
                .path("text").asText();
    }


    }



