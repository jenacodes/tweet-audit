package com.mycompany.tweet.audit;
import com.mycompany.tweet.audit.config.CriteriaLoader;
import com.mycompany.tweet.audit.evaluator.TweetEvaluator;
import com.mycompany.tweet.audit.model.*;
import java.util.List;

import com.mycompany.tweet.audit.output.ResultsWriter;
import com.mycompany.tweet.audit.parser.ArchiveParser;
import io.github.cdimascio.dotenv.Dotenv;
public class TweetAudit {

    public static void main(String[] args) {

        Dotenv dotenv = Dotenv.load();
        String myEnvValue = dotenv.get("GEMINI_API_KEY").trim();
        String myUsername = dotenv.get("X_USERNAME").trim();
        String criteria = CriteriaLoader.loadCriteria();

        try {
            //Parse the tweet archive JSON into a list of TweetWrapper objects
            List<TweetWrapper>tweets = ArchiveParser.parse("real_tweets.json");

            //Evaluate all tweets and get the results
            List<AuditResult> results = TweetEvaluator.evaluateAll(tweets, criteria, myEnvValue);

            ResultsWriter.writeToCsv(results, myUsername);


        } catch (Exception e) {
        System.err.println("An error occurred: " + e.getMessage());}
        }
}
