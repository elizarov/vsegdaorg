package org.vsegda.data

import junit.framework.Assert.*
import org.junit.*
import org.vsegda.util.*
import java.util.*

class DataArchiveTest {
    @Test
    fun testCodec() {
        val sid = 1L
        val archive = DataArchive()
        archive.streamId = sid
        val put = ArrayList<DataItem>()
        put.add(DataItem(sid, 12.5, TimeUtil.parseTime("20120101T150000.000", 0)))
        put.add(DataItem(sid, 12.5, TimeUtil.parseTime("20120101T150500.000", 0)))
        put.add(DataItem(sid, 12.5, TimeUtil.parseTime("20120101T151000.000", 0)))
        put.add(DataItem(sid, 12.5, TimeUtil.parseTime("20120101T152000.000", 0)))
        put.add(DataItem(sid, 12.6, TimeUtil.parseTime("20120101T152500.000", 0)))
        put.add(DataItem(sid, 12.7, TimeUtil.parseTime("20120101T153000.000", 0)))
        put.add(DataItem(sid, 12.9, TimeUtil.parseTime("20120101T153500.000", 0)))
        put.add(DataItem(sid, 13.0, TimeUtil.parseTime("20120101T154000.000", 0)))
        put.add(DataItem(sid, 13.5, TimeUtil.parseTime("20120101T154500.000", 0)))
        put.add(DataItem(sid, 13.4, TimeUtil.parseTime("20120101T155000.000", 0)))
        put.add(DataItem(sid, 13.2, TimeUtil.parseTime("20120101T155500.000", 0)))
        put.add(DataItem(sid, 12.2, TimeUtil.parseTime("20120101T160000.000", 0)))
        put.add(DataItem(sid, 12.0, TimeUtil.parseTime("20120101T160500.000", 0)))
        // encode
        archive.encodeItems(put)
        assertEquals(put.size, archive.count)
        // decode
        val got = archive.items
        assertEquals(put.size, got.size)
        for (i in put.indices) {
            val putItem = put[i]
            val gotItem = got[i]
            assertEquals(putItem.streamId, gotItem.streamId)
            assertEquals(putItem.value, gotItem.value)
            assertEquals(putItem.timeMillis, gotItem.timeMillis)
        }
    }
}
