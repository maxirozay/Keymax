package com.maxirozay.keymax.dictionary;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Maxime Rossier on 15.09.2015.
 */
public class Node extends RealmObject {

    @PrimaryKey
    private String lowercase;
    private String simpleWord; // Only lowercase letters with any accent.
    private String word, language;
    private RealmList<Node> followingWords;
    private RealmList<Node> suffixes;
    private int freq;
    private boolean isWord;

    public boolean isWord() {
        return isWord;
    }

    public void setWord(boolean word) {
        isWord = word;
    }

    public int getFreq() {
        return freq;
    }

    public void setFreq(int freq) {
        this.freq = freq;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public RealmList<Node> getFollowingWords() {
        return followingWords;
    }

    public void setFollowingWords(RealmList<Node> followings) {
        this.followingWords = followings;
    }

    public void addFollowingWord(Node word) {
        if (getFollowingWords().size() > 5) getFollowingWords().remove(5);
        int index = getFollowingWords().indexOf(word);
        if (index > 0) getFollowingWords().move(index, index - 1);
        else if (index == -1) getFollowingWords().add(word);
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void addLanguage(String language) {
        setLanguage(getLanguage() + language + ",");
    }

    public void removeLanguage(String language) {
        getLanguage().replace(language + ",", "");
    }

    public String getLowercase() {
        return lowercase;
    }

    public void setLowercase(String word) {
        lowercase = word;
    }

    public RealmList<Node> getSuffixes() {
        return suffixes;
    }

    public void setSuffixes(RealmList<Node> suffixes) {
        this.suffixes = suffixes;
    }

    public void addSuffix(Node word) {
        int index = getSuffixes().indexOf(word);
        int startIndex = index == -1 ? getSuffixes().size() - 1 : index - 1;
        for (int i = startIndex; i >= 0; i--) {
            if (getSuffixes().get(i).getFreq() >= word.getFreq()) {
                if (index == -1) getSuffixes().add(i + 1, word);
                else getSuffixes().move(index, i + 1);
                return;
            }
        }
        getSuffixes().add(word);
    }

    public String getSimpleWord() {
        return simpleWord;
    }

    public void setSimpleWord(String simpleWord) {
        this.simpleWord = simpleWord;
    }
}
