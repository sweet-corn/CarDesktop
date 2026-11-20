package com.cardesk.home;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONObject;

/** Activity-scoped local ADB. The vehicle stream does not expose a second IPC endpoint. */
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
    private boolean sampleSeen;
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
        sampleSeen = false;
        bridgeOutput = "尚无启动输出";
        LocalAdbClient current = new LocalAdbClient(context); client = current;
        state = "正在准备ADB连接…"; observer.run();
        worker.execute(() -> {
            try {
                current.connect(host, port, message -> update(request, message));
                update(request, "ADB已认证，正在启动只读辅助进程…");
                main.postDelayed(() -> {
                    if (generation != request || client != current || sampleSeen) return;
                    state = "ADB已认证，但尚未收到车辆快照；请检查桥接诊断";
                    observer.run();
                }, 8000);
                StringBuilder output = new StringBuilder();
                LineBuffer lines = new LineBuffer();
                current.shell(VehicleBridge.startCommand("stream"), chunk -> lines.append(chunk, line -> {
                    if (line.startsWith(VehicleBridgeMain.SNAPSHOT_PREFIX)) {
                        try {
                            JSONObject sample = new JSONObject(line.substring(VehicleBridgeMain.SNAPSHOT_PREFIX.length()));
                            if (sample.optInt("protocol") == VehicleBridge.PROTOCOL) main.post(() -> {
                                if (generation != request) return;
                                bridge.receive(sample);
                                if (!sampleSeen) { sampleSeen = true; state = "ADB已认证，已收到车辆快照；各接口状态见诊断"; observer.run(); }
                            });
                            return;
                        } catch (org.json.JSONException ignored) { line = "BRIDGE_INVALID_SNAPSHOT"; }
                    }
                    output.append(line).append('\n');
                    if (output.length() > 6000) output.delete(0, output.length() - 6000);
                    String report = output.toString();
                    main.post(() -> { if (generation == request) { bridgeOutput = report; observer.run(); } });
                }));
                update(request, "ADB shell已退出；辅助进程可能到期或启动失败。请查看桥接诊断和设备日志。 ");
            } catch (java.net.SocketTimeoutException e) { update(request, "ADB连接或授权超时；请检查端口并确认授权弹窗"); }
            catch (Exception e) {
                String message = e.getMessage();
                update(request, "ADB连接失败：" + e.getClass().getSimpleName() + (message == null ? "" : " · " + message));
            } finally {
                current.close();
                main.post(() -> { if (generation == request) { client = null; bridge.clear(); } });
            }
        });
    }
    public void disconnect() {
        generation++;
        LocalAdbClient previous = client; client = null;
        if (previous != null) previous.close();
        bridge.clear();
        state = "ADB已断开"; observer.run();
    }
    public void grantMusicAccess(String host, int port, java.util.function.Consumer<String> complete) {
        executeFixedCommand(host, port, "cmd notification allow_listener com.cardesk.home/com.cardesk.home.MusicNotificationListener", complete);
    }
    public void openRecents(String host, int port, java.util.function.Consumer<String> complete) {
        executeFixedCommand(host, port, "input keyevent 187; echo CAR_DESK_RECENTS_RESULT:$?", complete);
    }
    void climate(String host, int port, String command, java.util.function.Consumer<String> complete) {
        if (!VehicleBridge.isClimateCommand(command)) { complete.accept("不支持的空调操作"); return; }
        executeFixedCommand(host, port, VehicleBridge.startCommand(command), 8192, complete);
    }
    void launchMap(String host, int port, android.content.ComponentName target, int display, java.util.function.Consumer<String> complete) {
        String component = target.flattenToString();
        if (display <= 0 || !component.matches("[a-zA-Z0-9_.$/]+")) { complete.accept("地图启动参数无效"); return; }
        executeFixedCommand(host, port, "am start --display " + display + " -a android.intent.action.MAIN"
            + " -c android.intent.category.LAUNCHER -n '" + component + "'; echo MAP_RESULT:$?", complete);
    }
    void mapGesture(String host, int port, int display, int x, int y, int endX, int endY, int duration, java.util.function.Consumer<String> complete) {
        if (display <= 0) { complete.accept("地图画面已关闭"); return; }
        String gesture = duration == 0 ? "tap " + x + " " + y : "swipe " + x + " " + y + " " + endX + " " + endY + " " + duration;
        executeFixedCommand(host, port, "input touchscreen -d " + display + " " + gesture + "; echo TOUCH_RESULT:$?", complete);
    }
    private void executeFixedCommand(String host, int port, String command, java.util.function.Consumer<String> complete) {
        executeFixedCommand(host, port, command, 1024, complete);
    }
    private void executeFixedCommand(String host, int port, String command, int limit, java.util.function.Consumer<String> complete) {
        if (granting) { complete.accept("ADB操作正在执行，请稍候"); return; }
        granting = true;
        LocalAdbClient connection = new LocalAdbClient(context); permissionClient = connection;
        Runnable timeout = () -> {
            if (permissionClient != connection) return;
            permissionClient = null; granting = false; connection.close();
            complete.accept("ADB操作超时，结果未知，请检查系统授权及实际状态");
        };
        main.postDelayed(timeout, 70000);
        permissions.execute(() -> {
            String result;
            try {
                connection.connect(host, port, message -> { });
                StringBuilder response = new StringBuilder();
                connection.shell(command, chunk -> {
                    response.append(chunk);
                    if (response.length() > limit) response.delete(0, response.length() - limit);
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

    static final class LineBuffer {
        private static final int MAX_LINE = 32768;
        private final StringBuilder line = new StringBuilder();
        private boolean dropping;
        void append(String chunk, java.util.function.Consumer<String> handle) {
            for (int i = 0; i < chunk.length(); i++) {
                char c = chunk.charAt(i);
                if (c == '\n') {
                    if (!dropping) handle.accept(line.toString().trim());
                    line.setLength(0); dropping = false;
                } else if (!dropping) {
                    if (line.length() >= MAX_LINE) { line.setLength(0); dropping = true; }
                    else line.append(c);
                }
            }
        }
    }
}
