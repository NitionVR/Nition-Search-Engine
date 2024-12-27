package nitionsearch.controller;

import nitionsearch.model.Page;
import nitionsearch.persistence.dao.PageDAO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/test")
public class TestController {
    private final PageDAO pageDAO;

    public TestController(PageDAO pageDAO) {
        this.pageDAO = pageDAO;
    }

    @PostMapping("/page")
    public ResponseEntity<String> testDatabaseInsert() {
        try {
            Page testPage = new Page("http://test.com", "Test content");
            pageDAO.save(testPage);
            return ResponseEntity.ok("Page saved successfully with ID: " + testPage.getId());
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("Database error: " + e.getMessage());
        }
    }

    @GetMapping("/page/{url}")
    public ResponseEntity<?> testDatabaseQuery(@PathVariable String url) {
        try {
            Optional<Page> page = pageDAO.findByUrl(url);
            return page.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("Database error: " + e.getMessage());
        }
    }
}