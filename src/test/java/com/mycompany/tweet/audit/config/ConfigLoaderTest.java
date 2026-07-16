package com.mycompany.tweet.audit.config;
import com.mycompany.tweet.audit.model.Config;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ConfigLoaderTest {

    private static final String TEST_ENV_DIR = "src/test/resources/";

    @Test
    public void testLoadValidConfig() throws Exception{
        //Given
        String envFile = TEST_ENV_DIR  + ".env.valid";

        //when
        Config config = ConfigLoader.load(envFile);

        //then
        assertNotNull(config);
        assertEquals("test-api-key-123", config.apiKey());
        assertEquals("testuser", config.myUsername());
        assertEquals("gemini-2.5-flash", config.geminiModel());
        assertEquals(10, config.batchSize());
    }

    @Test
    public void testMissingAPIKeyThrowsException () {
        String envFile = TEST_ENV_DIR  + ".env.missing-api-key";
        assertThrows(Exception.class, ()-> ConfigLoader.load(envFile));

    }

    @Test
    public void testMissingUsernameThrowsException (){
        String envFile = TEST_ENV_DIR + ".env.missing-username";
        assertThrows(Exception.class, ()-> ConfigLoader.load(envFile));
    }

    @Test
    public void testMissingBothThrowsException () {

        String envFile = TEST_ENV_DIR + ".env.missing-both";
       Exception exception = assertThrows(Exception.class, () -> ConfigLoader.load(envFile));

        String errorMessage = exception.getMessage();
        assertTrue(errorMessage.contains("GEMINI_API_KEY"));
        assertTrue(errorMessage.contains("X_USERNAME"));
    }

    @Test
    public void testMissingGeminiModelUsesDefault () throws Exception{
        String envFile = TEST_ENV_DIR  + ".env.missing-model";

        Config config = ConfigLoader.load(envFile);
        assertEquals("gemini-2.5-flash", config.geminiModel());
    }

   @Test
   public void testInvalidBatchSizeUsesDefault () throws Exception{
        String envFile = TEST_ENV_DIR + ".env.invalid-batching-size";

        Config config = ConfigLoader.load(envFile);
        assertEquals(10, config.batchSize());
   }
}
