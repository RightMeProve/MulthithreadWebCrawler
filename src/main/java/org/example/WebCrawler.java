package org.example;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;

/**
 * WebCrawler orchestrates the crawling process.
 * It manages the ExecutorService, Phaser, and shared resources (URLStore,
 * URLFetcher).
 */
public class WebCrawler {
    private final int maxDepth;
    private final int maxThreads;
    private final String startUrl;

    public WebCrawler(String startUrl, int maxDepth, int maxThreads) {
        this.startUrl = startUrl;
        this.maxDepth = maxDepth;
        this.maxThreads = maxThreads;
    }

    /**
     * Starts the crawling process.
     */
    public void start() {
        URLStore urlStore = new URLStore();
        URLFetcher urlFetcher = new URLFetcher();
        Phaser phaser = new Phaser(1); // Register self (main thread)
        ExecutorService executorService = Executors.newFixedThreadPool(maxThreads);

        urlStore.addUrl(startUrl);

        long start = System.currentTimeMillis();
        System.out.println("Starting crawl with depth " + maxDepth + " and " + maxThreads + " threads...");

        // Submit the initial task
        // We pass the executorService to the task so it can recursively submit new
        // tasks
        executorService.submit(new CrawlerTask(urlStore, urlFetcher, 0, maxDepth, phaser, executorService));

        // Wait for all tasks to complete.
        // The initial task registers, and subsequent tasks register/deregister.
        // We assume the first phase completion means we are done?
        // Actually, Phaser logic in the original code was:
        // phaser.register() in loop, phaser.arriveAndDeregister() in finally.
        // The main thread registers (1), submits first task (which registers? No, first
        // task doesn't register in original code, it just runs).
        // IN ORIGINAL:
        // phaser init(1).
        // submitTask (no register).
        // awaitAdvance.
        // Task: run -> fetch -> loop -> if new -> phaser.register() -> submit.
        // finally arriveAndDeregister.
        // SO: The initial task is NOT registered. It runs. If it finds links, it
        // registers new parties.
        // The main thread waits.
        // PROBLEM: If the first task finishes without finding links or before
        // submitting, the phaser count might drop to 1 (main thread).
        // The main thread calls awaitAdvance.
        // If main thread is the only registered party, awaitAdvance returns
        // immediately?
        // No, awaitAdvance waits for the phase to advance. Who advances it?
        // If count is 1, and main calls awaitAdvance, it waits for arrival. Main hasn't
        // arrived.
        // Main should probably arriveAndAwaitAdvance?
        // Original: phaser.awaitAdvance(phaser.getPhase());
        // This just waits for the phase number to change.
        // If nobody arrives, it hangs?
        // Let's fix the Phaser logic to be robust.
        // Main thread registers 1.
        // Submit first task. First task should probably register?
        // If first task is just submitted, it runs.
        // If we want to wait for "all tasks", we usually treat the phaser as a count of
        // active tasks.
        // Main thread registers.
        // We submit first task. We should probably NOT let main thread be part of the
        // phaser for "completion" logic in the same way,
        // OR main thread registers, submits first task which registers, then main
        // thread deregisters and waits?
        // Standard pattern:
        // Phaser phaser = new Phaser(1); // 1 for the main thread
        // submit valid task -> phaser.register();
        // main thread: phaser.arriveAndAwaitAdvance(); // waits for everyone else? No.

        // Let's stick to the original logic's intent but clean it up.
        // Original: phaser = new Phaser(1). awaitAdvance(getPhase()).
        // It relies on tasks registering.
        // I will change it to:
        // Phaser phaser = new Phaser(1); // One party (main)
        // executor.submit(new Task(..., phaser));
        // // The task itself will internally register for subtasks.
        // // But the first task needs to be tracked too.
        // // Let's simplify: Pass phaser to task.
        // // For the very first task, we can just register it.
        // phaser.register();
        // executor.submit(new Task(..., phaser));
        // phaser.arriveAndAwaitAdvance(); // blocked until phase advance.

        // Better yet:
        // Phaser phaser = new Phaser(1); // Main thread
        // executor.submit(() -> { try { new CrawlerTask(...).run(); } finally {
        // phaser.arrive(); }? No.

        // Let's use the standard "wait until done"
        // phaser.register(); // register for the first task
        // executor.submit(new CrawlerTask(..., phaser));
        // phaser.arriveAndAwaitAdvance();

        // Wait, CrawlerTask in original code does `phaser.arriveAndDeregister`.
        // So the first task should also do `arriveAndDeregister`.
        // So main thread starts with 1.
        // We register 1 for the first task. count = 2.
        // Main thread calls arriveAndAwaitAdvance(). Main arrives. count waiting = 1
        // (the task).
        // Task runs. Task finishes -> arriveAndDeregister. count = 0? Phase advances.
        // Main thread wakes up.

        // CORRECTION:
        // If main initializes with 1.
        // We register 1 for the worker. Total 2.
        // Main calls arriveAndAwaitAdvance(). Main arrives. Waiting for worker.
        // Worker finishes -> arriveAndDeregister.
        // If worker creates subtasks:
        // Worker registers N subtasks. Total 1 + N.
        // Worker finishes (deregisters). Total N.
        // Main is still waiting for phase advance.
        // Works.

        phaser.register();
        executorService.submit(new CrawlerTask(urlStore, urlFetcher, 0, maxDepth, phaser, executorService));

        phaser.arriveAndAwaitAdvance();

        executorService.shutdown();
        System.out.println("Time taken: " + (System.currentTimeMillis() - start) + "ms");
    }
}
