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

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        toStringHelper(root, new StringBuilder(), builder);
        return builder.toString();
    }

    private void toStringHelper(TrieNode node, StringBuilder currentWord, StringBuilder result) {
        if (node.getOccurrences() != null && !node.getOccurrences().isEmpty()) {
            result.append(currentWord).append(": ").append(node.getOccurrences()).append("\n");
        }

        for (Map.Entry<Character, TrieNode> entry : node.getChildren().entrySet()) {
            currentWord.append(Character.toLowerCase(entry.getKey()));
            toStringHelper(entry.getValue(), currentWord, result);
            currentWord.deleteCharAt(currentWord.length() - 1);
        }
    }

}
