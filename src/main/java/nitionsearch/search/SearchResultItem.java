package nitionsearch.search;

import nitionsearch.model.Page;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SearchResultItem {
    private final Page page;
    private final String snippet;
    private final List<String> highlights;
    private final Map<String, Integer> termFrequencies;

    public SearchResultItem(Page page, String snippet, List<String> highlights,
                            Map<String, Integer> termFrequencies) {
        this.page = page;
        this.snippet = snippet;
        this.highlights = highlights;
        this.termFrequencies = termFrequencies;
    }

    // Getters
    public Page getPage() { return page; }
    public String getSnippet() { return snippet; }
    public List<String> getHighlights() { return highlights; }
    public Map<String, Integer> getTermFrequencies() { return termFrequencies; }
}

// Add to SearchEngine class:
