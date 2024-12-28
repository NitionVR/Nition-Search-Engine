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
    private final QueryParser queryParser;

    public SearchEngine() {
        pages = new ArrayList<>();
        suffixTrie = new SuffixTrie();
        searchCache = new SearchCache(1000,3600000);
        queryParser = new QueryParser();
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
        Optional<List<Page>> cachedResults = searchCache.get(query);
        QueryParser.ParsedQuery parsedQuery = queryParser.parse(query);

        List<Page> results;
        if (cachedResults.isPresent()) {
            results = cachedResults.get();
        } else {
            Map<Page, Integer> scores = calculateScores(parsedQuery);
            results = rankPagesByScore(scores);
            searchCache.put(query, results);
        }

        // Convert Pages to SearchResultItems with snippets and highlights
        List<SearchResultItem> resultItems = results.stream()
                .map(page -> createResultItem(page, parsedQuery))
                .collect(Collectors.toList());

        return paginateResults(resultItems, options);
    }

    private Map<Page, Integer> calculateScores(QueryParser.ParsedQuery parsedQuery) {
        Map<Page, Integer> scores = new HashMap<>();


        for (Page page : pages) {
            int score = 0;

            if (!parsedQuery.getMustContain().isEmpty()) {
                if (containsAllTerms(page, parsedQuery.getMustContain())) {
                    score += calculateTermsScore(page, parsedQuery.getMustContain());
                } else {
                    continue;
                }
            }


            if (!parsedQuery.getShouldContain().isEmpty()) {
                score += calculateTermsScore(page, parsedQuery.getShouldContain());
            }


            if (containsAnyTerm(page, parsedQuery.getMustNotContain())) {
                continue;
            }

            // Exact phrases
            for (String phrase : parsedQuery.getExactPhrases()) {
                if (containsExactPhrase(page, phrase)) {
                    score += calculatePhraseScore(page, phrase);
                }
            }

            if (score > 0) {
                scores.put(page, score);
            }
        }

        return scores;
    }

    private boolean containsAllTerms(Page page, List<String> terms) {
        String content = page.getContent().toLowerCase();
        return terms.stream().allMatch(term -> content.contains(term.toLowerCase()));
    }

    private boolean containsAnyTerm(Page page, List<String> terms) {
        String content = page.getContent().toLowerCase();
        return terms.stream().anyMatch(term -> content.contains(term.toLowerCase()));
    }

    private boolean containsExactPhrase(Page page, String phrase) {
        String content = page.getContent().toLowerCase();
        return content.contains(phrase.toLowerCase());
    }

    private int calculateTermsScore(Page page, List<String> terms) {
        List<TermOccurrence> occurrences = getTermOccurrences(page,
                terms.toArray(new String[0]));
        return calculateProximityScore(occurrences,
                terms.toArray(new String[0]));
    }

    private int calculatePhraseScore(Page page, String phrase) {
        // Give higher score for exact phrase matches
        String content = page.getContent().toLowerCase();
        String phraseLower = phrase.toLowerCase();

        // Count occurrences of the exact phrase
        int count = 0;
        int index = 0;
        while ((index = content.indexOf(phraseLower, index)) != -1) {
            count++;
            index += phraseLower.length();
        }

        return count * PROXIMITY_SCORE_BONUS * 2; // Double bonus for exact phrases
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

    private SearchResult paginateResults(List<SearchResultItem> results, SearchOptions options) {
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

    public int getIndexedPagesCount() {
        return pages.size();
    }


    private SearchResultItem createResultItem(Page page, QueryParser.ParsedQuery query) {
        String content = page.getContent();
        List<String> allTerms = new ArrayList<>();
        allTerms.addAll(query.getMustContain());
        allTerms.addAll(query.getShouldContain());
        allTerms.addAll(query.getExactPhrases());

        // Generate snippet
        String snippet = generateSnippet(content, allTerms);

        // Get term frequencies
        Map<String, Integer> frequencies = calculateTermFrequencies(content, allTerms);

        // Get highlights
        List<String> highlights = findBestMatches(content, allTerms);

        return new SearchResultItem(page, snippet, highlights, frequencies);
    }

    private String generateSnippet(String content, List<String> terms) {
        int snippetLength = 200;
        String[] sentences = content.split("[.!?]+");

        // Find best sentence containing most search terms
        int maxTerms = 0;
        String bestSentence = "";

        for (String sentence : sentences) {
            int termCount = 0;
            for (String term : terms) {
                if (sentence.toLowerCase().contains(term.toLowerCase())) {
                    termCount++;
                }
            }
            if (termCount > maxTerms) {
                maxTerms = termCount;
                bestSentence = sentence;
            }
        }

        // If no good sentence found, take first part of content
        if (bestSentence.isEmpty()) {
            bestSentence = content.substring(0, Math.min(content.length(), snippetLength));
        }

        // Trim snippet to reasonable length
        if (bestSentence.length() > snippetLength) {
            int startIndex = Math.max(0, bestSentence.indexOf(terms.get(0)) - 50);
            bestSentence = "..." + bestSentence.substring(startIndex,
                    Math.min(startIndex + snippetLength, bestSentence.length())) + "...";
        }

        return bestSentence.trim();
    }

    private Map<String, Integer> calculateTermFrequencies(String content, List<String> terms) {
        Map<String, Integer> frequencies = new HashMap<>();
        String contentLower = content.toLowerCase();

        for (String term : terms) {
            String termLower = term.toLowerCase();
            int count = 0;
            int index = 0;
            while ((index = contentLower.indexOf(termLower, index)) != -1) {
                count++;
                index += termLower.length();
            }
            frequencies.put(term, count);
        }

        return frequencies;
    }

    private List<String> findBestMatches(String content, List<String> terms) {
        List<String> matches = new ArrayList<>();
        String contentLower = content.toLowerCase();

        for (String term : terms) {
            String termLower = term.toLowerCase();
            int index = contentLower.indexOf(termLower);
            if (index != -1) {
                // Get surrounding context
                int start = Math.max(0, index - 20);
                int end = Math.min(content.length(), index + term.length() + 20);
                String match = content.substring(start, end);
                matches.add(match);
            }
        }

        return matches;
    }
}
