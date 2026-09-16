package com.damithecreative.sidevol;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.accessibility.AccessibilityManager;
import java.util.List;

public class MainActivity extends Activity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showScreen();
    }

    @Override protected void onResume() { super.onResume(); updateButton(); }

    private void showScreen() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL); root.setGravity(Gravity.CENTER); root.setPadding(48,48,48,48);
        TextView title = new TextView(this); title.setText("SideVol"); title.setTextSize(30); title.setGravity(Gravity.CENTER);
        TextView info = new TextView(this); info.setText("Floating volume controls without the physical buttons.\n\nSideVol uses an accessibility overlay because this device does not expose the normal overlay permission."); info.setTextSize(16); info.setGravity(Gravity.CENTER); info.setPadding(0,24,0,24);
        Button button = new Button(this); button.setOnClickListener(v -> openAccessibilitySettings());
        root.addView(title); root.addView(info); root.addView(button); setContentView(root);
        updateButton(button);
    }

    private void updateButton() { updateButton((Button) findViewById(android.R.id.button1)); }
    private void updateButton(Button b) { if (b != null) b.setText(isServiceEnabled() ? "SideVol is active" : "Enable SideVol"); }

    private boolean isServiceEnabled() {
        AccessibilityManager am = (AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> list = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);
        String target = new ComponentName(this, VolumeOverlayService.class).flattenToString();
        for (AccessibilityServiceInfo info : list) if (info.getResolveInfo().serviceInfo.getComponentName().flattenToString().equals(target)) return true;
        return false;
    }

    private void openAccessibilitySettings() { startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)); }
}
