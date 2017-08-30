package com.maxirozay.keymax;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.media.AudioManager;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.text.InputType;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;
import android.widget.Button;

import com.maxirozay.keymax.dictionary.DictionaryManager;
import com.maxirozay.keymax.dictionary.Node;
import com.maxirozay.keymax.keyboard.KeymaxKeyboard;
import com.maxirozay.keymax.keyboard.KeymaxKeyboardView;
import com.maxirozay.keymax.keyboard.OnKeymaxActionListener;
import com.maxirozay.keymax.util.Alert;
import com.maxirozay.keymax.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Created by Maxime Rossier on 6/21/17.
 */

public class InputService extends InputMethodService implements
        KeymaxKeyboardView.OnKeyboardActionListener,
        OnKeymaxActionListener,
        View.OnClickListener {

    private InputView inputView;
    private KeymaxKeyboard keyboard;
    private static final String RECENT_EMOJIS = "recent emojis";
    public static final int PREDICTION1 = -101,
            PREDICTION2 = -102,
            PREDICTION3 = -103,
            ALPHABETIC_KEYBOARD = -105,
            EMOJI_KEYBOARD = -106,
            ALT_KEYBOARD = -107,
            SETTINGS = -109,
            NUMBERIC = 1,
            SPACE_PRIMARY_CODE = 32;
    private int inputType, cursorPosition, shiftKey = 35;
    private String lastWord, currentWord;
    private int[] PREDICTION_KEYS;
    private String[] predictions = {" ", " ", " ", " "};
    private boolean[] addPredictionToDictionary = {false, false, false, true};
    private boolean isPredictable;
    private boolean autoUpperCase;
    private boolean capsLocked;
    private boolean isNewSentence;
    private boolean autoCorrect;
    private boolean currentWordIsDone = true;
    private DictionaryManager dictionaryManager;
    private List<Node> lastPredictions = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public View onCreateInputView() {
        inputView = (InputView)getLayoutInflater().inflate(R.layout.input_default, null);
        inputView.getKeyboardView().setOnKeyboardActionListener(this);
        inputView.getKeyboardView().setOnLongPressListener(this);
        inputView.setOnClickListener(this);
        inputView.getKeyboardView().setKeyboard(keyboard);
        return inputView;
    }

    @Override
    public void onStartInput(EditorInfo attribute, boolean restarting) {
        super.onStartInput(attribute, restarting);
        switch (attribute.inputType & InputType.TYPE_MASK_CLASS) {
            case InputType.TYPE_CLASS_NUMBER:
            case InputType.TYPE_CLASS_DATETIME:
            case InputType.TYPE_CLASS_PHONE:
                inputType = NUMBERIC;
                isPredictable = false;
                keyboard = new KeymaxKeyboard(this, R.xml.numeric);
                break;
            case InputType.TYPE_CLASS_TEXT:
            default:
                switch (attribute.inputType & InputType.TYPE_MASK_VARIATION) {
                    case InputType.TYPE_TEXT_VARIATION_PASSWORD:
                    case InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD:
                    case InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD:
                        isPredictable = false;
                        autoUpperCase = false;
                        predictions[0] = " ";
                        predictions[1] = " ";
                        predictions[2] = " ";
                        predictions[3] = " ";
                        break;
                    case InputType.TYPE_TEXT_VARIATION_URI:
                        isPredictable = false;
                        autoUpperCase = false;
                        predictions[0] = " ";
                        predictions[1] = "/";
                        predictions[2] = "https://";
                        predictions[3] = ".com";
                        break;
                    case InputType.TYPE_TEXT_VARIATION_WEB_EDIT_TEXT:
                    case InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS:
                    case InputType.TYPE_TEXT_VARIATION_EMAIL_SUBJECT:
                    case InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS:
                    case InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS:
                        isPredictable = true;
                        autoUpperCase = false;
                        predictions[0] = " ";
                        predictions[1] = " ";
                        predictions[2] = " ";
                        predictions[3] = " ";
                        break;
                    default:
                        autoUpperCase = true;
                        isPredictable = true;
                        predictions[0] = " ";
                        predictions[1] = " ";
                        predictions[2] = " ";
                        predictions[3] = " ";
                }
                inputType = ALPHABETIC_KEYBOARD;
                keyboard = getAlphaNumericKeyboard();
                initKeysId();
        }
        if (inputView != null) {
            inputView.showKeyboard();
            inputView.getKeyboardView().setKeyboard(keyboard);
            if (dictionaryManager != null) {
                initCursorPosition();
                updateSelection();
            }
        }
    }

    @Override
    public void onWindowShown() {
        super.onWindowShown();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        inputView.setPreview(preferences.getBoolean(getString(R.string.key_preview), true));
        dictionaryManager = new DictionaryManager(this,
                preferences);
        dictionaryManager.init();
        inputView.getRecentEmojis().clear();
        inputView.getRecentEmojis().addAll(Arrays
                .asList(preferences.getString(RECENT_EMOJIS, "").split(",")));
        initCursorPosition();
        updateSelection();
    }

    @Override
    public void onWindowHidden() {
        super.onWindowHidden();
        StringBuilder emojis = new StringBuilder();
        for (String emoji : inputView.getRecentEmojis()) emojis.append(emoji + ",");
        if (emojis.length() > 0) {
            SharedPreferences.Editor preferences = PreferenceManager
                    .getDefaultSharedPreferences(this).edit();
            preferences.putString(RECENT_EMOJIS, emojis.toString());
            preferences.apply();
        }
        currentWord = null;
        dictionaryManager.close();
        dictionaryManager = null;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        initKeysId();
    }

    public void initKeysId() {
        autoCorrect = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(getString(R.string.key_auto_correct), true);
        if (getResources().getConfiguration().orientation ==
                Configuration.ORIENTATION_PORTRAIT) {
            if (autoCorrect) PREDICTION_KEYS = new int[] {47, 3, 1, 2};
            else {
                PREDICTION_KEYS = new int[] {2, 3, 1};
                keyboard.getKeys().get(47).label = " ";
            }
            shiftKey = 35;
        }
        else {
            if (autoCorrect) PREDICTION_KEYS = new int[] {37, 3, 1, 2};
            else {
                PREDICTION_KEYS = new int[] {2, 3, 1};
                keyboard.getKeys().get(37).label = " ";
            }
            shiftKey = 25;
        }
        for (int i = 0; i < PREDICTION_KEYS.length; i++) {
            keyboard.getKeys().get(PREDICTION_KEYS[i]).label = predictions[i];
        }
    }

    @Override
    public void onUpdateSelection(int oldSelStart, int oldSelEnd,
                                  int newSelStart, int newSelEnd,
                                  int candidatesStart, int candidatesEnd) {
        super.onUpdateSelection(oldSelStart, oldSelEnd,
                newSelStart, newSelEnd,
                candidatesStart, candidatesEnd);
        if (newSelStart != oldSelStart && oldSelStart == cursorPosition) {
            cursorPosition = newSelStart;
            updateSelection();
        } else if (newSelStart != newSelEnd) {
            cursorPosition = newSelStart;
            getCurrentInputConnection().finishComposingText();
        } else if (oldSelStart != oldSelEnd) {
            cursorPosition = newSelStart;
            updateSelection();
        }
    }

    private void initCursorPosition() {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null) return;
        ExtractedText extractedText = ic.getExtractedText(new ExtractedTextRequest(), 0);
        if (extractedText == null) return;
        cursorPosition = extractedText.selectionStart;
    }

    private void updateSelection() {
        if (inputType == NUMBERIC) return;
        currentWord = "";
        if (isPredictable) {
            lastPredictions.clear();
            lastWord = "";
            InputConnection ic = getCurrentInputConnection();
            if (ic == null) return;
            if (cursorPosition == 0) startNewSentence();
            else {
                CharSequence lastCharacters = ic.getTextBeforeCursor(100,
                        InputConnection.GET_TEXT_WITH_STYLES);
                if (lastCharacters == null || lastCharacters.length() == 0) {
                    startNewSentence();
                } else {
                    char lastCharacter = lastCharacters.charAt(lastCharacters.length() - 1);
                    currentWordIsDone = lastCharacter == ' ' || lastCharacter == '\n';
                    updateCurrentWord(lastCharacters.toString());
                    setComposingRegion();
                    if (isNewSentence && autoUpperCase && currentWordIsDone) enableShift(true);
                    getPredictions();
                 }
            }
        }
    }

    private void startNewSentence() {
        isNewSentence = true;
        currentWordIsDone = true;
        if (autoUpperCase) enableShift(true);
        getCurrentInputConnection().finishComposingText();
        getPredictions();
    }

    private void updateCurrentWord(String lastCharacters) {
        String[] lastWords = lastCharacters.split("\\s+");
        lastWord = lastWords.length > 1 ? lastWords[lastWords.length - 2] : "";
        currentWord = lastWords.length > 0 ? lastWords[lastWords.length - 1] : "";
        checkIfIsNewSentence();
    }

    private void setComposingRegion() {
        InputConnection ic = getCurrentInputConnection();
        if (currentWordIsDone) ic.finishComposingText();
        else {
            ic.setComposingRegion(cursorPosition - currentWord.length(),
                    cursorPosition);
        }
    }

    private void checkIfIsNewSentence() {
        if (!currentWordIsDone && (lastWord.isEmpty() || StringUtil.isEndOfSentence(lastWord))
                || currentWordIsDone && StringUtil.isEndOfSentence(currentWord)) {
            if (currentWordIsDone && autoUpperCase) enableShift(true);
            isNewSentence = true;
        } else isNewSentence = false;
    }

    private void getPredictions() {
        if (inputType == ALPHABETIC_KEYBOARD && isPredictable && dictionaryManager != null) {
            if (currentWordIsDone)
                lastPredictions = dictionaryManager.getFollowingWords(currentWord);
            else lastPredictions = dictionaryManager.searchWord(currentWord, lastPredictions);

            for (int i = 0; i < 4; i++) {
                if (lastPredictions.size() > i && lastPredictions.get(i).isWord()) {
                    predictions[i] = lastPredictions.get(i).getWord() + " ";
                    addPredictionToDictionary[i] = false;
                } else {
                    predictions[i] = currentWord + " ";
                    addPredictionToDictionary[i] = true;
                }
            }
            checkPredictionsCase();
            if (!currentWordIsDone) predictions[3] = currentWord + " ";
            for (int i = 0; i < PREDICTION_KEYS.length; i++) {
                if (predictions[i].length() < 12) {
                    keyboard.getKeys().get(PREDICTION_KEYS[i]).label = predictions[i];
                } else {
                    keyboard.getKeys().get(PREDICTION_KEYS[i])
                            .label = ".." + predictions[i]
                            .substring(predictions[i].length() - 11, predictions[i].length());
                }
            }
            invalidatePredication();
            if (currentWordIsDone) lastPredictions.clear();
        }
    }

    private void checkPredictionsCase() {
        if (isNewSentence) {
            for (int i = 0; i < predictions.length; i++) {
                predictions[i] = Character.toUpperCase(
                        predictions[i].charAt(0)) +
                        predictions[i].substring(1);
            }
        }
        if (capsLocked) {
            for (int i = 0; i < predictions.length; i++) {
                predictions[i] = predictions[i].toUpperCase();
            }
        }
    }

    private void invalidatePredication() {
        for (int id : PREDICTION_KEYS){
            inputView.getKeyboardView().invalidateKey(id);
        }
    }
    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        playSound(primaryCode);
        InputConnection ic = getCurrentInputConnection();
        switch(primaryCode) {
            case SPACE_PRIMARY_CODE:
                if (inputType == ALPHABETIC_KEYBOARD && isPredictable && autoCorrect) {
                    if (addPredictionToDictionary[0]) addToDictionary(predictions[0]);
                    writeWord(predictions[0]);
                } else {
                    ic.finishComposingText();
                    currentWordIsDone = true;
                    cursorPosition++;
                    ic.commitText(" ", 1);
                    checkIfIsNewSentence();
                    getPredictions();
                }
                break;
            case PREDICTION1:
                if (addPredictionToDictionary[2]) addToDictionary(predictions[2]);
                writeWord(predictions[2]);
                break;
            case PREDICTION2:
                int predictionId = autoCorrect ? 3 : 0;
                writeWord(predictions[predictionId]);
                addToDictionary(predictions[predictionId]);
                break;
            case PREDICTION3:
                if (addPredictionToDictionary[1]) addToDictionary(predictions[1]);
                writeWord(predictions[1]);
                break;
            case Keyboard.KEYCODE_DELETE:
                deleteLastChar();
                return;
            case Keyboard.KEYCODE_SHIFT:
                if (keyboard.isShifted()) {
                    capsLocked = !capsLocked;
                    if (capsLocked)
                        keyboard.getKeys().get(shiftKey).icon =
                                ContextCompat.getDrawable(this,
                                        R.drawable.ic_keyboard_capslock_red_24dp);
                    else {
                        enableShift(false);
                        keyboard.getKeys().get(shiftKey).icon = ContextCompat
                                .getDrawable(this, R.drawable.ic_keyboard_arrow_up_red_24dp);
                    }
                }
                else enableShift(true);
                return;
            case Keyboard.KEYCODE_DONE:
                ic.finishComposingText();
                currentWordIsDone = true;
                sendDefaultEditorAction(true);
                ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                break;
            case ALPHABETIC_KEYBOARD:
                changeKeyboard(ALPHABETIC_KEYBOARD);
                break;
            case ALT_KEYBOARD:
                changeKeyboard(ALT_KEYBOARD);
                break;
            case EMOJI_KEYBOARD:
                changeKeyboard(EMOJI_KEYBOARD);
                break;
            case SETTINGS:
                Intent intent = new Intent(this, SettingsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                break;
            default:
                String text = String.valueOf((char) primaryCode);
                addString(text);
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.back_to_alphabet)
            changeKeyboard(ALPHABETIC_KEYBOARD);
        else if (view.getId() == R.id.del) {
            deleteLastChar();
        } else if (view.getTag().equals("emojiKey")){
            String emoji = ((Button) view).getText().toString();
            addString(emoji);
            inputView.addEmoji(emoji);
            inputView.updateRecentEmojis();
        } else if (view.getTag().equals("menu_emoji")){
            inputView.setEmojiGrid(view.getId());
        }
    }

    @Override
    public void onLongPressListener(final Keyboard.Key popupKey) {
        if (inputType == ALPHABETIC_KEYBOARD) {
            switch (popupKey.codes[0]) {
                case InputService.PREDICTION1:
                case InputService.PREDICTION2:
                case InputService.PREDICTION3:
                case 32:
                    final String word = popupKey.label.toString().replace(" ", "");
                    Alert.show(new ContextThemeWrapper(this, R.style.KeymaxDialog),
                            getString(R.string.remove_word_confirmation, word),
                            getString(R.string.yes),
                            getString(R.string.no),
                            (dialog, which) -> {
                                dictionaryManager.removeWord(word);
                                dialog.dismiss();
                                updateSelection();
                            },
                            (dialog, which) -> dialog.dismiss(),
                            inputView.getWindowToken());
                    break;
            }
        }
    }

    @Override
    public void onPress(int primaryCode) {}
    @Override
    public void onRelease(int primaryCode) {}
    @Override
    public void onText(CharSequence text) {}
    @Override
    public void swipeDown() {}
    @Override
    public void swipeLeft() {}
    @Override
    public void swipeRight() {}
    @Override
    public void swipeUp() {}

    public void changeKeyboard(int type) {
        switch (type) {
            case ALT_KEYBOARD:
                inputType = ALT_KEYBOARD;
                keyboard = new KeymaxKeyboard(this, R.xml.alt_keyboard);
                inputView.getKeyboardView().setKeyboard(keyboard);
                inputView.showKeyboard();
                break;
            case EMOJI_KEYBOARD:
                inputType = EMOJI_KEYBOARD;
                inputView.showEmoji();
                inputView.setEmojiGrid(R.id.last_emoji);
                inputView.updateRecentEmojis();
                break;
            default:
                inputType = ALPHABETIC_KEYBOARD;
                keyboard = getAlphaNumericKeyboard();
                inputView.getKeyboardView().setKeyboard(keyboard);
                inputView.showKeyboard();
        }
        updateSelection();
    }

    private void enableShift(boolean value){
        if (value == keyboard.isShifted()) return;
        keyboard.setShifted(value);

        // Invalidate all alphabetic keys
        int offset = inputView.getKeyboardView().getKeyboard().getKeys().size() > 40 ? 0 : -10;
        try {
            for (int i = 15 + offset; i < 43 + offset; i++) {
                inputView.getKeyboardView().invalidateKey(i);
            }
            invalidatePredication();
        } catch (StringIndexOutOfBoundsException e) {}
    }

    public void writeWord(String word) {
        if (isPredictable) {
            if (currentWordIsDone) {
                lastWord = currentWord;
                currentWord = "";
            }
            currentWordIsDone = true;
            cursorPosition -= currentWord.length();
            currentWord = word.substring(0, word.length() - 1);
            cursorPosition += word.length();
            getCurrentInputConnection().commitText(word, 1);
            if (!capsLocked) enableShift(false);
            checkIfIsNewSentence();
            getPredictions();
            addFollowingWord();
        } else addString(word);
    }

    public void addString(String string) {
        if (keyboard.isShifted()) string = string.toUpperCase();
        if (isPredictable) {
            if (currentWordIsDone) {
                lastWord = currentWord;
                currentWord = "";
            }
            currentWordIsDone = false;
            currentWord += string;
            cursorPosition += string.length();
            getCurrentInputConnection().setComposingText(currentWord, 1);
            if (!capsLocked) enableShift(false);
            checkIfIsNewSentence();
            getPredictions();
        } else {
            getCurrentInputConnection().commitText(string, 1);
        }
    }

    public void addToDictionary(String word) {
        if (!isPredictable) return;
        word = word.substring(0, word.length() - 1);
        if (word.isEmpty()) return;
        if (isNewSentence && !capsLocked) word = word.toLowerCase();
        dictionaryManager.addWord(word);
    }

    private void addFollowingWord() {
        if (!isPredictable) return;
        if (isNewSentence) dictionaryManager.addFollowingWord("", currentWord);
        else dictionaryManager.addFollowingWord(lastWord, currentWord);
    }

    private void deleteLastChar() {
        if (!capsLocked) enableShift(false);
        InputConnection ic = getCurrentInputConnection();
        if (ic.getSelectedText(0) == null) {
            if (cursorPosition > 0) {
                cursorPosition--;
                int charToDelete = 1;
                if (!currentWordIsDone && currentWord.length() > 0) {
                    char lastChar = currentWord.charAt(currentWord.length() - 1);
                    if ((int) lastChar > 0xD83D) charToDelete = 2;  //0xD83D and up is for emoji
                    ic.finishComposingText();
                    currentWord = currentWord.substring(0, currentWord.length() - charToDelete);
                }
                ic.deleteSurroundingText(charToDelete, 0);
                updateSelection();
            }
        } else ic.commitText("", 1);
    }

    private void playSound(int keyCode){
        AudioManager am = (AudioManager)getSystemService(AUDIO_SERVICE);
        switch(keyCode){
            case 32:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_SPACEBAR);
                break;
            case Keyboard.KEYCODE_DONE:
            case 10:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_RETURN);
                break;
            case Keyboard.KEYCODE_DELETE:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_DELETE);
                break;
            default: am.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD);
        }
    }

    public KeymaxKeyboard getAlphaNumericKeyboard() {
        switch (PreferenceManager.getDefaultSharedPreferences(this)
                .getString(getString(R.string.key_keyboard_layout),
                        getString(R.string.default_keyboard))) {
            case "qwerty":
                return new KeymaxKeyboard(this, R.xml.qwerty);
            case "qwertz":
                return new KeymaxKeyboard(this, R.xml.qwertz);
            case "azerty":
                return new KeymaxKeyboard(this, R.xml.azerty);
            default:
                return new KeymaxKeyboard(this, R.xml.qwerty);
        }
    }
}
