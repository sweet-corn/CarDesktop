package com.cardesk.home;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class BridgeProtocolTest {
    @Test public void chunkedLinesAndOversizedLineAreHandled() {
        AdbConnectionManager.LineBuffer parser = new AdbConnectionManager.LineBuffer();
        List<String> lines = new ArrayList<>();
        parser.append("CARDESK_SNAP", lines::add);
        parser.append("SHOT:{\"protocol\":3}\r\nfirst\n", lines::add);
        assertEquals(2, lines.size());
        assertEquals("CARDESK_SNAPSHOT:{\"protocol\":3}", lines.get(0));
        StringBuilder large = new StringBuilder();
        for (int i = 0; i < 33000; i++) large.append('x');
        parser.append(large.toString() + "\nnext\n", lines::add);
        assertEquals(3, lines.size());
        assertEquals("next", lines.get(2));
    }
    @Test public void bridgeCommandsAreClosedVocabulary() {
        assertTrue(VehicleBridge.isClimateCommand("power:0"));
        assertTrue(VehicleBridge.isClimateCommand("wind:7"));
        assertFalse(VehicleBridge.isClimateCommand("wind:8"));
        assertFalse(VehicleBridge.isClimateCommand("power:1;reboot"));
        try { VehicleBridge.startCommand("power:1;reboot"); fail(); }
        catch (IllegalArgumentException expected) { }
    }
}
