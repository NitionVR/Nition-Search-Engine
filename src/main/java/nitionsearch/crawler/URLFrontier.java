package nitionsearch.crawler;

import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

public class URLFrontier {
    private final Queue<CrawlURL> urlQueue;
    private final Set<String> visitedUrls;
    private final Map<String, Integer> domainPageCounts;
    private final Map<String, Instant> lastAccessTimes;
    private final CrawlerConfig config;

    public URLFrontier(CrawlerConfig config) {
        this.urlQueue = new ConcurrentLinkedQueue<>();
        this.visitedUrls = Collections.synchronizedSet(new HashSet<>());
        this.domainPageCounts = new ConcurrentHashMap<>();
        this.lastAccessTimes = new ConcurrentHashMap<>();
        this.config = config;
    }

    public void addURL(String url, int depth) {
        System.out.println("Adding URL to frontier: " + url + " at depth " + depth);
        if (!visitedUrls.contains(url)) {
            urlQueue.offer(new CrawlURL(url, depth));
            System.out.println("URL added successfully to queue");
        } else {
            System.out.println("URL already visited, skipping: " + url);
        }
    }

    public CrawlURL getNextURL() {
        CrawlURL nextUrl = urlQueue.poll();
        if (nextUrl != null) {
            System.out.println("Retrieved URL from frontier: " + nextUrl.getUrl());
        } else {
            System.out.println("No URLs available in queue");
        }
        return nextUrl;
    }

    public void markVisited(String url) {
        visitedUrls.add(url);
        String domain = extractDomain(url);
        domainPageCounts.merge(domain, 1, Integer::sum);
    }

    public void clear() {
        urlQueue.clear();
        visitedUrls.clear();
        domainPageCounts.clear();
        lastAccessTimes.clear();
    }

    public int getQueueSize() {
        return urlQueue.size();
    }

    private String extractDomain(String url) {
        try {
            String host = new URL(url).getHost();
            return host.startsWith("www.") ? host.substring(4) : host;
        } catch (Exception e) {
            return "";
        }
    }
}

