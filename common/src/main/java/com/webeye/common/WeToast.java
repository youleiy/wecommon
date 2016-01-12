package com.webeye.common;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

public class WeToast {
    private static final int DEFAULT_DURATION = 1500; // 1.5 S
    private static Toast mToast;
    private static Handler mHandler = new Handler();
    private static Runnable r = new Runnable() {
        public void run() {
            mToast.cancel();
        }
    };

    private static void showToast(Context mContext, String text, int duration) {
        mHandler.removeCallbacks(r);
        if (mToast != null) {
            mToast.setText(text);
        }
        else {
            mToast = Toast.makeText(mContext, text, Toast.LENGTH_SHORT);
        }
        mHandler.postDelayed(r, duration);

        mToast.show();
    }

    private static void showToast(Context mContext, int resId, int duration) {
        showToast(mContext, mContext.getResources().getString(resId), duration);
    }
    public static void showToast(Context mContext, String text) {
        showToast(mContext, text, DEFAULT_DURATION);
    }
    public static void showToast(Context mContext, int resId) {
        showToast(mContext, resId, DEFAULT_DURATION);
    }


}