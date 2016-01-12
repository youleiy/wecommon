package com.webeye.lockscreen;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.webeye.common.FastBlur;
import com.webeye.common.WeLog;
import com.webeye.common.WeTimeUtils;

/**
 * 锁屏页
 *
 * @author Thunder
 */
public class LockScreenActivity extends Activity {
    private static final String TAG = "LockScreenActivity";

    public static boolean isStarted = false; //判断锁屏页面是否打开状态
    private int mScreenWidth;
    private int mScreenHeight;
    private ImageView mKeyImageView;
    private ImageView mLockerImageView;
    private ImageView mViewer;
    private TextView timeNow;
    private TextView dateNow;
    private ImageView adContent;
    private RelativeLayout rootView;

    private Rect mLockerRect;
    private Rect mViewerRect;

    private int mScreenOffTime;
    private Bitmap mAdBmp;

    private String mImages[] = {"001.jpg", "002.jpg", "003.jpg", "004.jpg"};
    private int mCurrent = 0;

    /**
     * 屏蔽4.0+home键, 某些机型可以，不能适用所有版本和机型
     */
    // private static final int FLAG_HOMEKEY_DISPATCHED = 0x80000000;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 屏蔽4.0+home键, 某些机型可以，不能适用所有版本和机型
        // this.getWindow().setFlags(FLAG_HOMEKEY_DISPATCHED, FLAG_HOMEKEY_DISPATCHED);// 屏蔽4.0+ home键

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_lockscreen);

        isStarted = true;
        mKeyImageView = (ImageView) findViewById(R.id.key);

        timeNow = (TextView) findViewById(R.id.time);
        Typeface typeFace = Typeface.createFromAsset(getAssets(), "Roboto-Thin.ttf");
        timeNow.setTypeface(typeFace);
        timeNow.setText(WeTimeUtils.getTime());

        dateNow = (TextView) findViewById(R.id.date);
        typeFace = Typeface.createFromAsset(getAssets(), "Roboto-Light.ttf");
        dateNow.setTypeface(typeFace);
        dateNow.setText(WeTimeUtils.getDate());

        mLockerImageView = (ImageView) findViewById(R.id.locker);
        mViewer = (ImageView) findViewById(R.id.operation);
        mViewer.setScaleType(ImageView.ScaleType.FIT_XY);
        // keyImageView图片的touch事件
        mKeyImageView.setOnTouchListener(mKeyMoveListener);

        mScreenWidth = getWindowManager().getDefaultDisplay().getWidth();
        mScreenHeight = getWindowManager().getDefaultDisplay().getHeight();

        // Get sleep time
        mScreenOffTime = getScreenOffTime();

        adContent = (ImageView) findViewById(R.id.ad_content);
        rootView = (RelativeLayout) findViewById(R.id.lockscreen_root);
        /*if (null != mBackground) {
            getBackground(mBackground);
        }*/
        applyBlur();
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
            WeLog.e(TAG, "On Home Key");
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
        WeLog.e(TAG, "onBackPressed");
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
                        int[] lockPos = new int[2];
                        mLockerImageView.getLocationOnScreen(lockPos);
                        mLockerRect = new Rect(lockPos[0], lockPos[1],
                                lockPos[0] + mLockerImageView.getWidth(),
                                lockPos[1] + mLockerImageView.getHeight());
                    }
                    // Get Viewer pos
                    if (null == mViewerRect) {
                        int[] pos = new int[2];
                        mViewer.getLocationOnScreen(pos);
                        mViewerRect = new Rect(pos[0], pos[1],
                                pos[0] + mViewer.getWidth(),
                                pos[1] + mViewer.getHeight());
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

                    /*if (top < 0) {
                        top = 0;
                        bottom = top + v.getHeight();
                    }*/

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

                    if (mViewerRect.contains(lastX, lastY)) {
//                        changeAd();
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

    private void applyBlur() {
        View view = getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.buildDrawingCache(true);
        /**
         * 获取当前窗口快照，相当于截屏
         */
//        Bitmap bmp1 = view.getDrawingCache();
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
        // 获取当前壁纸
        Drawable wallpaperDrawable = wallpaperManager.getDrawable();
        // 将Drawable,转成Bitmap
        Bitmap bmp1 = ((BitmapDrawable) wallpaperDrawable).getBitmap();

        Rect frame = new Rect();
        getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        int height = frame.top;
        /**
         * 除去状态栏和标题栏
         */
        Bitmap bmp2 = Bitmap.createBitmap(bmp1, 0, height, bmp1.getWidth(), bmp1.getHeight() - height);
        blur(bmp2, rootView);
    }

    private void blur(Bitmap bkg, View view) {
        long startMs = System.currentTimeMillis();
        float scaleFactor = 8;//图片缩放比例；
        float radius = 20;//模糊程度

        Bitmap overlay = Bitmap.createBitmap(
                (int) (view.getMeasuredWidth() / scaleFactor),
                (int) (view.getMeasuredHeight() / scaleFactor),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(overlay);
        canvas.translate(-view.getLeft() / scaleFactor, -view.getTop() / scaleFactor);
        canvas.scale(1 / scaleFactor, 1 / scaleFactor);
        Paint paint = new Paint();
        paint.setFlags(Paint.FILTER_BITMAP_FLAG);
        canvas.drawBitmap(bkg, 0, 0, paint);

        overlay = FastBlur.doBlur(overlay, (int) radius, true);
        view.setBackground(new BitmapDrawable(getResources(), overlay));
        /**
         * 打印高斯模糊处理时间，如果时间大约16ms，用户就能感到到卡顿，时间越长卡顿越明显，如果对模糊完图片要求不高，可是将scaleFactor设置大一些。
         */
        WeLog.i(TAG, "blur time:" + (System.currentTimeMillis() - startMs));
    }
}