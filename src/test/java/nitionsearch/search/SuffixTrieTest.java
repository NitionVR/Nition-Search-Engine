package nitionsearch.search;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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

    @Test
    public void testSearchNonExistentWord(){
        trie.insert("banana",5);
        List<Integer> positions = trie.search("apple");
        assertTrue(positions.isEmpty());
    }

    @Test
    public void testInsertMultiplePositionsForSameWord(){
        trie.insert("lemon",10);
        trie.insert("lemon", 20);

        List<Integer> positions = trie.search("lemon");
        assertEquals(2, positions.size());
        assertTrue(positions.contains(10));
        assertTrue(positions.contains(20));
    }

    @Test
    public void testInsertAndSearchMultipleWords(){
        trie.insert("tree",10);
        trie.insert("leaf",20);
        List<Integer> treePositions = trie.search("tree");
        List<Integer> leafPositions = trie.search("leaf");
        assertEquals(1, treePositions.size());
        assertEquals(10,treePositions.get(0));
        assertEquals(1,leafPositions.size());
        assertEquals(20, leafPositions.get(0));
    }

    @Test
    public void testInsertAndSearchEmptyString(){
        trie.insert("",10);
        List<Integer> positions = trie.search("");
        assertEquals(1, positions.size());
        assertEquals(10, positions.get(0));
    }

    @Test
    public void testSearchEmptyString(){
        trie.insert("tree",10);
        List<Integer> positions = trie.search("");
        assertTrue(positions.isEmpty());
    }

    @Test
    public void testInsertAndSearchLongWord(){
        String longWord = "ahfdhaiihidhaijfidufsjhyfiwhfwjheahfaiyaewhejifhwe";
        trie.insert(longWord,10);
        List<Integer> positions = trie.search(longWord);
        assertEquals(1, positions.size());
        assertEquals(10,positions.get(0));
    }

    @Test
    public void testInsertAndSearchOverlappingWords(){
        trie.insert("treehouse",10);
        trie.insert("tree",10);
        List<Integer> treehousePositions = trie.search("treehouse");
        List<Integer> treePositions = trie.search("tree");
        assertEquals(1, treehousePositions.size());
        assertEquals(10, treehousePositions.getFirst());
        assertEquals(1, treePositions.size());
        assertEquals(10, treePositions.getFirst());
    }

    @Test
    public void testInsertAndSearchSingleCharacter(){
        trie.insert("a",10);
        List<Integer> positions = trie.search("a");
        assertEquals(1, positions.size());
        assertEquals(10, positions.getFirst());
    }

    @Test
    public void testInsertNullWord(){
        assertThrows(NullPointerException.class,() -> trie.insert(null,10));
    }

    @Test
    public void testSearchNullWord(){
        assertThrows(NullPointerException.class, () -> trie.search(null));
    }



}
