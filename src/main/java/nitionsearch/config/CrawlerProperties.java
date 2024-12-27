package nitionsearch.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "crawler")
public class CrawlerProperties {
    private int maxDepth = 5;
    private int threadCount = 4;
    private long crawlDelayMillis = 1000;
    private int maxPagesPerDomain = 1000;
    private String userAgent = "NitionBot/1.0";
    private int connectionTimeout = 5000;

    // Getters and setters
    public int getMaxDepth() { return maxDepth; }
    public void setMaxDepth(int maxDepth) { this.maxDepth = maxDepth; }

    public int getThreadCount() { return threadCount; }
    public void setThreadCount(int threadCount) { this.threadCount = threadCount; }

    public long getCrawlDelayMillis() { return crawlDelayMillis; }
    public void setCrawlDelayMillis(long crawlDelayMillis) { this.crawlDelayMillis = crawlDelayMillis; }

    public int getMaxPagesPerDomain() { return maxPagesPerDomain; }
    public void setMaxPagesPerDomain(int maxPagesPerDomain) { this.maxPagesPerDomain = maxPagesPerDomain; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public int getConnectionTimeout() { return connectionTimeout; }
    public void setConnectionTimeout(int connectionTimeout) { this.connectionTimeout = connectionTimeout; }
}