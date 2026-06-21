package com.mycompany.tweet.audit.api;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class GeminiClient {

    public static HttpResponse<String> sendRequest(String jsonString, String apiKey){
        String geminiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey;
        HttpResponse<String> response;

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
