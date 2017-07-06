package com.maxirozay.keymax.dictionary;

import java.util.Comparator;

/**
 * Created by max on 6/29/17.
 */

public class WordLengthComparator implements Comparator<Node> {

    public int compare(Node n1, Node n2) {
        String s1 = n1.getLowercase();
        String s2 = n2.getLowercase();
        return removePunctuationFromWord(s1).length()
                - removePunctuationFromWord(s2).length();
    }


    public static String removePunctuationFromWord(String s) {
        return s.replaceAll("['.-]", "");
    }
}