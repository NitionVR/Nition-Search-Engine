package nitionsearch.dto;

import java.util.Set;

public class CrawlRequest {
    private String url;
    private Set<String> allowedDomains;
    private int maxDepth;

    // Default constructor needed for Jackson
    public CrawlRequest() {}

    public CrawlRequest(String url) {
        this.url = url;
    }

    // Getters and setters
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Set<String> getAllowedDomains() {
        return allowedDomains;
    }

    public void setAllowedDomains(Set<String> allowedDomains) {
        this.allowedDomains = allowedDomains;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }
}


