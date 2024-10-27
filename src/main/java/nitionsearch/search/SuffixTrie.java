package nitionsearch.search;


import java.util.*;

public class SuffixTrie {
    private TrieNode root = new TrieNode();

    public void insert(String word, int pageId, int position) {
        TrieNode currentNode = root;
        for (char letter : word.toCharArray()) {
            currentNode = currentNode.getChildren().computeIfAbsent(letter, c -> new TrieNode());
        }
        currentNode.addOccurrence(pageId, position);
    }

    public Map<Integer, List<Integer>> search(String term) {
        TrieNode currentNode = root;
        for (char letter : term.toCharArray()) {
            currentNode = currentNode.getChildren().get(letter);
            if (currentNode == null) {
                return Collections.emptyMap();
            }
        }
        return currentNode.getOccurrences();
    }

}
