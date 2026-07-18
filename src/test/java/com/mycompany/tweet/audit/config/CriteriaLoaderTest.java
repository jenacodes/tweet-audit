package com.mycompany.tweet.audit.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CriteriaLoaderTest {

    private static final String TEST_CRITERIA_DIR = "src/test/resources/";

    @Test
    public void testLoadValidCriteria () throws Exception{
        String filePath = TEST_CRITERIA_DIR + "criteria.valid.txt";

        String criteria = CriteriaLoader.loadCriteria(filePath);

        assertNotNull(criteria);
        assertTrue(criteria.contains("Flag"));
    }

    @Test
    public void testEmptyCriteriaThrowsException () {
        String filePath = TEST_CRITERIA_DIR + "criteria.invalid.txt";

        assertThrows(Exception.class, () -> CriteriaLoader.loadCriteria(filePath));
    }

    @Test
    public void testNonExistentFilePath () {

        String filePath = TEST_CRITERIA_DIR + "non-existent.txt";

        assertThrows(Exception.class, ()-> CriteriaLoader.loadCriteria(filePath));
    }
}
