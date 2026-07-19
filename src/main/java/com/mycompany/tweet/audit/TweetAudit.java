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

        Config config;
        String criteria;
        List<Tweet> tweets;

        //Load config
        try {
            config = ConfigLoader.load(".env");
        } catch (Exception e) {
            System.err.println("Configuration error: " + e.getMessage());
            return;
        }

        //Load criteria
        try {
            criteria = CriteriaLoader.loadCriteria("resources/criteria.txt");
        } catch (Exception e) {
            System.err.println("Criteria loading Error: " + e.getMessage());
            return;
        }

        //Parse archive
        try {
            tweets = ArchiveParser.parse("data/tweets.json");
        } catch (Exception e) {
            System.err.println("Archive parsing error: "+ e.getMessage());
            return;
        }

        if (tweets.isEmpty()){
            System.err.println("No tweets found in the archive. Exiting.");
            return;
        }

        System.out.println("Total tweets to evaluate: " + tweets.size());

        //Evaluate
        try {
            TweetEvaluator.evaluateAll(tweets, criteria, config.geminiModel(), config.apiKey(), config.myUsername(), config.batchSize(), "output/checkpoint.txt", "output/output.csv");
        } catch (Exception e) {
            System.err.println("Evaluation error: " + e.getMessage());
        }
        System.out.println("Audit complete! All tweets saved successfully.");
    }
}
