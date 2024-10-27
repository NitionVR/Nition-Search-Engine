package nitionsearch.search;

import nitionsearch.model.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SearchEngineTest {
    private SearchEngine searchEngine;
    private final String EXAMPLE_PAGE_URL="http://example.com";
    private final String ANOTHER_EXAMPLE_PAGE_URL = "http://anotherexample.com";

    @BeforeEach
    public void setUp() {
        searchEngine = new SearchEngine();
    }

    @Test
    public void testSingleTermSearch() {
        searchEngine.addPage(new Page(1,EXAMPLE_PAGE_URL, "Nition search Engine Project"));
        List<Page> results = searchEngine.search("search");

        assertEquals(1, results.size(), "Expected 1 result for single term search");
        assertEquals(1, results.get(0).getId(), "Expected page ID 1 for search term 'search'");
    }

    @Test
    public void testMultiTermSearchWithProximityScores() {
        searchEngine.addPage(new Page(1, EXAMPLE_PAGE_URL,"apple bad banana apple"));
        searchEngine.addPage(new Page(2,ANOTHER_EXAMPLE_PAGE_URL, "apple banana orange bad apple"));

        List<Page> results = searchEngine.search("bad apple");

        assertEquals(2, results.size(), "Expected 2 results for the query 'bad apple'");
        assertEquals(2, results.get(0).getId(), "Expected page ID 2 as the highest scoring result");
        assertEquals(1, results.get(1).getId(), "Expected page ID 1 as the second result");
    }

    @Test
    public void testNoResults() {
        searchEngine.addPage(new Page(1,EXAMPLE_PAGE_URL, "apple banana orange"));
        List<Page> results = searchEngine.search("grape");

        assertEquals(0, results.size(), "Expected no results for the query 'grape'");
    }

    @Test
    public void testMultipleOccurrencesOfTerms() {
        searchEngine.addPage(new Page(1,EXAMPLE_PAGE_URL, "apple apple banana apple"));
        searchEngine.addPage(new Page(2,ANOTHER_EXAMPLE_PAGE_URL, "apple banana"));

        List<Page> results = searchEngine.search("apple");
        System.out.println(results);
        assertEquals(2, results.size(), "Expected 2 results for the query 'apple'");
        assertEquals(1, results.get(0).getId(), "Expected page ID 1 as the highest scoring result");
    }

    @Test
    public void testDifferentWordOrders() {
        searchEngine.addPage(new Page(1,EXAMPLE_PAGE_URL, "bad apple is tasty"));
        searchEngine.addPage(new Page(2,ANOTHER_EXAMPLE_PAGE_URL, "apple is bad but good"));

        List<Page> results = searchEngine.search("apple bad");

        assertEquals(2, results.size(), "Expected 2 results for the query 'apple bad'");
    }

    @Test
    public void testExactPhraseSearch() {
        searchEngine.addPage(new Page(1,EXAMPLE_PAGE_URL,"bad apple pie"));
        searchEngine.addPage(new Page(2,ANOTHER_EXAMPLE_PAGE_URL, "apple pie is bad"));

        List<Page> results = searchEngine.search("bad apple pie");

        assertEquals(2, results.size(), "Expected 1 result for the exact phrase 'bad apple pie'");
        assertEquals(1, results.get(0).getId(), "Expected page ID 1 for exact phrase match");
    }

    @Test
    public void testIrrelevantTerms() {
        searchEngine.addPage(new Page(1,EXAMPLE_PAGE_URL, "apple bad banana"));
        searchEngine.addPage(new Page(2,ANOTHER_EXAMPLE_PAGE_URL, "orange pear grape"));

        List<Page> results = searchEngine.search("bad apple pear");
        System.out.println(results);
        assertEquals(1, results.size(), "Expected 1 result for the query 'bad apple pear'");
        assertEquals(1, results.get(0).getId(), "Expected page ID 1 as the only relevant result");
    }

    @Test
    public void testCaseSensitivity() {
        searchEngine.addPage(new Page(1,EXAMPLE_PAGE_URL, "Apple Bad Banana"));
        searchEngine.addPage(new Page(2,ANOTHER_EXAMPLE_PAGE_URL, "apple bad banana"));

        List<Page> results = searchEngine.search("apple");
        assertEquals(2, results.size(), "Expected 2 results for the query 'apple'");
    }

    @Test
    public void testSimilarContentRanking() {
        searchEngine.addPage(new Page(1,EXAMPLE_PAGE_URL, "apple banana apple"));
        searchEngine.addPage(new Page(2,ANOTHER_EXAMPLE_PAGE_URL, "apple banana apple pie"));

        List<Page> results = searchEngine.search("apple banana");

        assertEquals(2, results.size(), "Expected 2 results for the query 'apple banana'");
        assertEquals(1, results.get(0).getId(), "Expected page ID 1 as the higher scoring result");
        assertEquals(2, results.get(1).getId(), "Expected page ID 2 as the lower scoring result");
    }
}
