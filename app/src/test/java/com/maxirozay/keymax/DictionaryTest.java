package com.maxirozay.keymax;

import com.maxirozay.keymax.dictionary.DictionaryManager;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by max on 6/26/17.
 */

public class DictionaryTest {
    @Test
    public void wordValidity() throws Exception {
        assertEquals(false, DictionaryManager.isWordValid("aaa,,,"));
        assertEquals(false, DictionaryManager.isWordValid(",,,aaa"));
        assertEquals(false, DictionaryManager.isWordValid("aaa,"));
        assertEquals(false, DictionaryManager.isWordValid(",aaa"));
        assertEquals(true, DictionaryManager.isWordValid("aaa,,,aaa"));
        assertEquals(true, DictionaryManager.isWordValid("a"));
        assertEquals(false, DictionaryManager.isWordValid(""));
        assertEquals(true, DictionaryManager.isWordValid("hi"));
        assertEquals(true, DictionaryManager.isWordValid("Asé"));
    }
}
