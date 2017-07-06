package com.maxirozay.keymax;

import com.maxirozay.keymax.util.StringUtil;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class StringUtilTest {
    @Test
    public void stripEncapsulatingPunctuation() throws Exception {
        assertEquals("aaa,,", StringUtil.stripEncapsulatingPunctuation("aaa,,,"));
        assertEquals("aaa", StringUtil.stripEncapsulatingPunctuation("aaa,"));
        assertEquals("a", StringUtil.stripEncapsulatingPunctuation("(a)"));
        assertEquals("aaa", StringUtil.stripEncapsulatingPunctuation(".aaa"));
        assertEquals("a.,a", StringUtil.stripEncapsulatingPunctuation("a.,a"));
        assertEquals("a", StringUtil.stripEncapsulatingPunctuation("a"));
    }

    @Test
    public void cleanWord() throws Exception {
        assertEquals("aceaeuao", StringUtil.cleanWord("a+*ç%&/()=0éàèüäö$"));
    }
}