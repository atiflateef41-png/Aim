package com.ff.overlay;

import android.content.Context;
import java.io.*;
import java.util.*;

public class AimEngine extends Thread {
    private final Context ctx;
    private final StreamerMode streamer;
    private final AimView view;
    private volatile boolean running = true;
    private int pid = -1;
    private long base = 0;

    private long OFF_LOCAL_PLAYER, OFF_ENTITY_LIST, OFF_HEAD_BONE,
                 OFF_CAM_MATRIX, OFF_AIM_TARGET, OFF_TEAM_ID, OFF_IS_DEAD;

    public AimEngine(Context c, StreamerMode s, AimView v) {
        ctx = c; streamer = s; view = v;
    }

    @Override public void run() {
        Random r = new Random();
        while (running) {
            try {
                if (streamer.isRecording()) {
                    view.aimVisible = false;
                    view.requestRedraw();
                    Thread.sleep(250);
                    continue;
                }
                view.aimVisible = true;
                if (pid < 0) resolveTarget();
                if (pid > 0) tick();
                view.requestRedraw();
                Thread.sleep(16 + r.nextInt(12));
            } catch (Exception e) {
                try { Thread.sleep(200); } catch (InterruptedException ignored) {}
            }
        }
    }

    private void resolveTarget() {
        for (File f : new File("/proc").listFiles()) {
            if (!f.getName().matches("\\d+")) continue;
            try {
                String cmd = readText(new File(f, "cmdline"));
                if (cmd != null && cmd.contains("com.dts.freefireth")) {
                    pid = Integer.parseInt(f.getName());
                    base = findBase(pid, "libil2cpp.so");
                    return;
                }
            } catch (Exception ignored) {}
        }
    }

    private long findBase(int pid, String lib) throws IOException {
        for (String line : readLines(new File("/proc/" + pid + "/maps")))
            if (line.contains(lib) && line.contains("r-xp"))
                return Long.parseUnsignedLong(line.split("-")[0], 16);
        return 0;
    }

    private void tick() throws IOException {
        RandomAccessFile mem = new RandomAccessFile("/proc/" + pid + "/mem", "r");
        long localPtr = readPtr(mem, base + OFF_LOCAL_PLAYER);
        long listPtr  = readPtr(mem, base + OFF_ENTITY_LIST);
        if (localPtr == 0 || listPtr == 0) { mem.close(); return; }
        mem.close();
    }

    private long readPtr(RandomAccessFile m, long a) throws IOException {
        m.seek(a); byte[] b = new byte[8];
        if (m.read(b) != 8) return 0;
        long v = 0; for (int i = 7; i >= 0; i--) v = (v << 8) | (b[i] & 0xFF);
        return v;
    }

    private String readText(File f) throws IOException {
        byte[] b = new byte[(int)Math.min(f.length(), 256)];
        try (FileInputStream in = new FileInputStream(f)) { in.read(b); }
        return new String(b).trim();
    }

    private List<String> readLines(File f) throws IOException {
        List<String> o = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String l; while ((l = br.readLine()) != null) o.add(l);
        }
        return o;
    }

    public void stop() { running = false; }
}
