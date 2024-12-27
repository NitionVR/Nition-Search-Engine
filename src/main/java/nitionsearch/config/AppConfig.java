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
                .maxDepth(properties.getMaxDepth())
                .threadCount(properties.getThreadCount())
                .crawlDelay(Duration.ofMillis(properties.getCrawlDelayMillis()))
                .maxPagesPerDomain(properties.getMaxPagesPerDomain())
                .userAgent(properties.getUserAgent())
                .connectionTimeout(properties.getConnectionTimeout())
                .build();
    }

    @Bean
    public WebCrawler webCrawler(SearchEngine searchEngine, CrawlerConfig crawlerConfig) {
        return new WebCrawler(searchEngine, crawlerConfig);
    }

    @Bean
    public PageDAO pageDAO(){
        return new PageDAOImpl();
    }
}

