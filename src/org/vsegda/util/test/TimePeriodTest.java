package org.vsegda.util.test;

import junit.framework.TestCase;
import org.vsegda.util.TimePeriod;

/**
 * @author Roman Elizarov
 */
public class TimePeriodTest extends TestCase {
    public void testParse() {
        assertEquals(0, TimePeriod.valueOf("0").period());
        assertEquals(1, TimePeriod.valueOf("1").period());
        assertEquals(1, TimePeriod.valueOf("+1").period());
        assertEquals(-1, TimePeriod.valueOf("-1").period());
        assertEquals(1000, TimePeriod.valueOf("1s").period());
        assertEquals(1500, TimePeriod.valueOf("1.5s").period());
        assertEquals(60000, TimePeriod.valueOf("1m").period());
        assertEquals(3600000, TimePeriod.valueOf("1h").period());
        assertEquals(24 * 3600000, TimePeriod.valueOf("1d").period());
        assertEquals(12 * 3600000, TimePeriod.valueOf(".5d").period());
        assertEquals(7 * 24 * 3600000, TimePeriod.valueOf("1w").period());
        assertEquals(7 * 24 * 3600000 + 15000, TimePeriod.valueOf("1w15s").period());
        assertEquals(-7 * 24 * 3600000 - 20 * 60000, TimePeriod.valueOf("-1w20m").period());
    }

    public void testString() {
        assertEquals("0", TimePeriod.valueOf(0).toString());
        assertEquals("1", TimePeriod.valueOf(1).toString());
        assertEquals("-1", TimePeriod.valueOf(-1).toString());
        assertEquals("1s", TimePeriod.valueOf(1000).toString());
        assertEquals("1.234s", TimePeriod.valueOf(1234).toString());
        assertEquals("1m0s", TimePeriod.valueOf(60000).toString());
        assertEquals("1h0m0s", TimePeriod.valueOf(60 * 60000).toString());
        assertEquals("1d0h0m0s", TimePeriod.valueOf(24 * 60 * 60000).toString());
        assertEquals("1w0d0h0m0s", TimePeriod.valueOf(7 * 24 * 60 * 60000).toString());
        assertEquals("1w0d0h30m0s", TimePeriod.valueOf(7 * 24 * 60 * 60000 + 30 * 60000).toString());
    }
}
