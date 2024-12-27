package nitionsearch.controller;

import jakarta.validation.Valid;
import nitionsearch.dto.ApiResponse;
import nitionsearch.dto.CrawlRequest;
import nitionsearch.dto.CrawlerStatusResponse;
import nitionsearch.dto.SearchRequest;
import nitionsearch.model.Page;
import nitionsearch.search.SearchEngine;
import nitionsearch.search.SearchOptions;
import nitionsearch.search.SearchResult;
import nitionsearch.search.SortOrder;
import nitionsearch.crawler.WebCrawler;
import nitionsearch.crawler.CrawlerConfig;
import nitionsearch.service.SearchService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.time.Duration;

@RestController
@RequestMapping("/api")
public class SearchController {
    private final SearchService searchService;
    private final WebCrawler webCrawler;

    public SearchController(SearchService searchService, WebCrawler webCrawler) {
        this.searchService = searchService;
        this.webCrawler = webCrawler;
    }

    @GetMapping("/search")
    public ResponseEntity<SearchResult> search(
            @RequestParam String query,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "RELEVANCE") String sortOrder) {

        try {
            // Safely convert string to enum
            SortOrder sort = (sortOrder != null) ?
                    SortOrder.valueOf(sortOrder.toUpperCase()) :
                    SortOrder.RELEVANCE;

            SearchOptions options = new SearchOptions.Builder()
                    .page(page)
                    .pageSize(pageSize)
                    .sortOrder(sort)
                    .build();

            SearchResult results = searchService.search(query, options);
            return ResponseEntity.ok(results);
        } catch (IllegalArgumentException e) {
            // Handle invalid sort order
            SearchOptions options = new SearchOptions.Builder()
                    .page(page)
                    .pageSize(pageSize)
                    .sortOrder(SortOrder.RELEVANCE)  // Default to RELEVANCE
                    .build();

            SearchResult results = searchService.search(query, options);
            return ResponseEntity.ok(results);
        }
    }
    @PostMapping("/crawl")
    public ResponseEntity<ApiResponse<Void>> startCrawling(@Valid @RequestBody CrawlRequest request) {
        try {
            webCrawler.startCrawling(request.getUrl());
            return ResponseEntity.ok(ApiResponse.success(
                    "Crawling started for URL: " + request.getUrl(), null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to start crawling: " + e.getMessage()));
        }
    }

    @PostMapping("/crawl/stop")
    public ResponseEntity<ApiResponse<Void>> stopCrawling() {
        webCrawler.stopCrawling();
        return ResponseEntity.ok(ApiResponse.success("Crawling stopped", null));
    }

    @GetMapping("/crawl/status")
    public ResponseEntity<Map<String, Object>> getCrawlerStatus() {
        Map<String, Object> stats = webCrawler.getMetrics().getStats();

        // Create a safe response map with default values
        Map<String, Object> safeStats = new HashMap<>();
        safeStats.put("processedPages", stats.getOrDefault("processedPages", 0));
        safeStats.put("failedPages", stats.getOrDefault("failedPages", 0));
        safeStats.put("queueSize", stats.getOrDefault("queueSize", 0));
        safeStats.put("crawlRate", stats.getOrDefault("crawlRate", 0.0));
        safeStats.put("status", stats.getOrDefault("status", "unknown"));

        // Safely handle response codes
        @SuppressWarnings("unchecked")
        Map<String, Integer> responseCodes = (Map<String, Integer>)
                stats.getOrDefault("responseCodes", new HashMap<String, Integer>());
        safeStats.put("responseCodes", responseCodes);

        return ResponseEntity.ok(safeStats);
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSearchStats() {
        return ResponseEntity.ok(ApiResponse.success(searchService.getStatistics()));
    }

    private double calculateCrawlRate(Map<String, Object> stats) {
        // Implementation of crawl rate calculation
        return 0.0; // Replace with actual calculation
    }
}