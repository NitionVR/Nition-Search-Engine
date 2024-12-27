package nitionsearch.service;

import nitionsearch.model.Page;
import nitionsearch.search.SearchEngine;
import nitionsearch.search.SearchOptions;
import nitionsearch.search.SearchResult;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class SearchService {
    private final SearchEngine searchEngine;
    private final AtomicLong totalSearches = new AtomicLong(0);
    private final Map<String, AtomicLong> queryStats = new HashMap<>();

    public SearchService(SearchEngine searchEngine) {
        this.searchEngine = searchEngine;
    }

    public SearchResult search(String query, SearchOptions options) {
        // Record statistics
        totalSearches.incrementAndGet();
        queryStats.computeIfAbsent(query, k -> new AtomicLong()).incrementAndGet();

        // Perform search
        return searchEngine.search(query, options);
    }

    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalSearches", totalSearches.get());
        stats.put("popularQueries", getTopQueries(10));
        stats.put("indexedPages", searchEngine.getIndexedPagesCount());
        return stats;
    }

    private Map<String, Long> getTopQueries(int limit) {
        Map<String, Long> topQueries = new HashMap<>();
        queryStats.entrySet().stream()
                .sorted((e1, e2) -> Long.compare(e2.getValue().get(), e1.getValue().get()))
                .limit(limit)
                .forEach(e -> topQueries.put(e.getKey(), e.getValue().get()));
        return topQueries;
    }
}