package com.maxirozay.keymax.util;

import java.text.Normalizer;

/**
 * Created by max on 6/26/17.
 */

public class StringUtil {

    // Keep only lowercase letters and remove accents.
    public static String cleanWord(String s) {
        s = Normalizer.normalize(s, Normalizer.Form.NFD);
        s = s.replaceAll("[^a-z]+","");
        return s;
    }

    public static boolean isNumeric(String s) {
        return s.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }

    public static boolean isEndOfSentence(String s) {
        if (s == null || s.isEmpty()) return true;
        return s.endsWith(".")
                || s.endsWith("?")
                || s.endsWith("!");
    }

    public static String stripEncapsulatingPunctuation(String s) {
        s =  s.matches(".+\\p{Punct}") ? s.substring(0, s.length() - 1) : s;
        s =  s.matches("\\p{Punct}.+") ? s.substring(1) : s;
        return s;
    }
}
