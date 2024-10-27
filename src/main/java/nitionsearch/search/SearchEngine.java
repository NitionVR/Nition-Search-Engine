package nitionsearch.search;

import nitionsearch.model.Page;
import nitionsearch.model.TermOccurrence;

import java.util.*;
import java.util.stream.Collectors;

public class SearchEngine {
    private static final int MAX_TERM_DISTANCE = 30;
    private final List<Page> pages;
    private final SuffixTrie suffixTrie;

    public SearchEngine() {
        pages = new ArrayList<>();
        suffixTrie = new SuffixTrie();
    }

    public void addPage(Page page) {
        pages.add(page);
        String content = page.getContent().toLowerCase();
        String[] words = content.split("\\s+");

        for (int position = 0; position < words.length; position++) {
            suffixTrie.insert(words[position], page.getId(), position);
        }
    }

    public List<Page> search(String query) {
        String[] terms = query.split(" ");
        Map<Page, Integer> scores = new HashMap<>();

        if (terms.length == 1) {
            // Handle single-term search
            for (Page page : pages) {
                List<TermOccurrence> occurrences = getTermOccurrences(page, terms);
                int score = calculateProximityScore(occurrences, terms);
                scores.put(page, score);
            }
        } else {
            // Handle multi-term search
            for (Page page : pages) {
                Set<String> pageTerms = getPageTerms(page, terms);
                int matchingTerms = (int) pageTerms.stream()
                        .filter(Arrays.asList(terms)::contains)
                        .count();

                // Only consider pages with at least two matching terms
                if (matchingTerms >= 2) {
                    List<TermOccurrence> occurrences = getTermOccurrences(page, terms);
                    int score = calculateProximityScore(occurrences, terms);

                    // Add bonus for exact phrase match
                    if (isExactPhraseMatch(page, terms)) {
                        score += 100;
                    }

                    scores.put(page, score);
                }
            }
        }

        List<Page> results = new ArrayList<>(scores.keySet());
        results.sort((p1, p2) -> scores.get(p2) - scores.get(p1));

        // Filter out pages with no matching terms
        results = results.stream()
                .filter(page -> scores.get(page) > 0)
                .collect(Collectors.toList());

        return results;
    }


    private Set<String> getPageTerms(Page page, String[] terms) {
        String content = page.getContent().toLowerCase();
        String[] words = content.split("\\s+");
        return Arrays.stream(words).collect(Collectors.toSet());
    }



    private boolean isExactPhraseMatch(Page page, String[] terms) {
        String content = page.getContent().toLowerCase();
        String phrase = String.join(" ", terms).toLowerCase();

        return content.contains(phrase);
    }

    private List<TermOccurrence> getTermOccurrences(Page page, String[] terms) {
        List<TermOccurrence> occurrences = new ArrayList<>();

        for (String term : terms) {
            Map<Integer, List<Integer>> termOccurrences = suffixTrie.search(term.toLowerCase());
            for (Map.Entry<Integer, List<Integer>> entry : termOccurrences.entrySet()) {
                int pageId = entry.getKey();
                for (int position : entry.getValue()) {
                    occurrences.add(new TermOccurrence(pageId, term, position));
                }
            }
        }

        return occurrences;
    }

    private int calculateProximityScore(List<TermOccurrence> occurrences, String[] terms) {
        int score = 0;
        int[] lastPosition = new int[terms.length];
        Arrays.fill(lastPosition, -1);
        occurrences.sort(Comparator.comparingInt(o -> o.getPosition()));

        int matchingTerms = 0;
        for (TermOccurrence occurrence : occurrences) {
            String term = occurrence.getTerm();
            if (Arrays.asList(terms).contains(term)) {
                matchingTerms++;
            }
        }

        score += matchingTerms * 50;

        for (TermOccurrence occurrence : occurrences) {
            int position = occurrence.getPosition();
            String term = occurrence.getTerm();
            for (int i = 0; i < terms.length; i++) {
                if (terms[i].equalsIgnoreCase(term)) {
                    lastPosition[i] = position;
                    for (int j = 0; j < lastPosition.length; j++) {
                        if (j != i && lastPosition[j] != -1) {
                            int distance = Math.abs(lastPosition[i] - lastPosition[j]);
                            if (distance == 1) {
                                score += 50;
                            } else if (distance <= MAX_TERM_DISTANCE) {
                                score += (MAX_TERM_DISTANCE - distance) / 2;
                            }
                        }
                    }
                }
            }
        }

        return score;
    }

}
