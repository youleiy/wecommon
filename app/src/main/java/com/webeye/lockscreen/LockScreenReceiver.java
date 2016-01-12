package com.webeye.lockscreen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.webeye.common.WeLog;

public class LockScreenReceiver extends BroadcastReceiver {

    private static final String TAG = "LockScreenReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            WeLog.e(TAG, "ACTION_SCREEN_OFF");
            // 隐式启动锁屏页
            // Intent intent1 = new Intent("android.intent.lockdemo");
            Intent intent1 = new Intent(context, LockScreenActivity.class);
            intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent1);
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            WeLog.e(TAG, "ACTION_SCREEN_ON");
        } else if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            WeLog.e(TAG, "ACTION_BOOT_COMPLETED");
        }

    }

}
