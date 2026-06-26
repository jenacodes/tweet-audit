package com.mycompany.tweet.audit.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.tweet.audit.model.AuditResult;

import java.net.http.HttpResponse;
import java.util.List;

public class ResilientGeminiClient {

    public static List<AuditResult> executeWithRetry(String jsonString, String myEnvValue, String geminiModel) throws InterruptedException {
        ObjectMapper objectMapper = new ObjectMapper();
        boolean batchSuccess = false;
        int attempt = 0;
        int maxAttempts = 5;

        while (!batchSuccess && attempt < maxAttempts) {
            attempt++;

            //Send the request to Gemini and handle the response
            try {
                HttpResponse<String> response = GeminiClient.sendRequest(jsonString, myEnvValue, geminiModel);
                if (response != null) {
                    JsonNode rootNode = objectMapper.readTree(response.body());

                    if (rootNode.has("error")) {
                        String errorMessage = rootNode.path("error").path("message").asText();
                        System.err.println("Attempt " + attempt + "failed: " + errorMessage);

                        if (attempt == maxAttempts) {
                            System.err.println("Max attempts reached. Skipping batch.");
                            return List.of();
                        }

                        long baseSleep = 8000L * (long) Math.pow(2, attempt);

                        System.out.println("Sleeping for " + (baseSleep / 1000) + "seconds...");
                        Thread.sleep(baseSleep);
                        continue;
                    }

                    String rawAiText = rootNode.path("candidates")
                            .get(0)
                            .path("content")
                            .path("parts")
                            .get(0)
                            .path("text").asText();

                    String cleanJson = rawAiText.replace("```json", "").replace("```", "");
                    return objectMapper.readValue(cleanJson, new TypeReference<>() {
                    });
                }
            } catch (Exception e) {
                System.err.println("Network/Parsing error on attempt " + attempt + ": " + e.getMessage());
                Thread.sleep(10000); // Base wait for hard network drops
            }
        }
        return List.of();
    }
}
