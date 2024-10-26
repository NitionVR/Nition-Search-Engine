package nitionsearch.search;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class SuffixTrieTest {
    private SuffixTrie trie;

    @BeforeEach
    public void setup(){
        trie = new SuffixTrie();
    }

    @Test
    public void testInsertAndSearchSingleWord(){
        trie.insert("tree", 10);
        List<Integer> positions = trie.search("tree");

        assertEquals(1, positions.size());
        assertEquals(10, positions.get(0));
    }

}
