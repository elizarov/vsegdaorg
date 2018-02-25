package org.vsegda.data

import junit.framework.Assert.*
import org.junit.*

class DeltaUtilTest {
    @Test
    fun testPrecision() {
        assertEquals(0, computePrecision(0.0))
        assertEquals(0, computePrecision(1.0))
        assertEquals(0, computePrecision(10.0))
        assertEquals(0, computePrecision(100.0))
        assertEquals(1, computePrecision(0.1))
        assertEquals(1, computePrecision(12.5))
        assertEquals(1, computePrecision(12.6))
        assertEquals(1, computePrecision(12.6 - 12.5))
        assertEquals(2, computePrecision(0.01))
        assertEquals(2, computePrecision(0.05))
        assertEquals(2, computePrecision(0.06))
        assertEquals(2, computePrecision(1.23))
    }
}
