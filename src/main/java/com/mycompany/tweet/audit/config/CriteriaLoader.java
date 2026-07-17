package com.mycompany.tweet.audit.config;
import java.nio.file.Files;
import java.nio.file.Path;

public class CriteriaLoader {

    public static String loadCriteria(String pathFile) throws Exception {
        Path filePath  = Path.of( pathFile);

        String content = Files.readString(filePath);


        if (content.isBlank()){
            throw new Exception("Criteria file exists but is empty: " + filePath);
        }

        return content;
    }
}
