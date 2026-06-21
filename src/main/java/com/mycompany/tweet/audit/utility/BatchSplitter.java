package com.mycompany.tweet.audit.utility;

import java.util.ArrayList;
import java.util.List;

//Utility method for batch splitting
public class BatchSplitter {

    public static <T> List<List<T>> splitWithLoop(List<T> list, int batchSize) {
        List<List<T>> batches = new ArrayList<>();
        if (list == null || list.isEmpty() || batchSize <= 0) {
            return batches; //Handle edge cases
        }
        int totalElements = list.size();

        for (int start = 0; start < totalElements; start += batchSize) {
            int end = Math.min(start + batchSize, list.size());
            List<T> batch = new ArrayList<>(list.subList(start, end));
            batches.add(batch);
        }
        return batches;
    }
}

