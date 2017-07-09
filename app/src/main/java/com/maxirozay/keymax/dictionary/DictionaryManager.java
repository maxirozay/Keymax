package com.maxirozay.keymax.dictionary;

import android.content.Context;
import android.content.SharedPreferences;

import com.maxirozay.keymax.R;
import com.maxirozay.keymax.util.StringUtil;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;


/**
 * Created by Maxime Rossier on 19.08.2015.
 */
public class DictionaryManager {

    private SharedPreferences preferences;
    private Realm realm;
    private static Map<Character, String> letterNeighbor;
    private Context context;

    private void initLetterNeighborQWERTZ() {
        letterNeighbor = new Hashtable<>();
        letterNeighbor.put('q', "w");
        letterNeighbor.put('w', "eq");
        letterNeighbor.put('e', "rw");
        letterNeighbor.put('r', "te");
        letterNeighbor.put('t', "zr");
        letterNeighbor.put('z', "ut");
        letterNeighbor.put('u', "iz");
        letterNeighbor.put('i', "ou");
        letterNeighbor.put('o', "pi");
        letterNeighbor.put('p', "o");
        letterNeighbor.put('a', "s");
        letterNeighbor.put('s', "da");
        letterNeighbor.put('d', "fs");
        letterNeighbor.put('f', "gd");
        letterNeighbor.put('g', "hf");
        letterNeighbor.put('h', "jg");
        letterNeighbor.put('j', "kh");
        letterNeighbor.put('k', "lj");
        letterNeighbor.put('l', "k'");
        letterNeighbor.put('y', "x");
        letterNeighbor.put('x', "cy");
        letterNeighbor.put('c', "vx");
        letterNeighbor.put('v', "bc");
        letterNeighbor.put('b', "nv");
        letterNeighbor.put('n', "bm");
        letterNeighbor.put('m', "n");
    }
    private void initLetterNeighborQWERTY() {
        letterNeighbor = new Hashtable<>();
        letterNeighbor.put('q', "w");
        letterNeighbor.put('w', "eq");
        letterNeighbor.put('e', "rw");
        letterNeighbor.put('r', "te");
        letterNeighbor.put('t', "yr");
        letterNeighbor.put('y', "ut");
        letterNeighbor.put('u', "yi");
        letterNeighbor.put('i', "ou");
        letterNeighbor.put('o', "pi");
        letterNeighbor.put('p', "o");
        letterNeighbor.put('a', "s");
        letterNeighbor.put('s', "da");
        letterNeighbor.put('d', "fs");
        letterNeighbor.put('f', "gd");
        letterNeighbor.put('g', "hf");
        letterNeighbor.put('h', "jg");
        letterNeighbor.put('j', "kh");
        letterNeighbor.put('k', "lj");
        letterNeighbor.put('l', "k'");
        letterNeighbor.put('z', "x");
        letterNeighbor.put('x', "cz");
        letterNeighbor.put('c', "vx");
        letterNeighbor.put('v', "bc");
        letterNeighbor.put('b', "nv");
        letterNeighbor.put('n', "bm");
        letterNeighbor.put('m', "n");
    }
    private void initLetterNeighborAZERTY() {
        letterNeighbor = new Hashtable<>();
        letterNeighbor.put('a', "z");
        letterNeighbor.put('z', "ea");
        letterNeighbor.put('e', "rz");
        letterNeighbor.put('r', "te");
        letterNeighbor.put('t', "yr");
        letterNeighbor.put('y', "ut");
        letterNeighbor.put('u', "iy");
        letterNeighbor.put('i', "ou");
        letterNeighbor.put('o', "pi");
        letterNeighbor.put('p', "o");
        letterNeighbor.put('q', "s");
        letterNeighbor.put('s', "da");
        letterNeighbor.put('d', "fs");
        letterNeighbor.put('f', "gd");
        letterNeighbor.put('g', "hf");
        letterNeighbor.put('h', "jg");
        letterNeighbor.put('j', "kh");
        letterNeighbor.put('k', "lj");
        letterNeighbor.put('l', "km");
        letterNeighbor.put('m', "l");
        letterNeighbor.put('w', "x");
        letterNeighbor.put('x', "cw");
        letterNeighbor.put('c', "vx");
        letterNeighbor.put('v', "bc");
        letterNeighbor.put('b', "nv");
        letterNeighbor.put('n', "b");
    }

    public DictionaryManager(final Context context,
                             final SharedPreferences preferences) {
        this.context = context;
        this.preferences = preferences;
        realm = Realm.getDefaultInstance();

    }

