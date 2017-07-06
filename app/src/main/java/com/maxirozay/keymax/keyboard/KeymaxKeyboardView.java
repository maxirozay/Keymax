package com.maxirozay.keymax.keyboard;

import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.util.AttributeSet;

import com.maxirozay.keymax.InputService;

/**
 * Created by max on 6/21/17.
 */

public class KeymaxKeyboardView extends KeyboardView {

    private OnKeymaxActionListener keymaxActionListener;

    public KeymaxKeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public KeymaxKeyboard getKeyboard() {
        return (KeymaxKeyboard)super.getKeyboard();
    }
    @Override
    protected boolean onLongPress(Keyboard.Key popupKey) {
        switch (popupKey.codes[0]) {
            case InputService.PREDICTION1:
            case InputService.PREDICTION2:
            case InputService.PREDICTION3:
            case 32:
                keymaxActionListener.onLongPressListener(popupKey);
                return true;
        }
        return super.onLongPress(popupKey);
    }

    public void setOnLongPressListener(OnKeymaxActionListener keymaxActionListener) {
        this.keymaxActionListener = keymaxActionListener;
    }
}
