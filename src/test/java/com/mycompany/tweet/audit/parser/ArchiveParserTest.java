package com.mycompany.tweet.audit.parser;
import com.mycompany.tweet.audit.model.Tweet;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class ArchiveParserTest {

@Test
    public void testParserProperlyMapsJsonToTweets() throws Exception {
    List<Tweet> tweets = ArchiveParser.parse("src/test/resources/test.json");

    assertEquals(4, tweets.size());
    assertEquals("10001", tweets.getFirst().id());
    assertEquals("Learning Java backend is actually pretty fun.", tweets.getLast().fullText());
}

    @Test
    public void testParseEmptyJsonArray() throws Exception{
    List<Tweet> tweets = ArchiveParser.parse("src/test/resources/empty.json");
    assertTrue(tweets.isEmpty());
    }

    @Test
    public void testParseFileNotFound(){
            assertThrows(Exception.class, () -> ArchiveParser.parse("src/test/resources/nonexistent.json"));
    }

    @Test
    public void testParserInvalidJson () {
        assertThrows(Exception.class, () -> ArchiveParser.parse("src/test/resources/invalid.json"));
}
}
