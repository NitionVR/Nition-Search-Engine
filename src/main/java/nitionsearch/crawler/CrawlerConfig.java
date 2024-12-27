package nitionsearch.crawler;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

public class CrawlerConfig {
    private final int maxDepth;
    private final int maxPagesPerDomain;
    private final Duration crawlDelay;
    private final int threadCount;
    private final String userAgent;
    private final Set<String> allowedDomains;
    private final Set<String> excludedPaths;
    private final int maxRetries;
    private final int connectionTimeout;

    private CrawlerConfig(Builder builder) {
        this.maxDepth = builder.maxDepth;
        this.maxPagesPerDomain = builder.maxPagesPerDomain;
        this.crawlDelay = builder.crawlDelay;
        this.threadCount = builder.threadCount;
        this.userAgent = builder.userAgent;
        this.allowedDomains = new HashSet<>(builder.allowedDomains);
        this.excludedPaths = new HashSet<>(builder.excludedPaths);
        this.maxRetries = builder.maxRetries;
        this.connectionTimeout = builder.connectionTimeout;
    }

    public static class Builder {
        private int maxDepth = 5;
        private int maxPagesPerDomain = 1000;
        private Duration crawlDelay = Duration.ofSeconds(1);
        private int threadCount = 4;
        private String userAgent = "NitionBot/1.0";
        private Set<String> allowedDomains = new HashSet<>();
        private Set<String> excludedPaths = new HashSet<>();
        private int maxRetries = 3;
        private int connectionTimeout = 5000;

        public Builder maxDepth(int maxDepth) {
            this.maxDepth = maxDepth;
            return this;
        }

        public Builder maxPagesPerDomain(int maxPagesPerDomain) {
            this.maxPagesPerDomain = maxPagesPerDomain;
            return this;
        }

        public Builder crawlDelay(Duration crawlDelay) {
            this.crawlDelay = crawlDelay;
            return this;
        }

        public Builder threadCount(int threadCount) {
            this.threadCount = threadCount;
            return this;
        }

        public Builder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public Builder allowDomain(String domain) {
            this.allowedDomains.add(domain);
            return this;
        }

        public Builder excludePath(String path) {
            this.excludedPaths.add(path);
            return this;
        }

        public Builder maxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }

        public Builder connectionTimeout(int connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
            return this;
        }

        public CrawlerConfig build() {
            return new CrawlerConfig(this);
        }
    }

    // Getters
    public int getMaxDepth() { return maxDepth; }
    public int getMaxPagesPerDomain() { return maxPagesPerDomain; }
    public Duration getCrawlDelay() { return crawlDelay; }
    public int getThreadCount() { return threadCount; }
    public String getUserAgent() { return userAgent; }
    public Set<String> getAllowedDomains() { return new HashSet<>(allowedDomains); }
    public Set<String> getExcludedPaths() { return new HashSet<>(excludedPaths); }
    public int getMaxRetries() { return maxRetries; }
    public int getConnectionTimeout() { return connectionTimeout; }
}