package org.example;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Phaser;

/**
 * CrawlerTask represents a single unit of work for the crawler.
 * It fetches links from a given URL and submits new tasks for discovered links.
 * It implements Runnable to be executed by the ExecutorService.
 */
public class CrawlerTask implements Runnable {
    private final URLStore urlStore;
    private final URLFetcher urlFetcher;
    private final int maxDepth;
    private final int currentDepth;
    private final Phaser phaser;
    private final ExecutorService executorService;

    public CrawlerTask(URLStore urlStore, URLFetcher urlFetcher, int currentDepth, int maxDepth, Phaser phaser,
            ExecutorService executorService) {
        this.urlStore = urlStore;
        this.urlFetcher = urlFetcher;
        this.maxDepth = maxDepth;
        this.currentDepth = currentDepth;
        this.phaser = phaser;
        this.executorService = executorService;
    }

    @Override
    public void run() {
        try {
            // Retrieve the next URL to process
            String url = urlStore.getNextUrl();
            if (url != null) {
                System.out.println(
                        Thread.currentThread().getName() + " processing: " + url + " at depth " + currentDepth);
            }

            // Stop if depth limit exceeded or no URL found
            if (url == null || currentDepth > maxDepth)
                return;

            // Fetch new links from the page
            Set<String> links = urlFetcher.fetchLinks(url);
            for (String link : links) {
                // If the link is new (added successfully to store), create a new task for it
                if (urlStore.addUrl(link)) {
                    // Register the new task with the Phaser to keep the main thread waiting
                    phaser.register();
                    // Submit the new task for the next depth level using the executor service
                    executorService.submit(
                            new CrawlerTask(urlStore, urlFetcher, currentDepth + 1, maxDepth, phaser, executorService));
                }
            }
        } catch (Exception e) {
            System.err.println("Error occurred in task: " + e.getMessage());
        } finally {
            // Signal that this task is complete.
            // arriveAndDeregister reduces the number of unarrived parties.
            phaser.arriveAndDeregister();
        }
    }
}
