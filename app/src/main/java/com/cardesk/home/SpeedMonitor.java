package com.cardesk.home;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import java.util.Locale;
import java.util.function.Consumer;

/** Foreground-only GPS speed; never infers motion from position differences. */
public final class SpeedMonitor implements LocationListener {
    private static final long MAX_AGE_MS = 5000;
    private final Activity activity;
    private final LocationManager locations;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Consumer<String> display;
    private long lastFix;
    private boolean running;
    private final Runnable expiry = new Runnable() {
        @Override public void run() {
            if (!running) return;
            if (lastFix == 0 || SystemClock.elapsedRealtime() - lastFix > MAX_AGE_MS) display.accept("-- km/h · GPS等待定位");
            handler.postDelayed(this, 1000);
        }
    };

    public SpeedMonitor(Activity activity, Consumer<String> display) {
        this.activity = activity; this.display = display;
        locations = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
    }
    public void start() {
        stop();
        if (activity.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            display.accept("-- km/h · 需要精确定位"); return;
        }
        if (locations == null || !locations.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            display.accept("-- km/h · GPS未开启"); return;
        }
        try {
            running = true; lastFix = 0;
            locations.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this, Looper.getMainLooper());
            expiry.run();
        } catch (SecurityException | IllegalArgumentException e) {
            running = false; display.accept("-- km/h · GPS不可用");
        }
    }
    public void stop() {
        running = false; handler.removeCallbacks(expiry);
        if (locations != null) try { locations.removeUpdates(this); } catch (SecurityException ignored) { }
        lastFix = 0;
    }
    @Override public void onLocationChanged(Location fix) {
        long age = SystemClock.elapsedRealtimeNanos() - fix.getElapsedRealtimeNanos();
        float speed = fix.getSpeed();
        if (!running || !fix.hasSpeed() || age < 0 || age > MAX_AGE_MS * 1000000L
                || Float.isNaN(speed) || Float.isInfinite(speed) || speed < 0 || speed > 100) return;
        lastFix = fix.getElapsedRealtimeNanos() / 1000000L;
        display.accept(String.format(Locale.getDefault(), "%.0f km/h · GPS", speed * 3.6f));
    }
    @Override public void onProviderDisabled(String provider) { lastFix = 0; display.accept("-- km/h · GPS未开启"); }
    @Override public void onProviderEnabled(String provider) { }
    @Override public void onStatusChanged(String provider, int status, Bundle extras) { }

    public static String probeVehicleApi(Context context) {
        try {
            Class<?> type = Class.forName("android.hardware.bydauto.speed.BYDAutoSpeedDevice");
            Object device = type.getMethod("getInstance", Context.class).invoke(null, context);
            if (device == null) return "接口存在，但未获取到设备实例";
            Object value = type.getMethod("getCurrentSpeed").invoke(device);
            return "接口调用成功，原始值：" + value + "；单位和实时性尚待验证，暂不用于显示";
        } catch (ClassNotFoundException e) { return "系统未提供比亚迪车速类"; }
        catch (Throwable e) {
            Throwable cause = e instanceof java.lang.reflect.InvocationTargetException && e.getCause() != null ? e.getCause() : e;
            return "接口不可用：" + cause.getClass().getSimpleName() + "（权限或车机版本可能不兼容）";
        }
    }
}
