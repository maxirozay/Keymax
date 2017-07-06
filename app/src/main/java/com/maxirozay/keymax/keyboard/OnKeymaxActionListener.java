package com.maxirozay.keymax.keyboard;

import android.inputmethodservice.Keyboard;

/**
 * Created by Maxime Rossier on 31.08.2015.
 */
public interface OnKeymaxActionListener {
    void onLongPressListener(Keyboard.Key popupKey);
}
