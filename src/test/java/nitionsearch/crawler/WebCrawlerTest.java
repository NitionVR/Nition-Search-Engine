package nitionsearch.crawler;

import nitionsearch.search.SearchEngine;
import nitionsearch.util.CrawlMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class WebCrawlerTest {

    private WebCrawler webCrawler;

    @Mock
    private SearchEngine mockSearchEngine;
    @Mock
    private CrawlerConfig mockCrawlerConfig;
    @Mock
    private URLFrontier mockURLFrontier;
    @Mock
    private RobotsTxtManager mockRobotsTxtManager;
    @Mock
    private CrawlMetrics mockCrawlMetrics;

    // We need to mock the ExecutorService that WebCrawler creates internally.
    // This is a bit tricky because WebCrawler creates it in its constructor.
    // For now, we'll test the public methods and assume the internal ExecutorService works.
    // A more advanced mocking technique might involve PowerMock or refactoring WebCrawler
    // to accept an ExecutorService in its constructor for testability.

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // Initialize mocks

        // Configure mockCrawlerConfig for basic behavior
        when(mockCrawlerConfig.getThreadCount()).thenReturn(1); // Use a single thread for simpler testing
        when(mockCrawlerConfig.getMaxDepth()).thenReturn(2);
        when(mockCrawlerConfig.getMaxRetries()).thenReturn(0); // No retries for simpler testing
        when(mockCrawlerConfig.getUserAgent()).thenReturn("TestBot");
        when(mockCrawlerConfig.getConnectionTimeout()).thenReturn(5000);

        // Mock the ExecutorService
        ExecutorService mockExecutorService = mock(ExecutorService.class);

        // Create a real WebCrawler instance with the mocked ExecutorService
        webCrawler = new WebCrawler(mockSearchEngine, mockCrawlerConfig, mockExecutorService);

        // Use reflection to set the other internal mocks. This is a workaround for testability.
        try {
            java.lang.reflect.Field frontierField = WebCrawler.class.getDeclaredField("frontier");
            frontierField.setAccessible(true);
            frontierField.set(webCrawler, mockURLFrontier);

            java.lang.reflect.Field robotsTxtManagerField = WebCrawler.class.getDeclaredField("robotsTxtManager");
            robotsTxtManagerField.setAccessible(true);
            robotsTxtManagerField.set(webCrawler, mockRobotsTxtManager);

            java.lang.reflect.Field metricsField = WebCrawler.class.getDeclaredField("metrics");
            metricsField.setAccessible(true);
            metricsField.set(webCrawler, mockCrawlMetrics);

        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Failed to set internal mocks via reflection: " + e.getMessage());
        }
    }

    @Test
    void testStartCrawlingInitializesStateAndSubmitsTask() throws InterruptedException {
        String seedUrl = "http://example.com";
        when(mockRobotsTxtManager.isAllowed(anyString())).thenReturn(true); // Allow all URLs for this test

        webCrawler.startCrawling(seedUrl);

        assertTrue(webCrawler.isRunning());
        verify(mockURLFrontier).clear();
        verify(mockCrawlMetrics).reset();
        verify(mockURLFrontier).addURL(seedUrl, 0);

        // Verify that crawlTask was submitted to the mocked ExecutorService
        ExecutorService internalExecutorService = webCrawler.getExecutorService();
        verify(internalExecutorService, times(mockCrawlerConfig.getThreadCount())).submit(any(Runnable.class));
    }

    @Test
    void testStopCrawlingStopsCrawlerAndShutsDownExecutor() throws InterruptedException {
        // Start crawling first to set up the state
        webCrawler.startCrawling("http://example.com");
        assertTrue(webCrawler.isRunning());

        webCrawler.stopCrawling();

        assertFalse(webCrawler.isRunning());

        // Verify that the internal ExecutorService was shut down
        ExecutorService internalExecutorService = webCrawler.getExecutorService();
        verify(internalExecutorService).shutdown();
        verify(internalExecutorService).awaitTermination(anyLong(), any(TimeUnit.class));
    }
}