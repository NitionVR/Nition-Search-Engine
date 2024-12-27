package nitionsearch.dto;

import java.util.Map;

public class CrawlerStatusResponse {
    private String status;
    private int pagesProcessed;
    private int pagesFailed;
    private int queueSize;
    private Map<String, Integer> responseCodes;
    private double crawlRate;

    public CrawlerStatusResponse(String status, int pagesProcessed, int pagesFailed,
                                 int queueSize, Map<String, Integer> responseCodes,
                                 double crawlRate) {
        this.status = status;
        this.pagesProcessed = pagesProcessed;
        this.pagesFailed = pagesFailed;
        this.queueSize = queueSize;
        this.responseCodes = responseCodes;
        this.crawlRate = crawlRate;
    }

    // Getters
    public String getStatus() { return status; }
    public int getPagesProcessed() { return pagesProcessed; }
    public int getPagesFailed() { return pagesFailed; }
    public int getQueueSize() { return queueSize; }
    public Map<String, Integer> getResponseCodes() { return responseCodes; }
    public double getCrawlRate() { return crawlRate; }
}