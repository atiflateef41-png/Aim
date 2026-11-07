package com.ff.overlay;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final int REQ_OVERLAY = 100;
    private static final int REQ_CAPTURE = 101;
    private MediaProjectionManager mpm;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        mpm = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 50, 50, 50);

        Button btnOverlay = new Button(this);
        btnOverlay.setText("1. Grant Overlay");
        btnOverlay.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(this)) {
                startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())), REQ_OVERLAY);
            } else {
                Toast.makeText(this, "Overlay Granted", Toast.LENGTH_SHORT).show();
            }
        });

        Button btnStart = new Button(this);
        btnStart.setText("2. Start Hack");
        btnStart.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "Grant Overlay first!", Toast.LENGTH_SHORT).show();
                return;
            }
            // Ask for screen capture permission
            startActivityForResult(mpm.createScreenCaptureIntent(), REQ_CAPTURE);
        });

        layout.addView(btnOverlay);
        layout.addView(btnStart);
        setContentView(layout);
    }

    @Override protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (req == REQ_CAPTURE && res == RESULT_OK) {
            Intent i = new Intent(this, OverlayService.class);
            i.putExtra("code", res);
            i.putExtra("data", data);
            startForegroundService(i);
        }
    }
}
