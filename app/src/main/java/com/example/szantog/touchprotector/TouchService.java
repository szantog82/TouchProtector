package com.example.szantog.touchprotector;


import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

public class TouchService extends Service {

    private WindowManager wm;
    private LinearLayout NoTouchLayout;

    private LinearLayout mainscreen_layout;
    private WindowManager.LayoutParams mainscreen_params;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        BroadcastReceiver powerOffReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction() == Intent.ACTION_SCREEN_OFF) {
                    StopNoTouch();
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(powerOffReceiver, intentFilter);

        wm = (WindowManager) getSystemService(WINDOW_SERVICE);

        Display display = wm.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        final float sizeX = point.x;
        final float sizeY = point.y;

        mainscreen_params = new WindowManager.LayoutParams();
        mainscreen_params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        mainscreen_params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        mainscreen_params.format = PixelFormat.TRANSLUCENT;
        mainscreen_params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mainscreen_params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mainscreen_params.gravity = Gravity.TOP | Gravity.LEFT;
        mainscreen_params.horizontalMargin = 0.3f;
        mainscreen_params.verticalMargin = 0.3f;

        ImageView start_btn = new ImageView(this);
        start_btn.setImageResource(R.drawable.protect64x64);
        start_btn.setAlpha(0.65f);

        ImageView quit_btn = new ImageView(this);
        quit_btn.setImageResource(R.drawable.quit64x64);
        quit_btn.setAlpha(0.45f);

        mainscreen_layout = new LinearLayout(this);
        mainscreen_layout.addView(start_btn);
        mainscreen_layout.addView(quit_btn);

        wm.addView(mainscreen_layout, mainscreen_params);

        NoTouchLayout = new LinearLayout(this) {

            @Override
            public boolean dispatchKeyEvent(KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP) {
                    StopNoTouch();
                } else if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN) {
                    StopNoTouch();
                } else if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                }
                return true;
            }
        };


        NoTouchLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        NoTouchLayout.setBackground(getResources().getDrawable(R.drawable.screen_frame));

        start_btn.setOnTouchListener(new View.OnTouchListener() {

            long downTime;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        downTime = System.currentTimeMillis();
                        break;
                    }
                    case MotionEvent.ACTION_MOVE: {
                        mainscreen_params.horizontalMargin = 0.85f * motionEvent.getRawX() / sizeX;
                        mainscreen_params.verticalMargin = 0.85f * motionEvent.getRawY() / sizeY;
                        wm.updateViewLayout(mainscreen_layout, mainscreen_params);
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        if (System.currentTimeMillis() - downTime < 100) {
                            Toast.makeText(getApplicationContext(), "Protector activated", Toast.LENGTH_SHORT).show();
                            StartNoTouch();
                        }
                        break;
                    }
                }
                return true;
            }
        });

        quit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Exit();
            }
        });
    }

    private void StartNoTouch() {
        wm.removeView(mainscreen_layout);

        WindowManager.LayoutParams no_touch_params = new WindowManager.LayoutParams();
        no_touch_params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        no_touch_params.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LOCAL_FOCUS_MODE;
        no_touch_params.format = PixelFormat.TRANSLUCENT;
        no_touch_params.height = WindowManager.LayoutParams.MATCH_PARENT;
        no_touch_params.width = WindowManager.LayoutParams.MATCH_PARENT;

        wm.addView(NoTouchLayout, no_touch_params);
    }

    private void StopNoTouch() {
        Toast.makeText(getApplicationContext(), "Protector deactivated", Toast.LENGTH_SHORT).show();
        wm.removeView(NoTouchLayout);
        try {
            wm.addView(mainscreen_layout, mainscreen_params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void Exit() {
        try {
            wm.removeView(NoTouchLayout);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            wm.removeView(mainscreen_layout);
        } catch (Exception e) {
            e.printStackTrace();
        }
        stopSelf();
        System.exit(0);
    }
}
