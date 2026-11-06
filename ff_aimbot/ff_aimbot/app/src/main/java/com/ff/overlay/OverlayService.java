package com.ff.overlay;

import android.app.*;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.WindowManager;

public class OverlayService extends Service {
    private WindowManager wm;
    private AimView overlay;
    private AimEngine engine;
    private StreamerMode streamer;

    @Override public IBinder onBind(Intent i) { return null; }

    @Override public void onCreate() {
        super.onCreate();
        streamer = new StreamerMode(this);
        streamer.start();

        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        overlay = new AimView(this, streamer);
        WindowManager.LayoutParams p = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            Build.VERSION.SDK_INT >= 26
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT);

        String ch = "ffsvc";
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel nc = new NotificationChannel(ch, "System", NotificationManager.IMPORTANCE_LOW);
            ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(nc);
        }
        Notification n = new Notification.Builder(this, ch)
            .setContentTitle("System Service")
            .setContentText("Running")
            .setSmallIcon(android.R.drawable.ic_menu_manage)
            .build();
        startForeground(1, n);

        wm.addView(overlay, p);
        engine = new AimEngine(this, streamer, overlay);
        engine.start();
    }

    @Override public void onDestroy() {
        if (engine != null) engine.stop();
        if (streamer != null) streamer.stop();
        if (overlay != null) wm.removeView(overlay);
        super.onDestroy();
    }
}
