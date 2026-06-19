package com.mycompany.tweet.audit.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CriteriaLoader {

    public static String loadCriteria(){
        Path filePath  = Path.of("criteria.txt");

        String content = "";
        try {
            content = Files.readString(filePath);
        } catch (IOException e) {
//            throw new RuntimeException(e);
            System.err.println("Error reading file: " + e.getMessage());
        }

        return content;
    }
}
