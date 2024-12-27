package nitionsearch.crawler;

import java.time.Instant;

public class CrawlURL {
    private final String url;
    private final int depth;
    private final Instant discovered;

    public CrawlURL(String url, int depth) {
        this.url = url;
        this.depth = depth;
        this.discovered = Instant.now();
    }

    public String getUrl() { return url; }
    public int getDepth() { return depth; }
    public Instant getDiscovered() { return discovered; }
}