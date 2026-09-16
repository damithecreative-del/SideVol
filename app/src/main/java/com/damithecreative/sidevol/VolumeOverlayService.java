package com.damithecreative.sidevol;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.os.Build;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.LinearLayout;
import android.widget.TextView;

public class VolumeOverlayService extends AccessibilityService {
    private static final int STREAM = AudioManager.STREAM_MUSIC;
    private AudioManager audio;
    private WindowManager wm;
    private TextView tab;
    private LinearLayout panel;
    private TextView percent;

    @Override public void onServiceConnected() {
        super.onServiceConnected();
        audio = (AudioManager) getSystemService(AUDIO_SERVICE);
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        createNotificationChannel();
        createOverlay();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel c = new NotificationChannel("sidevol", "SideVol", NotificationManager.IMPORTANCE_LOW);
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

    private WindowManager.LayoutParams params(int w, int h) {
        int type = Build.VERSION.SDK_INT >= 26 ? WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY : WindowManager.LayoutParams.TYPE_PHONE;
        return new WindowManager.LayoutParams(w, h, type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                android.graphics.PixelFormat.TRANSLUCENT);
    }

    private void createOverlay() {
        if (tab != null) return;
        tab = new TextView(this);
        tab.setText("VOL");
        tab.setTextColor(Color.WHITE);
        tab.setTextSize(11);
        tab.setGravity(Gravity.CENTER);
        tab.setTypeface(null, android.graphics.Typeface.BOLD);
        tab.setBackground(bg(Color.rgb(74,151,238), 14));
        tab.setElevation(dp(8));
        tab.setOnClickListener(v -> togglePanel());
        WindowManager.LayoutParams p = params(dp(48), dp(54));
        p.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
        wm.addView(tab, p);
    }

    private void togglePanel() {
        if (panel != null) {
            wm.removeView(panel); panel = null; return;
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
        panel.addView(minus, new LinearLayout.LayoutParams(dp(46), dp(48)));
        panel.addView(percent, new LinearLayout.LayoutParams(dp(64), dp(48)));
        panel.addView(plus, new LinearLayout.LayoutParams(dp(46), dp(48)));
        panel.addView(close, new LinearLayout.LayoutParams(dp(40), dp(48)));
        updatePercent();
        WindowManager.LayoutParams p = params(dp(206), dp(60));
        p.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
        p.x = dp(50);
        wm.addView(panel, p);
    }

    private TextView button(String text) {
        TextView v = new TextView(this);
        v.setText(text); v.setTextSize(22); v.setTextColor(Color.rgb(35,35,35)); v.setGravity(Gravity.CENTER);
        return v;
    }

    private void change(int direction) {
        if (audio == null) return;
        audio.adjustStreamVolume(STREAM, direction > 0 ? AudioManager.ADJUST_RAISE : AudioManager.ADJUST_LOWER, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        updatePercent();
    }

    private void updatePercent() {
        if (percent == null || audio == null) return;
        int current = audio.getStreamVolume(STREAM);
        int max = audio.getStreamMaxVolume(STREAM);
        percent.setText((max == 0 ? 0 : Math.round(current * 100f / max)) + "%");
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) { }
    @Override public void onInterrupt() { }

    @Override public void onDestroy() {
        if (wm != null) {
            try { if (panel != null) wm.removeView(panel); } catch (Exception ignored) {}
            try { if (tab != null) wm.removeView(tab); } catch (Exception ignored) {}
        }
        super.onDestroy();
    }
}
