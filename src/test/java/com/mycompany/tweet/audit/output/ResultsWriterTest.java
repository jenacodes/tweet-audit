package com.mycompany.tweet.audit.output;

import com.mycompany.tweet.audit.model.AuditResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ResultsWriterTest {

    private static final String TEST_CSV_FILE = "test-output/results.csv";

    @AfterEach
    public void cleanUp () throws Exception{
        //Delete the test file after each test

        Files.deleteIfExists(Path.of(TEST_CSV_FILE));
    }

    private List<String> readCsvFile () throws Exception{
        //read CSV file and return lines
        return Files.readAllLines(Path.of(TEST_CSV_FILE));
    }


    @Test
    public void testWriteToNewFileCreatesHeader () throws Exception{
        //GIVEN
        AuditResult result = new AuditResult("123", "offesnive tweet", "contains unprofessional words");
        List<AuditResult> results = List.of(result);
        String username = "testuser";

        //WHEN
        ResultsWriter.writeToCsv(results, username, TEST_CSV_FILE);

        //THEN
        List<String> lines = readCsvFile();
        assertTrue(lines.getFirst().contains("Tweet ID"));
        assertTrue(lines.getFirst().contains("Tweet"));
        assertTrue(lines.getFirst().contains("Reason"));
        assertTrue(lines.getFirst().contains("URL"));
    }

    @Test
    public void testWriteDataCorrectly () throws Exception {
        AuditResult result = new AuditResult("456", "crypto tweet", "contains unprofessional words");
        List<AuditResult> results = List.of(result);
        String username = "testuser";

        ResultsWriter.writeToCsv(results, username, TEST_CSV_FILE);
        List<String> lines = readCsvFile();

        String dataLine = lines.get(1);

        assertTrue(dataLine.contains("456"));
        assertTrue(dataLine.contains("crypto tweet"));
        assertTrue(dataLine.contains("contains unprofessional words"));
        assertTrue(dataLine.contains("https://x.com/testuser/status/456"));
    }

    @Test
    public void testAppendingDoesNotDuplicateHeader () throws Exception{
        AuditResult result1 = new AuditResult("000", "simp tweet", "Nigga you thirsty");
        String username = "testuser";

        ResultsWriter.writeToCsv(List.of(result1), username, TEST_CSV_FILE);

        AuditResult result2 = new AuditResult("222", "racism tweet", "Why you racist nigga");
        ResultsWriter.writeToCsv(List.of(result2), "user", TEST_CSV_FILE);

        List<String> lines = readCsvFile();

        //Should have: header + 2 data lines = 3 lines in total
        assertEquals(3, lines.size());

        //Both lines should be present
        assertTrue(lines.stream().anyMatch(line -> line.contains("000")));
        assertTrue(lines.stream().anyMatch(line -> line.contains("222")));


    }

    @Test
    public void testWriteEmptyListCreatesHeaderOnly () throws Exception{
        ResultsWriter.writeToCsv(List.of(), "user", TEST_CSV_FILE);
        List<String> lines = readCsvFile();


        //should only have header, no data

        assertEquals(1, lines.size());
        assertTrue(lines.getFirst().contains("Tweet ID"));
    }

}
