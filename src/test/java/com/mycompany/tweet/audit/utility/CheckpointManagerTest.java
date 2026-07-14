package com.mycompany.tweet.audit.utility;

import com.mycompany.tweet.audit.model.Tweet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CheckpointManagerTest {

    private static final String TEST_FILE = "test_checkpoint.txt";

    @AfterEach
    public void cleanUp() throws IOException {
        // Delete the test file after each test
        Files.deleteIfExists(Path.of(TEST_FILE));
    }


    private List<Tweet> createDummyTweets(){
        return new ArrayList<>(List.of(
                new Tweet("101", "First tweet"),
                new Tweet("102", "second tweet"),
                new Tweet("103", "third tweet"),
                new Tweet("104", "fourth tweet")
        ));
    }

    @Test
    public void testSaveAndLoadCheckpointWorks() throws IOException {
        // GIVEN: A dummy ID
        String testId = "123456789";

        // WHEN: We save it to the file and immediately read it back
        CheckpointManager.saveCheckpoint(testId, TEST_FILE);
        String result = CheckpointManager.loadCheckpoint(TEST_FILE);

        // THEN: The ID we loaded should exactly match the one we saved
        assertEquals(testId, result);
    }

    @Test
    public void testLoadCheckpointWhenFileDoesNotExist() {
        // GIVEN: A file path we guarantee does not exist
        String fakePath = "test-output/definitely_does_not_exist.txt";

        // WHEN: We attempt to load from it
        String result = CheckpointManager.loadCheckpoint(fakePath);

        // THEN: It should gracefully return null
        assertNull(result);
    }

    @Test
    public void testNullCheckpointReturnsFullList(){
        List<Tweet> dummyList = createDummyTweets();
        String checkpointId = null;

        //when
        List<Tweet> resultList = CheckpointManager.applyCheckpoint(dummyList, checkpointId);

        //Then

        assertEquals(4, resultList.size());

    }

    @Test
    public void testValidCheckpointReturnsSubList(){

        List<Tweet> dummyList = createDummyTweets();
        String checkpointId = "102";

        //WHEN
        List<Tweet> resultList = CheckpointManager.applyCheckpoint(dummyList, checkpointId);

        //Then
        assertEquals(2, resultList.size());
    }

    @Test
    public void testInValidCheckpointThrowsException(){

        List<Tweet> dummyList = createDummyTweets();
        String invalidId = "999";

        //WHEN / Then
        assertThrows(RuntimeException.class, () -> CheckpointManager.applyCheckpoint(dummyList, invalidId));
    }


}

