package org.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * URLFetcher is responsible for downloading web pages and extracting links from
 * them.
 * It uses the Jsoup library to parse HTML.
 */
public class URLFetcher {
    /**
     * Fetches all hyperlinks (anchor tags) from the given URL.
     * 
     * @param url The URL to fetch links from.
     * @return A Set of absolute URLs found on the page.
     */
    public Set<String> fetchLinks(String url) {
        Set<String> links = new HashSet<>();
        Document document = null;
        try {
            // Connect to the URL with a timeout of 5 seconds.
            document = Jsoup.connect(url).timeout(5000).get();
        } catch (IOException e) {
            // Log the error but don't stop the whole application. Return empty set.
            System.err.println("Failed to fetch URL: " + url + " - " + e.getMessage());
            return links;
        }

        // Extract all 'a' tags with an 'href' attribute
        Elements anchorTags = document.select("a[href]");
        for (Element link : anchorTags) {
            // absUrl returns the absolute URL, handling relative paths automatically
            String extractedUrl = link.absUrl("href");
            if (!extractedUrl.isEmpty()) {
                links.add(extractedUrl);
            }
        }

        return links;
    }
}
