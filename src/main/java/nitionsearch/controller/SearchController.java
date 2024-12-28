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

import org.springframework.http.MediaType;
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

    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> search(
            @RequestParam String query,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "RELEVANCE") String sortOrder) {

        SearchOptions options = new SearchOptions.Builder()
                .page(page)
                .pageSize(pageSize)
                .sortOrder(SortOrder.valueOf(sortOrder))
                .build();

        SearchResult results = searchService.search(query, options);

        // Convert to format expected by frontend
        Map<String, Object> response = new HashMap<>();
        response.put("items", results.getItems());
        response.put("totalResults", results.getTotalResults());
        response.put("totalPages", results.getTotalPages());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/crawl")
    public ResponseEntity<ApiResponse<Void>> startCrawling(@Valid @RequestBody CrawlRequest request) {
        try {
            String url = request.getUrl();
            System.out.println("Starting crawl for URL: " + url);
            webCrawler.startCrawling(url);
            return ResponseEntity.ok(ApiResponse.success(
                    "Crawling started for URL: " + url, null));
        } catch (Exception e) {
            e.printStackTrace();  // Add stack trace
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