    public void init() {
        checkKeyboardLayout();
        if (preferences.getBoolean(context.getString(R.string.key_reset_data), false)) {
            resetDatabase();
            preferences.edit().putBoolean(context.getString(R.string.key_reset_data), false).apply();
        }
        if (getRoot(realm) == null) {
            realm.executeTransaction(realm -> {
                Node root = createNode(realm, "", 0, "");
                String[] followingWords = {"I", "do", "can"};
                root.setFollowingWords(getFollowingWords(realm,
                        followingWords,
                        context.getString(R.string.key_english)));
            });
        }
        checkLanguages();
    }

    public void close() {
        realm.close();
    }

    private void checkKeyboardLayout() {
        switch (preferences.getString(context.getString(R.string.key_keyboard_layout),
                context.getString(R.string.default_keyboard))) {
            case "qwerty":
                initLetterNeighborQWERTY();
                break;
            case "qwertz":
                initLetterNeighborQWERTZ();
                break;
            case "azerty":
                initLetterNeighborAZERTY();
                break;
            default:
                initLetterNeighborQWERTY();
        }
    }

    private void checkLanguages() {
        String[] languages = {context.getString(R.string.key_french),
                context.getString(R.string.key_german),
                context.getString(R.string.key_italian),
                context.getString(R.string.key_portuguese),
                context.getString(R.string.key_spanish)};
        List<String> languagesToLoad = new ArrayList<>();
        List<String> languagesToDelete = new ArrayList<>();

        if (preferences.getBoolean(context.getString(R.string.key_english), true)) {
            languagesToLoad.add(context.getString(R.string.key_english));
        }
        else languagesToDelete.add(context.getString(R.string.key_english));
        for (String language : languages) {
            if (preferences.getBoolean(language, false)) languagesToLoad.add(language);
            else languagesToDelete.add(language);
        }
        Node root = getRoot(realm);
        for (String language : languagesToDelete) {
            if (root.getLanguage().contains(language)) removeLanguage(language);
        }
        for (String language : languagesToLoad) new DictionaryLoader(language).start();
    }

    private void resetDatabase() {
        realm.executeTransactionAsync(realm -> realm.deleteAll());
    }

    private void removeLanguage(final String language) {
        realm.executeTransactionAsync(realm -> {
            RealmResults<Node> nodes = realm.where(Node.class)
                    .contains("language", language).findAll();
            for (Node node : nodes) {
                node.removeLanguage(language);
                if (node.getLanguage().isEmpty()) node.setWord(false);
            }
        });
    }

    public void removeWord(final String word) {
        realm.executeTransactionAsync(realm -> {
            Node node = realm.where(Node.class)
                    .equalTo("lowercase", word.toLowerCase()).findFirst();
            if (node != null) {
                if (node.getSuffixes().size() == 0) {
                    node.deleteFromRealm();
                } else node.setWord(false);
            }
        });
    }

    class DictionaryLoader extends Thread {

        String language;

        private DictionaryLoader(String language) {
            this.language = language;
        }

