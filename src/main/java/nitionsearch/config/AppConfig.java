package nitionsearch.config;

import nitionsearch.persistence.dao.PageDAO;
import nitionsearch.persistence.dao.PageDAOImpl;
import nitionsearch.search.SearchEngine;
import nitionsearch.crawler.WebCrawler;
import nitionsearch.crawler.CrawlerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(CrawlerProperties.class)
public class AppConfig {

    @Bean
    public SearchEngine searchEngine() {
        return new SearchEngine();
    }

    @Bean
    public CrawlerConfig crawlerConfig(CrawlerProperties properties) {
        return new CrawlerConfig.Builder()
                .maxDepth(10)  // Override property for deeper crawling
                .threadCount(4)
                .crawlDelay(Duration.ofMillis(1000))
                .maxPagesPerDomain(5000)  // Allow more pages per domain
                .userAgent("Mozilla/5.0 (compatible; NitionBot/1.0; +http://localhost)")
                .connectionTimeout(10000)  // Longer timeout
                .maxRetries(3)            // Add retries
                .build();
    }

    @Bean
    public WebCrawler webCrawler(SearchEngine searchEngine, CrawlerConfig crawlerConfig) {
        return new WebCrawler(searchEngine, crawlerConfig);
    }

    @Bean
    public PageDAO pageDAO() {
        return new PageDAOImpl();
    }
}

