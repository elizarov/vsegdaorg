package org.vsegda.util

import junit.framework.Assert.*
import junit.framework.TestCase
import org.junit.*

class TimePeriodTest {
    @Test
    fun testParse() {
        assertEquals(0, TimePeriod.valueOf("0").period)
        assertEquals(1, TimePeriod.valueOf("1").period)
        assertEquals(1, TimePeriod.valueOf("+1").period)
        assertEquals(-1, TimePeriod.valueOf("-1").period)
        assertEquals(1000, TimePeriod.valueOf("1s").period)
        assertEquals(1500, TimePeriod.valueOf("1.5s").period)
        assertEquals(60000, TimePeriod.valueOf("1m").period)
        assertEquals(3600000, TimePeriod.valueOf("1h").period)
        assertEquals((24 * 3600000).toLong(), TimePeriod.valueOf("1d").period)
        assertEquals((12 * 3600000).toLong(), TimePeriod.valueOf(".5d").period)
        assertEquals((7 * 24 * 3600000).toLong(), TimePeriod.valueOf("1w").period)
        assertEquals((7 * 24 * 3600000 + 15000).toLong(), TimePeriod.valueOf("1w15s").period)
        assertEquals((-7 * 24 * 3600000 - 20 * 60000).toLong(), TimePeriod.valueOf("-1w20m").period)
    }

    @Test
    fun testString() {
        assertEquals("0", TimePeriod.valueOf(0).toString())
        assertEquals("0.001s", TimePeriod.valueOf(1).toString())
        assertEquals("-0.001s", TimePeriod.valueOf(-1).toString())
        assertEquals("1s", TimePeriod.valueOf(1000).toString())
        assertEquals("1.234s", TimePeriod.valueOf(1234).toString())
        assertEquals("1m", TimePeriod.valueOf(60000).toString())
        assertEquals("1h", TimePeriod.valueOf((60 * 60000).toLong()).toString())
        assertEquals("1d", TimePeriod.valueOf((24 * 60 * 60000).toLong()).toString())
        assertEquals("1w", TimePeriod.valueOf((7 * 24 * 60 * 60000).toLong()).toString())
        assertEquals("1w30m", TimePeriod.valueOf((7 * 24 * 60 * 60000 + 30 * 60000).toLong()).toString())
    }
}
