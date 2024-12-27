package nitionsearch.persistence;

import nitionsearch.model.Page;
import nitionsearch.persistence.dao.PageDAOImpl;
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

        Page page = new Page(UUID.randomUUID(), "https://example.com", "This is a test page content.");


        pageDAO.save(page);


        Optional<Page> savedPage = pageDAO.findById(page.getId());


        assertTrue(savedPage.isPresent());
        assertEquals(page, savedPage.get());
    }

    @Test
    void testFindByUrl() {

        Page page = new Page(UUID.randomUUID(), "https://example.com", "This is a test page content.");
        pageDAO.save(page);


        Optional<Page> foundPage = pageDAO.findByUrl("https://example.com");


        assertTrue(foundPage.isPresent());
        assertEquals(page, foundPage.get());
    }

    @Test
    void testFindByUrl_notFound() {

        Optional<Page> foundPage = pageDAO.findByUrl("https://nonexistent.com");


        assertFalse(foundPage.isPresent());
    }

    @Test
    void testSearchByKeyword() {
        Page page1 = new Page(UUID.randomUUID(), "https://example.com", "This is a test page amos.");
        Page page2 = new Page(UUID.randomUUID(), "https://example2.com", "This page contains a test amos amo content.");
        pageDAO.save(page1);
        pageDAO.save(page2);


        Collection<Page> searchResults = pageDAO.searchByKeyword("amos");


        assertEquals(2, searchResults.size());
        assertTrue(searchResults.contains(page1));
        assertTrue(searchResults.contains(page2));
    }

    @Test
    void testSearchByKeyword_noResults() {

        Page page = new Page(UUID.randomUUID(), "https://example.com", "This is a test page.");
        pageDAO.save(page);


        Collection<Page> searchResults = pageDAO.searchByKeyword("nonexistent");


        assertTrue(searchResults.isEmpty());
    }

    @Test
    void testSave_duplicatePage() {

        Page page1 = new Page(UUID.randomUUID(), "https://example.com", "This is a test page.");
        pageDAO.save(page1);


        Page page2 = new Page(UUID.randomUUID(), "https://example.com", "This is a different content.");


        pageDAO.save(page2);


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

