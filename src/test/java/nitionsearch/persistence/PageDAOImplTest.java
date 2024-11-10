package nitionsearch.persistence;

import nitionsearch.model.Page;
import nitionsearch.persistence.PageDAOImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class PageDAOImplTest {

    private PageDAOImpl pageDAO;

    @BeforeEach
    void setUp() {
        pageDAO = new PageDAOImpl();
    }

    @Test
    void testSave() {
        // Create a new page
        Page page = new Page(UUID.randomUUID(), "https://example.com", "This is a test page content.");

        // Save the page
        pageDAO.save(page);

        // Retrieve the page by ID
        Optional<Page> savedPage = pageDAO.findById(page.getId());

        // Assert that the page is saved correctly
        assertTrue(savedPage.isPresent());
        assertEquals(page, savedPage.get());
    }

    @Test
    void testFindByUrl() {
        // Create and save a new page
        Page page = new Page(UUID.randomUUID(), "https://example.com", "This is a test page content.");
        pageDAO.save(page);

        // Retrieve the page by URL
        Optional<Page> foundPage = pageDAO.findByUrl("https://example.com");

        // Assert that the page is found correctly by URL
        assertTrue(foundPage.isPresent());
        assertEquals(page, foundPage.get());
    }

    @Test
    void testFindByUrl_notFound() {
        // Attempt to find a page by URL that doesn't exist
        Optional<Page> foundPage = pageDAO.findByUrl("https://nonexistent.com");

        // Assert that no page is found
        assertFalse(foundPage.isPresent());
    }

    @Test
    void testSearchByKeyword() {
        Page page1 = new Page(UUID.randomUUID(), "https://example.com", "This is a test page.");
        Page page2 = new Page(UUID.randomUUID(), "https://example2.com", "This page contains a test content.");
        pageDAO.save(page1);
        pageDAO.save(page2);

        // Search for pages containing the keyword "test"
        Collection<Page> searchResults = pageDAO.searchByKeyword("test");

        // Assert that both pages are returned
        assertEquals(2, searchResults.size());
        assertTrue(searchResults.contains(page1));
        assertTrue(searchResults.contains(page2));
    }

    @Test
    void testSearchByKeyword_noResults() {
        // Create and save a page
        Page page = new Page(UUID.randomUUID(), "https://example.com", "This is a test page.");
        pageDAO.save(page);

        // Search for pages that do not contain the keyword
        Collection<Page> searchResults = pageDAO.searchByKeyword("nonexistent");

        // Assert that no pages are returned
        assertTrue(searchResults.isEmpty());
    }

    @Test
    void testSave_duplicatePage() {
        // Create and save a page
        Page page1 = new Page(UUID.randomUUID(), "https://example.com", "This is a test page.");
        pageDAO.save(page1);

        // Create a duplicate page with the same URL
        Page page2 = new Page(UUID.randomUUID(), "https://example.com", "This is a different content.");

        // Save the duplicate page
        pageDAO.save(page2);

        // Verify that the second page is saved (overwriting the first page)
        Optional<Page> savedPage = pageDAO.findByUrl("https://example.com");
        assertTrue(savedPage.isPresent());
        assertEquals(page2, savedPage.get());
    }

    @Test
    void testFindById_notFound() {

        Optional<Page> foundPage = pageDAO.findById(UUID.randomUUID());
        assertFalse(foundPage.isPresent());
    }
}

