package nitionsearch.crawler;


import nitionsearch.model.Page;
import nitionsearch.search.SearchEngine;
import nitionsearch.util.CrawlMetrics;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

public class WebCrawler {
    private final SearchEngine searchEngine;
    private final CrawlerConfig config;
    private final URLFrontier frontier;
    private final ExecutorService executorService;
    private final AtomicBoolean isRunning;
    private final RobotsTxtManager robotsTxtManager;
    private final CrawlMetrics metrics;

    public WebCrawler(SearchEngine searchEngine, CrawlerConfig config) {
        this.searchEngine = searchEngine;
        this.config = config;
        this.frontier = new URLFrontier(config);
        this.executorService = Executors.newFixedThreadPool(config.getThreadCount());
        this.isRunning = new AtomicBoolean(false);
        this.robotsTxtManager = new RobotsTxtManager(config);
        this.metrics = new CrawlMetrics();
    }

    public void startCrawling(String seedUrl) {
        if (!isRunning.compareAndSet(false, true)) {
            throw new IllegalStateException("Crawler is already running");
        }

        frontier.addURL(seedUrl, 0);

        // Start crawler threads
        for (int i = 0; i < config.getThreadCount(); i++) {
            executorService.submit(this::crawlTask);
        }
    }

    private void crawlTask() {
        while (isRunning.get() && !Thread.currentThread().isInterrupted()) {
            CrawlURL crawlUrl = frontier.getNextURL();
            if (crawlUrl == null) {
                // No more URLs to crawl
                if (frontier.getQueueSize() == 0) {
                    isRunning.set(false);
                }
                continue;
            }

            try {
                processUrl(crawlUrl);
                metrics.incrementProcessedPages();
            } catch (Exception e) {
                metrics.incrementFailedPages();
                // Log error
            }
        }
    }

    private void processUrl(CrawlURL crawlUrl) throws Exception {
        String url = crawlUrl.getUrl();

        // Check robots.txt
        if (!robotsTxtManager.isAllowed(url)) {
            return;
        }

        // Fetch and parse page
        Document doc = fetchPage(url);
        if (doc == null) return;

        // Extract and clean content
        String content = extractContent(doc);
        if (content.isEmpty()) return;

        // Create and index page
        Page page = new Page(url, content);
        searchEngine.addPage(page);
        frontier.markVisited(url);

        // Extract and queue new URLs if not at max depth
        if (crawlUrl.getDepth() < config.getMaxDepth()) {
            queueNewUrls(doc, crawlUrl.getDepth() + 1);
        }
    }

    private Document fetchPage(String url) {
        int retries = 0;
        while (retries < config.getMaxRetries()) {
            try {
                return Jsoup.connect(url)
                        .userAgent(config.getUserAgent())
                        .timeout(config.getConnectionTimeout())
                        .get();
            } catch (Exception e) {
                retries++;
                if (retries == config.getMaxRetries()) {
                    metrics.incrementFailedPages();
                    return null;
                }
                // Exponential backoff
                try {
                    Thread.sleep((long) Math.pow(2, retries) * 1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return null;
                }
            }
        }
        return null;
    }

    private String extractContent(Document doc) {
        // Remove unwanted elements
        doc.select("script, style, iframe, noscript").remove();

        // Extract text content
        String content = doc.body().text();

        // Basic cleaning
        content = content.replaceAll("\\s+", " ").trim();

        return content;
    }

    private void queueNewUrls(Document doc, int depth) {
        Elements links = doc.select("a[href]");
        for (Element link : links) {
            String newUrl = link.attr("abs:href");
            if (isValidUrl(newUrl)) {
                frontier.addURL(newUrl, depth);
            }
        }
    }

    private boolean isValidUrl(String url) {
        return url.matches("^https?://.*") &&
                !url.matches(".*\\.(jpg|jpeg|png|gif|pdf|zip|exe)$") &&
                !Pattern.compile(String.join("|", config.getExcludedPaths()))
                        .matcher(url)
                        .find();
    }

    public void stopCrawling() {
        isRunning.set(false);
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public CrawlMetrics getMetrics() {
        return metrics;
    }
}
