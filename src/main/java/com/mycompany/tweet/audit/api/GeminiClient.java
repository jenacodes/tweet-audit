package com.mycompany.tweet.audit.api;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class GeminiClient {

    public static HttpResponse<String> sendRequest(String jsonString, String apiKey, String geminiModel){
        String geminiUrl = "https://generativelanguage.googleapis.com/v1beta/models/" + geminiModel + ":generateContent?key=" + apiKey;

        //This is a simple implementation of sending a request to the Gemini API.
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString(jsonString))
                    .header("accept", "application/json")
                    .header("Content-Type", "application/json")
                    .uri(URI.create(geminiUrl))
                    .build();
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e){
            System.err.println("Error sending request to Gemini API: " + e.getMessage());
            return null;
        }
    }
}
