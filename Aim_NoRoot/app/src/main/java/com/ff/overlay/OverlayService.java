package com.ff.overlay;

import android.app.*;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.IBinder;
import android.view.WindowManager;

public class OverlayService extends Service {
    private WindowManager wm;
    private AimView overlay;
    private MediaProjection mp;
    private VirtualDisplay vd;
    private ImageReader imageReader;

    @Override public IBinder onBind(Intent i) { return null; }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) return START_NOT_STICKY;
        
        String ch = "ffsvc";
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel nc = new NotificationChannel(ch, "System", NotificationManager.IMPORTANCE_LOW);
            ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(nc);
        }
        Notification n = new Notification.Builder(this, ch)
            .setContentTitle("System Service").setContentText("Running")
            .setSmallIcon(android.R.drawable.ic_menu_info_details).build();
        startForeground(1, n);

        // Setup Overlay
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        overlay = new AimView(this);
        WindowManager.LayoutParams p = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT,
            Build.VERSION.SDK_INT >= 26 ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT);
        wm.addView(overlay, p);

        // Setup Screen Capture
        int resultCode = intent.getIntExtra("code", 0);
        Intent data = intent.getParcelableExtra("data");
        MediaProjectionManager mpm = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        mp = mpm.getMediaProjection(resultCode, data);

        int density = getResources().getDisplayMetrics().densityDpi;
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;

        imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2);
        vd = mp.createVirtualDisplay("ScreenCapture", width, height, density,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, imageReader.getSurface(), null, null);

        // Start image processing
        imageReader.setOnImageAvailableListener(reader -> {
            Image img = reader.acquireLatestImage();
            if (img != null) {
                // TODO: Process pixel array to find enemy (Red pixels)
                img.close();
            }
        }, null);

        return START_STICKY;
    }

    @Override public void onDestroy() {
        if (vd != null) vd.release();
        if (mp != null) mp.stop();
        if (overlay != null) wm.removeView(overlay);
        super.onDestroy();
    }
}
