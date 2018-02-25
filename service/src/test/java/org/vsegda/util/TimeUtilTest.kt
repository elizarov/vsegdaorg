package org.vsegda.util

import junit.framework.Assert.*
import org.junit.*

class TimeUtilTest {
    @Test
    fun testArchiveLimit() {
        assertEquals(
            TimeUtil.parseTime("20110304T000000.000"),
            TimeUtil.getArchiveLimit(TimeUtil.parseTime("20110303T011514.345"))
        )
    }
}
