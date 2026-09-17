package com.damithecreative.sidevol;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(48, 48, 48, 48);
        TextView title = new TextView(this);
        title.setText("SideVol");
        title.setTextSize(30);
        title.setGravity(Gravity.CENTER);
        TextView info = new TextView(this);
        info.setText("Quick Settings volume controls\n\nAdd the SideVol + and SideVol − tiles to your notification shade. Tap + to raise media volume or − to lower it.");
        info.setTextSize(17);
        info.setGravity(Gravity.CENTER);
        info.setPadding(0, 24, 0, 0);
        root.addView(title);
        root.addView(info);
        setContentView(root);
    }
}
