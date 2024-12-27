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
        this.urlQueue = new PriorityBlockingQueue<>(1000,
                Comparator.comparingInt(CrawlURL::getDepth));
        this.visitedUrls = Collections.synchronizedSet(new HashSet<>());
        this.domainPageCounts = new ConcurrentHashMap<>();
        this.lastAccessTimes = new ConcurrentHashMap<>();
        this.config = config;
    }

    public void addURL(String url, int depth) {
        try {
            URL parsedUrl = new URL(url);
            String domain = parsedUrl.getHost();
            String normalizedUrl = normalizeURL(url);

            if (canCrawl(normalizedUrl, domain, depth)) {
                urlQueue.offer(new CrawlURL(normalizedUrl, depth));
            }
        } catch (Exception e) {
            // Log invalid URL
        }
    }

    public CrawlURL getNextURL() {
        CrawlURL nextUrl = null;
        while (nextUrl == null && !urlQueue.isEmpty()) {
            CrawlURL candidate = urlQueue.poll();
            if (candidate == null) continue;

            String domain = getDomain(candidate.getUrl());
            if (domain == null) continue;

            Instant lastAccess = lastAccessTimes.get(domain);
            if (lastAccess != null) {
                Duration waitTime = Duration.between(lastAccess, Instant.now());
                if (waitTime.compareTo(config.getCrawlDelay()) < 0) {
                    // Re-queue the URL if we need to wait
                    urlQueue.offer(candidate);
                    continue;
                }
            }

            nextUrl = candidate;
            lastAccessTimes.put(domain, Instant.now());
        }
        return nextUrl;
    }

    private boolean canCrawl(String url, String domain, int depth) {
        if (visitedUrls.contains(url)) return false;
        if (depth > config.getMaxDepth()) return false;

        int pageCount = domainPageCounts.getOrDefault(domain, 0);
        if (pageCount >= config.getMaxPagesPerDomain()) return false;

        if (!config.getAllowedDomains().isEmpty() &&
                !config.getAllowedDomains().contains(domain)) return false;

        return true;
    }

    public void markVisited(String url) {
        String normalizedUrl = normalizeURL(url);
        visitedUrls.add(normalizedUrl);

        String domain = getDomain(url);
        if (domain != null) {
            domainPageCounts.merge(domain, 1, Integer::sum);
        }
    }

    private String normalizeURL(String url) {
        // Remove trailing slash, fragment, etc.
        return url.replaceAll("/$", "")
                .replaceAll("#.*", "")
                .replaceAll("\\?$", "");
    }

    private String getDomain(String url) {
        try {
            return new URL(url).getHost();
        } catch (Exception e) {
            return null;
        }
    }

    public boolean hasMoreURLs() {
        return !urlQueue.isEmpty();
    }

    public int getQueueSize() {
        return urlQueue.size();
    }

    public Map<String, Integer> getDomainStatistics() {
        return new HashMap<>(domainPageCounts);
    }
}

