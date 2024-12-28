package nitionsearch.search;

import nitionsearch.model.Page;

import java.util.Collections;
import java.util.List;

public class SearchResult {
    private final List<SearchResultItem> items;
    private final int totalResults;
    private final int totalPages;

    public SearchResult(List<SearchResultItem> items, int totalResults, int totalPages) {
        this.items = items;
        this.totalResults = totalResults;
        this.totalPages = totalPages;
    }

    public List<SearchResultItem> getItems() { return items; }
    public int getTotalResults() { return totalResults; }
    public int getTotalPages() { return totalPages; }
}
