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
    private final SearchCache searchCache;

    public SearchEngine() {
        pages = new ArrayList<>();
        suffixTrie = new SuffixTrie();
        searchCache = new SearchCache(1000,3600000);
    }

    public void addPage(Page page) {
        if (canAddPage(page)) {
            pages.add(page);
            String content = page.getContent().toLowerCase();
            String[] words = content.split("\\s+");
            UUID pageId = page.getId();
            for (int position = 0; position < words.length; position++) {
                suffixTrie.insert(words[position], pageId, position);
            }
        }
    }

    public boolean canAddPage(Page page){
        return (!pages.stream().anyMatch(p -> p.getUrl().equals(page.getUrl()))
        && !page.getContent().trim().isEmpty());
    }

    public SearchResult search(String query, SearchOptions options) {
        // Try cache first
        Optional<List<Page>> cachedResults = searchCache.get(query);
        if (cachedResults.isPresent()) {
            return paginateResults(cachedResults.get(), options);
        }

        // Perform search
        String[] terms = preprocessQuery(query);
        Map<Page, Integer> scores = terms.length == 1 ?
                calculateSingleTermScores(terms) :
                calculateMultiTermScores(terms);

        List<Page> results = rankPagesByScore(scores);
        searchCache.put(query, results);

        return paginateResults(results, options);
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
        for (String term : terms) {
            Map<UUID, List<Integer>> termOccurrences = suffixTrie.search(term.toLowerCase());
            for (Map.Entry<UUID, List<Integer>> entry : termOccurrences.entrySet()) {
                UUID pageId = entry.getKey();
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

    protected int calculateProximityScore(List<TermOccurrence> occurrences, String[] terms) {
        int baseScore = calculateMatchingTermsScore(occurrences, terms);
        int proximityBonus = calculateProximityBonus(occurrences, terms);

        // Get the page from occurrences
        UUID pageId = occurrences.isEmpty() ? null : occurrences.get(0).getDocumentId();
        if (pageId == null) return 0;

        Optional<Page> page = pages.stream()
                .filter(p -> p.getId().equals(pageId))
                .findFirst();

        if (!page.isPresent()) return 0;

        // Normalize score based on document length
        double contentLength = page.get().getContent().length();
        double normalizedScore = (baseScore + proximityBonus) / Math.log(contentLength);

        // Add term frequency impact without losing proximity advantage
        double termFrequencyBonus = calculateTermFrequencyImpact(occurrences, terms);

        return (int)(normalizedScore * (1 + termFrequencyBonus));
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


    private String[] preprocessQuery(String query) {
        // Handle boolean operators
        if (query.contains(" AND ")) {
            return handleAndOperator(query);
        } else if (query.contains(" OR ")) {
            return handleOrOperator(query);
        } else if (query.contains(" NOT ")) {
            return handleNotOperator(query);
        }
        return query.toLowerCase().split("\\s+");
    }

    private SearchResult paginateResults(List<Page> results, SearchOptions options) {
        int start = (options.getPage() - 1) * options.getPageSize();
        int end = Math.min(start + options.getPageSize(), results.size());

        if (start >= results.size()) {
            return new SearchResult(Collections.emptyList(), 0, 0);
        }

        return new SearchResult(
                results.subList(start, end),
                results.size(),
                (int) Math.ceil((double) results.size() / options.getPageSize())
        );
    }



    private double calculateTermFrequencyImpact(List<TermOccurrence> occurrences, String[] terms) {
        Map<String, Integer> termFrequencies = new HashMap<>();
        for (TermOccurrence occurrence : occurrences) {
            termFrequencies.merge(occurrence.getTerm(), 1, Integer::sum);
        }

        double avgFrequency = termFrequencies.values().stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);

        return Math.log(1 + avgFrequency);
    }

    private String[] handleAndOperator(String query) {
        String[] parts = query.split(" AND ");
        Set<String> terms = new HashSet<>();
        for (String part : parts) {
            terms.addAll(Arrays.asList(part.trim().toLowerCase().split("\\s+")));
        }
        return terms.toArray(new String[0]);
    }

    private String[] handleOrOperator(String query) {
        String[] parts = query.split(" OR ");
        Set<String> terms = new HashSet<>();
        for (String part : parts) {
            terms.addAll(Arrays.asList(part.trim().toLowerCase().split("\\s+")));
        }
        return terms.toArray(new String[0]);
    }

    private String[] handleNotOperator(String query) {
        String[] parts = query.split(" NOT ");
        if (parts.length != 2) {
            return query.toLowerCase().split("\\s+");
        }
        // consider the first part and exclude pages containing the second part
        return parts[0].trim().toLowerCase().split("\\s+");
    }

    public int getIndexedPagesCount() {
        return pages.size();
    }
}
