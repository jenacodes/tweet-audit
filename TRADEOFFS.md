# Tradeoffs

## Language choice: Java

I chose Java because I'm familiar with the syntax and its compile-time type safety coupled with a stable ecosystem. Although a little verbose, leveraging modern features like records, text blocks  to eliminate boilerplate code which makes data models much clearer.

## Architecture: Separation of Concerns(deep modules, reducing cognitive load)

I built this using a modular design. By encapsulating all the messy logic, JSON deserialization, retries etc. inside separate files.

I created deep modules and clean interfaces which makes the code more readable, cleaner and reduces cognitive load for developers which simplifies the evolution of the system.

## Error Handling: Fail Fast vs Fail Gracefully

I chose the Fail Fast principle for unexpected errors in this project because it protects data integrity and immediately gives the user clarity on exactly what is wrong. This contrasts with systems that fail gracefully, which often masks the root cause of an issue leading to silent data loss.

For network errors, I built this as cynical software—which means expecting bad things to happen. I used a retry pattern with exponential backoff (waiting 1s, 2s, 4s, 8s, 16s) alongside a sleep timer between batches. In the event of network drops or rate limits, this prevents crashing the whole application.

## Concurrency & Performance

This tool is built to execute task in batches sequentially on a single thread. Sending each tweets sequentially would be slow and sending all at once(Multithreading) which is faster but risks rate limits errors and API quota exhaustion. Sending tweets in batches sequentially is the sweet spot.
Increasing the batchSize can increase throughput but can also increase the risk of the AI hallucinating and returning false positives. Furthermore, Staying single-threaded is significantly easier to debug and reduces architectural overhead.

## Resilience: Checkpointing Mechanism

I implemented a checkpointing mechanism that allows the application to resume from the last successfully processed tweet in case of a crash or unexpected termination. This ensures that users do not lose progress and can continue their audit without starting over, enhancing the overall resilience of the tool.





