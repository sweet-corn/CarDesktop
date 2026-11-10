package com.cardesk.home;

import android.content.ComponentName;
import android.content.Context;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.os.Handler;
import android.os.Looper;
import java.util.List;

/** Observes only the selected player's session; never controls a different app as fallback. */
public final class MusicSession {
    private final MediaSessionManager manager;
    private final ComponentName listener;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable changed;
    private MediaController controller;
    private String selectedPackage = "";
    private boolean started;
    private final MediaController.Callback callback = new MediaController.Callback() {
        @Override public void onMetadataChanged(MediaMetadata metadata) { changed.run(); }
        @Override public void onPlaybackStateChanged(PlaybackState state) { changed.run(); }
        @Override public void onSessionDestroyed() { refresh(); }
    };
    private final MediaSessionManager.OnActiveSessionsChangedListener sessions = controllers -> refresh();

    public MusicSession(Context context, Runnable changed) {
        manager = (MediaSessionManager) context.getSystemService(Context.MEDIA_SESSION_SERVICE);
        listener = new ComponentName(context, MusicNotificationListener.class);
        this.changed = changed;
    }

    public void start(String packageName) {
        stop(); selectedPackage = packageName;
        try {
            manager.addOnActiveSessionsChangedListener(sessions, listener, handler);
            started = true; refresh();
        } catch (SecurityException e) { bind(null); }
    }

    public void refresh() {
        if (!started) return;
        try {
            List<MediaController> controllers = manager.getActiveSessions(listener);
            MediaController best = null;
            for (MediaController item : controllers) {
                if (!selectedPackage.isEmpty() && !selectedPackage.equals(item.getPackageName())) continue;
                if (item.getPlaybackState() == null && item.getMetadata() == null) continue;
                if (best == null) best = item;
                PlaybackState state = item.getPlaybackState();
                if (state != null && state.getState() == PlaybackState.STATE_PLAYING) { best = item; break; }
            }
            bind(best);
        } catch (SecurityException e) { bind(null); }
    }

    private void bind(MediaController next) {
        if (controller != null && next != null && controller.getSessionToken().equals(next.getSessionToken())) { changed.run(); return; }
        if (controller != null) controller.unregisterCallback(callback);
        controller = next;
        if (controller != null) controller.registerCallback(callback, handler);
        changed.run();
    }

    public MediaController controller() { return controller; }
    public String diagnostics() {
        StringBuilder report = new StringBuilder("指定播放器：").append(selectedPackage.isEmpty() ? "自动" : selectedPackage);
        try {
            List<MediaController> items = manager.getActiveSessions(listener);
            report.append("\n可见媒体会话：").append(items.size());
            for (MediaController item : items) {
                PlaybackState state = item.getPlaybackState();
                report.append("\n\n").append(item.getPackageName()).append("\n状态：")
                    .append(state == null ? "未提供" : String.valueOf(state.getState()))
                    .append(" / 操作标记：").append(state == null ? "无" : String.valueOf(state.getActions()));
            }
        } catch (SecurityException e) { report.append("\n系统拒绝读取媒体会话。请开启通知使用权，或关闭后重新开启。车机也可能限制第三方读取。"); }
        return report.toString();
    }
    public void stop() {
        if (started) manager.removeOnActiveSessionsChangedListener(sessions);
        started = false;
        if (controller != null) controller.unregisterCallback(callback);
        controller = null;
    }
}
