package com.webeye.lockscreen;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/**
 * 本界面只是App启动后的第一个界面，显示一个按钮，点击触发锁屏监听
 * 本界面并非锁屏页，也不是辅助锁屏定制的Home页
 *
 * @author Thunder
 */
public class LaunchActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_launch);
        startLockScreen();
        finish();
    }

    /**
     * 启动锁屏 按钮的点击响应
     *
     * @param v
     */
    public void startLockScreen(View v) {
        Intent sintent = new Intent();
        sintent.setClass(this, LockScreenService.class);
        startService(sintent);
    }

    public void startLockScreen() {
        Intent intent = new Intent();
        intent.setClass(this, LockScreenService.class);
        startService(intent);

        // 启动锁屏界面
        Intent intent1 = new Intent(this, LockScreenActivity.class);
        intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent1);
    }
}
