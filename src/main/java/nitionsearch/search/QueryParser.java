package nitionsearch.search;

import java.util.ArrayList;
import java.util.List;

public class QueryParser {
    private static final String PHRASE_DELIMITER = "\"";
    private static final String AND_OPERATOR = "AND";
    private static final String OR_OPERATOR = "OR";
    private static final String NOT_OPERATOR = "NOT";

    public static class ParsedQuery {


        private final List<String> mustContain;     // AND terms
        private final List<String> shouldContain;   // OR terms
        private final List<String> mustNotContain;  // NOT terms
        private final List<String> exactPhrases;    // Quoted phrases

        public ParsedQuery() {
            this.mustContain = new ArrayList<>();
            this.shouldContain = new ArrayList<>();
            this.mustNotContain = new ArrayList<>();
            this.exactPhrases = new ArrayList<>();
        }

        // Getters and necessary methods
        public List<String> getMustContain() {
            return mustContain;
        }

        public List<String> getShouldContain() {
            return shouldContain;
        }

        public List<String> getMustNotContain() {
            return mustNotContain;
        }

        public List<String> getExactPhrases() {
            return exactPhrases;
        }
    }

    public ParsedQuery parse(String query) {
        ParsedQuery result = new ParsedQuery();

        // Handle quoted phrases first
        List<String> phrases = extractPhrases(query);
        result.exactPhrases.addAll(phrases);

        // Remove processed phrases from query
        String remainingQuery = removePhrases(query);

        // Process boolean operators
        String[] parts = remainingQuery.split("\\s+");
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].equalsIgnoreCase(AND_OPERATOR)) {
                if (i + 1 < parts.length) {
                    result.mustContain.add(parts[i + 1]);
                    i++;
                }
            } else if (parts[i].equalsIgnoreCase(OR_OPERATOR)) {
                if (i + 1 < parts.length) {
                    result.shouldContain.add(parts[i + 1]);
                    i++;
                }
            } else if (parts[i].equalsIgnoreCase(NOT_OPERATOR)) {
                if (i + 1 < parts.length) {
                    result.mustNotContain.add(parts[i + 1]);
                    i++;
                }
            } else {
                result.mustContain.add(parts[i]);
            }
        }

        return result;
    }

    private String removePhrases(String query) {
        StringBuilder result = new StringBuilder(query);
        int startIndex = -1;

        // Process the string from right to left to avoid index shifting
        for (int i = result.length() - 1; i >= 0; i--) {
            if (result.charAt(i) == '"') {
                if (startIndex == -1) {
                    startIndex = i;
                } else {
                    // Remove the phrase and the quotes
                    result.delete(i, startIndex + 1);
                    startIndex = -1;
                }
            }
        }

        // Clean up extra spaces
        return result.toString()
                .replaceAll("\\s+", " ")
                .trim();
    }

    private List<String> extractPhrases(String query) {
        List<String> phrases = new ArrayList<>();
        int start = -1;
        boolean inPhrase = false;

        for (int i = 0; i < query.length(); i++) {
            if (query.charAt(i) == '"') {
                if (!inPhrase) {
                    start = i + 1;
                    inPhrase = true;
                } else {
                    phrases.add(query.substring(start, i));
                    inPhrase = false;
                }
            }
        }

        return phrases;
    }
}