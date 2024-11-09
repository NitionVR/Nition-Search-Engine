package nitionsearch.search;

import java.util.*;

public class TrieNode {


    private Map<Character, TrieNode> children = new HashMap<>();
    private Map<UUID, List<Integer>> occurrences = new HashMap<>();


    public Map<Character, TrieNode> getChildren() {
        return children;
    }

    public void addOccurrence(UUID pageId, int position) {
        occurrences.computeIfAbsent(pageId, k -> new ArrayList<>()).add(position);
    }

    public Map<UUID, List<Integer>> getOccurrences() {
        return occurrences;
    }


}
