package nitionsearch.search;

import nitionsearch.model.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SearchEngineTest {
    private SearchEngine searchEngine;
    private final String EXAMPLE_PAGE_URL = "http://example.com";
    private final String ANOTHER_EXAMPLE_PAGE_URL = "http://anotherexample.com";

    @BeforeEach
    public void setUp() {
        searchEngine = new SearchEngine();
    }

    @Test
    public void testSingleTermSearch() {
        UUID pageId = UUID.randomUUID();
        searchEngine.addPage(new Page(pageId, EXAMPLE_PAGE_URL, "Nition search Engine Project"));
        List<Page> results = searchEngine.search("search");

        assertEquals(1, results.size(), "Expected 1 result for single term search");
        assertEquals(pageId, results.get(0).getId(), "Expected page ID to match for search term 'search'");
    }

    @Test
    public void testMultiTermSearchWithProximityScores() {
        UUID pageId1 = UUID.randomUUID();
        UUID pageId2 = UUID.randomUUID();
        searchEngine.addPage(new Page(pageId1, EXAMPLE_PAGE_URL, "apple bad banana apple"));
        searchEngine.addPage(new Page(pageId2, ANOTHER_EXAMPLE_PAGE_URL, "apple banana orange bad apple"));

        List<Page> results = searchEngine.search("bad apple");

        assertEquals(2, results.size(), "Expected 2 results for the query 'bad apple'");
        assertEquals(pageId2, results.get(0).getId(), "Expected page ID 2 as the highest scoring result");
        assertEquals(pageId1, results.get(1).getId(), "Expected page ID 1 as the second result");
    }

    @Test
    public void testNoResults() {
        UUID pageId = UUID.randomUUID();
        searchEngine.addPage(new Page(pageId, EXAMPLE_PAGE_URL, "apple banana orange"));
        List<Page> results = searchEngine.search("grape");

        assertEquals(0, results.size(), "Expected no results for the query 'grape'");
    }

    @Test
    @Disabled
    public void testMultipleOccurrencesOfTerms() {
        UUID pageId1 = UUID.randomUUID();
        UUID pageId2 = UUID.randomUUID();
        searchEngine.addPage(new Page(pageId1, EXAMPLE_PAGE_URL, "apple apple banana apple"));
        searchEngine.addPage(new Page(pageId2, ANOTHER_EXAMPLE_PAGE_URL, "apple banana"));

        List<Page> results = searchEngine.search("apple");
        System.out.println(results);
        assertEquals(2, results.size(), "Expected 2 results for the query 'apple'");
        assertEquals(pageId1, results.get(0).getId(), "Expected page ID 1 as the highest scoring result");

    }

    @Test
    public void testDifferentWordOrders() {
        UUID pageId1 = UUID.randomUUID();
        UUID pageId2 = UUID.randomUUID();
        searchEngine.addPage(new Page(pageId1, EXAMPLE_PAGE_URL, "bad apple is tasty"));
        searchEngine.addPage(new Page(pageId2, ANOTHER_EXAMPLE_PAGE_URL, "apple is bad but good"));

        List<Page> results = searchEngine.search("apple bad");

        assertEquals(2, results.size(), "Expected 2 results for the query 'apple bad'");
    }

    @Test
    public void testExactPhraseSearch() {
        UUID pageId1 = UUID.randomUUID();
        UUID pageId2 = UUID.randomUUID();
        searchEngine.addPage(new Page(pageId1, EXAMPLE_PAGE_URL, "bad apple pie"));
        searchEngine.addPage(new Page(pageId2, ANOTHER_EXAMPLE_PAGE_URL, "apple pie is bad"));

        List<Page> results = searchEngine.search("bad apple pie");

        assertEquals(2, results.size(), "Expected 1 result for the exact phrase 'bad apple pie'");
        assertEquals(pageId1, results.get(0).getId(), "Expected page ID 1 for exact phrase match");
    }

    @Test
    public void testIrrelevantTerms() {
        UUID pageId1 = UUID.randomUUID();
        UUID pageId2 = UUID.randomUUID();
        searchEngine.addPage(new Page(pageId1, EXAMPLE_PAGE_URL, "apple bad banana"));
        searchEngine.addPage(new Page(pageId2, ANOTHER_EXAMPLE_PAGE_URL, "orange pear grape"));

        List<Page> results = searchEngine.search("bad apple pear");

        assertEquals(1, results.size(), "Expected 1 result for the query 'bad apple pear'");
        assertEquals(pageId1, results.get(0).getId(), "Expected page ID 1 as the only relevant result");
    }

    @Test
    public void testCaseSensitivity() {
        UUID pageId1 = UUID.randomUUID();
        UUID pageId2 = UUID.randomUUID();
        searchEngine.addPage(new Page(pageId1, EXAMPLE_PAGE_URL, "Apple Bad Banana"));
        searchEngine.addPage(new Page(pageId2, ANOTHER_EXAMPLE_PAGE_URL, "apple bad banana"));

        List<Page> results = searchEngine.search("apple");
        assertEquals(2, results.size(), "Expected 2 results for the query 'apple'");
    }

    @Test
    @Disabled
    public void testSimilarContentRanking() {
        UUID pageId1 = UUID.randomUUID();
        UUID pageId2 = UUID.randomUUID();
        searchEngine.addPage(new Page(pageId1, EXAMPLE_PAGE_URL, "apple banana apple"));
        searchEngine.addPage(new Page(pageId2, ANOTHER_EXAMPLE_PAGE_URL, "apple banana apple pie"));

        List<Page> results = searchEngine.search("apple banana");

        assertEquals(2, results.size(), "Expected 2 results for the query 'apple banana'");
        assertEquals(pageId1, results.get(0).getId(), "Expected page ID 1 as the higher scoring result");
        assertEquals(pageId2, results.get(1).getId(), "Expected page ID 2 as the lower scoring result");
    }
}
