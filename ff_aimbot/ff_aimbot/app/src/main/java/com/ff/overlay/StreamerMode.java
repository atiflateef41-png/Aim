package com.ff.overlay;

import android.app.ActivityManager;
import android.content.Context;
import android.hardware.display.DisplayManager;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import java.util.List;

public class StreamerMode {
    private final Context ctx;
    private volatile boolean recordingDetected = false;
    private Thread watcher;

    public StreamerMode(Context c) { ctx = c; }

    public void start() {
        watcher = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    boolean rec = checkMediaProjection()
                               || checkScreenRecordProcess()
                               || checkVirtualDisplay();
                    recordingDetected = rec;
                    Thread.sleep(rec ? 500 : 1500);
                } catch (InterruptedException e) { return; }
                  catch (Exception ignored) {}
            }
        });
        watcher.start();
    }

    public boolean isRecording() { return recordingDetected; }
    public void stop() { if (watcher != null) watcher.interrupt(); }

    private boolean checkMediaProjection() {
        if (Build.VERSION.SDK_INT < 21) return false;
        try {
            Object mgr = ctx.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            java.lang.reflect.Field f = mgr.getClass().getDeclaredField("mProjections");
            f.setAccessible(true);
            Object list = f.get(mgr);
            return list instanceof List && !((List<?>) list).isEmpty();
        } catch (Throwable t) { return false; }
    }

    private boolean checkScreenRecordProcess() {
        ActivityManager am = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> procs = am.getRunningAppProcesses();
        if (procs == null) return false;
        String[] recs = {
            "com.azrecorder.free", "com.duapps.recorder", "com.kimcy929.screenrecorder",
            "com.hecorat.screenrecorder.free", "com.recorder.screen.video",
            "com.mobisystems.screenrecorder", "com.nll.screenrecorder",
            "com.mediatek.screenrecord", "com.android.systemui:recorder",
            "com.geniuscloud.screenrecorder", "screenrecorder.recorder.editor"
        };
        for (ActivityManager.RunningAppProcessInfo p : procs)
            for (String r : recs)
                if (p.processName != null && p.processName.contains(r)) return true;
        return false;
    }

    private boolean checkVirtualDisplay() {
        DisplayManager dm = (DisplayManager) ctx.getSystemService(Context.DISPLAY_SERVICE);
        if (dm == null) return false;
        for (android.view.Display d : dm.getDisplays())
            if (d.getType() == android.view.Display.TYPE_VIRTUAL
                && d.getDisplayId() != android.view.Display.DEFAULT_DISPLAY) return true;
        return false;
    }
}
