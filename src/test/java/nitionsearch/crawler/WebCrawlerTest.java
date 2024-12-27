//package nitionsearch.crawler;
//
//import nitionsearch.search.SearchEngine;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import static org.junit.jupiter.api.Assertions.*;
//
//public class WebCrawlerTest {
//
//    private WebCrawler crawler;
//    private SearchEngine searchEngine;
//
//    @BeforeEach
//    public void setUp() {
//        searchEngine = new SearchEngine();
//        crawler = new WebCrawler(searchEngine);
//    }
//
//    @Test
//    public void testCrawlAndIndexPage() {
//        crawler.crawlAndIndex("http://example.com", "sample content for testing");
//        assertEquals(1, searchEngine.search("content").size());
//    }
//
//    @Test
//    public void testCrawlAndIndexMultiplePages() {
//        crawler.crawlAndIndex("http://example.com", "sample content for testing");
//        crawler.crawlAndIndex("http://anotherexample.com", "another sample content");
//        assertEquals(2, searchEngine.search("content").size());
//    }
//
//    @Test
//    public void testCrawlAndIndexEmptyPage() {
//        crawler.crawlAndIndex("http://example.com", "");
//        assertEquals(0, searchEngine.search("content").size());
//    }
//
//    @Test
//    public void testCrawlAndIndexNullPage() {
//        assertThrows(NullPointerException.class, () -> crawler.crawlAndIndex("http://example.com", null));
//    }
//
//    @Test
//    public void testCrawlAndIndexInvalidURL() {
//        assertThrows(IllegalArgumentException.class, () -> crawler.crawlAndIndex("invalid url", "sample content"));
//    }
//
//    @Test
//    public void testCrawlAndIndexDuplicatePages() {
//        crawler.crawlAndIndex("http://example.com", "sample content for testing");
//        crawler.crawlAndIndex("http://example.com", "sample content for testing");
//        assertEquals(1, searchEngine.search("content").size());  // No duplication of content
//    }
//
//    @Test
//    public void testCrawlAndIndexMultipleTerms() {
//        crawler.crawlAndIndex("http://example.com", "sample content for testing with multiple terms");
//        assertEquals(1, searchEngine.search("content").size());
//        assertEquals(1, searchEngine.search("testing").size());
//        assertEquals(1, searchEngine.search("multiple").size());
//    }
//}
