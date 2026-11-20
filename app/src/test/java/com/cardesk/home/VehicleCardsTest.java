package com.cardesk.home;

import org.json.JSONObject;
import org.junit.Test;
import static org.junit.Assert.*;

public class VehicleCardsTest {
    @Test public void pressureRejectsMissingInvalidAndAbnormalStates() throws Exception {
        JSONObject snapshot = new JSONObject().put("tyre_1", 238);
        assertEquals("238", VehicleCards.pressure(snapshot, 1));
        assertEquals("--", VehicleCards.pressure(snapshot.put("tyre_signal_1", 1), 1));
        assertEquals("--", VehicleCards.pressure(snapshot.put("tyre_signal_1", 0).put("tyre_system", 3), 1));
        assertEquals("--", VehicleCards.pressure(snapshot.put("tyre_system", 0).put("tyre_1", 4095), 1));
        assertEquals("--", VehicleCards.pressure(snapshot.put("tyre_1", 0), 1));
        assertEquals("--", VehicleCards.pressure(snapshot.put("tyre_1", 238.5), 1));
        assertEquals("--", VehicleCards.pressure(null, 1));
    }
    @Test public void climateRequiresKnownUnitAndRange() throws Exception {
        JSONObject snapshot = new JSONObject().put("ac_temp_1", 23);
        assertEquals("--", VehicleCards.temperature(snapshot, 1));
        assertEquals("23°C", VehicleCards.temperature(snapshot.put("ac_temp_unit", 1), 1));
        assertEquals("--", VehicleCards.temperature(snapshot.put("ac_temp_1", 0), 1));
        assertEquals("72°F", VehicleCards.temperature(snapshot.put("ac_temp_unit", 0).put("ac_temp_1", 72), 1));
        assertEquals(-1, VehicleCards.code(snapshot.put("ac_wind", 1.5), "ac_wind", 0, 7));
    }
    @Test public void consumptionBoundsHandleRegeneration() throws Exception {
        JSONObject snapshot = new JSONObject().put("electric_recent_raw", -5.5).put("fuel_recent_raw", 200);
        assertNotEquals("--", VehicleCards.number(snapshot, "electric_recent_raw", -99.9, 99.9));
        assertEquals("--", VehicleCards.number(snapshot, "fuel_recent_raw", 0, 51.1));
    }
}
