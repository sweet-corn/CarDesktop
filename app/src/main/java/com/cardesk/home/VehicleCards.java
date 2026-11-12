package com.cardesk.home;

import org.json.JSONObject;
import java.util.Locale;

/** Presentation validation kept separate from transport; no substitutions for missing readings. */
final class VehicleCards {
    private VehicleCards() { }
    static String number(JSONObject data, String key, double max) {
        if (data == null || !(data.opt(key) instanceof Number)) return "--";
        double value = ((Number) data.opt(key)).doubleValue();
        return Double.isFinite(value) && value >= 0 && value <= max ? String.format(Locale.getDefault(), "%.1f", value) : "--";
    }
    static String pressure(JSONObject data, int wheel) {
        if (data == null || data.optInt("tyre_system", -1) != 0 || data.optInt("tyre_signal_" + wheel, -1) != 0
                || !(data.opt("tyre_" + wheel) instanceof Number)) return "--";
        double value = ((Number) data.opt("tyre_" + wheel)).doubleValue();
        // BYD SDK reserves 4095; zero/no signal is not a measured tire pressure.
        return Double.isFinite(value) && value > 0 && value < 4095 && value == Math.rint(value)
            ? String.format(Locale.getDefault(), "%.0f", value) : "--";
    }
}
