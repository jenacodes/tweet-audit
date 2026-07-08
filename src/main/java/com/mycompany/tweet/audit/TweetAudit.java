package com.mycompany.tweet.audit;
import com.mycompany.tweet.audit.config.ConfigLoader;
import com.mycompany.tweet.audit.config.CriteriaLoader;
import com.mycompany.tweet.audit.evaluator.TweetEvaluator;
import com.mycompany.tweet.audit.model.*;
import java.util.List;
import com.mycompany.tweet.audit.parser.ArchiveParser;
public class TweetAudit {

    public static void main(String[] args) {

        System.out.println("Tweet audit starting...");


        try {
            //Load all variables from the .env file
            Config config = ConfigLoader.load();
            String apiKey = config.apiKey();
            String myUsername = config.myUsername();
            String geminiModel = config.geminiModel();
            int batchSize = config.batchSize();


            String criteria = CriteriaLoader.loadCriteria();
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
