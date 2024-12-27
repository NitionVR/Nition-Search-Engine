package nitionsearch.search;

import nitionsearch.model.Page;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.time.Instant;

public class SearchCache {
    private final Map<String, CacheEntry> cache;
    private final int maxSize;
    private final long ttlMillis;

    public SearchCache(int maxSize, long ttlMillis) {
        this.cache = new ConcurrentHashMap<>();
        this.maxSize = maxSize;
        this.ttlMillis = ttlMillis;
    }

    public Optional<List<Page>> get(String query) {
        cleanup();
        CacheEntry entry = cache.get(query);
        if (entry != null && !entry.isExpired()) {
            return Optional.of(entry.getResults());
        }
        return Optional.empty();
    }

    public void put(String query, List<Page> results) {
        cleanup();
        if (cache.size() >= maxSize) {
            removeOldestEntry();
        }
        cache.put(query, new CacheEntry(results));
    }

    private void cleanup() {
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    private void removeOldestEntry() {
        Optional<Map.Entry<String, CacheEntry>> oldest = cache.entrySet().stream()
                .min(Comparator.comparing(e -> e.getValue().getTimestamp()));
        oldest.ifPresent(entry -> cache.remove(entry.getKey()));
    }

    private class CacheEntry {
        private final List<Page> results;
        private final long timestamp;

        public CacheEntry(List<Page> results) {
            this.results = new ArrayList<>(results);
            this.timestamp = System.currentTimeMillis();
        }

        public List<Page> getResults() {
            return new ArrayList<>(results);
        }

        public long getTimestamp() {
            return timestamp;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() - timestamp > ttlMillis;
        }
    }
}