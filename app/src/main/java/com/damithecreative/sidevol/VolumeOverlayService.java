package com.damithecreative.sidevol;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

public class VolumeOverlayService extends Service {
    private static final String CHANNEL_ID = "sidevol";
    private static final int STREAM = AudioManager.STREAM_MUSIC;
    private WindowManager wm;
    private AudioManager audio;
    private TextView tab;
    private LinearLayout panel;
    private TextView percent;
    private WindowManager.LayoutParams tabParams;
    private WindowManager.LayoutParams panelParams;

    @Override public void onCreate() {
        super.onCreate();
        audio = (AudioManager) getSystemService(AUDIO_SERVICE);
        createNotificationChannel();
        startForeground(7, buildNotification());
        if (Settings.canDrawOverlays(this)) createOverlay();
    }

    private Notification buildNotification() {
        Intent i = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        if (Build.VERSION.SDK_INT >= 26) {
            return new Notification.Builder(this, CHANNEL_ID)
                    .setSmallIcon(com.damithecreative.sidevol.R.drawable.ic_volume)
                    .setContentTitle("SideVol")
                    .setContentText("Floating volume control is active")
                    .setContentIntent(pi)
                    .setOngoing(true)
                    .build();
        }
        return new Notification.Builder(this)
                .setSmallIcon(com.damithecreative.sidevol.R.drawable.ic_volume)
                .setContentTitle("SideVol")
                .setContentText("Floating volume control is active")
                .setContentIntent(pi)
                .setOngoing(true)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel c = new NotificationChannel(CHANNEL_ID, "SideVol", NotificationManager.IMPORTANCE_LOW);
            c.setDescription("SideVol floating volume control");
            ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(c);
        }
    }

    private int dp(int value) { return (int) (value * getResources().getDisplayMetrics().density + 0.5f); }

    private GradientDrawable bg(int color, float radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp((int) radius));
        return d;
    }

    private void createOverlay() {
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);

        tab = new TextView(this);
        tab.setText("VOL");
        tab.setTextColor(Color.WHITE);
        tab.setTextSize(11);
        tab.setGravity(Gravity.CENTER);
        tab.setTypeface(null, android.graphics.Typeface.BOLD);
        tab.setBackground(bg(Color.rgb(74,151,238), 14));
        tab.setElevation(dp(8));
        tab.setOnClickListener(v -> togglePanel());

        int type = Build.VERSION.SDK_INT >= 26 ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_PHONE;
        tabParams = new WindowManager.LayoutParams(dp(48), dp(54), type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                android.graphics.PixelFormat.TRANSLUCENT);
        tabParams.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
        tabParams.x = 0;
        tabParams.y = 0;
        wm.addView(tab, tabParams);
    }

    private void togglePanel() {
        if (panel != null) {
            wm.removeView(panel);
            panel = null;
            return;
        }
        panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.HORIZONTAL);
        panel.setGravity(Gravity.CENTER);
        panel.setPadding(dp(8), dp(6), dp(8), dp(6));
        panel.setBackground(bg(Color.WHITE, 16));
        panel.setElevation(dp(10));

        TextView minus = button("−");
        percent = button("0%");
        TextView plus = button("+");
        TextView close = button("×");

        minus.setOnClickListener(v -> change(-1));
        plus.setOnClickListener(v -> change(1));
        close.setOnClickListener(v -> togglePanel());
        percent.setOnClickListener(v -> updatePercent());

        panel.addView(minus, new LinearLayout.LayoutParams(dp(46), dp(48)));
        panel.addView(percent, new LinearLayout.LayoutParams(dp(64), dp(48)));
        panel.addView(plus, new LinearLayout.LayoutParams(dp(46), dp(48)));
        panel.addView(close, new LinearLayout.LayoutParams(dp(40), dp(48)));
        updatePercent();

        int type = Build.VERSION.SDK_INT >= 26 ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_PHONE;
        panelParams = new WindowManager.LayoutParams(dp(206), dp(60), type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                android.graphics.PixelFormat.TRANSLUCENT);
        panelParams.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
        panelParams.x = dp(50);
        panelParams.y = 0;
        wm.addView(panel, panelParams);
    }

    private TextView button(String text) {
        TextView v = new TextView(this);
        v.setText(text);
        v.setTextSize(22);
        v.setTextColor(Color.rgb(35,35,35));
        v.setGravity(Gravity.CENTER);
        return v;
    }

    private void change(int direction) {
        audio.adjustStreamVolume(STREAM, direction > 0 ? AudioManager.ADJUST_RAISE : AudioManager.ADJUST_LOWER, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        updatePercent();
    }

    private void updatePercent() {
        if (percent == null) return;
        int current = audio.getStreamVolume(STREAM);
        int max = audio.getStreamMaxVolume(STREAM);
        int p = max == 0 ? 0 : Math.round(current * 100f / max);
        percent.setText(p + "%");
    }

    @Override public void onDestroy() {
        if (wm != null) {
            try { if (panel != null) wm.removeView(panel); } catch (Exception ignored) {}
            try { if (tab != null) wm.removeView(tab); } catch (Exception ignored) {}
        }
        super.onDestroy();
    }

    @Override public IBinder onBind(Intent intent) { return null; }
}
