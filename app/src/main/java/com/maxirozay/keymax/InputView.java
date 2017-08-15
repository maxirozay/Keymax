package com.maxirozay.keymax;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.maxirozay.keymax.keyboard.KeymaxKeyboardView;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Maxime Rossier on 6/21/17.
 */

public class InputView extends LinearLayout {

    private KeymaxKeyboardView keyboardView;
    private Button[] emojiMenuButtons = new Button[5];
    private GridLayout[] emojiGrid = new GridLayout[5];
    private int[] lastEmojis = new int[40];
    private List<String> recentEmojis = new LinkedList<>();
    private int[] humanEmojis = {0x2764, 0x270C, 0x270B, 0x270A, 0x1F440, 0x261D, 0x1F595};
    private int[] natureEmojis = {0x1F30F, 0x1F311, 0x1F313, 0x1F314, 0x1F315, 0x1F319, 0x1F31B,
            0x1F31F, 0x1F320, 0x1F334, 0x1F335, 0x1F339, 0x1F33B, 0x1F33D};
    private int widthEmoji;
    private OnClickListener clickListener;

    public InputView(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public void onFinishInflate(){
        super.onFinishInflate();
        keyboardView = findViewById(R.id.keyboardView);
        keyboardView.setProximityCorrectionEnabled(false);
        initEmojiLayout();
    }

    private void initEmojiLayout(){
        for (int i = 0; i < lastEmojis.length; i++)
            lastEmojis[i] = 32;

        emojiMenuButtons[0] = findViewById(R.id.last_emoji);
        emojiMenuButtons[0].setText(String.valueOf(Character.toChars(0x1F550)));
        emojiMenuButtons[1] = findViewById(R.id.emoticons);
        emojiMenuButtons[1].setText(String.valueOf(Character.toChars(0x1F60A)));
        emojiMenuButtons[2] = findViewById(R.id.human);
        emojiMenuButtons[2].setText(String.valueOf(Character.toChars(0x270C)));
        emojiMenuButtons[3] = findViewById(R.id.nature);
        emojiMenuButtons[3].setText(String.valueOf(Character.toChars(0x1F30F)));
        emojiMenuButtons[4] = findViewById(R.id.transport);
        emojiMenuButtons[4].setText(String.valueOf(Character.toChars(0x1F697)));
        initEmojiGrid();

        keyboardView = findViewById(R.id.keyboardView);
        keyboardView.setProximityCorrectionEnabled(false);
    }

    public void setOnClickListener(OnClickListener listener) {
        clickListener = listener;
        for (Button button : emojiMenuButtons) button.setOnClickListener(listener);
        findViewById(R.id.del).setOnClickListener(listener);
        findViewById(R.id.back_to_alphabet).setOnClickListener(listener);
    }

    public void setPreview(boolean isEnable) {
        keyboardView.setPreviewEnabled(isEnable);
    }

    public KeymaxKeyboardView getKeyboardView() {
        return keyboardView;
    }

    public void showKeyboard(){
        findViewById(R.id.emojiView).setVisibility(View.GONE);
        keyboardView.setVisibility(View.VISIBLE);
    }

    public void showEmoji(){
        findViewById(R.id.emojiView).setVisibility(View.VISIBLE);
        keyboardView.setVisibility(View.GONE);
    }

    private void initEmojiGrid(){
        new Thread(){
            public void run(){
                Display display = ((WindowManager) getContext().getSystemService(
                        Activity.WINDOW_SERVICE)).getDefaultDisplay();
                int orientation = display.getRotation();
                int col;
                if (orientation == Surface.ROTATION_90 || orientation == Surface.ROTATION_270) {
                    widthEmoji = (int) ((getContext().getResources().getDisplayMetrics().
                            widthPixels - 8 * getContext().getResources().
                            getDisplayMetrics().density) / 12);
                    col = 12;
                }
                else {
                    widthEmoji = (int) ((getContext().getResources().getDisplayMetrics().
                            widthPixels - 4 * getContext().getResources().
                            getDisplayMetrics().density) / 8);
                    col = 8;
                }
                for (int i = 0; i < 5; i++) {
                    emojiGrid[i] = new GridLayout(getContext());
                    emojiGrid[i].setColumnCount(col);
                }
                //0x2764
                addEmojiList(emojiGrid[0], lastEmojis);
                addEmojiList(emojiGrid[1], 0x1F911, 0x1F918);
                addEmojiList(emojiGrid[1], 0x1F600, 0x1F640);
                addEmojiList(emojiGrid[1], 0x1F645, 0x1F64F);
                addEmojiList(emojiGrid[1], 0x1F466, 0x1F483);
                addEmojiList(emojiGrid[2], humanEmojis);
                addEmojiList(emojiGrid[2], 0x1F442, 0x1F465);
                addEmojiList(emojiGrid[2], 0x1F484, 0x1F52F);
                addEmojiList(emojiGrid[3], natureEmojis);
                addEmojiList(emojiGrid[3], 0x1F40C, 0x1F43E);
                addEmojiList(emojiGrid[3], 0x1F340, 0x1F370);
                addEmojiList(emojiGrid[4], 0x1F680, 0x1F6C0);
                addEmojiList(emojiGrid[4], 0x2194, 0x2194);
                addEmojiList(emojiGrid[4], 0x1F374, 0x1F393);
                addEmojiList(emojiGrid[4], 0x1F3A0, 0x1F3CA);
            }
        }.start();
    }

    //add a sequence of emoji/unicode between two unicode value
    private void addEmojiList(GridLayout gridLayout, int from, int to){
        for (int i=from; i < to; i++) {
            addEmojiButton(gridLayout, i);
        }
    }

    //add a list of emoji to a GridLayout
    private void addEmojiList(GridLayout gridLayout, int[] unicodes){
        for (int unicode : unicodes) {
            addEmojiButton(gridLayout, unicode);
        }
    }

    //add an emojiButton to a GridLayout
    private void addEmojiButton(GridLayout gridLayout, int unicode){
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        //params.setMargins(1,1,1,1);
        params.width = widthEmoji;
        Button emoji = new Button(getContext());
        emoji.setTag("emojiKey");
        emoji.setTextSize(30f);
        emoji.setPadding(0, 0, 0, 0);
        emoji.setBackground(null);
        emoji.setText(String.valueOf(Character.toChars(unicode)));
        emoji.setOnClickListener(clickListener);
        gridLayout.addView(emoji, params);
    }

    protected void addEmoji(String emoji) {
        int index = recentEmojis.indexOf(emoji);
        if (index > 0) recentEmojis.remove(index);
        recentEmojis.add(0, emoji);
    }

    protected void updateRecentEmojis() {
        for (int i = 0; i < lastEmojis.length; i++) {
            if (i < recentEmojis.size())
                ((TextView) emojiGrid[0].getChildAt(i)).setText(recentEmojis.get(i));
            else
                ((TextView) emojiGrid[0].getChildAt(i)).setText(" ");
        }
    }

    //set the emoji gridlayout
    protected void setEmojiGrid(int id) {
        findViewById(R.id.emojiView).setLayoutParams(
                new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                        keyboardView.getHeight()));
        ScrollView scrollView = findViewById(R.id.emojiGrid);
        scrollView.scrollTo(0,0);
        scrollView.removeAllViews();
        switch (id) {
            case R.id.emoticons:
                scrollView.addView(emojiGrid[1]);
                break;
            case R.id.human:
                scrollView.addView(emojiGrid[2]);
                break;
            case R.id.nature:
                scrollView.addView(emojiGrid[3]);
                break;
            case R.id.transport:
                scrollView.addView(emojiGrid[4]);
                break;
            default:
                scrollView.addView(emojiGrid[0]);
        }
    }

    public List<String> getRecentEmojis() {
        return recentEmojis;
    }

}
