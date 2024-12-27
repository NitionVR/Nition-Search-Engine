package nitionsearch.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class CrawlMetrics {
    private final AtomicInteger processedPages = new AtomicInteger(0);
    private final AtomicInteger failedPages = new AtomicInteger(0);
    private final Map<String, AtomicInteger> responseCodeCounts = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> errorCounts = new ConcurrentHashMap<>();

    public void incrementProcessedPages() {
        processedPages.incrementAndGet();
    }

    public void incrementFailedPages() {
        failedPages.incrementAndGet();
    }

    public void recordResponseCode(int code) {
        responseCodeCounts
                .computeIfAbsent(String.valueOf(code), k -> new AtomicInteger())
                .incrementAndGet();
    }

    public void recordError(Exception e) {
        errorCounts
                .computeIfAbsent(e.getClass().getSimpleName(), k -> new AtomicInteger())
                .incrementAndGet();
    }

    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("processedPages", processedPages.get());
        stats.put("failedPages", failedPages.get());
        stats.put("responseCodes", new HashMap<>(responseCodeCounts));
        stats.put("errors", new HashMap<>(errorCounts));
        return stats;
    }
}