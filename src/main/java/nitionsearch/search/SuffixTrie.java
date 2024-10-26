package nitionsearch.search;


import java.util.ArrayList;
import java.util.List;

public class SuffixTrie {
    private final TrieNode root = new TrieNode();

    public void insert(String word, int position){
        TrieNode node = root;

        for (char c:word.toCharArray()){
            node = node.children.computeIfAbsent(c, k -> new TrieNode());
        }
        node.positions.add(position);
    }

    public List<Integer> search(String word){
        TrieNode node = root;

        for (char c:word.toCharArray()){
            node = node.children.get(c);
            if (node == null)
                return new ArrayList<>();
        }
        return node.positions;

    }
}
