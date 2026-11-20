package com.cardesk.home;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

/** A private secondary display for the selected map; never mirrors the primary display. */
final class EmbeddedMapView extends FrameLayout implements TextureView.SurfaceTextureListener {
    private final Activity activity;
    private final Intent intent;
    private final AdbConnectionManager adb;
    private final String host;
    private final int port;
    private final TextureView picture;
    private final TextView status;
    private final Handler main = new Handler(Looper.getMainLooper());
    private Surface surface;
    private VirtualDisplay display;
    private int displayId = -1, renderWidth, renderHeight;
    private boolean touchBusy, touchAllowed, validGesture, closed;
    private int downX, downY;
    private final Runnable noFrame = () -> showStatus("未收到地图画面；试试全屏打开地图，或更换地图应用");

    EmbeddedMapView(Activity activity, Intent intent, AdbConnectionManager adb, String host, int port) {
        super(activity);
        this.activity = activity; this.intent = new Intent(intent); this.adb = adb; this.host = host; this.port = port;
        setBackgroundColor(0xff1c2630);
        picture = new TextureView(activity); picture.setSurfaceTextureListener(this);
        addView(picture, new LayoutParams(-1, -1));
        status = new TextView(activity); status.setTextColor(Color.WHITE); status.setTextSize(14);
        status.setGravity(Gravity.CENTER); status.setPadding(12, 10, 12, 10); status.setBackgroundColor(0xcc1c2630);
        addView(status, new LayoutParams(-1, -2, Gravity.CENTER));
        showStatus("正在准备导航画面…");
        picture.setOnTouchListener((v, event) -> gesture(event));
    }

    private void showStatus(String message) { status.setText(message); status.setVisibility(View.VISIBLE); }

    @Override public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
        if (closed || width < 1 || height < 1) return;
        release();
        try {
            texture.setDefaultBufferSize(width, height);
            surface = new Surface(texture);
            DisplayManager manager = activity.getSystemService(DisplayManager.class);
            display = manager.createVirtualDisplay("CarDesk navigation", width, height,
                activity.getResources().getDisplayMetrics().densityDpi, surface, DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY);
            if (display == null || display.getDisplay() == null || !display.getDisplay().isValid()) {
                release(); showStatus("车机未能创建导航虚拟屏"); return;
            }
            renderWidth = width; renderHeight = height;
            final int id = displayId = display.getDisplay().getDisplayId();
            main.postDelayed(noFrame, 8000);
            ActivityManager activities = activity.getSystemService(ActivityManager.class);
            if (Build.VERSION.SDK_INT >= 29 && activities != null && activities.isActivityStartAllowedOnDisplay(activity, id, intent)) {
                try {
                    activity.startActivity(intent, ActivityOptions.makeBasic().setLaunchDisplayId(id).toBundle());
                    return;
                } catch (RuntimeException ignored) { /* Try the already authorized shell path. */ }
            }
            showStatus("正在请求车机在右侧打开地图…");
            adb.launchMap(host, port, intent.getComponent(), id, result -> {
                if (displayId != id) return;
                if (!result.contains("MAP_RESULT:0") || result.contains("Error") || result.contains("Exception")) {
                    release(); showStatus("车机拒绝在右侧启动地图\n" + (result.isEmpty() ? "无返回信息" : result));
                }
            });
        } catch (RuntimeException error) {
            release(); showStatus("导航虚拟屏不可用：" + error.getClass().getSimpleName());
        }
    }

    @Override public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
        if (display == null || width < 1 || height < 1) return;
        try {
            texture.setDefaultBufferSize(width, height);
            display.resize(width, height, activity.getResources().getDisplayMetrics().densityDpi);
            renderWidth = width; renderHeight = height;
        } catch (RuntimeException error) { release(); showStatus("导航虚拟屏尺寸更新失败"); }
    }
    @Override public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) { release(); return true; }
    @Override public void onSurfaceTextureUpdated(SurfaceTexture texture) {
        if (displayId > 0 && !touchAllowed) {
            touchAllowed = true;
            main.removeCallbacks(noFrame);
            status.setVisibility(View.GONE);
        }
    }

    private boolean gesture(MotionEvent e) {
        if (!touchAllowed || displayId <= 0) return false;
        int action = e.getActionMasked();
        if (action == MotionEvent.ACTION_POINTER_DOWN || action == MotionEvent.ACTION_CANCEL) validGesture = false;
        if (action != MotionEvent.ACTION_DOWN && action != MotionEvent.ACTION_UP) return true;
        int x = axis(e.getX(), picture.getWidth(), renderWidth), y = axis(e.getY(), picture.getHeight(), renderHeight);
        if (action == MotionEvent.ACTION_DOWN) { downX = x; downY = y; validGesture = !touchBusy; return true; }
        if (!validGesture || touchBusy) return true;
        validGesture = false;
        int slop = android.view.ViewConfiguration.get(getContext()).getScaledTouchSlop();
        long elapsed = e.getEventTime() - e.getDownTime();
        int duration = Math.abs(x - downX) <= slop && Math.abs(y - downY) <= slop && elapsed < 400
            ? 0 : (int) Math.max(100, Math.min(600, elapsed));
        int id = displayId;
        touchBusy = true;
        adb.mapGesture(host, port, id, downX, downY, x, y, duration, result -> {
            if (id != displayId) return;
            touchBusy = false;
            if (!result.contains("TOUCH_RESULT:0") || result.contains("Error") || result.contains("Exception"))
                showStatus("车机未确认虚拟屏触控\n" + result);
            else status.setVisibility(View.GONE);
        });
        return true;
    }
    private static int axis(float location, int viewSize, int renderSize) {
        return Math.max(0, Math.min(renderSize - 1, (int) (location * renderSize / Math.max(1, viewSize))));
    }
    void close() { closed = true; release(); }
    private void release() {
        main.removeCallbacks(noFrame);
        touchAllowed = false; touchBusy = false; validGesture = false; displayId = -1;
        try { if (display != null) display.release(); }
        finally { display = null; if (surface != null) { surface.release(); surface = null; } }
    }
}
