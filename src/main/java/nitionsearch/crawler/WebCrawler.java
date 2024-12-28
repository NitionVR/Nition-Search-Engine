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
    private ExecutorService executorService;
    private final AtomicBoolean isRunning;
    private final RobotsTxtManager robotsTxtManager;
    private final CrawlMetrics metrics;
    private String baseDomain;
    private static final int QUEUE_CHECK_INTERVAL = 1000; // 1 second

    public WebCrawler(SearchEngine searchEngine, CrawlerConfig config) {
        this.searchEngine = searchEngine;
        this.config = config;
        this.frontier = new URLFrontier(config);
        this.executorService = createExecutorService();
        this.isRunning = new AtomicBoolean(false);
        this.robotsTxtManager = new RobotsTxtManager(config);
        this.metrics = new CrawlMetrics();
    }

    private ExecutorService createExecutorService() {
        return Executors.newFixedThreadPool(config.getThreadCount());
    }

    public void startCrawling(String seedUrl) {
        if (isRunning.get()) {
            System.out.println("Crawler is already running, stopping current crawl...");
            stopCurrentCrawl();
        }

        // Start new crawl
        isRunning.set(true);
        this.baseDomain = extractDomain(seedUrl);
        frontier.clear();
        metrics.reset();
        frontier.addURL(seedUrl, 0);
        System.out.println("Starting new crawl for domain: " + baseDomain);

        // Ensure executor service is running
        if (executorService == null || executorService.isShutdown()) {
            executorService = createExecutorService();
        }

        // Start crawler threads
        for (int i = 0; i < config.getThreadCount(); i++) {
            if (!executorService.isShutdown()) {
                executorService.submit(this::crawlTask);
            }
        }
    }

    private void stopCurrentCrawl() {
        isRunning.set(false);
        frontier.clear();
        metrics.reset();
        // Don't shutdown executor service, just stop current crawl
        System.out.println("Stopped current crawl");
    }

    private void crawlTask() {
        String threadName = Thread.currentThread().getName();
        System.out.println(threadName + " started");

        while (isRunning.get() && !Thread.currentThread().isInterrupted()) {
            try {
                CrawlURL crawlUrl = frontier.getNextURL();
                if (crawlUrl == null) {
                    // Check if there are no more URLs and all threads are idle
                    if (frontier.getQueueSize() == 0) {
                        System.out.println(threadName + ": No URLs in queue, waiting...");
                        Thread.sleep(QUEUE_CHECK_INTERVAL);
                        continue;
                    }
                    continue;
                }

                System.out.println(threadName + ": Processing " + crawlUrl.getUrl());
                processUrl(crawlUrl);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.out.println(threadName + ": Error: " + e.getMessage());
                metrics.incrementFailedPages();
            }
        }
        System.out.println(threadName + " finished");
    }

    private void processUrl(CrawlURL crawlUrl) throws Exception {
        String url = crawlUrl.getUrl();

        // Check robots.txt
        if (!robotsTxtManager.isAllowed(url)) {
            System.out.println("URL not allowed by robots.txt: " + url);
            return;
        }

        // Fetch page
        Document doc = fetchPage(url);
        if (doc == null) return;

        // Process content
        String content = extractContent(doc);
        if (content.isEmpty()) return;

        // Save page
        Page page = new Page(url, content);
        searchEngine.addPage(page);
        frontier.markVisited(url);
        metrics.incrementProcessedPages();

        // Process links if not at max depth
        if (crawlUrl.getDepth() < config.getMaxDepth()) {
            queueNewUrls(doc, crawlUrl.getDepth() + 1);
        }
    }

    private void queueNewUrls(Document doc, int depth) {
        Elements links = doc.select("a[href]");
        int addedUrls = 0;

        for (Element link : links) {
            String newUrl = link.attr("abs:href");
            if (isValidUrl(newUrl) && isSameDomain(newUrl)) {
                frontier.addURL(newUrl, depth);
                addedUrls++;
            }
        }
        System.out.println("Added " + addedUrls + " new URLs to queue");
    }

    private Document fetchPage(String url) {
        for (int retry = 0; retry < config.getMaxRetries(); retry++) {
            try {
                return Jsoup.connect(url)
                        .userAgent(config.getUserAgent())
                        .timeout(config.getConnectionTimeout())
                        .followRedirects(true)
                        .get();
            } catch (Exception e) {
                System.out.println("Fetch failed, attempt " + (retry + 1) + " of " + config.getMaxRetries());
                if (retry == config.getMaxRetries() - 1) return null;
                try {
                    Thread.sleep((1L << retry) * 1000); // Exponential backoff
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return null;
                }
            }
        }
        return null;
    }

    private String extractContent(Document doc) {
        doc.select("script, style, iframe, noscript").remove();
        return doc.body().text().replaceAll("\\s+", " ").trim();
    }

    private boolean isValidUrl(String url) {
        if (url == null || url.trim().isEmpty()) return false;

        try {
            new java.net.URL(url);
            return url.startsWith("http") &&
                    !url.matches(".*\\.(jpg|jpeg|png|gif|pdf|zip|exe|css|js)$") &&
                    !url.contains("mailto:") &&
                    !url.contains("javascript:") &&
                    !url.contains("#");
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isSameDomain(String url) {
        try {
            String urlDomain = extractDomain(url);
            return baseDomain != null && baseDomain.equals(urlDomain);
        } catch (Exception e) {
            return false;
        }
    }

    private String extractDomain(String url) {
        try {
            String host = new java.net.URL(url).getHost();
            return host.startsWith("www.") ? host.substring(4) : host;
        } catch (Exception e) {
            return "";
        }
    }

    public void stopCrawling() {
        // First stop the crawling process
        isRunning.set(false);
        System.out.println("Stopping crawler for domain: " + baseDomain);

        // Then shutdown the executor service
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        // Create new executor service for future use
        executorService = createExecutorService();
    }

    public CrawlMetrics getMetrics() {
        return metrics;
    }

    public boolean isRunning() {
        return isRunning.get();
    }
}