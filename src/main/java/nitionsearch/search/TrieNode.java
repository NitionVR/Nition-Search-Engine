package nitionsearch.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrieNode {


    private Map<Character, TrieNode> children = new HashMap<>();
    private Map<Integer, List<Integer>> occurrences = new HashMap<>();


    public Map<Character, TrieNode> getChildren() {
        return children;
    }

    public void addOccurrence(int pageId, int position) {
        occurrences.computeIfAbsent(pageId, k -> new ArrayList<>()).add(position);
    }

    public Map<Integer, List<Integer>> getOccurrences() {
        return occurrences;
    }


}
