package org.example;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * URLStore acts as a centralized repository for URLs to be crawled.
 * It manages two main data structures:
 * 1. visitedUrl: A ConcurrentHashMap to keep track of URLs that have already
 * been processed to avoid cycles and redundant processing.
 * 2. urlQueue: A BlockingQueue to hold URLs waiting to be processed, ensuring
 * thread-safe consumption by crawler threads.
 */
public class URLStore {
    // ConcurrentHashMap is used for thread-safe access to the set of visited URLs.
    // The key is the URL string, and the value is just a boolean marker.
    private final ConcurrentHashMap<String, Boolean> visitedUrl = new ConcurrentHashMap<>();

    // BlockingQueue is used to store URLs to be crawled. It handles concurrent
    // access automatically,
    // blocking if necessary when retrieving an element if the queue is empty
    // (though we use poll here).
    private final BlockingQueue<String> urlQueue = new LinkedBlockingQueue<>();

    /**
     * Adds a URL to the store if it hasn't been visited yet.
     * 
     * @param url The URL to add.
     * @return true if the URL was new and added, false otherwise.
     */
    public boolean addUrl(String url) {
        // putIfAbsent atomically checks if the key exists and puts it if not.
        // Returns null if the key was not present (meaning it's a new URL).
        if (visitedUrl.putIfAbsent(url, true) == null) {
            urlQueue.offer(url); // Add to the queue for processing
            return true;
        }
        return false;
    }

    /**
     * Retrieves the next URL from the queue.
     * 
     * @return The next URL, or null if the queue is empty.
     * @throws InterruptedException if the thread is interrupted while waiting.
     */
    public String getNextUrl() throws InterruptedException {
        return urlQueue.poll(); // Retrieves and removes the head of the queue, or returns null if empty.
    }

    /**
     * Checks if there are any pending URLs in the queue.
     * 
     * @return true if the queue is empty, false otherwise.
     */
    public boolean isQueueEmpty() {
        return urlQueue.isEmpty();
    }
}
