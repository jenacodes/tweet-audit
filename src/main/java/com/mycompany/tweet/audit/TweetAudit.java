package com.mycompany.tweet.audit;
import com.mycompany.tweet.audit.config.CriteriaLoader;
import com.mycompany.tweet.audit.evaluator.TweetEvaluator;
import com.mycompany.tweet.audit.model.*;
import java.util.List;
import com.mycompany.tweet.audit.parser.ArchiveParser;
import io.github.cdimascio.dotenv.Dotenv;
public class TweetAudit {

    public static void main(String[] args) {

        System.out.println("Tweet audit starting...");

        Dotenv dotenv = Dotenv.load();
        String apiKey = dotenv.get("GEMINI_API_KEY").trim();
        String myUsername = dotenv.get("X_USERNAME").trim();
        String criteria = CriteriaLoader.loadCriteria();
        String geminiModel = dotenv.get("GEMINI_MODEL");

        // If GEMINI_MODEL is not set in the .env file, default to "gemini-2.5-flash"
        if (geminiModel == null || geminiModel.isBlank()) {
            geminiModel = "gemini-2.5-flash";
        }

        //if BATCH_SIZE is set in the .env file, use that value. Otherwise, default to 10.
        int batchSize = 10;
        String envBatchSize = dotenv.get("BATCH_SIZE");
        if (envBatchSize != null && !envBatchSize.isBlank()) {
            try {
                batchSize = Integer.parseInt(envBatchSize.trim());
            } catch (NumberFormatException e) {
                System.err.println("Warning: BATCH_SIZE in .env is invalid. Using default: " + batchSize);
            }
        }

        try {
            //Parse the tweet archive JSON into a list of Tweet objects
            List<Tweet>tweets = ArchiveParser.parse("real_tweets.json");

            if (tweets.isEmpty()) {
                System.err.println("No tweets found in the archive. Exiting.");
                return;
            }

            System.out.println("Total tweets to evaluate: " + tweets.size());

            //Evaluate all tweets and get the results and save the results to the CSV file after each batch is processed. This ensures that even if the program is interrupted, progress is saved.
            TweetEvaluator.evaluateAll(tweets, criteria, geminiModel, apiKey, myUsername, batchSize);

            System.out.println("Audit complete! All tweets saved successfully.");

        } catch (Exception e) {
        System.err.println("An error occurred: " + e.getMessage());
        }
    }
}
