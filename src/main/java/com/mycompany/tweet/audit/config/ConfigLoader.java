package com.mycompany.tweet.audit.config;

import com.mycompany.tweet.audit.model.Config;
import io.github.cdimascio.dotenv.Dotenv;

public class ConfigLoader {
    public static Config load(String envFilePath) throws Exception {
            Dotenv dotenv = Dotenv.configure()
                    .directory(".")
                    .filename(envFilePath)
                    .load();

            String apiKey = dotenv.get("GEMINI_API_KEY");
            String myUsername = dotenv.get("X_USERNAME");
            String geminiModel = dotenv.get("GEMINI_MODEL");

            //create new stringBuilder object to build the error message if any of the required environment variables are missing
            StringBuilder errors = new StringBuilder();

            if (apiKey == null || apiKey.isBlank()){
                errors.append("Error: GEMINI_API_KEY is not set in the .env file. \n");
            }else {
                apiKey = apiKey.trim();
            }

            if (myUsername == null || myUsername.isBlank()){
                errors.append("Error: X_USERNAME is not set in the .env file. \n");
            }else {
                myUsername = myUsername.trim();
            }

            if (geminiModel == null || geminiModel.isBlank()) {
                geminiModel = "gemini-2.5-flash";
            }else {
                geminiModel = geminiModel.trim();
            }

            int batchSize = 10;
            String envBatchSize = dotenv.get("BATCH_SIZE");
            if (envBatchSize != null && !envBatchSize.isBlank()) {
                try {
                    batchSize = Integer.parseInt(envBatchSize.trim());
                } catch (NumberFormatException e) {
                    System.err.println("Warning: BATCH_SIZE in .env is invalid. Using default: " + batchSize);
                }
            }

            //Check if the error is not empty
            if (!errors.isEmpty()){
                throw new Exception(errors.toString());
            }else {
                return new Config(apiKey, myUsername, geminiModel, batchSize);
            }

    }

}
