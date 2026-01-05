package org.example;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("Enter the url (e.g., https://example.com): ");
        String url = sc.nextLine();

        // Basic validation for URL
        if (url == null || url.trim().isEmpty()) {
            System.out.println("URL cannot be empty. Exiting.");
            return;
        }

        System.out.println("Enter the depth (e.g., 2): ");
        int maxDepth = 2;
        try {
            String depthInput = sc.nextLine();
            if (!depthInput.trim().isEmpty()) {
                maxDepth = Integer.parseInt(depthInput);
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid depth, defaulting to 2.");
        }

        System.out.println("Enter the number of threads (e.g., 4): ");
        int maxThreads = 4;
        try {
            String threadInput = sc.nextLine();
            if (!threadInput.trim().isEmpty()) {
                maxThreads = Integer.parseInt(threadInput);
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid thread count, defaulting to 4.");
        }

        WebCrawler crawler = new WebCrawler(url, maxDepth, maxThreads);
        crawler.start();

        sc.close();
    }
}