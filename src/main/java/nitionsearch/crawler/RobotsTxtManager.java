package nitionsearch.crawler;

import org.jsoup.Jsoup;

import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class RobotsTxtManager {
    private final Map<String, RobotsTxtRules> rulesCache;
    private final CrawlerConfig config;

    public RobotsTxtManager(CrawlerConfig config) {
        this.rulesCache = new ConcurrentHashMap<>();
        this.config = config;
    }

    public boolean isAllowed(String url) {
        try {
            URL parsedUrl = new URL(url);
            String domain = parsedUrl.getHost();
            String path = parsedUrl.getPath();

            RobotsTxtRules rules = rulesCache.computeIfAbsent(domain,
                    this::fetchRobotsTxtRules);

            return rules.isAllowed(path, config.getUserAgent());
        } catch (Exception e) {
            // If we can't parse the URL or fetch robots.txt, assume it's allowed
            return true;
        }
    }

    private RobotsTxtRules fetchRobotsTxtRules(String domain) {
        try {
            String robotsUrl = String.format("https://%s/robots.txt", domain);
            String content = Jsoup.connect(robotsUrl)
                    .userAgent(config.getUserAgent())
                    .timeout(config.getConnectionTimeout())
                    .execute()
                    .body();
            return RobotsTxtRules.parse(content);
        } catch (Exception e) {
            // If we can't fetch robots.txt, return permissive rules
            return new RobotsTxtRules();
        }
    }
}