        public void run() {
            DataInputStream inputStream = null;
            Scanner sc = null;
            final Realm threadRealm = Realm.getDefaultInstance();
            threadRealm.beginTransaction();
            Node root = getRoot(threadRealm);
            String currentLanguages = root.getLanguage();
            if (currentLanguages.contains(language)) {
                threadRealm.commitTransaction();
                threadRealm.close();
                return;
            }
            root.addLanguage(language);
            threadRealm.commitTransaction();
            try {
                inputStream = new DataInputStream(context.getAssets().open(language + ".txt"));
                sc = new Scanner(inputStream, "UTF-8");
                String[] lines = new String[10];
                int cpt = 0;
                sc.nextLine(); // Jump the first line to get rid of the leading space.
                while (sc.hasNextLine()) {
                    lines[cpt++] = sc.nextLine();
                    if (cpt % lines.length == 0) {
                        cpt = 0;
                        loadWords(threadRealm, lines, language);
                    }
                }
                loadWords(threadRealm, Arrays.copyOf(lines, cpt), language);
                // note that Scanner suppresses exceptions
                if (sc.ioException() != null) {
                    throw sc.ioException();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            threadRealm.close();
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (sc != null) {
                sc.close();
            }

        }
    }

    private void loadWords(Realm realm, final String[] lines, final String language) {
        realm.executeTransaction(realm1 -> {
            for (String line: lines){
                final String[] splittedLine = line.split("\\s+", 3);
                final String[] followingWords = splittedLine.length > 2 ?
                        splittedLine[2].split("\\s+") : null;
                addWord(realm1,
                        splittedLine[0],
                        Integer.parseInt(splittedLine[1]),
                        getFollowingWords(realm1, followingWords, language),
                        language);
            }
        });
    }

    private RealmList<Node> getFollowingWords(final Realm realm, String[] words, String language) {
        if (words == null || words.length == 0) return null;
        RealmList<Node> followingWords = new RealmList<>();
        for (String word : words) {
            followingWords.add(addWord(realm,
                    word,
                    0,
                    null,
                    language));
        }
        return followingWords;
    }

    private Node addWord(final Realm realm,
                         final String word,
                         final int freq,
                         final RealmList<Node> followingWords,
                         final String language) {
        String lowercase = word.toLowerCase();
        Node node = realm.where(Node.class).equalTo("lowercase", lowercase).findFirst();
        if (node != null) {
            if (freq > 0) {
                node.setFreq(freq);
                node.setWord(true);
                node.setSimpleWord(StringUtil.cleanWord(lowercase));
                node.setWord(word);
            }
            if (followingWords != null) node.setFollowingWords(followingWords);
            return node;
        }
        return populateTree(realm,
                lowercase,
                word,
                freq,
                followingWords,
                language);
    }

    private Node getRoot(Realm realm) {
        return realm.where(Node.class).equalTo("lowercase", "").findFirst();
    }

    private Node findPrefix(Realm realm, String lowercase) {
        if (lowercase.length() == 0) return getRoot(realm);
        Node node = realm.where(Node.class).equalTo("lowercase", lowercase).findFirst();
        if (node == null) return findPrefix(realm, lowercase.substring(0, lowercase.length() - 1));
        return node;
    }

    private Node populateTree(final Realm realm,
                              final String lowercase,
                              final String word,
                              final int freq,
                              final RealmList<Node> followingWords,
                              final String language) {
        Node prefix = findPrefix(realm, lowercase.substring(0, lowercase.length() - 1));
        int depth = prefix.getLowercase().length();
        for (int i = 0; i < prefix.getSuffixes().size(); i++) {
            Node suffix = prefix.getSuffixes().get(i);
            if (lowercase.charAt(depth) == suffix.getLowercase().charAt(depth)) {
                for (int j = depth + 1; j < suffix.getLowercase().length(); j++) {
                    if (j == word.length()) {
                        Node newWord = createWord(realm,
                                lowercase,
                                word,
                                freq,
                                followingWords,
                                language);
                        newWord.addSuffix(suffix);
                        prefix.getSuffixes().remove(i);
                        prefix.addSuffix(newWord);
                        return newWord;
                    }
                    if (lowercase.charAt(j) != suffix.getLowercase().charAt(j)) {
                        Node newNode = createNode(realm,
                                lowercase.substring(0, j),
                                Math.max(freq, suffix.getFreq()),
                                language);
                        Node newWord = createWord(realm,
                                lowercase,
                                word,
                                freq,
                                followingWords,
                                language);
                        newNode.addSuffix(suffix);
                        newNode.addSuffix(newWord);
                        prefix.getSuffixes().remove(i);
                        prefix.addSuffix(newNode);
                        return newWord;
                    }
                }
                if (suffix.getLowercase().length() == word.length()) {
                    if (!suffix.isWord()) {
                        suffix.setWord(true);
                        suffix.setWord(word);
                        suffix.setSimpleWord(StringUtil.cleanWord(lowercase));
                        suffix.setFreq(Math.max(freq, suffix.getFreq()));
                        suffix.setFollowingWords(followingWords);
                        if (!suffix.getLanguage().contains(language)) {
                            suffix.addLanguage(language);
                        }
                        prefix.addSuffix(suffix);
                    }
                    return suffix;
                }
                Node newWord = createWord(realm,
                        lowercase,
                        word,
                        freq,
                        followingWords,
                        language);
                return newWord;
            }
        }
        Node newNode = createWord(realm,
                lowercase,
                word,
                freq,
                followingWords,
                language);
        prefix.addSuffix(newNode);
        return newNode;
    }

    private Node createWord(final Realm realm,
                            final String lowercase,
                            final String wordName,
                            final int freq,
                            final RealmList<Node> followingWords,
                            final String language) {
        Node newNode = realm.createObject(Node.class, lowercase);
        newNode.setWord(wordName);
        newNode.setSimpleWord(StringUtil.cleanWord(lowercase));
        newNode.setFreq(freq);
        newNode.setFollowingWords(followingWords);
        newNode.setLanguage(language + ",");
        newNode.setWord(true);
        return newNode;
    }

    private Node createNode(final Realm realm,
                            final String lowercase,
                            final int freq,
                            final String language) {
        Node newNode = realm.createObject(Node.class, lowercase);
        newNode.setSimpleWord(StringUtil.cleanWord(lowercase));
        newNode.setFreq(freq);
        newNode.setLanguage(language + ",");
        return newNode;
    }

    public List<Node> searchWord(String query, List<Node> prefixes) {
        if (prefixes.size() == 0) prefixes.add(getRoot(realm));
        List<Node> perfectMatches = new ArrayList<>();
        List<Node> matches = new ArrayList<>();
        List<Node> corrections = new ArrayList<>();
        for (Node prefix : prefixes) {
            searchInTree(query.toLowerCase(), prefix, 0, 0, perfectMatches, matches, corrections);
        }
        perfectMatches.addAll(matches);
        perfectMatches.addAll(corrections);
        if (perfectMatches.size() > 0 && !perfectMatches.get(0).isWord()) {
            Node node = perfectMatches.remove(0);
            perfectMatches.addAll(node.getSuffixes());
        }
        return perfectMatches;
    }

    private int searchInTree(String query,
                             Node node,
                             int errorCount,
                             int depth,
                             List<Node> perfectMatches,
                             List<Node> matches,
                             List<Node> corrections) {
        int matchCount = 0;
        String editedQuery = query;
        for (int i = depth; i < node.getLowercase().length(); i++) {
            if (i >= editedQuery.length()) break;
            if (editedQuery.charAt(i) != node.getLowercase().charAt(i)) {
                if ("'-.".contains(node.getLowercase().substring(i, i + 1))) {
                    editedQuery = query.substring(0, i)
                            + node.getLowercase().charAt(i)
                            + query.substring(i);
                } else if (node.getSimpleWord().length() > i
                        && editedQuery.charAt(i) == node.getSimpleWord().charAt(i)) {
                    editedQuery = query.substring(0, i)
                            + node.getLowercase().charAt(i);
                    if (query.length() > i + 1) {
                        editedQuery += query.substring(i + 1);
                    }
                } else if (letterNeighbor.containsKey(editedQuery.charAt(i))
                        && letterNeighbor.get(editedQuery.charAt(i))
                        .contains(node.getLowercase().substring(i, i + 1))) {
                    if (++errorCount > 1 || perfectMatches.size() > 2) return 0;
                    editedQuery = query.substring(0, i)
                            + node.getLowercase().charAt(i);
                    if (query.length() > i + 1) {
                        editedQuery += query.substring(i + 1);
                    }
                } else return 0;
            }
        }
        if (node.getLowercase().length() < editedQuery.length()){
            for (Node suffix : node.getSuffixes()) {
                matchCount += searchInTree(editedQuery,
                        suffix,
                        errorCount,
                        node.getLowercase().length(),
                        perfectMatches,
                        matches,
                        corrections);
                if (matchCount > 2) return matchCount;
            }
        } else {
            if (errorCount == 0) {
                if (node.isWord()) {
                    if (node.getLowercase().length() == editedQuery.length()) {
                        perfectMatches.add(node);
                    } else matches.add(node);
                }
                else matches.addAll(node.getSuffixes());
                return 1;
            }
            else {
                if (node.isWord()) corrections.add(node);
                else corrections.addAll(node.getSuffixes());
                return 0;
            }
        }
        return 0;
    }

    public List<Node> getFollowingWords(String word) {
        Node node = realm.where(Node.class)
                .equalTo("lowercase", word.toLowerCase()).findFirst();
        if (node == null || node.getFollowingWords().size() == 0) node = getRoot(realm);
        else {
            realm.beginTransaction();
            node.setFreq(node.getFreq() + 1);
            realm.commitTransaction();
        }
        List<Node> results = new ArrayList<>();
        results.addAll(node.getFollowingWords());
        return results;
    }

    public void addWord(final String word) {
        realm.executeTransactionAsync(realm -> {
            String wordEdited = StringUtil.stripEncapsulatingPunctuation(word);
            if (!isWordValid(wordEdited)) return;
            String lowercase = wordEdited.toLowerCase();
            Node node = realm.where(Node.class).equalTo("lowercase", lowercase).findFirst();
            if (node == null) {
                populateTree(realm,
                        lowercase,
                        wordEdited,
                        0,
                        null,
                        "USER");
            } else {
                node.setWord(wordEdited);
                node.setWord(true);
            }
        });
    }

    public void addFollowingWord(final String word, final String following) {
        realm.executeTransactionAsync(realm -> {
            String wordEdited = StringUtil.stripEncapsulatingPunctuation(word);
            String followingEdited = StringUtil.stripEncapsulatingPunctuation(following);
            if ((wordEdited.isEmpty() || isWordValid(wordEdited))
                    && isWordValid(followingEdited)) {
                Node wordNode = realm.where(Node.class)
                        .equalTo("lowercase", wordEdited.toLowerCase()).findFirst();
                if (wordNode == null) return;
                Node followingNode = realm.where(Node.class)
                        .equalTo("lowercase", followingEdited.toLowerCase()).findFirst();
                if (followingNode == null) return;
                wordNode.addFollowingWord(followingNode);
            }
        });
    }

    public static boolean isWordValid(String s) {
        return s.length() == 1 && s.matches("[\\p{L}]")
                || s.matches("[\\p{L}].*[\\p{L}]");
    }

}
