package nitionsearch.crawler;

import nitionsearch.search.SearchEngine;
import nitionsearch.model.Page;
import java.net.MalformedURLException;
import java.net.URI;

public class WebCrawler {
    private final SearchEngine searchEngine;
    private int nextPageId = 0;
    public WebCrawler (SearchEngine searchEngine){
        this.searchEngine = searchEngine;
    }

    public void crawlAndIndex(String url, String content){
        if (!isValidURL(url)) {
            throw new IllegalArgumentException("Invalid URL: " + url);
        }
        Page page = new Page(nextPageId++, url, content);
        searchEngine.addPage(page);
    }

    public boolean isValidURL(String url) {
        try {
            URI.create(url).toURL();
        } catch (MalformedURLException | IllegalArgumentException e) {
            return false;
        }

        return true;
    }
}
