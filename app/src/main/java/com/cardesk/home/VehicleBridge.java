package com.cardesk.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.Handler;
import android.os.Looper;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.security.SecureRandom;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

/** Foreground polling client. Never starts a privileged process without user action. */
public final class VehicleBridge {
    private final String token;
    private final Handler main = new Handler(Looper.getMainLooper());
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private final Consumer<JSONObject> updated;
    private boolean running;
    private int generation;
    private JSONObject data;
    private long receivedAt;
    private final Runnable expire = new Runnable() {
        @Override public void run() {
            if (!running) return;
            if (data != null && android.os.SystemClock.elapsedRealtime() - receivedAt > 5000) {
                data = null; status = "车辆数据更新超时"; updated.accept(null);
            }
            main.postDelayed(this, 1000);
        }
    };
    private String status = "未连接ADB辅助进程";
    private final Runnable poll = () -> poll();
    public VehicleBridge(Context context, Consumer<JSONObject> updated) {
        this.updated = updated;
        SharedPreferences prefs = context.getSharedPreferences("vehicle_bridge", Context.MODE_PRIVATE);
        String saved = prefs.getString("token", "");
        if (saved.isEmpty()) {
            byte[] bytes = new byte[32]; new SecureRandom().nextBytes(bytes);
            StringBuilder result = new StringBuilder(); for (byte value : bytes) result.append(String.format(java.util.Locale.ROOT, "%02x", value & 255));
            saved = result.toString(); prefs.edit().putString("token", saved).apply();
        }
        token = saved;
    }
    public String startCommand() {
        return "CLASSPATH=$(pm path com.cardesk.home | head -n 1 | cut -d: -f2) app_process /system/bin com.cardesk.home.VehicleBridgeMain " + token;
    }
    public void start() { if (running) return; running = true; generation++; expire.run(); poll(); }
    public void stop() { running = false; generation++; main.removeCallbacks(poll); main.removeCallbacks(expire); data = null; updated.accept(null); }
    public void close() { stop(); worker.shutdownNow(); }
    public JSONObject data() { return data; }
    public String diagnostics() { return status + "\n\n" + (data == null ? "无实时数据" : data.toString()); }
    private void poll() {
        if (!running) return;
        int request = generation;
        worker.execute(() -> {
            JSONObject result = null; String message;
            try (LocalSocket socket = new LocalSocket()) {
                socket.connect(new LocalSocketAddress(VehicleBridgeMain.SOCKET)); socket.setSoTimeout(2500);
                if (socket.getPeerCredentials().getUid() != 2000) throw new SecurityException("Unexpected bridge UID");
                socket.getOutputStream().write((token + "\n").getBytes("UTF-8")); socket.getOutputStream().flush();
                ByteArrayOutputStream response = new ByteArrayOutputStream(); int ch;
                while ((ch = socket.getInputStream().read()) != -1 && ch != '\n') {
                    if (response.size() >= 32768) throw new java.io.IOException("Oversized response"); response.write(ch);
                }
                result = new JSONObject(response.toString("UTF-8"));
                long age = android.os.SystemClock.elapsedRealtime() - result.getLong("elapsed");
                if (result.getInt("protocol") != 1 || age < 0 || age > 5000) throw new java.io.IOException("Stale response");
                message = "ADB辅助进程已连接（只读）";
            } catch (Exception error) { result = null; message = "辅助进程未连接：" + error.getClass().getSimpleName(); }
            JSONObject snapshot = result; String state = message;
            main.post(() -> {
                if (!running || generation != request) return;
                data = snapshot; receivedAt = android.os.SystemClock.elapsedRealtime(); status = state; updated.accept(data); main.postDelayed(poll, 2000);
            });
        });
    }
}
