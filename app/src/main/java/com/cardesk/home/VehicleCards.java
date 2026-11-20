package com.cardesk.home;

import org.json.JSONObject;
import java.util.Locale;

/** Presentation validation kept separate from transport; no substitutions for missing readings. */
final class VehicleCards {
    private VehicleCards() { }
    private static double value(JSONObject data, String key) {
        Object raw = data == null ? null : data.opt(key);
        return raw instanceof Number ? ((Number) raw).doubleValue() : Double.NaN;
    }
    static String number(JSONObject data, String key, double min, double max) {
        double value = value(data, key);
        return value >= min && value <= max ? String.format(Locale.getDefault(), "%.1f", value) : "--";
    }
    static int code(JSONObject data, String key, int min, int max) {
        double value = value(data, key);
        return value >= min && value <= max && value == Math.rint(value) ? (int) value : -1;
    }
    static String temperature(JSONObject data, int zone) {
        int unit = code(data, "ac_temp_unit", 0, 1);
        if (unit < 0) return "--";
        int temp = code(data, "ac_temp_" + zone, unit == 1 ? 17 : 64, unit == 1 ? 33 : 91);
        return temp < 0 ? "--" : temp + (unit == 1 ? "°C" : "°F");
    }
    static String pressure(JSONObject data, int wheel) {
        if (data == null || (data.has("tyre_system") && code(data, "tyre_system", 0, 0) != 0)
                || (data.has("tyre_signal_" + wheel) && code(data, "tyre_signal_" + wheel, 0, 0) != 0)) return "--";
        // BYD SDK reserves 4095; zero/no signal is not a measured tire pressure.
        int pressure = code(data, "tyre_" + wheel, 1, 4094);
        return pressure < 0 ? "--" : Integer.toString(pressure);
    }
}
