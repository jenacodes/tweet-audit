package com.mycompany.tweet.audit.evaluator;

import com.mycompany.tweet.audit.api.ResilientGeminiClient;
import com.mycompany.tweet.audit.model.AuditResult;
import com.mycompany.tweet.audit.model.Tweet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;

public class TweetEvaluatorTest {

    //List of tweets
    private List<Tweet> createTestTweets() {
        List<Tweet> tweets = new ArrayList<>();
        tweets.add(new Tweet("101", "First tweet"));
        tweets.add(new Tweet("102", "Second tweet"));
        tweets.add(new Tweet("103", "Third tweet"));
        tweets.add(new Tweet("104", "Fourth tweet"));
        tweets.add(new Tweet("105", "Fifth tweet"));
        return tweets;
    }

    //Results
    private List<AuditResult> createFakeResults(List<Tweet> batch) {
        // Return one result per tweet in the batch
        List<AuditResult> results = new ArrayList<>();
        for (Tweet tweet : batch) {
            results.add(new AuditResult(tweet.id(), tweet.fullText(), "flagged for review"));
        }
        return results;
    }

    @TempDir
    Path tempDir;

    @Test

    // 1. Setup the Temp paths
    public void testEvaluateAllProcessesBatchAndSaves() throws Exception{
        String checkpointPath = tempDir.resolve("checkpoint.txt").toString();
        String outputPath = tempDir.resolve("output.csv").toString();

        //GIVEN
        List<Tweet> tweets = createTestTweets();
        String criteria = "Unprofessional language\n hate speech";
        String username = "Jena";
        int batchSize = 3;

        //Mock ResilientGeminiClient to return fake results

        try (MockedStatic<ResilientGeminiClient> mockedGemini = mockStatic(ResilientGeminiClient.class)) {

            // Tell the mock to return our fakeResponse whenever executeWithRetry is called
            mockedGemini.when(()-> ResilientGeminiClient.executeWithRetry(anyString(),anyString(),anyString())).thenAnswer(_ -> createFakeResults(tweets));

            //WHEN
            TweetEvaluator.evaluateAll(tweets, criteria, "gemini-2.5-flash","fake-key", username, batchSize, checkpointPath, outputPath);
        }



        //THEN
        assertTrue(Files.exists(Path.of(outputPath)), "Output CSV should have been created");
        assertTrue(Files.exists(Path.of(checkpointPath)), "Checkpoint.txt should have been created");

        List<String> csvLines = Files.readAllLines(Path.of(outputPath));
        assertTrue(csvLines.size() > 1);

        String checkpointTxt = Files.readString(Path.of(checkpointPath));
        assertTrue(checkpointTxt.contains("105"));

    }
}
