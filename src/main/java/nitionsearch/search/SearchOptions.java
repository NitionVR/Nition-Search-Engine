package nitionsearch.search;

import java.util.HashSet;
import java.util.Set;

public class SearchOptions {
    private int page = 1;
    private int pageSize = 10;
    private SortOrder sortOrder = SortOrder.RELEVANCE;
    private Set<String> filters = new HashSet<>();

    // Builder pattern
    public static class Builder {
        private final SearchOptions options = new SearchOptions();

        public Builder page(int page) {
            options.page = Math.max(1, page);
            return this;
        }

        public Builder pageSize(int pageSize) {
            options.pageSize = Math.max(1, Math.min(100, pageSize));
            return this;
        }

        public Builder sortOrder(SortOrder sortOrder) {
            options.sortOrder = sortOrder;
            return this;
        }

        public Builder addFilter(String filter) {
            options.filters.add(filter);
            return this;
        }

        public SearchOptions build() {
            return options;
        }
    }


    public int getPage() { return page; }
    public int getPageSize() { return pageSize; }
    public SortOrder getSortOrder() { return sortOrder; }
    public Set<String> getFilters() { return new HashSet<>(filters); }
}



