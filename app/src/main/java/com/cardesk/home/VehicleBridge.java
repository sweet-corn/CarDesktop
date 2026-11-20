package com.cardesk.home;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import org.json.JSONObject;

/** Foreground view of the telemetry streamed by the authorized local ADB session. */
final class VehicleBridge {
    static final int PROTOCOL = 3;
    static final long MAX_AGE_MS = 5000;
    private final Handler main = new Handler(Looper.getMainLooper());
    private final Runnable updated;
    private boolean foreground;
    private JSONObject latest;
    private final Runnable expire = this::clear;

    VehicleBridge(Runnable updated) { this.updated = updated; }

    void foreground(boolean value) {
        foreground = value;
        if (!value) clear(); else updated.run();
    }
    void clear() {
        main.removeCallbacks(expire);
        latest = null;
        updated.run();
    }
    void receive(JSONObject snapshot) {
        long age = age(snapshot);
        if (!foreground || snapshot.optInt("protocol") != PROTOCOL || age < 0 || age >= MAX_AGE_MS) return;
        latest = snapshot;
        main.removeCallbacks(expire);
        main.postDelayed(expire, MAX_AGE_MS - age);
        updated.run();
    }
    JSONObject data() {
        long age = latest == null ? -1 : age(latest);
        return age >= 0 && age < MAX_AGE_MS ? latest : null;
    }
    String diagnostics() {
        JSONObject data = data();
        if (data == null) return "无实时数据";
        try { return data.toString(2); } catch (org.json.JSONException ignored) { return data.toString(); }
    }
    private static long age(JSONObject snapshot) {
        long elapsed = snapshot.optLong("elapsed", -1);
        return elapsed < 0 ? Long.MAX_VALUE : SystemClock.elapsedRealtime() - elapsed;
    }

    static boolean isClimateCommand(String command) { return command != null && command.matches("power:[01]|wind:[1-7]"); }
    static String startCommand(String command) {
        if (!"stream".equals(command) && !isClimateCommand(command)) throw new IllegalArgumentException("Unsupported command");
        return "apk=$(pm path com.cardesk.home | head -n 1 | cut -d: -f2-); "
            + "if [ -r \"$apk\" ]; then CLASSPATH=\"$apk\" app_process /system/bin com.cardesk.home.VehicleBridgeMain "
            + command + " 2>&1; echo BRIDGE_EXIT:$?; else echo BRIDGE_APK_UNREADABLE; fi";
    }
}
