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
    private volatile LocalAdbClient permissionClient;
    private final ExecutorService permissions = Executors.newSingleThreadExecutor();
    private boolean granting;
    private int generation;
    private String state = "未连接";
    private String bridgeOutput = "尚无启动输出";
    public String diagnostics() { return state + "\n\n辅助进程输出：\n" + bridgeOutput; }
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
                current.shell(bridge.startCommand() + " 2>&1; echo BRIDGE_EXIT:$?", chunk -> {
                    String safe = bridge.redact(chunk);
                    output.append(safe);
                    if (output.length() > 6000) output.delete(0, output.length() - 6000);
                    String report = bridge.redact(output.toString());
                    main.post(() -> { if (generation == request) { bridgeOutput = report; observer.run(); } });
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
    public void grantMusicAccess(String host, int port, java.util.function.Consumer<String> complete) {
        executeFixedCommand(host, port, "cmd notification allow_listener com.cardesk.home/com.cardesk.home.MusicNotificationListener", complete);
    }
    public void openRecents(String host, int port, java.util.function.Consumer<String> complete) {
        executeFixedCommand(host, port, "input keyevent 187; echo CAR_DESK_RECENTS_RESULT:$?", complete);
    }
    public void startSplit(String host, int port, android.content.ComponentName left, android.content.ComponentName right,
            int width, int height, int leftPercent, java.util.function.Consumer<String> complete) {
        String a = left.flattenToString(), b = right.flattenToString();
        if (!a.matches("[a-zA-Z0-9_.$/]+") || !b.matches("[a-zA-Z0-9_.$/]+") || width < 1 || height < 1
                || leftPercent < 20 || leftPercent > 80) { complete.accept("分屏参数无效"); return; }
        // Android 10 am start has no --bounds option. Ratio is controlled by SystemUI.
        String command = "am start --windowingMode 3 -n '" + a
            + "'; r=$?; echo LEFT_RESULT:$r; if [ $r -eq 0 ]; then am start --windowingMode 4 -n '" + b
            + "'; echo RIGHT_RESULT:$?; else echo RIGHT_SKIPPED; fi";
        executeFixedCommand(host, port, command, complete);
    }
    private void executeFixedCommand(String host, int port, String command, java.util.function.Consumer<String> complete) {
        if (granting) { complete.accept("ADB操作正在执行，请稍候"); return; }
        granting = true;
        LocalAdbClient connection = new LocalAdbClient(context); permissionClient = connection;
        Runnable timeout = () -> {
            if (permissionClient != connection) return;
            permissionClient = null; granting = false; connection.close();
            complete.accept("ADB操作超时，请检查连接和系统授权弹窗");
        };
        main.postDelayed(timeout, 70000);
        permissions.execute(() -> {
            String result;
            try {
                connection.connect(host, port, message -> { });
                StringBuilder response = new StringBuilder();
                connection.shell(command, chunk -> {
                    if (response.length() < 1024) response.append(chunk, 0, Math.min(chunk.length(), 1024 - response.length()));
                });
                result = response.toString().trim();
            } catch (Exception error) { result = "ADB操作失败：" + error.getClass().getSimpleName(); }
            finally { connection.close(); }
            String output = result;
            main.post(() -> {
                main.removeCallbacks(timeout);
                if (permissionClient != connection) return;
                permissionClient = null; granting = false; complete.accept(output);
            });
        });
    }
    public void close() {
        disconnect(); worker.shutdownNow(); observer = () -> { };
        LocalAdbClient pending = permissionClient; permissionClient = null;
        if (pending != null) pending.close(); permissions.shutdownNow();
    }
}
