package com.maxirozay.keymax.util;

import android.content.Context;
import android.content.DialogInterface;
import android.os.IBinder;
import android.app.AlertDialog;
import android.view.Window;
import android.view.WindowManager;

/**
 * Created by max on 6/29/17.
 */

public class Alert {

    public static void show(Context context,
                             String title,
                             String positiveButton,
                             String negativeButton,
                             DialogInterface.OnClickListener positiveListener,
                             DialogInterface.OnClickListener negativeListener,
                             IBinder token) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setPositiveButton(positiveButton, positiveListener);
        builder.setNegativeButton(negativeButton, negativeListener);
        AlertDialog alertDialog = builder.create();
        Window window = alertDialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.token = token;
        lp.type = WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG;
        window.setAttributes(lp);
        window.addFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        alertDialog.show();
    }
}
