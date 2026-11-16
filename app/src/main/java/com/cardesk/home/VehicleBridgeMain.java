package com.cardesk.home;

import android.content.Context;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.os.Looper;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/** Read-only shell entry point launched explicitly with adb/app_process. No exported car commands. */
public final class VehicleBridgeMain {
    static final String SOCKET = "com.cardesk.home.vehicle.v2";
    private final Context context;
    private final android.os.Handler sdkHandler = new android.os.Handler(Looper.getMainLooper());
    private final Map<String, Object> devices = new HashMap<>();
    private VehicleBridgeMain(Context context) { this.context = context; }

    public static void main(String[] args) throws Exception {
        if (android.os.Process.myUid() != 2000 || args.length != 1 || !args[0].matches("[a-f0-9]{64}")) {
            System.out.println("BRIDGE_REJECTED: uid=" + android.os.Process.myUid()); return;
        }
        System.out.println("BRIDGE_START: shell uid=2000");
        Thread.setDefaultUncaughtExceptionHandler((thread, error) -> {
            android.util.Log.e("CarDeskBridge", "Bridge failed", error);
            System.out.println("BRIDGE_FATAL: " + error.getClass().getSimpleName() + ": " + error.getMessage());
            System.exit(1);
        });
        Looper.prepareMainLooper();
        Class<?> thread = Class.forName("android.app.ActivityThread");
        Object instance = thread.getMethod("systemMain").invoke(null);
        Context context = (Context) thread.getMethod("getSystemContext").invoke(instance);
        System.out.println("BRIDGE_CONTEXT_READY");
        VehicleBridgeMain bridge = new VehicleBridgeMain(context);
        LocalServerSocket server = new LocalServerSocket(SOCKET);
        System.out.println("BRIDGE_SOCKET_READY");
        // Bound process lifetime: restart explicitly instead of leaving a permanent shell daemon.
        Thread lifetime = new Thread(() -> {
            try { Thread.sleep(30 * 60 * 1000L); } catch (InterruptedException ignored) { }
            System.exit(0);
        });
        lifetime.setDaemon(true); lifetime.start();
        Thread serverThread = new Thread(() -> serve(server, bridge, context, args[0]), "CarDesk-vehicle-reader");
        serverThread.start();
        Looper.loop();
    }
    private static void serve(LocalServerSocket server, VehicleBridgeMain bridge, Context context, String tokenValue) {
        while (true) {
            try (LocalSocket socket = server.accept()) {
                socket.setSoTimeout(2500);
                // Only our app's uid is accepted; token also prevents stale installations connecting.
                int appUid = context.getPackageManager().getApplicationInfo("com.cardesk.home", 0).uid;
                if (socket.getPeerCredentials().getUid() != appUid) continue;
                BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                String token = readBounded(input);
                if (!tokenValue.equals(token)) continue;
                String command = readBounded(input);
                PrintWriter output = new PrintWriter(socket.getOutputStream());
                java.util.concurrent.FutureTask<JSONObject> sample = new java.util.concurrent.FutureTask<>(() -> {
                    if ("snapshot".equals(command)) return bridge.snapshot();
                    return bridge.control(command);
                });
                // Vendor SDKs commonly create Handlers during getInstance; initialize on a Looper thread.
                bridge.sdkHandler.post(sample);
                try {
                    output.println(sample.get(1800, java.util.concurrent.TimeUnit.MILLISECONDS).toString()); output.flush();
                } catch (java.util.concurrent.TimeoutException timeout) {
                    sample.cancel(false); bridge.sdkHandler.removeCallbacks(sample);
                    throw timeout;
                }
            } catch (Exception error) {
                android.util.Log.w("CarDeskBridge", "Read failed", error);
                System.out.println("BRIDGE_READ_FAILED: " + error.getClass().getSimpleName() + ": " + error.getMessage());
            }
        }
    }
    private static String readBounded(BufferedReader input) throws Exception {
        StringBuilder value = new StringBuilder();
        for (int i = 0; i <= 64; i++) {
            int ch = input.read(); if (ch == '\n') return value.toString();
            if (ch < 0) return ""; value.append((char) ch);
        }
        return "";
    }
    private void read(JSONObject result, String key, String area, String type, String method, Integer argument) {
        try {
            String className = "android.hardware.bydauto." + area + "." + type;
            Object device = devices.get(className);
            if (device == null) {
                device = Class.forName(className).getMethod("getInstance", Context.class).invoke(null, context);
                if (device == null) throw new IllegalStateException("No device");
                devices.put(className, device);
            }
            Object value = argument == null ? device.getClass().getMethod(method).invoke(device)
                : device.getClass().getMethod(method, int.class).invoke(device, argument);
            if (!(value instanceof Number) || !Double.isFinite(((Number) value).doubleValue())) throw new IllegalStateException("Invalid number");
            result.put(key, value);
        } catch (Exception | LinkageError error) {
            Throwable cause = error.getCause() == null ? error : error.getCause();
            try { result.put(key + "_error", cause.getClass().getSimpleName() + ": " + String.valueOf(cause.getMessage())); } catch (Exception ignored) { }
        }
    }
    private JSONObject snapshot() throws Exception {
        JSONObject data = new JSONObject(); data.put("protocol", 1); data.put("elapsed", android.os.SystemClock.elapsedRealtime());
        for (int wheel = 1; wheel <= 4; wheel++) {
            read(data, "tyre_" + wheel, "tyre", "BYDAutoTyreDevice", "getTyrePressureValue", wheel);
            read(data, "tyre_signal_" + wheel, "tyre", "BYDAutoTyreDevice", "getTyreSignalState", wheel);
        }
        read(data, "tyre_system", "tyre", "BYDAutoTyreDevice", "getTyreSystemState", null);
        read(data, "speed_raw", "speed", "BYDAutoSpeedDevice", "getCurrentSpeed", null);
        read(data, "ac_wind", "ac", "BYDAutoAcDevice", "getAcWindLevel", null);
        read(data, "ac_start", "ac", "BYDAutoAcDevice", "getAcStartState", null);
        read(data, "ac_cycle", "ac", "BYDAutoAcDevice", "getAcCycleMode", null);
        read(data, "ac_temp_1", "ac", "BYDAutoAcDevice", "getTemprature", 1);
        read(data, "ac_temp_2", "ac", "BYDAutoAcDevice", "getTemprature", 2);
        read(data, "electric_recent_raw", "statistic", "BYDAutoStatisticDevice", "getLastElecConPHMValue", null);
        read(data, "fuel_recent_raw", "statistic", "BYDAutoStatisticDevice", "getLastFuelConPHMValue", null);
        read(data, "trip_distance_raw", "instrument", "BYDAutoInstrumentDevice", "getCurrentJourneyDriveMileage", null);
        read(data, "trip_time_raw", "instrument", "BYDAutoInstrumentDevice", "getCurrentJourneyDriveTime", null);
        return data;
    }
    private JSONObject control(String command) throws Exception {
        JSONObject result = new JSONObject();
        // Closed command vocabulary: no arbitrary reflection from IPC input.
        String[] parts = command.split(":");
        try {
            Class<?> type = Class.forName("android.hardware.bydauto.ac.BYDAutoAcDevice");
            Object device = type.getMethod("getInstance", Context.class).invoke(null, context);
            int code;
            if (parts.length == 2 && "power".equals(parts[0]) && ("0".equals(parts[1]) || "1".equals(parts[1]))) {
                code = ((Number) type.getMethod("1".equals(parts[1]) ? "start" : "stop", int.class).invoke(device, 0)).intValue();
            } else if (parts.length == 2 && "wind".equals(parts[0])) {
                int level = Integer.parseInt(parts[1]); if (level < 1 || level > 7) throw new IllegalArgumentException("风量范围1–7");
                code = ((Number) type.getMethod("setAcWindLevel", int.class, int.class).invoke(device, 0, level)).intValue();
            } else throw new IllegalArgumentException("不支持的命令");
            result.put("code", code); result.put("accepted", code == 0);
        } catch (Exception error) {
            Throwable cause = error.getCause() == null ? error : error.getCause();
            result.put("accepted", false); result.put("error", cause.getClass().getSimpleName() + ": " + cause.getMessage());
        }
        return result;
    }
}
