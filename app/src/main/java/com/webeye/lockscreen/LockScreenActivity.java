package com.webeye.lockscreen;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 锁屏页
 *
 * @author Thunder
 */
public class LockScreenActivity extends Activity{
    private static final String TAG = "LockScreenActivity";

    public static boolean isStarted = false; //判断锁屏页面是否打开状态
    private int mScreenWidth;
    private int mScreenHeight;
    private ImageView mKeyImageView;
    private ImageView mLockerImageView;
    private TextView timeNow;
    private TextView dateNow;

    private int[] mLockerPos = new int[2];
    private Rect mLockerRect;

    private int mSleepTime;

    /**
     * 屏蔽4.0+home键, 某些机型可以，不能适用所有版本和机型
     */
    // private static final int FLAG_HOMEKEY_DISPATCHED = 0x80000000;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        // 屏蔽4.0+home键, 某些机型可以，不能适用所有版本和机型
        // this.getWindow().setFlags(FLAG_HOMEKEY_DISPATCHED, FLAG_HOMEKEY_DISPATCHED);// 屏蔽4.0+ home键

        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_lockscreen);

        isStarted = true;
        mKeyImageView = (ImageView) findViewById(R.id.key);

        timeNow = (TextView) findViewById(R.id.time);
        Typeface typeFace = Typeface.createFromAsset(getAssets(), "Roboto-Thin.ttf");
        timeNow.setTypeface(typeFace);
        timeNow.setText(TimeUtils.getTime());

        dateNow = (TextView) findViewById(R.id.date);
        typeFace = Typeface.createFromAsset(getAssets(), "Roboto-Light.ttf");
        dateNow.setTypeface(typeFace);
        dateNow.setText(TimeUtils.getDate());

        mLockerImageView = (ImageView) findViewById(R.id.locker);
        // keyImageView图片的touch事件
        mKeyImageView.setOnTouchListener(mKeyMoveListener);

        mScreenWidth = getWindowManager().getDefaultDisplay().getWidth();
        mScreenHeight = getWindowManager().getDefaultDisplay().getHeight();

        //
        mSleepTime = getScreenOffTime();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {

        if ((keyCode == KeyEvent.KEYCODE_HOME)) {
            // Key code constant: Home key. This key is handled by the framework
            // and is never delivered to applications.
            Log.e(TAG, "On Home Key");
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onAttachedToWindow() {
    /*    final int TYPE_KEYGUARD = 2004; // WindowManager.LayoutParams.TYPE_KEYGUARD
        this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);// Android4.0以下屏蔽Home键*/
        super.onAttachedToWindow();
    }

    @Override
    public void onBackPressed() {
        Log.e(TAG, "onBackPressed");
        return;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * 震动
     */
    private void vibrate() {
        Vibrator vibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(200);
    }

    private View.OnTouchListener mKeyMoveListener = new View.OnTouchListener() {
        int lastX, lastY;
        int oldLeft = 0;
        int oldTop = 0;
        int oldRight = 0;
        int oldBottom = 0;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    lastX = (int) event.getRawX();
                    lastY = (int) event.getRawY();

                    // Get locker pos
                    if (null == mLockerRect) {
                        mLockerImageView.getLocationOnScreen(mLockerPos);
                        mLockerRect = new Rect(mLockerPos[0], mLockerPos[1],
                                mLockerPos[0] + mLockerImageView.getWidth(),
                                mLockerPos[1] + mLockerImageView.getHeight());
                    }
                    oldLeft = v.getLeft();
                    oldTop = v.getTop();
                    oldRight = v.getRight();
                    oldBottom = v.getBottom();
                    break;

                case MotionEvent.ACTION_MOVE:
                    int dx = (int) event.getRawX() - lastX;
                    int dy = (int) event.getRawY() - lastY;

                    int left = v.getLeft() + dx;
                    int top = v.getTop() + dy;
                    int right = v.getRight() + dx;
                    int bottom = v.getBottom() + dy;
                    // 设置不能出界
                    if (left < 0) {
                        left = 0;
                        right = left + v.getWidth();
                    }

                    if (right > mScreenWidth) {
                        right = mScreenWidth;
                        left = right - v.getWidth();
                    }

                    if (top < 0) {
                        top = 0;
                        bottom = top + v.getHeight();
                    }

                    if (bottom > mScreenHeight) {
                        bottom = mScreenHeight;
                        top = bottom - v.getHeight();
                    }

                    v.layout(left, top, right, bottom);

                    lastX = (int) event.getRawX();
                    lastY = (int) event.getRawY();

                    if (mLockerRect.contains(lastX, lastY)) {
                        onUnlock();
                    }

                    break;

                case MotionEvent.ACTION_UP:
                    // 如果松开手时未重叠，则钥匙图片返回原位置
                    if (!mLockerRect.contains(lastX, lastY)) {
                        v.layout(oldLeft, oldTop, oldRight, oldBottom);
                    }
                    break;
            }
            return true;
        }
    };

    private void onUnlock() {
        //v.setVisibility(View.GONE);
        vibrate();// 震动
        isStarted = false;
        finish();
    }

    /**
     * 获得休眠时间 毫秒
     */
    private int getScreenOffTime() {
        int screenOffTime = 0;
        try {
            screenOffTime = Settings.System.getInt(getContentResolver(),
                    Settings.System.SCREEN_OFF_TIMEOUT);
        } catch (Exception localException) {

        }
        return screenOffTime;
    }
}