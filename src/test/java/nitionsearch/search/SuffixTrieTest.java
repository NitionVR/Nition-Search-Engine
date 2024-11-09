package nitionsearch.search;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class SuffixTrieTest {

    private SuffixTrie trie;

    @BeforeEach
    public void setup() {
        trie = new SuffixTrie();
    }

    @Test
    public void testInsertAndSearchSingleWord() {
        UUID pageId = UUID.randomUUID(); // Create a UUID for the pageId
        trie.insert("tree", pageId, 0);
        Map<UUID, List<Integer>> positions = trie.search("tree");
        assertEquals(1, positions.size());
        assertEquals(1, positions.get(pageId).size());
        assertEquals(0, positions.get(pageId).get(0).intValue());
    }

    @Test
    public void testSearchNonExistentWord() {
        UUID pageId = UUID.randomUUID();
        trie.insert("banana", pageId, 0);
        Map<UUID, List<Integer>> positions = trie.search("apple");
        assertTrue(positions.isEmpty());
    }

    @Test
    public void testInsertMultiplePositionsForSameWord() {
        UUID pageId1 = UUID.randomUUID();
        UUID pageId2 = UUID.randomUUID();
        trie.insert("lemon", pageId1, 0);
        trie.insert("lemon", pageId2, 0);
        Map<UUID, List<Integer>> positions = trie.search("lemon");
        assertEquals(2, positions.size());
        assertTrue(positions.containsKey(pageId1));
        assertTrue(positions.containsKey(pageId2));
        assertEquals(1, positions.get(pageId1).size());
        assertEquals(0, positions.get(pageId1).get(0).intValue());
        assertEquals(1, positions.get(pageId2).size());
        assertEquals(0, positions.get(pageId2).get(0).intValue());
    }

    @Test
    public void testInsertAndSearchMultipleWords() {
        UUID pageId1 = UUID.randomUUID();
        UUID pageId2 = UUID.randomUUID();
        trie.insert("tree", pageId1, 0);
        trie.insert("leaf", pageId2, 0);
        Map<UUID, List<Integer>> treePositions = trie.search("tree");
        Map<UUID, List<Integer>> leafPositions = trie.search("leaf");
        assertEquals(1, treePositions.size());
        assertEquals(1, treePositions.get(pageId1).size());
        assertEquals(0, treePositions.get(pageId1).get(0).intValue());
        assertEquals(1, leafPositions.size());
        assertEquals(1, leafPositions.get(pageId2).size());
        assertEquals(0, leafPositions.get(pageId2).get(0).intValue());
    }

    @Test
    public void testSearchEmptyString() {
        UUID pageId = UUID.randomUUID();
        trie.insert("tree", pageId, 0);
        Map<UUID, List<Integer>> positions = trie.search("");
        assertTrue(positions.isEmpty());
    }

    @Test
    public void testInsertAndSearchLongWord() {
        String longWord = "ahfdhaiihidhaijfidufsjhyfiwhfwjheahfaiyaewhejifhwe";
        UUID pageId = UUID.randomUUID();
        trie.insert(longWord, pageId, 0);
        Map<UUID, List<Integer>> positions = trie.search(longWord);
        assertEquals(1, positions.size());
        assertEquals(1, positions.get(pageId).size());
        assertEquals(0, positions.get(pageId).get(0).intValue());
    }

    @Test
    public void testInsertAndSearchOverlappingWords() {
        UUID pageId = UUID.randomUUID();
        trie.insert("treehouse", pageId, 0);
        trie.insert("tree", pageId, 0);
        Map<UUID, List<Integer>> treehousePositions = trie.search("treehouse");
        Map<UUID, List<Integer>> treePositions = trie.search("tree");
        assertEquals(1, treehousePositions.size());
        assertEquals(1, treehousePositions.get(pageId).size());
        assertEquals(0, treehousePositions.get(pageId).get(0).intValue());
        assertEquals(1, treePositions.size());
        assertEquals(1, treePositions.get(pageId).size());
        assertEquals(0, treePositions.get(pageId).get(0).intValue());
    }

    @Test
    public void testInsertAndSearchSingleCharacter() {
        UUID pageId = UUID.randomUUID();
        trie.insert("a", pageId, 0);
        Map<UUID, List<Integer>> positions = trie.search("a");
        assertEquals(1, positions.size());
        assertEquals(1, positions.get(pageId).size());
        assertEquals(0, positions.get(pageId).get(0).intValue());
    }

    @Test
    public void testInsertNullWord() {
        assertThrows(NullPointerException.class, () -> trie.insert(null, UUID.randomUUID(), 0));
    }

    @Test
    public void testSearchNullWord() {
        assertThrows(NullPointerException.class, () -> trie.search(null));
    }
}
