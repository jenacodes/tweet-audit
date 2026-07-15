package com.mycompany.tweet.audit.api;
import com.mycompany.tweet.audit.model.AuditResult;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ResilientGeminiClientTest {

    String fakeResponseBody = """
        {
          "candidates": [
            {
              "content": {
                "parts": [
                  {
                    "text": "[{\\"id\\":\\"1001\\",\\"reason\\":\\"offensive\\"}, {\\"id\\":\\"1002\\",\\"reason\\":\\"spam\\"}]"
                  }
                ]
              }
            }
          ]
        }
        """;

    String dummyInputJson = "[{\"id\":\"10013\", \"text\":\"test\"}]";
    String apiKey = "fake-api-key";
    String model = "gemini-1.5-flash";

    @Test
    public void testSuccessfulResponseIsParsedCorrectly () throws Exception {


        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(fakeResponseBody);

        // Mock GeminiClient.sendRequest to return our fake response
        try (MockedStatic<GeminiClient> mockedStaticClient = mockStatic(GeminiClient.class)) {


            mockedStaticClient.when(()-> GeminiClient.sendRequest(anyString(), anyString(), anyString())).thenReturn(mockResponse);
            List<AuditResult> results = ResilientGeminiClient.executeWithRetry(dummyInputJson,apiKey,model);

            assertNotNull(results);
            assertEquals(2, results.size());
            assertEquals("1002", results.get(1).id());
            assertEquals("offensive",results.getFirst().reason());
        }


    }
    @Test
    public void testRetryOn429RateLimit () throws Exception{

        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(429, 200);
        when(mockResponse.body()).thenReturn(fakeResponseBody);


        try (MockedStatic<GeminiClient> mockedStaticClient = mockStatic(GeminiClient.class)) {

            mockedStaticClient.when(()-> GeminiClient.sendRequest(anyString(), anyString(), anyString())).thenReturn(mockResponse);

            List<AuditResult> results = ResilientGeminiClient.executeWithRetry(dummyInputJson,apiKey,model);

            assertNotNull(results);
            assertEquals(2, results.size());
            assertEquals("1002", results.get(1).id());
            assertEquals("offensive",results.getFirst().reason());

            mockedStaticClient.verify(
                    () -> GeminiClient.sendRequest(anyString(), anyString(), anyString()), times(2)
            );

        }
    }

    @Test
    public void testThrowsExceptionAfterMaxRetries (){

        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(500);
        when(mockResponse.body()).thenReturn(fakeResponseBody);


        try (MockedStatic<GeminiClient> mockedStaticClient = mockStatic(GeminiClient.class)) {

            mockedStaticClient.when(()-> GeminiClient.sendRequest(anyString(), anyString(), anyString())).thenReturn(mockResponse);

            assertThrows(Exception.class, ()-> ResilientGeminiClient.executeWithRetry(dummyInputJson, apiKey, model));
            mockedStaticClient.verify(
                    () -> GeminiClient.sendRequest(anyString(), anyString(), anyString()), times(5)
            );

        }


    }

    @Test
    public void testThrowsImmediatelyOn400BadRequest (){

        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(400);
        when(mockResponse.body()).thenReturn("{\"error\": \"bad request\"}");

        try (MockedStatic<GeminiClient> mockedStaticClient = mockStatic(GeminiClient.class)) {

            mockedStaticClient.when(()-> GeminiClient.sendRequest(anyString(), anyString(), anyString())).thenReturn(mockResponse);

            assertThrows(Exception.class, ()-> ResilientGeminiClient.executeWithRetry(dummyInputJson, apiKey, model));

            mockedStaticClient.verify(
                    () -> GeminiClient.sendRequest(anyString(), anyString(), anyString()), times(1)
            );

        }
    }
}
