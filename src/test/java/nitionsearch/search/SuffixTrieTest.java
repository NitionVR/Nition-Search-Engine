package nitionsearch.search;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


import java.util.Map;

public class SuffixTrieTest {

    private SuffixTrie trie;

    @BeforeEach
    public void setup(){
        trie = new SuffixTrie();
    }

    @Test
    public void testInsertAndSearchSingleWord(){
        trie.insert("tree", 10, 0);
        Map<Integer, List<Integer>> positions = trie.search("tree");
        assertEquals(1, positions.size());
        assertEquals(1, positions.get(10).size());
        assertEquals(0, positions.get(10).get(0).intValue());
    }

    @Test
    public void testSearchNonExistentWord(){
        trie.insert("banana", 5, 0);
        Map<Integer, List<Integer>> positions = trie.search("apple");
        assertTrue(positions.isEmpty());
    }

    @Test
    public void testInsertMultiplePositionsForSameWord(){
        trie.insert("lemon", 10, 0);
        trie.insert("lemon", 20, 0);
        Map<Integer, List<Integer>> positions = trie.search("lemon");
        assertEquals(2, positions.size());
        assertTrue(positions.containsKey(10));
        assertTrue(positions.containsKey(20));
        assertEquals(1, positions.get(10).size());
        assertEquals(0, positions.get(10).get(0).intValue());
        assertEquals(1, positions.get(20).size());
        assertEquals(0, positions.get(20).get(0).intValue());
    }

    @Test
    public void testInsertAndSearchMultipleWords(){
        trie.insert("tree", 10, 0);
        trie.insert("leaf", 20, 0);
        Map<Integer, List<Integer>> treePositions = trie.search("tree");
        Map<Integer, List<Integer>> leafPositions = trie.search("leaf");
        assertEquals(1, treePositions.size());
        assertEquals(1, treePositions.get(10).size());
        assertEquals(0, treePositions.get(10).get(0).intValue());
        assertEquals(1, leafPositions.size());
        assertEquals(1, leafPositions.get(20).size());
        assertEquals(0, leafPositions.get(20).get(0).intValue());
    }

    @Test
    public void testSearchEmptyString(){
        trie.insert("tree", 10, 0);
        Map<Integer, List<Integer>> positions = trie.search("");
        assertTrue(positions.isEmpty());
    }

    @Test
    public void testInsertAndSearchLongWord(){
        String longWord = "ahfdhaiihidhaijfidufsjhyfiwhfwjheahfaiyaewhejifhwe";
        trie.insert(longWord, 10, 0);
        Map<Integer, List<Integer>> positions = trie.search(longWord);
        assertEquals(1, positions.size());
        assertEquals(1, positions.get(10).size());
        assertEquals(0, positions.get(10).get(0).intValue());
    }

    @Test
    public void testInsertAndSearchOverlappingWords(){
        trie.insert("treehouse", 10, 0);
        trie.insert("tree", 10, 0);
        Map<Integer, List<Integer>> treehousePositions = trie.search("treehouse");
        Map<Integer, List<Integer>> treePositions = trie.search("tree");
        assertEquals(1, treehousePositions.size());
        assertEquals(1, treehousePositions.get(10).size());
        assertEquals(0, treehousePositions.get(10).get(0).intValue());
        assertEquals(1, treePositions.size());
        assertEquals(1, treePositions.get(10).size());
        assertEquals(0, treePositions.get(10).get(0).intValue());
    }

    @Test
    public void testInsertAndSearchSingleCharacter(){
        trie.insert("a", 10, 0);
        Map<Integer, List<Integer>> positions = trie.search("a");
        assertEquals(1, positions.size());
        assertEquals(1, positions.get(10).size());
        assertEquals(0, positions.get(10).get(0).intValue());
    }

    @Test
    public void testInsertNullWord(){
        assertThrows(NullPointerException.class,() -> trie.insert(null, 10, 0));
    }

    @Test
    public void testSearchNullWord(){
        assertThrows(NullPointerException.class, () -> trie.search(null));
    }
}
