package nitionsearch.search;

import nitionsearch.model.Page;
import nitionsearch.model.TermOccurrence;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SearchEngine {
    private static final int MAX_TERM_DISTANCE = 30;
    private static final int PROXIMITY_SCORE_BONUS = 50;
    private final List<Page> pages;
    private final SuffixTrie suffixTrie;

    public SearchEngine() {
        pages = new ArrayList<>();
        suffixTrie = new SuffixTrie();
    }

    public void addPage(Page page) {
        if (canAddPage(page)) {
            pages.add(page);
            String content = page.getContent().toLowerCase();
            String[] words = content.split("\\s+");
            String url = page.getUrl();
            for (int position = 0; position < words.length; position++) {
                suffixTrie.insert(words[position], page.getId(), position);
            }
        }
    }

    public boolean canAddPage(Page page){
        return (!pages.stream().anyMatch(p -> p.getUrl().equals(page.getUrl()))
        && !page.getContent().trim().isEmpty());
    }

    public List<Page> search(String query) {
        String[] terms = query.split(" ");
        Map<Page, Integer> scores;

        if (terms.length == 1) {
            // Handle single-term search
            scores = calculateSingleTermScores(terms);
        } else {
            scores = calculateMultiTermScores(terms);
        }

       return rankPagesByScore(scores);
    }

    private Map<Page, Integer> calculateSingleTermScores (String [] searchTerms){
        Map<Page, Integer> scores = new HashMap<>();

        for (Page page: pages){
            List<TermOccurrence> occurrences = getTermOccurrences(page,searchTerms);
            int score = calculateProximityScore(occurrences,searchTerms);
            scores.put(page,score);
        }
        return scores;
    }

    private Map<Page, Integer> calculateMultiTermScores (String[] searchTerms) {
        Map<Page, Integer> scores = new HashMap<>();

        for (Page page : pages) {
            Set<String> pageTerms = getPageTerms(page, searchTerms);
            int matchingTerms = (int) pageTerms.stream()
                    .filter(Arrays.asList(searchTerms)::contains)
                    .count();

            // Only consider pages with at least two matching terms
            if (matchingTerms >= 2) {
                List<TermOccurrence> occurrences = getTermOccurrences(page, searchTerms);
                int score = calculateProximityScore(occurrences, searchTerms);

                // Add bonus for exact phrase match
                if (isExactPhraseMatch(page, searchTerms)) {
                    score += 100;
                }

                scores.put(page, score);
            }
        }
        return scores;
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
        System.out.println(suffixTrie);
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

    /**
     * Calculates proximity score for term occurrences.
     *
     * @param occurrences Term occurrences.
     * @param terms Search terms.
     * @return Proximity score.
     */
    private int calculateProximityScore(List<TermOccurrence> occurrences, String[] terms) {
        int score = calculateMatchingTermsScore(occurrences, terms);
        score += calculateProximityBonus(occurrences, terms);
        return score;
    }

    /**
     * Calculates proximity score for matching terms in the occurrences.
     * <p>
     * This method rewards occurrences that contain all search terms.
     * @param occurrences Term occurrences.
     * @param terms Search terms.
     * @return Proximity score.
     */
    private int calculateMatchingTermsScore(List<TermOccurrence> occurrences, String[] terms) {
        Map<String, Boolean> termMap = Arrays.stream(terms)
                .collect(Collectors.toMap(Function.identity(), term -> Boolean.TRUE));

        return (int) occurrences.stream()
                .filter(occurrence -> termMap.containsKey(occurrence.getTerm()))
                .count() * PROXIMITY_SCORE_BONUS;
    }

    /**
     * Calculates the proximity bonus score for term occurrences.
     * <p>
     * This method rewards occurrences where search terms appear close to each other.
     * @param occurrences Term occurrences.
     * @param terms Search terms.
     * @return Proximity score.
     */
    private int calculateProximityBonus(List<TermOccurrence> occurrences, String[] terms) {
        int[] termPositions = new int[terms.length];
        Arrays.fill(termPositions, -1);
        int score = 0;

        occurrences.sort(Comparator.comparingInt(TermOccurrence::getPosition));

        for (TermOccurrence occurrence : occurrences) {
            int position = occurrence.getPosition();
            String term = occurrence.getTerm();

            for (int i = 0; i < terms.length; i++) {
                if (terms[i].equalsIgnoreCase(term)) {
                    termPositions[i] = position;
                    score += calculateTermDistanceScore(termPositions, MAX_TERM_DISTANCE);
                }
            }
        }
        return score;
    }

    /**
     * Calculates score for term distance score.
     * <p>
     * This method rewards smaller distances between search terms.
     * @param termPositions Term occurrences.
     * @param maxDistance Search terms.
     * @return Proximity score.
     */
    private int calculateTermDistanceScore(int[] termPositions, int maxDistance) {
        int score = 0;
        // iterate over the term pairs to calculate score based on distance
        for (int i = 0; i < termPositions.length; i++) {
            for (int j = 0; j < termPositions.length; j++) {
                if (i != j && termPositions[j] != -1) {
                    int distance = Math.abs(termPositions[i] - termPositions[j]);
                    // reward adjacent terms with maximum bonus :)
                    if (distance == 1) {
                        score += PROXIMITY_SCORE_BONUS;
                    }
                    // decrease score for larger distances
                    else if (distance <= maxDistance) {
                        score += (maxDistance - distance) / 2;
                    }
                }
            }
        }
        return score;
    }

    /**
     * Ranks web pages based on their search scores.
     * <p>
     * This method sorts pages in descending order of their scores and
     * filters out pages with no matching terms.
     * @param scores Map of pages to their corresponding search scores.
     * @return List of ranked pages.
     */
    private List<Page> rankPagesByScore(Map<Page, Integer> scores){
        List<Page> results = new ArrayList<>(scores.keySet());

        // prioritize page relevance by sorting scores in descending order
        results.sort((p1, p2) -> scores.get(p2) - scores.get(p1));

        // remove pages with no matching terms from the result
        results = results.stream()
                .filter(page -> scores.get(page) > 0)
                .collect(Collectors.toList());

        return results;
    }

}
