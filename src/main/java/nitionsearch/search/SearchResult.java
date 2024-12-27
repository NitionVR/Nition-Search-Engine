package nitionsearch.search;

import nitionsearch.model.Page;

import java.util.Collections;
import java.util.List;

public class SearchResult {
    private final List<Page> pages;
    private final int totalResults;
    private final int totalPages;

    public SearchResult(List<Page> pages, int totalResults, int totalPages) {
        this.pages = Collections.unmodifiableList(pages);
        this.totalResults = totalResults;
        this.totalPages = totalPages;
    }

    public List<Page> getPages() { return pages; }
    public int getTotalResults() { return totalResults; }
    public int getTotalPages() { return totalPages; }
}
