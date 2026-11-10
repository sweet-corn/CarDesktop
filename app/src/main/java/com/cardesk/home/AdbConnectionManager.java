package com.cardesk.home;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** ADB connection is opt-in, activity-scoped, and starts only our fixed read-only entry point. */
public final class AdbConnectionManager {
    private final Context context;
    private final VehicleBridge bridge;
    private final Handler main = new Handler(Looper.getMainLooper());
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private volatile LocalAdbClient client;
    private int generation;
    private String state = "未连接";
    private Runnable observer = () -> { };
    public AdbConnectionManager(Context context, VehicleBridge bridge) { this.context = context.getApplicationContext(); this.bridge = bridge; }
    public String state() { return state; }
    public void observe(Runnable callback) { observer = callback == null ? () -> { } : callback; }
    private void update(int request, String message) {
        main.post(() -> { if (generation == request) { state = message; observer.run(); } });
    }
    public void connect(String host, int port) {
        disconnect();
        if (port < 1 || port > 65535) { state = "端口必须为 1–65535"; observer.run(); return; }
        int request = ++generation;
        LocalAdbClient current = new LocalAdbClient(context); client = current;
        state = "正在准备ADB连接…"; observer.run();
        worker.execute(() -> {
            try {
                current.connect(host, port, message -> update(request, message));
                update(request, "ADB已认证，正在启动只读辅助进程…");
                main.postDelayed(() -> {
                    if (generation != request || client != current) return;
                    state = bridge.data() == null ? "ADB已认证，但尚未收到桥接数据；可打开桥接诊断检查"
                        : "ADB已认证，只读辅助进程已连接";
                    observer.run();
                }, 8000);
                StringBuilder output = new StringBuilder();
                current.shell(bridge.startCommand(), chunk -> {
                    if (output.length() < 2048) output.append(chunk, 0, Math.min(chunk.length(), 2048 - output.length()));
                });
                update(request, "ADB shell已退出；辅助进程可能到期或启动失败。请查看桥接诊断和设备日志。 ");
            } catch (java.net.SocketTimeoutException e) { update(request, "ADB连接或授权超时；请检查端口并确认授权弹窗"); }
            catch (Exception e) {
                String message = e.getMessage();
                update(request, "ADB连接失败：" + e.getClass().getSimpleName() + (message == null ? "" : " · " + message));
            } finally {
                current.close();
                main.post(() -> { if (generation == request) client = null; });
            }
        });
    }
    public void disconnect() {
        generation++;
        LocalAdbClient previous = client; client = null;
        if (previous != null) previous.close();
        state = "ADB已断开（辅助进程可能继续运行至30分钟到期）"; observer.run();
    }
    public void close() { disconnect(); worker.shutdownNow(); observer = () -> { }; }
}
