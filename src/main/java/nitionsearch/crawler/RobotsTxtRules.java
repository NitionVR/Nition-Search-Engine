package nitionsearch.crawler;

import java.util.*;

public class RobotsTxtRules {
    private final Map<String, Set<String>> disallowedPaths;
    private final Map<String, Integer> crawlDelays;

    public RobotsTxtRules() {
        this.disallowedPaths = new HashMap<>();
        this.crawlDelays = new HashMap<>();
    }

    public static RobotsTxtRules parse(String content) {
        RobotsTxtRules rules = new RobotsTxtRules();
        String currentUserAgent = null;

        Scanner scanner = new Scanner(content);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim().toLowerCase();
            if (line.isEmpty() || line.startsWith("#")) continue;

            if (line.startsWith("user-agent:")) {
                currentUserAgent = line.substring(11).trim();
            } else if (currentUserAgent != null) {
                if (line.startsWith("disallow:")) {
                    String path = line.substring(9).trim();
                    rules.disallowedPaths
                            .computeIfAbsent(currentUserAgent, k -> new HashSet<>())
                            .add(path);
                } else if (line.startsWith("crawl-delay:")) {
                    try {
                        int delay = Integer.parseInt(line.substring(12).trim());
                        rules.crawlDelays.put(currentUserAgent, delay);
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
        return rules;
    }

    public boolean isAllowed(String path, String userAgent) {
        Set<String> paths = disallowedPaths.get(userAgent);
        if (paths == null) {
            paths = disallowedPaths.get("*");
        }
        if (paths == null) return true;

        return paths.stream().noneMatch(path::startsWith);
    }

    public int getCrawlDelay(String userAgent) {
        return crawlDelays.getOrDefault(userAgent,
                crawlDelays.getOrDefault("*", 0));
    }
}