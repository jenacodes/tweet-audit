package com.mycompany.tweet.audit.utility;

import com.mycompany.tweet.audit.model.Tweet;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BatchSplitterTest {

    private List<Tweet> generateTweets(int count){
        List<Tweet> list = new ArrayList<>();
        for (int i = 0; i<count; i++){
            list.add(new Tweet(String.valueOf(i), "Dummy text "+ i));
        }
        return list;
    }

    @Test
    public void testPerfectFitBatching (){

       List<List<Tweet>> resultList = BatchSplitter.splitWithLoop(generateTweets(20), 10);

        assertEquals(2, resultList.size()); // Expect exactly 2 batches
        assertEquals(10, resultList.getFirst().size());
        assertEquals(10, resultList.get(1).size());
    }

    @Test
    public void testRemainderBatching(){
        List<List<Tweet>> resultList = BatchSplitter.splitWithLoop(generateTweets(25), 10);

        assertEquals(3, resultList.size());
        assertEquals(10, resultList.getFirst().size());
        assertEquals(10, resultList.get(1).size());
        assertEquals(5, resultList.getLast().size());
    }

    @Test
    public void testUnderSizeBatching(){
        List<List<Tweet>> resultList = BatchSplitter.splitWithLoop(generateTweets(5), 10);

        assertEquals(1, resultList.size());
        assertEquals(5, resultList.getFirst().size());
    }
}
