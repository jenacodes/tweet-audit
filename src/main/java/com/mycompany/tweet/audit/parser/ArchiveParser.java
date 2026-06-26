package com.mycompany.tweet.audit.parser;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.tweet.audit.model.TweetWrapper;

import java.io.File;
import java.util.List;

public class ArchiveParser {

    public static List<TweetWrapper> parse(String filePath){
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            // ignore any JSON fields that aren't in our Records
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            //JSON to java Object
            return objectMapper.readValue(new File(filePath), new TypeReference<>() {
            });

        } catch (Exception e) {
            System.err.println("Failed to parse archive: " + e.getMessage());
            return null;
        }

    }
}
