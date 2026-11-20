package com.cardesk.home;

import android.content.Context;
import android.os.Looper;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

/** Fixed telemetry stream or one explicitly requested climate command, running as shell. */
public final class VehicleBridgeMain {
    static final String SNAPSHOT_PREFIX = "CARDESK_SNAPSHOT:";
    static final String CONTROL_PREFIX = "CARDESK_CONTROL:";
    private final Context context;
    private final Map<String, Object> devices = new HashMap<>();
    private VehicleBridgeMain(Context context) { this.context = context; }

    public static void main(String[] args) throws Exception {
        if (android.os.Process.myUid() != 2000 || args.length != 1
                || !("stream".equals(args[0]) || VehicleBridge.isClimateCommand(args[0]))) {
            System.out.println("BRIDGE_REJECTED: uid=" + android.os.Process.myUid()); return;
        }
        System.out.println("BRIDGE_START: shell uid=2000");
        boolean stream = "stream".equals(args[0]);
        // Bound the process even if context creation or a vendor getter blocks.
        Thread watchdog = new Thread(() -> {
            android.os.SystemClock.sleep(stream ? 30 * 60 * 1000L : 10000L);
            android.os.Process.killProcess(android.os.Process.myPid());
        }, "CarDesk-bridge-lifetime");
        watchdog.setDaemon(true); watchdog.start();
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
        if (!stream) {
            System.out.println(CONTROL_PREFIX + bridge.control(args[0]));
            System.out.flush();
            System.exit(0);
        }
        android.os.Handler handler = new android.os.Handler(Looper.getMainLooper());
        long deadline = android.os.SystemClock.elapsedRealtime() + 30 * 60 * 1000L;
        Runnable tick = new Runnable() {
            @Override public void run() {
                if (android.os.SystemClock.elapsedRealtime() >= deadline) { System.exit(0); return; }
                try {
                    System.out.println(SNAPSHOT_PREFIX + bridge.snapshot());
                    System.out.flush();
                    if (System.out.checkError()) { System.exit(0); return; }
                } catch (Exception error) {
                    System.out.println("BRIDGE_SNAPSHOT_FAILED: " + error.getClass().getSimpleName() + ": " + error.getMessage());
                }
                handler.postDelayed(this, 2000);
            }
        };
        handler.post(tick);
        Looper.loop();
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
            try { result.put(key + "_error", describe(cause)); } catch (Exception ignored) { }
        }
    }
    private JSONObject snapshot() throws Exception {
        JSONObject data = new JSONObject(); data.put("protocol", VehicleBridge.PROTOCOL); data.put("elapsed", android.os.SystemClock.elapsedRealtime());
        for (int wheel = 1; wheel <= 4; wheel++) {
            read(data, "tyre_" + wheel, "tyre", "BYDAutoTyreDevice", "getTyrePressureValue", wheel);
            read(data, "tyre_signal_" + wheel, "tyre", "BYDAutoTyreDevice", "getTyreSignalState", wheel);
        }
        read(data, "tyre_system", "tyre", "BYDAutoTyreDevice", "getTyreSystemState", null);
        read(data, "speed_raw", "speed", "BYDAutoSpeedDevice", "getCurrentSpeed", null);
        read(data, "ac_wind", "ac", "BYDAutoAcDevice", "getAcWindLevel", null);
        read(data, "ac_start", "ac", "BYDAutoAcDevice", "getAcStartState", null);
        read(data, "ac_cycle", "ac", "BYDAutoAcDevice", "getAcCycleMode", null);
        read(data, "ac_temp_unit", "ac", "BYDAutoAcDevice", "getTemperatureUnit", null);
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
            result.put("accepted", false); result.put("error", describe(cause));
        }
        return result;
    }

    private static String describe(Throwable error) {
        String message = error.getClass().getSimpleName() + ": " + error.getMessage();
        return message.substring(0, Math.min(message.length(), 240));
    }
}
