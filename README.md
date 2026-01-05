# Multithreaded Web Crawler

A high-performance, multithreaded web crawler implemented in Java. This application uses `ExecutorService` for thread management and `Phaser` for synchronization to efficiently crawl web pages up to a specified depth.

## Features

-   **Multithreaded Crawling**: Uses a thread pool to fetch and process multiple URLs concurrently.
-   **Synchronization**: Utilizes `Phaser` to coordinate tasks and ensure the main process waits for all crawling activities to complete.
-   **Thread-Safe Components**: Implements a `URLStore` using `ConcurrentHashMap` and `BlockingQueue` to safely manage URLs across threads.
-   **Robust Error Handling**: Gracefully handles network errors and bad URLs without crashing.
-   **Depth Control**: Limits the crawl to a user-defined depth to prevent infinite loops.

## Project Structure

-   `Main.java`: The entry point of the application. Handles user input (URL, depth, threads) and initializes the crawler.
-   `WebCrawler.java`: The core engine that orchestrates the crawling process, manages the thread pool, and synchronizes tasks.
-   `CrawlerTask.java`: A `Runnable` task that processes a single URL, extracts links, and schedules new tasks for discovered links.
-   `URLStore.java`: A centralized, thread-safe repository for managing visited URLs and the queue of URLs to be processed.
-   `URLFetcher.java`: A utility class using [Jsoup](https://jsoup.org/) to fetch HTML content and extract hyperlinks.

## Design Decisions

1.  **ExecutorService**: Used `Executors.newFixedThreadPool(n)` to manage a fixed number of worker threads. This prevents resource exhaustion by limiting the number of concurrent connections.
2.  **Phaser**: Chosen over `CountDownLatch` or `CyclicBarrier` because the number of tasks is dynamic. As new links are found, new tasks are registered with the Phaser.
3.  **Concurrent Data Structures**:
    -   `ConcurrentHashMap` (as a Set backing) handles concurrent checks for visited URLs (O(1) average time).
    -   `LinkedBlockingQueue` manages the backlog of URLs to visit.

## Prerequisites

-   Java 11 or higher
-   Maven (for dependency management, specifically Jsoup)

## How to Run

1.  **Compile the project**:
    Ensure you have the dependencies installed (check `pom.xml`).
    ```bash
    mvn clean install
    ```

2.  **Run the application**:
    You can run the `Main` class directly from your IDE or via command line.
    ```bash
    java -cp target/classes:target/dependency/* org.example.Main
    ```

3.  **Input Configuration**:
    The application will prompt you for:
    -   **URL**: The starting point (e.g., `https://example.com`).
    -   **Depth**: How deep to crawl (e.g., `2` means start page -> links on start page -> links on those pages).
    -   **Threads**: Number of concurrent threads to use.

## Example Output

```text
Enter the url (e.g., https://example.com): 
https://crawler-test.com/
Enter the depth (e.g., 2): 
2
Enter the number of threads (e.g., 4): 
4
Starting crawl with depth 2 and 4 threads...
pool-1-thread-1 processing: https://crawler-test.com/ at depth 0
pool-1-thread-2 processing: https://crawler-test.com/mobile/separate_mobile_site at depth 1
pool-1-thread-3 processing: https://crawler-test.com/description_tags/description_with_whitespace at depth 1
...
Time taken: 1245ms
```

## Dependencies

-   [Jsoup](https://jsoup.org/): For HTML parsing and link extraction.
