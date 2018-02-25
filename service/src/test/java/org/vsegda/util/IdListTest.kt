package org.vsegda.util

import junit.framework.Assert.*
import org.junit.*
import java.util.*

class IdListTest {
    @Test
    fun testList() {
        val id = IdList("1-3,code,5,7")
        assertEquals(Arrays.asList("1", "2", "3", "code", "5", "7"), id)
        assertEquals("1-3,code,5,7", id.toString())
    }
}
