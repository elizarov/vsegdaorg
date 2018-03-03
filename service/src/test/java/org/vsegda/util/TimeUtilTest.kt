package org.vsegda.util

import junit.framework.Assert.*
import org.junit.*

class TimeUtilTest {
    @Test
    fun testArchiveLimit() {
        assertEquals(
            parseTime("20110304T000000.000"),
            getArchiveLimit(parseTime("20110303T011514.345"))
        )
    }
}
