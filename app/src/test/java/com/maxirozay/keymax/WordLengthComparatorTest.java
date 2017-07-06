package com.maxirozay.keymax;

import com.maxirozay.keymax.dictionary.Node;
import com.maxirozay.keymax.dictionary.WordLengthComparator;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by max on 6/29/17.
 */

public class WordLengthComparatorTest {

    @Test
    public void removePunctuationFromWord() throws Exception {
        assertEquals("ab", WordLengthComparator.removePunctuationFromWord("a'.-b"));
    }

    @Test
    public void compareWords() throws Exception {
        WordLengthComparator comparator = new WordLengthComparator();
        Node n1 = new Node();
        n1.setLowercase("a'.-b");
        Node n2 = new Node();
        n2.setLowercase("ab");
        assertEquals(0, comparator.compare(n1, n2));
    }
}
